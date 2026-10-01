package br.edu.infnet.pagamento.api;

import br.edu.infnet.pagamento.aplicacao.ResultadoPagamento;
import br.edu.infnet.pagamento.aplicacao.ServicoDePagamento;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** ADAPTER de entrada (REST) do contexto de Pagamento. */
@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

    private final ServicoDePagamento servico;

    public PagamentoController(ServicoDePagamento servico) {
        this.servico = servico;
    }

 
    @PostMapping
    public ResponseEntity<ResultadoPagamento> processar(
            @Valid @RequestBody ProcessarPagamentoRequest request,
            @RequestParam(defaultValue = "false") boolean simulacao) {
        if (simulacao) {
            return ResponseEntity.ok(servico.simular(request.paraComando()));
        }
        ResultadoPagamento resultado = servico.processar(request.paraComando());
        return ResponseEntity
                .status(resultado.jaProcessadoAnteriormente() ? HttpStatus.OK : HttpStatus.CREATED)
                .body(resultado);
    }

    @GetMapping("/{pagamentoId}")
    public ResultadoPagamento buscar(@PathVariable String pagamentoId) {
        return servico.consultarPorId(pagamentoId);
    }

    @GetMapping
    public ResultadoPagamento buscarPorPedido(@RequestParam Long pedidoId) {
        return servico.consultarPorPedido(pedidoId);
    }

    /** Compensacao: estorna um pagamento aprovado. */
    @PostMapping("/{pagamentoId}/estorno")
    public ResultadoPagamento estornar(@PathVariable String pagamentoId) {
        return servico.estornar(pagamentoId);
    }
}
