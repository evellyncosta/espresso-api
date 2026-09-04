## Why

A API ja possui health checks e um `requestId` proprio nos logs, mas ainda nao tem telemetria distribuida padronizada para entender latencia, erros e chamadas a dependencias em producao. Adotar OpenTelemetry agora cria uma base neutra para observar a aplicacao e enviar dados posteriormente ao SigNoz sem acoplar o codigo ao fornecedor.

## What Changes

- Substituir a instrumentacao baseada em Datadog Java Agent por instrumentacao OpenTelemetry compativel com OTLP.
- Expor traces automaticos para requisicoes HTTP, Spring MVC, JDBC/PostgreSQL, Redis e erros de execucao.
- Habilitar metricas tecnicas da JVM/aplicacao e preparar o envio por OTLP.
- Correlacionar logs da aplicacao com trace e span quando houver contexto ativo, preservando o `requestId` atual.
- Configurar variaveis `OTEL_*` para nome do servico, atributos de recurso, endpoint OTLP, propagadores e exporters.
- Integrar a API a um endpoint OTLP de collector dedicado, provisionado e operado pelo repositorio de observabilidade.
- Documentar configuracao local e de container para rodar a API com telemetria.

## Capabilities

### New Capabilities

- `observability/telemetry`: define os requisitos de telemetria OpenTelemetry para traces, metricas, correlacao de logs e exportacao OTLP da API.

### Modified Capabilities

- Nenhuma.

## Impact

- `build.gradle.kts`: dependencias/configuracoes de build para obter o OpenTelemetry Java Agent e remover o acoplamento ao Datadog Agent.
- `Dockerfile`: empacotamento do Java Agent, `JAVA_TOOL_OPTIONS` e variaveis de ambiente OTEL.
- `src/main/resources/application.yaml`: configuracoes de Actuator, metricas e padrao de logs com correlacao.
- `README.md` e `.env.example`: documentacao e variaveis de configuracao de telemetria.
- Infraestrutura/runtime: dependencia de um endpoint OTLP interno fornecido pelo collector dedicado no repositorio de observabilidade; o provisionamento, as redes e a integracao com SigNoz nao fazem parte deste repositorio.
