# LexFinance — Especificação do MVP

## Visão Geral

Dashboard financeiro para escritórios de advocacia brasileiros.  
Resolve um problema específico: **visibilidade do fluxo de honorários e despesas processuais**.

O escritório cadastra clientes e processos, lança honorários e custas, e vê no
dashboard o que tem a receber, o que está em atraso, e o fluxo dos próximos 30 dias.

---

## Modelo de Negócio

- SaaS com cobrança **flat monthly** por escritório
- MVP simula **1 tenant fixo** (escritório único)
- Arquitetura já estruturada para multi-tenancy futuro (`tenant_id` em todas as tabelas)

---

## Stack Técnica

| Camada | Tecnologia |
|--------|-----------|
| Backend | Java 21 + Spring Boot 3.5 |
| Segurança | Spring Security + JWT (access + refresh token) |
| ORM | Spring Data JPA + Hibernate |
| Banco | PostgreSQL 16 |
| Migrations | Flyway |
| Frontend | Angular 18+ |
| Containerização | Docker + Docker Compose |
| Build | Maven |

---

## Usuários

No MVP, todos os usuários do escritório têm **acesso completo** ao sistema.  
Não existe diferenciação de roles neste momento.

---

## Regime Financeiro

**Regime de Caixa.**

Lançamentos existem apenas quando o dinheiro efetivamente entra ou sai.  
Honorários são "recebidos" quando o pagamento ocorre — não quando o direito nasce.

> **Consequência direta:** o status `ATRASADO` **nunca é persistido no banco**.  
> É calculado em runtime: `status = PENDENTE && data_vencimento < hoje`.

---

## Domínio

### Tenant (Escritório)

Representa o escritório. Existe mesmo no MVP com 1 tenant.

| Campo | Tipo | Observação |
|-------|------|-----------|
| id | UUID | PK, gerado pela aplicação |
| nome | VARCHAR(200) | |
| cnpj | VARCHAR(14) | Único |
| ativo | BOOLEAN | Soft delete |
| created_at | TIMESTAMP | Auditoria |
| updated_at | TIMESTAMP | Auditoria |

---

### Usuario

| Campo | Tipo | Observação |
|-------|------|-----------|
| id | UUID | PK |
| tenant_id | UUID | FK → Tenant |
| nome | VARCHAR(200) | |
| email | VARCHAR(200) | Único por tenant |
| senha_hash | VARCHAR(255) | BCrypt |
| ativo | BOOLEAN | Soft delete |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

---

### Cliente

| Campo | Tipo | Observação |
|-------|------|-----------|
| id | UUID | PK |
| tenant_id | UUID | FK → Tenant |
| nome | VARCHAR(200) | |
| tipo | ENUM | `PESSOA_FISICA` / `PESSOA_JURIDICA` |
| cpf_cnpj | VARCHAR(14) | Único por tenant |
| email | VARCHAR(200) | Opcional |
| telefone | VARCHAR(20) | Opcional |
| ativo | BOOLEAN | Soft delete |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

---

### Processo

| Campo | Tipo | Observação |
|-------|------|-----------|
| id | UUID | PK |
| tenant_id | UUID | FK → Tenant |
| cliente_id | UUID | FK → Cliente |
| numero_cnj | VARCHAR(25) | Opcional, único por tenant |
| descricao | VARCHAR(500) | |
| area | ENUM | `CIVIL`, `TRABALHISTA`, `CRIMINAL`, `TRIBUTARIO`, `PREVIDENCIARIO`, `OUTROS` |
| status | ENUM | `ATIVO`, `ENCERRADO`, `SUSPENSO` |
| data_inicio | DATE | |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

---

### ContratoHonorarios

Contrato de honorários para um processo. Um processo pode ter múltiplos contratos.

| Campo | Tipo | Observação |
|-------|------|-----------|
| id | UUID | PK |
| tenant_id | UUID | FK → Tenant |
| processo_id | UUID | FK → Processo |
| tipo | ENUM | `FIXO` / `HORA` |
| valor_fixo | DECIMAL(12,2) | Preenchido quando `tipo = FIXO` |
| valor_hora | DECIMAL(12,2) | Preenchido quando `tipo = HORA` |
| descricao | VARCHAR(500) | |
| data_contrato | DATE | |
| ativo | BOOLEAN | Soft delete |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

**Regra:** `valor_fixo` e `valor_hora` são mutuamente exclusivos conforme `tipo`.

---

### ParcelaHonorarios

Parcela individual de cobrança de honorários.

- Para `FIXO`: criadas manualmente com valor e vencimento.
- Para `HORA`: geradas a partir dos `LancamentoHora` ao "faturar horas".

| Campo | Tipo | Observação |
|-------|------|-----------|
| id | UUID | PK |
| tenant_id | UUID | FK → Tenant |
| contrato_id | UUID | FK → ContratoHonorarios |
| numero_parcela | INTEGER | Sequencial por contrato (1, 2, 3…) |
| valor | DECIMAL(12,2) | |
| data_vencimento | DATE | |
| status | ENUM | `PENDENTE` / `RECEBIDO` |
| data_recebimento | DATE | Preenchido quando `RECEBIDO` |
| observacao | VARCHAR(500) | Opcional |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

**Regras:**
1. Status `ATRASADO` é calculado em runtime. Nunca persistido.
2. Ao mudar status para `RECEBIDO`, `data_recebimento` é obrigatório.

---

### LancamentoHora

Registro de horas trabalhadas para contratos do tipo `HORA`.

| Campo | Tipo | Observação |
|-------|------|-----------|
| id | UUID | PK |
| tenant_id | UUID | FK → Tenant |
| contrato_id | UUID | FK → ContratoHonorarios (tipo = HORA) |
| descricao | VARCHAR(500) | O que foi feito |
| data | DATE | Data do trabalho |
| horas | DECIMAL(5,2) | Ex: 1.5 = 1h30 |
| valor_calculado | DECIMAL(12,2) | Snapshot: `horas × valor_hora` no momento do lançamento |
| faturado | BOOLEAN | Se já foi incluído em uma ParcelaHonorarios |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

**Regra:** `valor_calculado` é um **snapshot** calculado na criação.  
Se `valor_hora` do contrato mudar depois, lançamentos anteriores **não** são recalculados.

---

### DespesaProcessual

Despesa adiantada pelo escritório no processo (custas judiciais, perícias, diligências).

| Campo | Tipo | Observação |
|-------|------|-----------|
| id | UUID | PK |
| tenant_id | UUID | FK → Tenant |
| processo_id | UUID | FK → Processo |
| descricao | VARCHAR(500) | |
| tipo | ENUM | `CUSTA_JUDICIAL`, `HONORARIO_PERITO`, `DILIGENCIA`, `OUTROS` |
| valor | DECIMAL(12,2) | |
| data_despesa | DATE | Quando ocorreu |
| status_ressarcimento | ENUM | `PENDENTE` / `RESSARCIDO` |
| data_ressarcimento | DATE | Preenchido quando `RESSARCIDO` |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

---

## APIs do MVP

### Autenticação

| Método | Endpoint | Descrição |
|--------|---------|-----------|
| POST | `/api/auth/login` | Login, retorna access + refresh token |
| POST | `/api/auth/refresh` | Renova access token |

### Clientes

| Método | Endpoint | Descrição |
|--------|---------|-----------|
| GET | `/api/clientes` | Lista paginada |
| POST | `/api/clientes` | Cria cliente |
| GET | `/api/clientes/{id}` | Detalhe |
| PUT | `/api/clientes/{id}` | Atualiza |
| DELETE | `/api/clientes/{id}` | Soft delete |

### Processos

| Método | Endpoint | Descrição |
|--------|---------|-----------|
| GET | `/api/processos` | Lista paginada (filtros: status, area, cliente_id) |
| POST | `/api/processos` | Cria processo |
| GET | `/api/processos/{id}` | Detalhe com contratos e despesas |
| PUT | `/api/processos/{id}` | Atualiza |
| DELETE | `/api/processos/{id}` | Soft delete |

### Honorários

| Método | Endpoint | Descrição |
|--------|---------|-----------|
| POST | `/api/processos/{id}/contratos` | Cria contrato de honorários |
| GET | `/api/contratos/{id}/parcelas` | Lista parcelas do contrato |
| POST | `/api/contratos/{id}/parcelas` | Cria parcelas (aceita array para batch) |
| PATCH | `/api/parcelas/{id}/receber` | Marca parcela como recebida (body: data_recebimento) |

### Horas (contratos HORA)

| Método | Endpoint | Descrição |
|--------|---------|-----------|
| POST | `/api/contratos/{id}/lancamentos-hora` | Registra horas trabalhadas |
| GET | `/api/contratos/{id}/lancamentos-hora` | Lista (filtro: faturado) |
| POST | `/api/contratos/{id}/faturar-horas` | Consolida horas não faturadas em uma parcela |

### Despesas Processuais

| Método | Endpoint | Descrição |
|--------|---------|-----------|
| GET | `/api/processos/{id}/despesas` | Lista despesas do processo |
| POST | `/api/processos/{id}/despesas` | Registra despesa |
| PATCH | `/api/despesas/{id}/ressarcir` | Marca despesa como ressarcida |

### Dashboard

| Método | Endpoint | Descrição |
|--------|---------|-----------|
| GET | `/api/dashboard/resumo` | Cards: a receber, em atraso, recebido no mês |
| GET | `/api/dashboard/fluxo-caixa` | Projeção dia a dia dos próximos 30 dias |

---

## Dashboard — Lógica das Métricas

### A Receber no Mês
```sql
SELECT SUM(valor)
FROM parcelas_honorarios
WHERE tenant_id = :tenantId
  AND status = 'PENDENTE'
  AND DATE_TRUNC('month', data_vencimento) = DATE_TRUNC('month', CURRENT_DATE)
```

### Em Atraso
```sql
SELECT SUM(valor)
FROM parcelas_honorarios
WHERE tenant_id = :tenantId
  AND status = 'PENDENTE'
  AND data_vencimento < CURRENT_DATE
```
> Nota: parcelas do mês atual que já venceram aparecem **nos dois cards** (a receber no mês + em atraso). É o comportamento esperado.

### Recebido no Mês
```sql
SELECT SUM(valor)
FROM parcelas_honorarios
WHERE tenant_id = :tenantId
  AND status = 'RECEBIDO'
  AND DATE_TRUNC('month', data_recebimento) = DATE_TRUNC('month', CURRENT_DATE)
```

### Fluxo de Caixa (30 dias)
```sql
SELECT data_vencimento, SUM(valor) AS total_esperado
FROM parcelas_honorarios
WHERE tenant_id = :tenantId
  AND status = 'PENDENTE'
  AND data_vencimento BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '30 days'
GROUP BY data_vencimento
ORDER BY data_vencimento
```
Retorna array de `{ data, totalEsperado }` para o frontend montar o gráfico de barras.

---

## Fora do Escopo do MVP

- Integração bancária (OFX, Open Finance, PIX)
- Emissão de Nota Fiscal
- Contabilidade / balancete / DRE
- Honorários por êxito (contingência)
- Onboarding de múltiplos tenants
- Relatórios exportáveis (PDF / Excel)
- Geração automática de parcelas recorrentes
- Notificações e alertas de vencimento
- App mobile

---

## Regras de Negócio Globais

1. Todo acesso ao banco **sempre** filtra por `tenant_id` — sem exceção.
2. `tenant_id` é extraído do JWT — **nunca** aceito do request body.
3. Decimais monetários: `DECIMAL(12,2)` no banco, `BigDecimal` no Java. Nunca `double` ou `float`.
4. Datas: `LocalDate` para datas de negócio, `LocalDateTime` para timestamps de auditoria.
5. PKs em UUID v4 gerado pela aplicação, não pelo banco (`UUID.randomUUID()`).
6. Status `ATRASADO` nunca é persistido. Sempre calculado em runtime.
7. Soft delete em entidades cadastrais via coluna `ativo = false`.

---

## Estrutura de Pacotes

```
com.lexfinance
├── auth/
│   ├── controller/
│   ├── service/
│   └── dto/
├── dashboard/
│   ├── controller/
│   ├── service/
│   └── dto/
├── cliente/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── domain/
│   └── dto/
├── processo/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── domain/
│   └── dto/
├── honorario/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── domain/
│   └── dto/
└── despesa/
    ├── controller/
    ├── service/
    ├── repository/
    ├── domain/
    └── dto/
```
