package br.edu.infnet.pagamento.dominio.modelo;

import java.util.Optional;

/**
 * Regras de negocio que o PROPRIO dominio decide, antes de envolver o gateway externo.
 * No monolito estavam misturadas com a simulacao do adquirente dentro de ProcessadorPagamento.
 */
public record PoliticaDePagamento(Dinheiro limitePorTransacao) {

    public Optional<MotivoRecusa> avaliar(Dinheiro valor, Optional<FormaPagamento> forma, DadosCartao cartao) {
        if (!valor.ehPositivo()) {
            return Optional.of(MotivoRecusa.VALOR_INVALIDO);
        }
        if (forma.isEmpty()) {
            return Optional.of(MotivoRecusa.FORMA_PAGAMENTO_NAO_SUPORTADA);
        }
        if (!cartao.ehValido()) {
            return Optional.of(MotivoRecusa.CARTAO_INVALIDO);
        }
        if (valor.ehMaiorQue(limitePorTransacao)) {
            return Optional.of(MotivoRecusa.LIMITE_EXCEDIDO);
        }
        return Optional.empty();
    }
}
