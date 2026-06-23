-- Migração inicial para criação do esquema do banco de dados LexFinance

-- 1. Tabela de Tenants (Escritórios)
CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    cnpj VARCHAR(14) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_tenants_cnpj UNIQUE (cnpj)
);

-- 2. Tabela de Usuários
CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    nome VARCHAR(200) NOT NULL,
    email VARCHAR(200) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_usuarios_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT uq_usuarios_tenant_email UNIQUE (tenant_id, email)
);

-- 3. Tabela de Clientes
CREATE TABLE clientes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    nome VARCHAR(200) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    cpf_cnpj VARCHAR(14) NOT NULL,
    email VARCHAR(200),
    telefone VARCHAR(20),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_clientes_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT uq_clientes_tenant_cpf_cnpj UNIQUE (tenant_id, cpf_cnpj),
    CONSTRAINT chk_clientes_tipo CHECK (tipo IN ('PESSOA_FISICA', 'PESSOA_JURIDICA'))
);

-- 4. Tabela de Processos
CREATE TABLE processos (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    cliente_id UUID NOT NULL,
    numero_cnj VARCHAR(25),
    descricao VARCHAR(500) NOT NULL,
    area VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    data_inicio DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_processos_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_processos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT uq_processos_tenant_cnj UNIQUE (tenant_id, numero_cnj),
    CONSTRAINT chk_processos_area CHECK (area IN ('CIVIL', 'TRABALHISTA', 'CRIMINAL', 'TRIBUTARIO', 'PREVIDENCIARIO', 'OUTROS')),
    CONSTRAINT chk_processos_status CHECK (status IN ('ATIVO', 'ENCERRADO', 'SUSPENSO'))
);

-- 5. Tabela de Contratos de Honorários
CREATE TABLE contratos_honorarios (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    processo_id UUID NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    valor_fixo DECIMAL(12,2),
    valor_hora DECIMAL(12,2),
    descricao VARCHAR(500) NOT NULL,
    data_contrato DATE NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_contratos_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_contratos_processo FOREIGN KEY (processo_id) REFERENCES processos(id),
    CONSTRAINT chk_contratos_tipo CHECK (tipo IN ('FIXO', 'HORA')),
    CONSTRAINT chk_contratos_valores CHECK (
        (tipo = 'FIXO' AND valor_fixo IS NOT NULL AND valor_hora IS NULL) OR 
        (tipo = 'HORA' AND valor_hora IS NOT NULL AND valor_fixo IS NULL)
    )
);

-- 6. Tabela de Parcelas de Honorários
CREATE TABLE parcelas_honorarios (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    contrato_id UUID NOT NULL,
    numero_parcela INTEGER NOT NULL,
    valor DECIMAL(12,2) NOT NULL,
    data_vencimento DATE NOT NULL,
    status VARCHAR(10) NOT NULL,
    data_recebimento DATE,
    observacao VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_parcelas_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_parcelas_contrato FOREIGN KEY (contrato_id) REFERENCES contratos_honorarios(id),
    CONSTRAINT chk_parcelas_status CHECK (status IN ('PENDENTE', 'RECEBIDO')),
    CONSTRAINT chk_parcelas_recebimento CHECK (
        (status = 'RECEBIDO' AND data_recebimento IS NOT NULL) OR 
        (status = 'PENDENTE' AND data_recebimento IS NULL)
    )
);

-- 7. Tabela de Lançamentos de Horas
CREATE TABLE lancamentos_hora (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    contrato_id UUID NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    data DATE NOT NULL,
    horas DECIMAL(5,2) NOT NULL,
    valor_calculado DECIMAL(12,2) NOT NULL,
    faturado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_lancamentos_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_lancamentos_contrato FOREIGN KEY (contrato_id) REFERENCES contratos_honorarios(id)
);

-- 8. Tabela de Despesas Processuais
CREATE TABLE despesas_processuais (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    processo_id UUID NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    valor DECIMAL(12,2) NOT NULL,
    data_despesa DATE NOT NULL,
    status_ressarcimento VARCHAR(20) NOT NULL,
    data_ressarcimento DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_despesas_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_despesas_processo FOREIGN KEY (processo_id) REFERENCES processos(id),
    CONSTRAINT chk_despesas_tipo CHECK (tipo IN ('CUSTA_JUDICIAL', 'HONORARIO_PERITO', 'DILIGENCIA', 'OUTROS')),
    CONSTRAINT chk_despesas_ressarcimento CHECK (
        (status_ressarcimento = 'RESSARCIDO' AND data_ressarcimento IS NOT NULL) OR 
        (status_ressarcimento = 'PENDENTE' AND data_ressarcimento IS NULL)
    )
);

-- Criação de Índices para chaves estrangeiras e buscas frequentes
CREATE INDEX idx_tenants_cnpj ON tenants(cnpj);
CREATE INDEX idx_usuarios_tenant_id ON usuarios(tenant_id);
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_clientes_tenant_id ON clientes(tenant_id);
CREATE INDEX idx_clientes_cpf_cnpj ON clientes(cpf_cnpj);
CREATE INDEX idx_processos_tenant_id ON processos(tenant_id);
CREATE INDEX idx_processos_cliente_id ON processos(cliente_id);
CREATE INDEX idx_processos_numero_cnj ON processos(numero_cnj);
CREATE INDEX idx_contratos_honorarios_tenant_id ON contratos_honorarios(tenant_id);
CREATE INDEX idx_contratos_honorarios_processo_id ON contratos_honorarios(processo_id);
CREATE INDEX idx_parcelas_honorarios_tenant_id ON parcelas_honorarios(tenant_id);
CREATE INDEX idx_parcelas_honorarios_contrato_id ON parcelas_honorarios(contrato_id);
CREATE INDEX idx_parcelas_honorarios_data_vencimento ON parcelas_honorarios(data_vencimento);
CREATE INDEX idx_parcelas_honorarios_status ON parcelas_honorarios(status);
CREATE INDEX idx_lancamentos_hora_tenant_id ON lancamentos_hora(tenant_id);
CREATE INDEX idx_lancamentos_hora_contrato_id ON lancamentos_hora(contrato_id);
CREATE INDEX idx_despesas_processuais_tenant_id ON despesas_processuais(tenant_id);
CREATE INDEX idx_despesas_processuais_processo_id ON despesas_processuais(processo_id);
