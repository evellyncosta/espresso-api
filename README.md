# Espresso API

API REST de um sistema simplificado de pedidos, construída para consultas e experimentos de desempenho com PostgreSQL. O projeto utiliza Kotlin, Spring Boot, Spring Data JPA e Flyway.

## Stack

- Kotlin 2.4
- Java 25
- Spring Boot 4
- PostgreSQL
- Spring Data JPA
- Flyway
- Springdoc OpenAPI
- Gradle Wrapper

## Funcionalidades

- Listagem paginada de clientes.
- Listagem paginada de itens de pedido usando projections.
- Relatório dos 100 clientes que mais gastaram em um intervalo de datas.
- Migrations para o schema, índices, funções de seed e resumo diário de clientes.
- Health check e readiness check via Spring Actuator.

## Pré-requisitos

- Java 25.
- PostgreSQL 14+.
- Docker, caso prefira executar a aplicação em container.

O banco precisa existir antes da inicialização da aplicação. Exemplo:

```sql
CREATE DATABASE "espresso-dev";
```

## Configuração

Copie o arquivo de exemplo para `.env` e ajuste os valores, se necessário:

```bash
cp .env.example .env
```

Variáveis disponíveis:

```dotenv
DB_HOST=localhost
DB_PORT=5432
DB_NAME=espresso-dev
DB_USER=postgres
DB_PASSWORD=postgres
OTEL_SERVICE_NAME=espresso-api
OTEL_RESOURCE_ATTRIBUTES=deployment.environment.name=local
OTEL_EXPORTER_OTLP_PROTOCOL=grpc
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
OTEL_TRACES_EXPORTER=otlp
OTEL_METRICS_EXPORTER=otlp
OTEL_LOGS_EXPORTER=none
OTEL_PROPAGATORS=tracecontext,baggage
```

Também é possível informar uma URL completa com `DB_URL`, por exemplo:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/espresso-dev
```

Ao iniciar, o Flyway executa automaticamente as migrations em `src/main/resources/db/migration`.

## Executando localmente

```bash
./gradlew bootRun
```

A aplicação ficará disponível em `http://localhost:8080`.

Para executar os testes:

```bash
./gradlew test
```

## API

### Listar clientes

```http
GET /customers?page=0&size=20&sort=createdAt,desc
```

O tamanho padrão da página é 20 e o máximo permitido é 100. A resposta segue o formato paginado do Spring Data.

### Listar itens de pedido

```http
GET /order-items?page=0&size=20
```

Retorna `id`, `orderId`, `productId`, `quantity` e `unitPrice`.

### Consultar maiores consumidores

```http
GET /reports/customers/top-spenders?startDate=2025-01-01&endDate=2025-12-31
```

As datas são inclusivas. O resultado contém no máximo 100 registros, ordenados por `totalSpent` decrescente, com os campos:

```json
{
  "customerId": 1,
  "customerName": "Customer 1",
  "totalOrders": 12,
  "totalItems": 35,
  "totalSpent": 1234.50
}
```

`startDate` deve ser anterior ou igual a `endDate`.

### Observabilidade

```http
GET /actuator/health
GET /actuator/health/readiness
```

## Telemetria OpenTelemetry

A imagem da API usa o OpenTelemetry Java Agent para instrumentar automaticamente Spring MVC, JDBC/PostgreSQL, Redis e a JVM. Os logs preservam `requestId` e incluem `trace_id` e `span_id` quando existe um trace ativo. Logs continuam no console; a coleta centralizada de logs nao faz parte desta configuracao.

Em producao, a API deve enviar OTLP/gRPC ao endpoint interno do collector dedicado fornecido pela plataforma de observabilidade, sem acessar diretamente as portas publicas do SigNoz:

```dotenv
OTEL_EXPORTER_OTLP_PROTOCOL=grpc
OTEL_EXPORTER_OTLP_ENDPOINT=http://espresso-app-otel-collector:4317
OTEL_RESOURCE_ATTRIBUTES=deployment.environment.name=production
```

Para desenvolvimento com um collector local, mantenha `OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317`. Para desabilitar exportacao sem remover o agent, use:

```dotenv
OTEL_TRACES_EXPORTER=none
OTEL_METRICS_EXPORTER=none
```

### Contrato com o collector

O collector dedicado, suas redes Docker, exporters, credenciais e integracao com o SigNoz sao operados pelo repositorio de observabilidade. Este repositorio mantem apenas a configuracao cliente da API: o endpoint OTLP interno, a identidade do servico e o comportamento nao bloqueante quando o collector estiver indisponivel.

O endpoint de producao mostrado acima (`http://espresso-app-otel-collector:4317`) e o contrato atual; a plataforma de observabilidade deve fornecer esse DNS, ou o deploy da API deve sobrescrever `OTEL_EXPORTER_OTLP_ENDPOINT` com o endpoint interno acordado. Sidecar e collector gateway compartilhado continuam fora do escopo desta mudanca.

## Geração de massa de dados

Depois que as migrations forem executadas, as funções PostgreSQL podem ser chamadas na ordem abaixo:

```sql
SELECT seed_customers(100000);
SELECT seed_products(20000);
SELECT seed_orders(1000000);
SELECT seed_order_items(5000000);
```

As funções também podem ser usadas com quantidades menores para desenvolvimento. `seed_orders` depende da existência de clientes, e `seed_order_items` depende da existência de pedidos e produtos.

## OpenAPI

Com a aplicação em execução, a documentação interativa está disponível em:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Especificação OpenAPI: `http://localhost:8080/v3/api-docs`

## Docker

Para gerar a imagem:

```bash
docker build -t espresso-api .
```

Para executar o container, informe as variáveis de conexão com o PostgreSQL:

```bash
docker run --rm -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=espresso-dev \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  espresso-api
```

Para testar o agent no container sem um collector disponivel, acrescente `-e OTEL_TRACES_EXPORTER=none -e OTEL_METRICS_EXPORTER=none`. A API continua atendendo health checks quando o collector esta indisponivel; a telemetria desse periodo pode ser descartada.

Em ambientes Linux, `host.docker.internal` pode exigir configuração adicional; nesse caso, use o endereço acessível do PostgreSQL ou conecte os serviços à mesma rede Docker.

## Estrutura principal

```text
src/main/kotlin/com/espresso/api/
├── customer/       # clientes
├── order/          # pedidos
├── orderitem/      # itens de pedido
├── product/        # produtos
└── report/         # relatórios

src/main/resources/db/migration/  # migrations Flyway
```

O script `scripts/build-and-deploy.sh` automatiza build, publicação da imagem no Amazon ECR e deploy da infraestrutura Pulumi. Ele espera que o projeto de infraestrutura esteja disponível em `/home/evellyn/projetos/espresso-infra`, ou no caminho definido por `INFRA_DIR`.
