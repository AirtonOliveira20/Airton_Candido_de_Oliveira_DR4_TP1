package br.edu.infnet.ecommerce.payment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class ProcessadorPagamento {

    
    public ResultadoProcessamento processar(
            BigDecimal valor,
            String formaPagamento,
            String numeroCartao
    ) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return ResultadoProcessamento.recusado("VALOR_INVALIDO");
        }

        if (!"CARTAO".equalsIgnoreCase(formaPagamento)) {
            return ResultadoProcessamento.recusado("FORMA_PAGAMENTO_NAO_SUPORTADA");
        }

        if (numeroCartao == null || numeroCartao.length() < 4) {
            return ResultadoProcessamento.recusado("CARTAO_INVALIDO");
        }

        if (valor.compareTo(new BigDecimal("10000.00")) > 0) {
            return ResultadoProcessamento.recusado("LIMITE_EXCEDIDO");
        }

        if (numeroCartao.endsWith("0000")) {
            return ResultadoProcessamento.recusado("CARTAO_BLOQUEADO");
        }

        return ResultadoProcessamento.aprovado(
                UUID.randomUUID().toString().substring(0, 8).toUpperCase()
        );
    }
}
