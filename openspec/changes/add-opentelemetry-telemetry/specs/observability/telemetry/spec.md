## Purpose

Define o comportamento observavel da API para que requisicoes, dependencias, metricas tecnicas e logs possam ser correlacionados e exportados de forma padronizada para uma plataforma compativel com OTLP.

## ADDED Requirements

### Requirement: Exportacao de traces distribuidos

O sistema MUST produzir traces para requisicoes HTTP atendidas pela API e para chamadas relevantes a dependencias externas usadas durante o processamento, incluindo banco de dados e cache, quando a telemetria estiver habilitada.

#### Scenario: Requisicao HTTP bem-sucedida

- **WHEN** uma requisicao HTTP e processada com sucesso pela API
- **THEN** o sistema exporta um trace contendo um span servidor com metodo, rota ou endpoint, codigo de status e duracao da requisicao

#### Scenario: Erro durante requisicao HTTP

- **WHEN** uma requisicao HTTP falha por erro da aplicacao
- **THEN** o sistema exporta um trace que marca o span afetado como erro e inclui informacoes suficientes para diagnostico sem expor segredos

#### Scenario: Chamada a dependencia instrumentada

- **WHEN** a API acessa uma dependencia instrumentada durante uma requisicao com contexto de trace ativo
- **THEN** o sistema exporta spans filhos correlacionados ao trace da requisicao original

### Requirement: Configuracao por ambiente

O sistema MUST permitir configurar telemetria por variaveis de ambiente sem exigir alteracao de codigo entre execucoes locais, containers e ambientes remotos.

#### Scenario: Endpoint OTLP configurado

- **WHEN** a aplicacao inicia com um endpoint OTLP configurado
- **THEN** o sistema envia a telemetria habilitada para esse endpoint usando configuracao externa

#### Scenario: Collector fornecido externamente

- **WHEN** a API e implantada em um ambiente com collector dedicado
- **THEN** ela usa apenas o endpoint OTLP interno fornecido pela plataforma de observabilidade e nao depende de configuracao de redes, exporters ou credenciais do backend de observabilidade

#### Scenario: Identidade do servico configurada

- **WHEN** a aplicacao inicia em qualquer ambiente
- **THEN** a telemetria exportada identifica o servico como `espresso-api` e inclui atributos de recurso que distinguem o ambiente de execucao

#### Scenario: Telemetria desabilitada ou endpoint ausente

- **WHEN** a aplicacao inicia sem endpoint OTLP valido ou com exportacao desabilitada
- **THEN** a API continua atendendo requisicoes sem depender da disponibilidade do backend de observabilidade

### Requirement: Metricas tecnicas da aplicacao

O sistema MUST disponibilizar metricas tecnicas suficientes para acompanhar saude operacional, capacidade e desempenho da API.

#### Scenario: Metricas de runtime

- **WHEN** a telemetria de metricas esta habilitada
- **THEN** o sistema exporta metricas de runtime, incluindo uso de JVM, threads, memoria e garbage collection quando disponiveis

#### Scenario: Metricas HTTP

- **WHEN** a API processa requisicoes HTTP
- **THEN** o sistema disponibiliza metricas agregadas de volume, latencia e resultado das requisicoes

#### Scenario: Falha de exportacao de metricas

- **WHEN** o backend de telemetria esta indisponivel temporariamente
- **THEN** a indisponibilidade de exportacao de metricas nao interrompe o processamento normal da API

### Requirement: Logs correlacionaveis

O sistema MUST emitir logs que permitam correlacionar eventos da aplicacao com requisicoes e traces quando houver contexto ativo.

#### Scenario: Log com contexto de requisicao

- **WHEN** a aplicacao emite um log durante o processamento de uma requisicao
- **THEN** o log contem o `requestId` atual e, quando disponiveis, identificadores de trace e span

#### Scenario: Log fora de requisicao

- **WHEN** a aplicacao emite um log sem contexto de requisicao ou trace ativo
- **THEN** o log continua sendo emitido sem falhar por ausencia de identificadores de correlacao

### Requirement: Seguranca e privacidade da telemetria

O sistema MUST evitar exportar segredos, credenciais, tokens e valores sensiveis em atributos, logs ou mensagens de erro de telemetria.

#### Scenario: Configuracao contem credenciais

- **WHEN** a aplicacao possui variaveis de ambiente com credenciais de banco, cache ou backend de telemetria
- **THEN** esses valores nao aparecem em spans, metricas ou logs exportados

#### Scenario: Erro contem dados de entrada

- **WHEN** uma falha envolve dados recebidos de cliente ou parametros de consulta
- **THEN** a telemetria exportada limita os detalhes a informacoes diagnosticas seguras e nao registra valores sensiveis integralmente
