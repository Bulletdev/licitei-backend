# Sistema de Gestão de Licitações - Backend

API REST para captura e consulta de licitações públicas do ComprasNet.

## 🚀 Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database** (desenvolvimento)
- **JSoup** (web scraping)
- **SpringDoc OpenAPI** (documentação)
- **JUnit 5** (testes)
- **Maven** (build)

## 📋 Funcionalidades

- ✅ Captura automática de licitações do ComprasNet
- ✅ API REST para consulta de licitações
- ✅ Filtros por código UASG e número do pregão
- ✅ Paginação e ordenação
- ✅ Documentação Swagger
- ✅ Testes automatizados
- ✅ Tratamento de erros centralizado

## 🏗️ Arquitetura

O projeto segue os princípios de **Clean Architecture** e **Domain-Driven Design**:

```
src/main/java/com/effecti/licitacoes/
├── domain/                 # Entidades e regras de negócio
│   ├── entity/            # Entidades JPA
│   └── repository/        # Interfaces de repositório
├── application/           # Casos de uso e DTOs
│   ├── dto/              # Data Transfer Objects
│   └── service/          # Serviços de aplicação
├── infrastructure/       # Implementações técnicas
│   ├── exception/        # Tratamento de exceções
│   └── service/          # Serviços de infraestrutura
└── presentation/         # Controladores REST
    └── controller/
```

## 🛠️ Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.6+

### Executando a aplicação

1. **Clone o repositório**
```bash
git clone <url-do-repositorio>
cd licitacoes-api
```

2. **Execute a aplicação**
```bash
mvn spring-boot:run
```

3. **Acesse a aplicação**
- API: http://localhost:9991
- Swagger UI: http://localhost:9991/swagger-ui.html
- H2 Console: http://localhost:9991/h2-console

### Executando os testes

```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=LicitacaoControllerTest
mvn test -Dtest=LicitacaoServiceTest
```

### Build para produção

```bash
mvn clean package
java -jar target/licitacoes-api-1.0.0.jar
```

## 📡 Endpoints da API

### Listar Licitações
```http
GET /api/licitacoes
```

**Parâmetros de consulta:**
- `codigoUasg` (opcional): Código da UASG
- `numeroPregao` (opcional): Número do pregão
- `page` (padrão: 0): Número da página
- `size` (padrão: 20): Tamanho da página
- `sort` (opcional): Ordenação (ex: `dataAbertura,desc`)

**Exemplo:**
```http
GET /api/licitacoes?codigoUasg=123456&numeroPregao=001/2024&page=0&size=10
```

### Buscar Licitação por ID
```http
GET /api/licitacoes/{id}
```

## 📊 Modelo de Dados

### Licitação
```json
{
  "id": 1,
  "codigoUasg": "123456",
  "numeroPregao": "001/2024",
  "objeto": "Descrição do objeto licitado",
  "dataAbertura": "2024-01-15T10:00:00",
  "endereco": "Endereço da sessão",
  "modalidade": "Pregão Eletrônico",
  "itens": [...],
  "criadoEm": "2024-01-15T08:00:00",
  "atualizadoEm": "2024-01-15T08:00:00"
}
```

### Item de Licitação
```json
{
  "id": 1,
  "numeroItem": "001",
  "descricao": "Descrição do item",
  "quantidade": 100,
  "valorUnitario": 25.50,
  "unidade": "UN"
}
```

## 🔄 Captura de Dados

O sistema possui um **scheduler** que executa a cada 30 minutos para capturar novas licitações do ComprasNet. A captura é feita através de web scraping usando JSoup.

**Características:**
- Execução automática a cada 30 minutos
- Verifica duplicatas antes de inserir
- Log detalhado das operações
- Tratamento de erros robusto

## 🧪 Testes

### Cobertura de Testes

- **Controllers**: Testes de integração com MockMvc
- **Services**: Testes unitários com Mockito
- **Repositories**: Testes de integração com @DataJpaTest
- **Scraping**: Testes unitários mockando dependências

### Executar testes com relatório

```bash
mvn test jacoco:report
```

O relatório será gerado em `target/site/jacoco/index.html`

## 🛡️ Segurança e Boas Práticas

- **Validação**: Bean Validation nas entidades
- **Tratamento de Erros**: Handler global com respostas padronizadas
- **Logging**: Logs estruturados com níveis apropriados
- **Performance**: Índices no banco, paginação obrigatória
- **Clean Code**: Código limpo, métodos pequenos, responsabilidade única

## 📈 Melhorias para Performance 

### 1. Cache Distribuído
**Implementação**: Redis com Spring Cache
```java
@Cacheable(value = "licitacoes", key = "#codigoUasg + '-' + #numeroPregao + '-' + #pageable.pageNumber")
public Page<Licitacao> findByFilters(String codigoUasg, String numeroPregao, Pageable pageable)
```

**Justificativa**: 
- Reduz consultas ao banco para filtros frequentes
- TTL configurável para dados que mudam pouco
- Invalidação automática em atualizações

### 2. Banco de Dados Otimizado
**Implementação**: PostgreSQL com particionamento
```sql
CREATE TABLE licitacoes_2024 PARTITION OF licitacoes
FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');

CREATE INDEX CONCURRENTLY idx_licitacoes_uasg_pregao_data 
ON licitacoes (codigo_uasg, numero_pregao, data_abertura DESC);
```

**Justificativa**:
- Particionamento por ano reduz escaneamento
- Índices compostos otimizam consultas com filtros
- Connection pooling adequado (HikariCP)

## 🔧 Configurações

### Ambiente de Desenvolvimento
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:licitacoes
  jpa:
    show-sql: true
  logging:
    level:
      com.effecti.licitacoes: DEBUG
```

### Ambiente de Produção
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/licitacoes
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    show-sql: false
  logging:
    level:
      com.effecti.licitacoes: INFO
```

## 📝 Decisões Técnicas

### 1. **H2 Database**
- **Motivo**: Facilita execução e testes
- **Produção**: Migrado para PostgreSQL

### 2. **JSoup para Web Scraping**
- **Motivo**: Biblioteca robusta para parsing HTML
- **Alternativa**: Selenium para sites com JavaScript

### 3. **Records para DTOs**
- **Motivo**: Imutabilidade, menos boilerplate
- **Java 17**: Suporte nativo a records

### 4. **Paginação Obrigatória**
- **Motivo**: Evita sobrecarga com grandes volumes
- **Implementação**: Spring Data Pageable

### 5. **Scheduler para Captura**
- **Motivo**: Automação da coleta de dados
- **Configuração**: Intervalo ajustável via properties

## ⚡ Performance Benchmarks

### Otimizações Implementadas
1. **Índices**: `codigo_uasg`, `numero_pregao`
2. **Paginação**: Máximo 100 registros por página
3. **Lazy Loading**: Relacionamentos carregados sob demanda
4. **Query Optimization**: JPQL otimizado

## 🚀 Deploy

### Docker
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/licitacoes-api-1.0.0.jar app.jar
EXPOSE 9991
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: licitacoes-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: licitacoes-api
  template:
    metadata:
      labels:
        app: licitacoes-api
    spec:
      containers:
      - name: licitacoes-api
        image: licitacoes-api:1.0.0
        ports:
        - containerPort: 9991
```

## 📊 Monitoramento

### Métricas Implementadas
- **Actuator**: Health checks, métricas JVM
- **Custom Metrics**: Contadores de scraping
- **Logs Estruturados**: JSON format para ELK


## 📋 Checklist de Implementação

### Backend ✅
- [x] API REST com Spring Boot
- [x] Captura de dados do ComprasNet
- [x] Filtros por UASG e Pregão
- [x] Paginação e ordenação
- [x] Testes automatizados (JUnit)
- [x] Documentação Swagger
- [x] Tratamento de erros
- [x] Validações

### Extras Implementados ✅
- [x] Clean Architecture
- [x] Cache strategy design
- [x] Performance optimization
- [x] Docker support
- [x] Monitoring setup
- [x] CI/CD considerations

## ⏱️ Tempo Estimado de Desenvolvimento

- **Arquitetura e Setup**: 2h
- **Entidades e Repositórios**: 1.5h
- **Serviços e Web Scraping**: 3h
- **Controllers e DTOs**: 1.5h
- **Testes Automatizados**: 2.5h
- **Documentação e README**: 1.5h
- **Total**: ~12 horas

## 📄 Licença

Este projeto está sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.