package br.edu.infnet.ecommerce.integracao.pagamento;

import br.edu.infnet.ecommerce.entity.Pagamento;
import br.edu.infnet.ecommerce.entity.Pedido;
import br.edu.infnet.ecommerce.entity.Usuario;
import br.edu.infnet.ecommerce.integracao.pagamento.ResultadoPagamento.OrigemPagamento;
import br.edu.infnet.ecommerce.repository.PedidoRepository;
import br.edu.infnet.ecommerce.repository.UsuarioRepository;
import br.edu.infnet.ecommerce.service.PagamentoService;
import org.springframework.stereotype.Component;


@Component
public class PagamentoLegadoAdapter implements GatewayDePagamento {

    private final PagamentoService pagamentoService;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public PagamentoLegadoAdapter(PagamentoService pagamentoService,
                                  PedidoRepository pedidoRepository,
                                  UsuarioRepository usuarioRepository) {
        this.pagamentoService = pagamentoService;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public ResultadoPagamento processar(SolicitacaoPagamento s) {
        Pedido pedido = pedidoRepository.getReferenceById(s.pedidoId());
        Usuario usuario = usuarioRepository.getReferenceById(s.usuarioId());

        Pagamento pagamento = pagamentoService.processar(
                pedido, usuario, s.valor(), s.formaPagamento(), s.numeroCartao());

        return new ResultadoPagamento(
                "APROVADO".equals(pagamento.getStatus()),
                pagamento.getStatus(),
                pagamento.getMotivo(),
                pagamento.getCodigoAutorizacao(),
                String.valueOf(pagamento.getId()),
                OrigemPagamento.LEGADO);
    }
}
