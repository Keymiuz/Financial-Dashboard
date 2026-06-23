package com.lexfinance.honorario.domain;

import com.lexfinance.processo.domain.Processo;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contratos_honorarios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE contratos_honorarios SET ativo = false WHERE id = ?")
@SQLRestriction("ativo = true")
@EntityListeners(AuditingEntityListener.class)
public class ContratoHonorarios {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "processo_id", nullable = false)
    private Processo processo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private TipoContrato tipo;

    @Column(name = "valor_fixo", precision = 12, scale = 2)
    private BigDecimal valorFixo;

    @Column(name = "valor_hora", precision = 12, scale = 2)
    private BigDecimal valorHora;

    @Column(name = "descricao", nullable = false, length = 500)
    private String descricao;

    @Column(name = "data_contrato", nullable = false)
    private LocalDate dataContrato;

    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Embedded
    @Builder.Default
    private AuditInfo auditInfo = new AuditInfo();
}
