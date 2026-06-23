package com.lexfinance.despesa.domain;

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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "despesas_processuais")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DespesaProcessual {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "processo_id", nullable = false)
    private Processo processo;

    @Column(name = "descricao", nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private TipoDespesa tipo;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_despesa", nullable = false)
    private LocalDate dataDespesa;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_ressarcimento", nullable = false, length = 20)
    private StatusRessarcimento statusRessarcimento;

    @Column(name = "data_ressarcimento")
    private LocalDate dataRessarcimento;

    @Embedded
    @Builder.Default
    private AuditInfo auditInfo = new AuditInfo();
}
