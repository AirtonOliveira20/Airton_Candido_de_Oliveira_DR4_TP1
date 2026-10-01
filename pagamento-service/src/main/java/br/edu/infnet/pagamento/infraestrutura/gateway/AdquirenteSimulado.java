package br.edu.infnet.pagamento.infraestrutura.gateway;

import br.edu.infnet.pagamento.dominio.modelo.DadosCartao;
import br.edu.infnet.pagamento.dominio.modelo.Dinheiro;
import br.edu.infnet.pagamento.dominio.modelo.MotivoRecusa;
import br.edu.infnet.pagamento.dominio.porta.GatewayDeAutorizacao;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ADAPTER de saida que simula o adquirente/emissor (mesmo comportamento do ProcessadorPagamento legado).
 * Para integrar com um adquirente real basta criar outro adapter desta porta - o dominio nao muda.
 */
@Component
public class AdquirenteSimulado implements GatewayDeAutorizacao {

    @Override
    public RespostaAutorizacao autorizar(Dinheiro valor, DadosCartao cartao) {
        if (cartao.numero().endsWith("0000")) {
            return RespostaAutorizacao.recusada(MotivoRecusa.CARTAO_BLOQUEADO);
        }
        return RespostaAutorizacao.aprovada(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }
}
