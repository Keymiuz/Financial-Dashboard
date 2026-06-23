# CLAUDE.md — LexFinance

## O que é esse projeto

SaaS de dashboard financeiro para escritórios de advocacia brasileiros.  
MVP com 1 tenant fixo. Consulte o SPEC.md para escopo completo e regras de negócio.

---

## Stack

- Java 21 + Spring Boot 3.5
- Spring Security + JWT (access token 15min, refresh token 7 dias)
- Spring Data JPA + Hibernate (LAZY fetch por padrão)
- PostgreSQL 16
- Flyway para migrations
- Angular 18+ (frontend separado, consome a API REST)
- Docker + Docker Compose

---

## Decisões Técnicas

### Regime de Caixa — não Competência
Lançamentos existem quando o dinheiro move. O status `ATRASADO` não existe
no banco. É sempre calculado: `status == PENDENTE && dataVencimento.isBefore(LocalDate.now())`.
Não crie jobs de atualização de status — é anti-padrão nesse modelo.

### UUID como PK
`UUID.randomUUID()` gerado pela aplicação antes de persistir.
Nunca usar `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
Facilita multi-tenancy e evita exposição de sequências na URL.

### tenant_id vem sempre do JWT
O `tenant_id` nunca é aceito do request body. Sempre extraído do claim do JWT
via `SecurityContextHolder`. Criar um `TenantContext` que encapsula essa extração.

### BigDecimal para dinheiro — sem exceção
`DECIMAL(12,2)` no banco. `BigDecimal` no Java. Nunca `double`, nunca `float`.
Usar `BigDecimal.ZERO` como default, nunca `null` em campos monetários.

### Snapshot em LancamentoHora.valorCalculado
Ao criar um `LancamentoHora`, calcular e persistir `valorCalculado = horas × contrato.valorHora`.
Se o `valorHora` do contrato mudar depois, os lançamentos anteriores **não** são recalculados.
Isso é intencional — preserva o histórico.

### Vertical Slicing — não camadas técnicas
Pacotes por feature: `cliente/`, `processo/`, `honorario/`, `despesa/`, `dashboard/`.
Cada feature tem seus próprios `domain/`, `service/`, `repository/`, `dto/`, `controller/`.
Não existe package `com.lexfinance.service` global — isso seria layered architecture.

---

## Convenções

### Banco de dados
| Item | Padrão | Exemplo |
|------|--------|---------|
| Tabelas | snake_case plural | `parcelas_honorarios` |
| Colunas | snake_case | `data_vencimento` |
| PKs | `id` UUID | `id UUID PRIMARY KEY` |
| FKs | `{entidade}_id` | `contrato_id`, `processo_id` |
| Índices | `idx_{tabela}_{coluna}` | `idx_parcelas_contrato_id` |
| Migrations | `V{YYYYMMDD}{seq}__{descricao}.sql` | `V20250623001__create_schema.sql` |

### Java
| Item | Padrão | Exemplo |
|------|--------|---------|
| Request DTOs | `{Acao}{Entidade}Request` | `CriarClienteRequest` |
| Response DTOs | `{Entidade}Response` | `ClienteResponse` |
| Exceções de negócio | `{Entidade}NaoEncontradoException` | `ProcessoNaoEncontradoException` |
| Enums | PascalCase | `StatusParcela.PENDENTE` |

### REST
- Endpoints em plural, kebab-case: `/api/clientes`, `/api/processos`
- `tenant_id` **nunca** na URL — sempre extraído do JWT
- Ações não-CRUD como verbos: `PATCH /api/parcelas/{id}/receber`, `POST /api/contratos/{id}/faturar-horas`

---

## O que NÃO fazer

- **Não usar herança em entities JPA** — usar composição e `@Embeddable` para campos compartilhados (auditoria).
- **Não criar abstrações genéricas** (`GenericService<T>`, `BaseRepository<T>`) antes de ter 3+ usos reais.
- **Não usar EAGER fetch** em relacionamentos — sempre `LAZY` + `@EntityGraph` quando necessário.
- **Não retornar entities JPA nos controllers** — sempre DTOs de resposta.
- **Não colocar `@Transactional` em controllers** — apenas em services.
- **Não criar endpoint sem validação** — sempre `@Valid` + Bean Validation no DTO.
- **Não persistir status ATRASADO** — calculado em runtime.
- **Não aceitar tenant_id do request body** — sempre do JWT.

---

## Auditoria (campos comuns)

Usar `@Embeddable` para campos de auditoria presentes em todas as entidades:

```java
@Embeddable
public class AuditInfo {
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

Com `@PrePersist` e `@PreUpdate` em um `@EntityListeners(AuditListener.class)`.

---

## Como Rodar Localmente

```bash
# 1. Subir o banco
docker-compose up -d postgres

# 2. Flyway roda automaticamente ao subir o Spring Boot
# 3. Rodar o backend
./mvnw spring-boot:run

# 4. Rodar o frontend (em outro terminal)
cd frontend && ng serve
```

A aplicação sobe em `http://localhost:8080`.  
O frontend sobe em `http://localhost:4200`.

---

## Variáveis de Ambiente

```env
# Banco
DB_URL=jdbc:postgresql://localhost:5432/lexfinance
DB_USERNAME=lexfinance
DB_PASSWORD=lexfinance

# JWT
JWT_SECRET=<base64-encoded-256bit-secret>
JWT_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000

# MVP: tenant fixo (UUID do único escritório)
APP_TENANT_ID_FIXO=<uuid-gerado-uma-vez-e-fixado>
```

---

## Seed de Desenvolvimento

O arquivo `V2__seed_dev.sql` cria:
- 1 tenant (escritório)
- 1 usuário admin (email: `admin@lexfinance.dev`, senha: `admin123`)
- 3 clientes
- 5 processos variados
- Contratos com honorários (mix de FIXO e HORA)
- Parcelas com datas variadas (algumas em atraso, algumas futuras)
- Despesas processuais

Nunca alterar o seed diretamente. Criar nova migration se necessário.
