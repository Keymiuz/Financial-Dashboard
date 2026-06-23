package com.lexfinance.cliente.domain;

import com.lexfinance.shared.domain.AuditInfo;
import com.lexfinance.tenant.domain.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE clientes SET ativo = false WHERE id = ?")
@SQLRestriction("ativo = true")
@EntityListeners(AuditingEntityListener.class)
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoCliente tipo;

    @Column(name = "cpf_cnpj", nullable = false, length = 14)
    private String cpfCnpj;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Embedded
    @Builder.Default
    private AuditInfo auditInfo = new AuditInfo();
}
