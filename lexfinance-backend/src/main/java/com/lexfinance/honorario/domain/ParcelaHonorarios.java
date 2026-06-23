package com.lexfinance.honorario.domain;

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
import jakarta.persistence.Transient;
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
@Table(name = "parcelas_honorarios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ParcelaHonorarios {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private ContratoHonorarios contrato;

    @Column(name = "numero_parcela", nullable = false)
    private Integer numeroParcela;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private StatusParcela status;

    @Column(name = "data_recebimento")
    private LocalDate dataRecebimento;

    @Column(name = "observacao", length = 500)
    private String observacao;

    @Embedded
    @Builder.Default
    private AuditInfo auditInfo = new AuditInfo();

    @Transient
    public StatusParcela getStatusCalculado() {
        if (status == StatusParcela.RECEBIDO) {
            return StatusParcela.RECEBIDO;
        }

        if (status == StatusParcela.PENDENTE
                && dataVencimento.isBefore(LocalDate.now())) {
            return StatusParcela.ATRASADO;
        }

        return status;
    }
}
