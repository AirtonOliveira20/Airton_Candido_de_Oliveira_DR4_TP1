package br.edu.infnet.pagamento.infraestrutura.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Modelo de PERSISTENCIA (detalhe de infraestrutura), separado do Aggregate de dominio.
 * Repare que pedido_id e cliente_id sao colunas simples: nao ha FK para tabelas de outro contexto
 * (elas nem existem neste banco).
 */
@Entity
@Table(name = "pagamentos")
public class PagamentoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "pedido_id", nullable = false, unique = true)   // invariante: 1 pagamento por pedido
    private Long pedidoId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    private String formaPagamento;

    @Column(nullable = false, length = 4)
    private String cartaoUltimosDigitos;

    @Column(nullable = false)
    private String status;

    private String motivoRecusa;

    private String codigoAutorizacao;

    @Column(nullable = false)
    private Instant criadoEm;

    private Instant processadoEm;

    protected PagamentoJpaEntity() {
    }

    PagamentoJpaEntity(UUID id, Long pedidoId, Long clienteId, BigDecimal valor, String formaPagamento,
                       String cartaoUltimosDigitos, String status, String motivoRecusa,
                       String codigoAutorizacao, Instant criadoEm, Instant processadoEm) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.clienteId = clienteId;
        this.valor = valor;
        this.formaPagamento = formaPagamento;
        this.cartaoUltimosDigitos = cartaoUltimosDigitos;
        this.status = status;
        this.motivoRecusa = motivoRecusa;
        this.codigoAutorizacao = codigoAutorizacao;
        this.criadoEm = criadoEm;
        this.processadoEm = processadoEm;
    }

    UUID getId() { return id; }
    Long getPedidoId() { return pedidoId; }
    Long getClienteId() { return clienteId; }
    BigDecimal getValor() { return valor; }
    String getFormaPagamento() { return formaPagamento; }
    String getCartaoUltimosDigitos() { return cartaoUltimosDigitos; }
    String getStatus() { return status; }
    String getMotivoRecusa() { return motivoRecusa; }
    String getCodigoAutorizacao() { return codigoAutorizacao; }
    Instant getCriadoEm() { return criadoEm; }
    Instant getProcessadoEm() { return processadoEm; }
}
