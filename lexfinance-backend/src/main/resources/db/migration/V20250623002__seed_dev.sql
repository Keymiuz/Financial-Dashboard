-- Seed de dados para desenvolvimento do LexFinance
-- Data Base: 2026-06-23 (Hoje no contexto da aplicação)

-- 1. Inserir Tenant (Escritório)
INSERT INTO tenants (id, nome, cnpj, ativo, created_at, updated_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'LexFinance Advocacia e Associados',
    '12345678000190',
    TRUE,
    '2026-01-01 08:00:00',
    '2026-01-01 08:00:00'
);

-- 2. Inserir Usuário Administrador (Senha criptografada "admin123")
INSERT INTO usuarios (id, tenant_id, nome, email, senha_hash, ativo, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    '550e8400-e29b-41d4-a716-446655440000',
    'Administrador LexFinance',
    'admin@lexfinance.dev',
    '$2a$10$zBeorUpq5pt2X/eviYXMHOoKFmccTDiMcs.EpeXdfLBeVW4zI8HFe',
    TRUE,
    '2026-01-01 08:30:00',
    '2026-01-01 08:30:00'
);

-- 3. Inserir 3 Clientes (Pessoa Física e Pessoa Jurídica)
-- Cliente 1: PF (João Silva)
INSERT INTO clientes (id, tenant_id, nome, tipo, cpf_cnpj, email, telefone, ativo, created_at, updated_at)
VALUES (
    'c1111111-1111-1111-1111-111111111111',
    '550e8400-e29b-41d4-a716-446655440000',
    'João Silva',
    'PESSOA_FISICA',
    '11122233344',
    'joao.silva@email.com',
    '(11) 98888-7777',
    TRUE,
    '2026-01-10 10:00:00',
    '2026-01-10 10:00:00'
);

-- Cliente 2: PJ (Transportadora Rapidão)
INSERT INTO clientes (id, tenant_id, nome, tipo, cpf_cnpj, email, telefone, ativo, created_at, updated_at)
VALUES (
    'c2222222-2222-2222-2222-222222222222',
    '550e8400-e29b-41d4-a716-446655440000',
    'Empresa de Transportes Rapidão LTDA',
    'PESSOA_JURIDICA',
    '99888777000111',
    'contato@rapidao.com.br',
    '(11) 3300-4400',
    TRUE,
    '2026-02-15 14:00:00',
    '2026-02-15 14:00:00'
);

-- Cliente 3: PF (Maria Souza)
INSERT INTO clientes (id, tenant_id, nome, tipo, cpf_cnpj, email, telefone, ativo, created_at, updated_at)
VALUES (
    'c3333333-3333-3333-3333-333333333333',
    '550e8400-e29b-41d4-a716-446655440000',
    'Maria Souza',
    'PESSOA_FISICA',
    '55566677788',
    'maria.souza@yahoo.com',
    '(21) 97777-6666',
    TRUE,
    '2026-03-20 09:00:00',
    '2026-03-20 09:00:00'
);

-- 4. Inserir 5 Processos em Áreas Diferentes
-- Processo 1: CIVIL - Ativo (Cliente 1 - João Silva)
INSERT INTO processos (id, tenant_id, cliente_id, numero_cnj, descricao, area, status, data_inicio, created_at, updated_at)
VALUES (
    'e1111111-1111-1111-1111-111111111111',
    '550e8400-e29b-41d4-a716-446655440000',
    'c1111111-1111-1111-1111-111111111111',
    '5001234-56.2024.8.26.0100',
    'Ação de Indenização por Danos Morais contra Companhia Aérea',
    'CIVIL',
    'ATIVO',
    '2026-01-15',
    '2026-01-15 11:00:00',
    '2026-01-15 11:00:00'
);

-- Processo 2: TRABALHISTA - Ativo (Cliente 2 - Transportadora Rapidão)
INSERT INTO processos (id, tenant_id, cliente_id, numero_cnj, descricao, area, status, data_inicio, created_at, updated_at)
VALUES (
    'e2222222-2222-2222-2222-222222222222',
    '550e8400-e29b-41d4-a716-446655440000',
    'c2222222-2222-2222-2222-222222222222',
    '1000789-12.2024.5.02.0001',
    'Defesa Trabalhista - Reclamatória proposta por ex-motorista',
    'TRABALHISTA',
    'ATIVO',
    '2026-02-20',
    '2026-02-20 15:30:00',
    '2026-02-20 15:30:00'
);

-- Processo 3: TRIBUTARIO - Suspenso (Cliente 2 - Transportadora Rapidão)
INSERT INTO processos (id, tenant_id, cliente_id, numero_cnj, descricao, area, status, data_inicio, created_at, updated_at)
VALUES (
    'e3333333-3333-3333-3333-333333333333',
    '550e8400-e29b-41d4-a716-446655440000',
    'c2222222-2222-2222-2222-222222222222',
    '5009876-43.2023.4.03.6100',
    'Mandado de Segurança contra Auto de Infração de ICMS',
    'TRIBUTARIO',
    'SUSPENSO',
    '2026-03-01',
    '2026-03-01 10:15:00',
    '2026-03-01 10:15:00'
);

-- Processo 4: CRIMINAL - Ativo (Cliente 3 - Maria Souza)
INSERT INTO processos (id, tenant_id, cliente_id, numero_cnj, descricao, area, status, data_inicio, created_at, updated_at)
VALUES (
    'e4444444-4444-4444-4444-444444444444',
    '550e8400-e29b-41d4-a716-446655440000',
    'c3333333-3333-3333-3333-333333333333',
    '1500456-78.2024.8.19.0001',
    'Acompanhamento de Inquérito Policial e Defesa Criminal',
    'CRIMINAL',
    'ATIVO',
    '2026-03-25',
    '2026-03-25 14:00:00',
    '2026-03-25 14:00:00'
);

-- Processo 5: PREVIDENCIARIO - Encerrado (Cliente 1 - João Silva)
INSERT INTO processos (id, tenant_id, cliente_id, numero_cnj, descricao, area, status, data_inicio, created_at, updated_at)
VALUES (
    'e5555555-5555-5555-5555-555555555555',
    '550e8400-e29b-41d4-a716-446655440000',
    'c1111111-1111-1111-1111-111111111111',
    '5011223-99.2022.4.03.6183',
    'Ação de Concessão de Aposentadoria Especial por Tempo de Serviço',
    'PREVIDENCIARIO',
    'ENCERRADO',
    '2026-01-05',
    '2026-01-05 09:30:00',
    '2026-06-20 17:00:00'
);

-- 5. Inserir 3 Contratos de Honorários (Mix de FIXO e HORA)
-- Contrato 1: FIXO de R$ 5.000,00 no Processo 1 (Civil)
INSERT INTO contratos_honorarios (id, tenant_id, processo_id, tipo, valor_fixo, valor_hora, descricao, data_contrato, ativo, created_at, updated_at)
VALUES (
    'f1111111-1111-1111-1111-111111111111',
    '550e8400-e29b-41d4-a716-446655440000',
    'e1111111-1111-1111-1111-111111111111',
    'FIXO',
    5000.00,
    NULL,
    'Honorários contratuais de êxito e fase inicial',
    '2026-01-16',
    TRUE,
    '2026-01-16 10:00:00',
    '2026-01-16 10:00:00'
);

-- Contrato 2: HORA com valor/hora de R$ 250,00 no Processo 2 (Trabalhista)
INSERT INTO contratos_honorarios (id, tenant_id, processo_id, tipo, valor_fixo, valor_hora, descricao, data_contrato, ativo, created_at, updated_at)
VALUES (
    'f2222222-2222-2222-2222-222222222222',
    '550e8400-e29b-41d4-a716-446655440000',
    'e2222222-2222-2222-2222-222222222222',
    'HORA',
    NULL,
    250.00,
    'Honorários advocatícios cobrados por hora de serviço executado',
    '2026-02-21',
    TRUE,
    '2026-02-21 16:00:00',
    '2026-02-21 16:00:00'
);

-- Contrato 3: FIXO de R$ 8.000,00 no Processo 4 (Criminal)
INSERT INTO contratos_honorarios (id, tenant_id, processo_id, tipo, valor_fixo, valor_hora, descricao, data_contrato, ativo, created_at, updated_at)
VALUES (
    'f3333333-3333-3333-3333-333333333333',
    '550e8400-e29b-41d4-a716-446655440000',
    'e4444444-4444-4444-4444-444444444444',
    'FIXO',
    8000.00,
    NULL,
    'Defesa criminal integral em primeira instância',
    '2026-03-26',
    TRUE,
    '2026-03-26 11:30:00',
    '2026-03-26 11:30:00'
);

-- 6. Inserir Lançamentos de Horas para Contrato 2 (Trabalhista)
-- Lançamento 1: Faturado
INSERT INTO lancamentos_hora (id, tenant_id, contrato_id, descricao, data, horas, valor_calculado, faturado, created_at, updated_at)
VALUES (
    'a1111111-1111-1111-1111-111111111111',
    '550e8400-e29b-41d4-a716-446655440000',
    'f2222222-2222-2222-2222-222222222222',
    'Reunião inicial com gerência para levantamento de provas',
    '2026-03-05',
    2.00,
    500.00, -- 2h * R$ 250
    TRUE,
    '2026-03-05 18:00:00',
    '2026-03-05 18:00:00'
);

-- Lançamento 2: Faturado
INSERT INTO lancamentos_hora (id, tenant_id, contrato_id, descricao, data, horas, valor_calculado, faturado, created_at, updated_at)
VALUES (
    'a2222222-2222-2222-2222-222222222222',
    '550e8400-e29b-41d4-a716-446655440000',
    'f2222222-2222-2222-2222-222222222222',
    'Elaboração de peça de contestação e cálculo de liquidação',
    '2026-03-10',
    3.00,
    750.00, -- 3h * R$ 250
    TRUE,
    '2026-03-10 17:30:00',
    '2026-03-10 17:30:00'
);

-- Lançamento 3: Não Faturado (Para teste do endpoint de faturamento)
INSERT INTO lancamentos_hora (id, tenant_id, contrato_id, descricao, data, horas, valor_calculado, faturado, created_at, updated_at)
VALUES (
    'a3333333-3333-3333-3333-333333333333',
    '550e8400-e29b-41d4-a716-446655440000',
    'f2222222-2222-2222-2222-222222222222',
    'Análise de réplica da parte autora e petição de provas',
    '2026-06-22',
    1.50,
    375.00, -- 1.5h * R$ 250
    FALSE,
    '2026-06-22 16:00:00',
    '2026-06-22 16:00:00'
);

-- 7. Inserir 8 Parcelas de Honorários com datas variadas
-- (Data Base de Referência: 2026-06-23)

-- Parcela 1: Contrato 1 (FIXO) - R$ 1.250,00 - Recebida no prazo
INSERT INTO parcelas_honorarios (id, tenant_id, contrato_id, numero_parcela, valor, data_vencimento, status, data_recebimento, observacao, created_at, updated_at)
VALUES (
    'b1111111-1111-1111-1111-111111111111',
    '550e8400-e29b-41d4-a716-446655440000',
    'f1111111-1111-1111-1111-111111111111',
    1,
    1250.00,
    '2026-05-10',
    'RECEBIDO',
    '2026-05-08',
    'Recebimento via PIX',
    '2026-01-16 10:30:00',
    '2026-05-08 11:00:00'
);

-- Parcela 2: Contrato 1 (FIXO) - R$ 1.250,00 - ATRASADA (Venceu em 2026-06-10 e está PENDENTE)
INSERT INTO parcelas_honorarios (id, tenant_id, contrato_id, numero_parcela, valor, data_vencimento, status, data_recebimento, observacao, created_at, updated_at)
VALUES (
    'b2222222-2222-2222-2222-222222222222',
    '550e8400-e29b-41d4-a716-446655440000',
    'f1111111-1111-1111-1111-111111111111',
    2,
    1250.00,
    '2026-06-10',
    'PENDENTE',
    NULL,
    'Aguardando contato do cliente',
    '2026-01-16 10:30:00',
    '2026-01-16 10:30:00'
);

-- Parcela 3: Contrato 1 (FIXO) - R$ 1.250,00 - FUTURA (Vence em 2026-07-10 e está PENDENTE)
INSERT INTO parcelas_honorarios (id, tenant_id, contrato_id, numero_parcela, valor, data_vencimento, status, data_recebimento, observacao, created_at, updated_at)
VALUES (
    'b3333333-3333-3333-3333-333333333333',
    '550e8400-e29b-41d4-a716-446655440000',
    'f1111111-1111-1111-1111-111111111111',
    3,
    1250.00,
    '2026-07-10',
    'PENDENTE',
    NULL,
    NULL,
    '2026-01-16 10:30:00',
    '2026-01-16 10:30:00'
);

-- Parcela 4: Contrato 1 (FIXO) - R$ 1.250,00 - FUTURA (Vence em 2026-08-10 e está PENDENTE)
INSERT INTO parcelas_honorarios (id, tenant_id, contrato_id, numero_parcela, valor, data_vencimento, status, data_recebimento, observacao, created_at, updated_at)
VALUES (
    'b4444444-4444-4444-4444-444444444444',
    '550e8400-e29b-41d4-a716-446655440000',
    'f1111111-1111-1111-1111-111111111111',
    4,
    1250.00,
    '2026-08-10',
    'PENDENTE',
    NULL,
    NULL,
    '2026-01-16 10:30:00',
    '2026-01-16 10:30:00'
);

-- Parcela 5: Contrato 3 (FIXO) - R$ 4.000,00 - ATRASADA (Venceu em 2026-06-01 e está PENDENTE)
INSERT INTO parcelas_honorarios (id, tenant_id, contrato_id, numero_parcela, valor, data_vencimento, status, data_recebimento, observacao, created_at, updated_at)
VALUES (
    'b5555555-5555-5555-5555-555555555555',
    '550e8400-e29b-41d4-a716-446655440000',
    'f3333333-3333-3333-3333-333333333333',
    1,
    4000.00,
    '2026-06-01',
    'PENDENTE',
    NULL,
    'Cliente solicitou boleto atualizado',
    '2026-03-26 11:45:00',
    '2026-03-26 11:45:00'
);

-- Parcela 6: Contrato 3 (FIXO) - R$ 4.000,00 - FUTURA (Vence em 2026-07-01 e está PENDENTE)
INSERT INTO parcelas_honorarios (id, tenant_id, contrato_id, numero_parcela, valor, data_vencimento, status, data_recebimento, observacao, created_at, updated_at)
VALUES (
    'b6666666-6666-6666-6666-666666666666',
    '550e8400-e29b-41d4-a716-446655440000',
    'f3333333-3333-3333-3333-333333333333',
    2,
    4000.00,
    '2026-07-01',
    'PENDENTE',
    NULL,
    NULL,
    '2026-03-26 11:45:00',
    '2026-03-26 11:45:00'
);

-- Parcela 7: Contrato 2 (HORA) - R$ 500,00 - Recebida (Horas Faturadas de Lançamento 1)
INSERT INTO parcelas_honorarios (id, tenant_id, contrato_id, numero_parcela, valor, data_vencimento, status, data_recebimento, observacao, created_at, updated_at)
VALUES (
    'b7777777-7777-7777-7777-777777777777',
    '550e8400-e29b-41d4-a716-446655440000',
    'f2222222-2222-2222-2222-222222222222',
    1,
    500.00,
    '2026-04-10',
    'RECEBIDO',
    '2026-04-10',
    'Faturamento de 2.0h em Março',
    '2026-04-01 09:00:00',
    '2026-04-10 14:00:00'
);

-- Parcela 8: Contrato 2 (HORA) - R$ 750,00 - FUTURA (Horas Faturadas de Lançamento 2, vence em 2026-07-20 e está PENDENTE)
INSERT INTO parcelas_honorarios (id, tenant_id, contrato_id, numero_parcela, valor, data_vencimento, status, data_recebimento, observacao, created_at, updated_at)
VALUES (
    'b8888888-8888-8888-8888-888888888888',
    '550e8400-e29b-41d4-a716-446655440000',
    'f2222222-2222-2222-2222-222222222222',
    2,
    750.00,
    '2026-07-20',
    'PENDENTE',
    NULL,
    'Faturamento de 3.0h em Março',
    '2026-04-01 09:00:00',
    '2026-04-01 09:00:00'
);

-- 8. Inserir 2 Despesas Processuais
-- Despesa 1: PENDENTE no Processo 1
INSERT INTO despesas_processuais (id, tenant_id, processo_id, descricao, tipo, valor, data_despesa, status_ressarcimento, data_ressarcimento, created_at, updated_at)
VALUES (
    'd1111111-1111-1111-1111-111111111111',
    '550e8400-e29b-41d4-a716-446655440000',
    'e1111111-1111-1111-1111-111111111111',
    'Taxa de expedição de custas iniciais',
    'CUSTA_JUDICIAL',
    350.00,
    '2026-01-20',
    'PENDENTE',
    NULL,
    '2026-01-20 14:00:00',
    '2026-01-20 14:00:00'
);

-- Despesa 2: RESSARCIDA no Processo 2
INSERT INTO despesas_processuais (id, tenant_id, processo_id, descricao, tipo, valor, data_despesa, status_ressarcimento, data_ressarcimento, created_at, updated_at)
VALUES (
    'd2222222-2222-2222-2222-222222222222',
    '550e8400-e29b-41d4-a716-446655440000',
    'e2222222-2222-2222-2222-222222222222',
    'Honorários de Perito Judicial Grafotécnico',
    'HONORARIO_PERITO',
    2000.00,
    '2026-03-01',
    'RESSARCIDO',
    '2026-03-15',
    '2026-03-01 10:00:00',
    '2026-03-15 16:30:00'
);
