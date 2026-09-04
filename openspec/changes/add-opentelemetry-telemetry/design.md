## Context

O projeto e uma API Kotlin com Spring Boot 4, Java 25, Spring MVC, Spring Data JPA, PostgreSQL, Redis, Flyway e Actuator. O build e o container ja embutem o Datadog Java Agent e definem variaveis `DD_*`; a aplicacao tambem possui um filtro de correlacao que propaga `X-Request-Id` e inclui `requestId` no padrao de log.

Veja `proposal.md` para a motivacao. A spec `observability/telemetry` define o comportamento esperado.

## Goals / Non-Goals

**Goals:**

- Usar OpenTelemetry como formato e API operacional padrao para traces e metricas.
- Minimizar alteracoes no codigo da aplicacao usando auto-instrumentacao como primeira etapa.
- Permitir envio a um collector dedicado do Espresso, operado pelo repositorio de observabilidade, sem acoplar a aplicacao diretamente ao SigNoz.
- Preservar o `requestId` existente e acrescentar correlacao com trace/span.
- Manter a API funcional quando o backend de observabilidade estiver indisponivel.

**Non-Goals:**

- Criar dashboards, alertas ou SLOs no SigNoz nesta mudanca.
- Adicionar instrumentacao manual de metricas de negocio nesta primeira etapa.
- Implementar coleta centralizada de logs via agente externo.
- Manter Datadog e OpenTelemetry ativos simultaneamente como solucao permanente.
- Usar sidecar acoplado ao container/compose da aplicacao nesta mudanca.
- Criar um collector gateway compartilhado para multiplas aplicacoes nesta mudanca.
- Provisionar, configurar ou operar o collector, as redes Docker ou o SigNoz neste repositorio.

## Decisions

### Usar OpenTelemetry Java Agent como primeira camada

Substituir o Datadog Java Agent pelo OpenTelemetry Java Agent no build e no container. Isso cobre automaticamente Spring MVC, clientes HTTP suportados, JDBC, Redis e runtime JVM com baixo impacto no codigo.

Alternativas consideradas:

- Instrumentacao manual com SDK OpenTelemetry: da mais controle, mas aumenta acoplamento e custo inicial.
- Micrometer Tracing sem Java Agent: integra bem com Spring, mas tende a exigir mais configuracao e nao cobre tantas bibliotecas automaticamente.
- Manter Datadog Agent e adicionar Otel em paralelo: aumenta risco de spans duplicados, overhead e diagnostico confuso.

### Exportar via OTLP para um collector dedicado operado externamente

A aplicacao deve enviar OTLP/gRPC para um OpenTelemetry Collector dedicado ao Espresso, executado separadamente do lifecycle da aplicacao. O collector e sua configuracao operacional pertencem ao repositorio de observabilidade, que tambem administra as redes Docker e a exportacao para o SigNoz. Este repositorio define somente o contrato de cliente: protocolo, endpoint interno, identidade do servico e comportamento em caso de indisponibilidade.

Endpoint alvo da aplicacao:

```env
OTEL_EXPORTER_OTLP_PROTOCOL=grpc
OTEL_EXPORTER_OTLP_ENDPOINT=http://espresso-app-otel-collector:4317
```

Racional: um collector dedicado mantem a aplicacao desacoplada do SigNoz e permite que batch, retry, filtros, sampling e mudancas de destino sejam operados fora do ciclo de deploy da API. Ao mesmo tempo, ele evita acoplar o collector ao deploy/restart do container da API, que seria o efeito pratico de um sidecar em Docker Compose.

Alternativas consideradas:

- Enviar diretamente da aplicacao para as portas publicas `4317`/`4318` do SigNoz: reduz componentes, mas acopla runtime, rede e endpoint do fornecedor a cada deploy e faz trafego interno depender de interface publica da VPS.
- Usar sidecar no mesmo Docker Compose da aplicacao: aproxima collector e app, mas acopla o lifecycle do pipeline de telemetria ao deploy da API e complica o reaproveitamento quando a aplicacao escalar.
- Criar collector gateway compartilhado: prepara multiplas aplicacoes, mas introduz uma responsabilidade compartilhada antes de existir essa necessidade.
- Usar apenas endpoint local de debug: util para desenvolvimento, mas nao resolve o caminho de producao.

### Configurar por variaveis `OTEL_*`

O runtime deve usar variaveis como `OTEL_SERVICE_NAME`, `OTEL_RESOURCE_ATTRIBUTES`, `OTEL_EXPORTER_OTLP_ENDPOINT`, `OTEL_EXPORTER_OTLP_PROTOCOL`, `OTEL_TRACES_EXPORTER`, `OTEL_METRICS_EXPORTER`, `OTEL_LOGS_EXPORTER` e `OTEL_PROPAGATORS`.

Alternativas consideradas:

- Colocar configuracao fixa em `application.yaml`: facil de versionar, mas menos flexivel para ambientes.
- Usar propriedades Java `-D`: funciona, mas piora ergonomia em container e plataformas de deploy.

### Tratar logs como correlacao, nao como ingestao completa

Nesta mudanca, logs continuam saindo no console da aplicacao. O padrao deve incluir `requestId` e campos de trace/span quando disponiveis. A ingestao centralizada de logs pode ser adicionada depois via runtime ou Collector.

Alternativas consideradas:

- Exportar logs via OpenTelemetry imediatamente: mais completo, mas exige decidir pipeline e formato de logs agora.
- Remover `requestId`: simplifica o padrao, mas quebra uma correlacao ja existente e util para clientes.

### Nao adicionar metricas de negocio agora

A primeira entrega deve focar metricas tecnicas e spans automaticos. Metricas de negocio, como cache hit/miss de relatorios ou duracao de consultas especificas, devem entrar quando houver uma pergunta operacional clara.

Alternativas consideradas:

- Instrumentar relatorios manualmente ja nesta mudanca: pode ser util, mas mistura fundacao de observabilidade com analise de produto/performance.

## Risks / Trade-offs

- Compatibilidade do Java Agent com Java 25 e Spring Boot 4 -> validar em build/container e em execucao local antes de aplicar em ambiente remoto.
- Overhead de telemetria em endpoints de alta carga -> manter sampling e exporters configuraveis por ambiente.
- Vazamento de dados sensiveis em atributos de spans ou logs -> revisar atributos capturados automaticamente e evitar registrar parametros sensiveis.
- Collector dedicado indisponivel causando perda temporaria de telemetria -> configurar a aplicacao para falhar de forma nao bloqueante e coordenar monitoramento e restart policy com o repositorio de observabilidade.
- Collector dedicado saturado quando a aplicacao escalar -> o repositorio de observabilidade deve configurar `memory_limiter`, `batch` e sampling por ambiente; se houver varias aplicacoes ou alta carga, reavaliar collector gateway em uma mudanca futura.
- Contrato de rede ou endpoint divergente entre a API e o collector -> validar o endpoint interno fornecido pelo repositorio de observabilidade antes do deploy.
- Divergencia entre `requestId` e trace id -> manter ambos nos logs e documentar que `requestId` e correlacao externa, enquanto trace id e correlacao distribuida.

## Migration Plan

1. Remover a copia e configuracao do Datadog Java Agent do build e do container.
2. Adicionar obtencao e empacotamento do OpenTelemetry Java Agent.
3. Atualizar variaveis de ambiente do container para `OTEL_*` e `JAVA_TOOL_OPTIONS`.
4. Configurar a aplicacao para enviar OTLP/gRPC ao endpoint interno do collector dedicado, fornecido pelo repositorio de observabilidade.
6. Ajustar logs para incluir trace/span quando houver contexto ativo.
7. Documentar configuracao local e remota com endpoint OTLP interno e destino SigNoz via collector dedicado.
8. Validar localmente que a API inicia, atende health checks e exporta traces/metricas para o collector dedicado.

Rollback: restaurar a imagem anterior ou desabilitar o agent removendo `JAVA_TOOL_OPTIONS`/exporters `OTEL_*`. A aplicacao deve continuar funcional sem telemetria.

## Open Questions

- Qual e o endpoint interno estavel do collector dedicado que o repositorio de observabilidade fornecera para a API?
