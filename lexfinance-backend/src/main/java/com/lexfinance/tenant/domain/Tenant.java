package com.lexfinance.tenant.domain;

import com.lexfinance.shared.domain.AuditInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "tenants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE tenants SET ativo = false WHERE id = ?")
@SQLRestriction("ativo = true")
@EntityListeners(AuditingEntityListener.class)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "cnpj", nullable = false, unique = true, length = 14)
    private String cnpj;

    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Embedded
    @Builder.Default
    private AuditInfo auditInfo = new AuditInfo();
}
