package br.edu.infnet.pagamento.aplicacao;

import br.edu.infnet.pagamento.dominio.modelo.MotivoRecusa;
import br.edu.infnet.pagamento.dominio.modelo.Pagamento;
import br.edu.infnet.pagamento.dominio.modelo.StatusPagamento;

import java.math.BigDecimal;
import java.time.Instant;


public record ResultadoPagamento(String pagamentoId,
                                 Long pedidoId,
                                 boolean aprovado,
                                 String status,
                                 String motivo,
                                 String codigoAutorizacao,
                                 BigDecimal valor,
                                 String cartao,
                                 Instant processadoEm,
                                 boolean jaProcessadoAnteriormente) {

    public static ResultadoPagamento de(Pagamento p, boolean jaProcessadoAnteriormente) {
        return new ResultadoPagamento(
                p.id().toString(),
                p.pedidoId().valor(),
                p.status() == StatusPagamento.APROVADO,
                p.status().name(),
                p.motivoRecusa().map(MotivoRecusa::name).orElse(null),
                p.codigoAutorizacao().orElse(null),
                p.valor().valor(),
                p.cartao().formatado(),
                p.processadoEm().orElse(null),
                jaProcessadoAnteriormente);
    }
}
