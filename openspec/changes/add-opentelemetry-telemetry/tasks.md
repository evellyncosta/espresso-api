## 1. Build e empacotamento do agent

- [x] 1.1 Remover a configuracao do Datadog Java Agent do `build.gradle.kts` e verificar que nao existem referencias `datadogAgent` ou `copyDatadogJavaAgent` restantes.
- [x] 1.2 Adicionar configuracao para obter o OpenTelemetry Java Agent no build e verificar que `./gradlew bootJar` gera o jar da aplicacao e disponibiliza o agent no diretorio esperado.
- [x] 1.3 Atualizar o `Dockerfile` para copiar o OpenTelemetry Java Agent e verificar com `docker build -t espresso-api .` que a imagem e criada com o agent no caminho configurado.

## 2. Configuracao de runtime OTEL

- [x] 2.1 Substituir variaveis `DD_*` no `Dockerfile` por variaveis `OTEL_*` e verificar que nao restam referencias a `DD_SERVICE`, `DD_TRACE_ENABLED`, `DD_LOGS_INJECTION`, `DD_RUNTIME_METRICS_ENABLED`, `DD_AGENT_HOST` ou `DD_TRACE_AGENT_PORT`.
- [x] 2.2 Configurar `JAVA_TOOL_OPTIONS` para carregar o OpenTelemetry Java Agent e verificar que a aplicacao em container registra a inicializacao do agent sem impedir o boot.
- [x] 2.3 Configurar `OTEL_EXPORTER_OTLP_PROTOCOL=grpc` e `OTEL_EXPORTER_OTLP_ENDPOINT` apontando para o collector dedicado interno, e verificar que a aplicacao nao depende das portas publicas `4317`/`4318` do host da VPS.
- [x] 2.4 Atualizar `.env.example` com variaveis OTEL seguras e verificar que nenhum segredo real ou ingestion key foi versionado.

## 3. Logs e correlacao

- [x] 3.1 Atualizar o padrao de logs para incluir `trace_id` e `span_id` quando disponiveis, preservando `requestId`, e verificar em execucao local que logs fora de trace continuam sendo emitidos.
- [x] 3.2 Validar que `X-Request-Id` continua sendo aceito e retornado pela API e verificar com uma requisicao HTTP que o header de resposta permanece presente.
- [x] 3.3 Revisar logs e atributos configurados para evitar exposicao de credenciais e verificar que variaveis sensiveis de banco, cache e OTLP nao aparecem nos logs de inicializacao.

## 4. Contrato com collector dedicado externo

- [x] 4.1 Configurar a API para exportar OTLP/gRPC para o endpoint interno do collector dedicado e verificar que a configuracao nao referencia portas publicas do SigNoz.
- [x] 4.2 Remover os manifestos operacionais do collector deste repositorio e atualizar a documentacao para declarar que collector, redes, exporters e credenciais sao responsabilidade do repositorio de observabilidade.

## 5. Validacao OTLP ponta a ponta

- [ ] 5.1 Executar a API apontando para o collector dedicado e verificar que chamadas a `/actuator/health` e endpoints de negocio geram traces com servico `espresso-api`.
- [ ] 5.2 Exercitar endpoints que acessam PostgreSQL e Redis e verificar que os traces contem spans filhos de banco/cache correlacionados a requisicao HTTP.
- [x] 5.3 Verificar que, com o collector dedicado indisponivel ou exportacao desabilitada, a API continua iniciando e respondendo health checks.

## 6. Documentacao e verificacao final

- [x] 6.1 Atualizar o `README.md` com configuracao local, variaveis OTEL e caminho recomendado para SigNoz via collector dedicado, e verificar que os comandos documentados sao coerentes com o `Dockerfile` e `.env.example`.
- [x] 6.2 Documentar que sidecar e collector gateway compartilhado estao fora do escopo desta mudanca, e verificar que a documentacao descreve o collector dedicado como unidade separada do lifecycle da aplicacao.
- [x] 6.3 Executar `./gradlew test` e verificar que a suite automatizada passa.
- [x] 6.4 Executar `openspec validate add-opentelemetry-telemetry --strict` e verificar que a mudanca OpenSpec e valida.
