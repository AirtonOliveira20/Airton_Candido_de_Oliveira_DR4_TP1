package br.edu.infnet.pagamento.dominio.porta;

import br.edu.infnet.pagamento.dominio.modelo.DadosCartao;
import br.edu.infnet.pagamento.dominio.modelo.Dinheiro;
import br.edu.infnet.pagamento.dominio.modelo.MotivoRecusa;

/**
 * PORTA DE SAIDA para o adquirente/emissor do cartao (sistema externo).
 * No monolito, PagamentoService dependia da classe concreta ProcessadorPagamento;
 * aqui o dominio depende so desta interface (Dependency Inversion).
 */
public interface GatewayDeAutorizacao {

    RespostaAutorizacao autorizar(Dinheiro valor, DadosCartao cartao);

    /** Resultado devolvido pelo gateway, ja traduzido para a linguagem do dominio. */
    record RespostaAutorizacao(boolean aprovada, String codigoAutorizacao, MotivoRecusa motivo) {

        public static RespostaAutorizacao aprovada(String codigo) {
            return new RespostaAutorizacao(true, codigo, null);
        }

        public static RespostaAutorizacao recusada(MotivoRecusa motivo) {
            return new RespostaAutorizacao(false, null, motivo);
        }
    }
}
