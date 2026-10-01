package br.edu.infnet.pagamento.api;

import br.edu.infnet.pagamento.aplicacao.PagamentoNaoEncontradoException;
import br.edu.infnet.pagamento.dominio.modelo.TransicaoDeStatusInvalidaException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;


@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(PagamentoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(PagamentoNaoEncontradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(TransicaoDeStatusInvalidaException.class)
    ProblemDetail transicaoInvalida(TransicaoDeStatusInvalidaException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    /* Duas requisicoes simultaneas para o mesmo pedido: a chave unica de pedido_id barra a segunda. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail concorrencia(DataIntegrityViolationException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Pagamento deste pedido ja esta sendo processado. Consulte GET /api/pagamentos?pedidoId=");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail argumentoInvalido(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacao(MethodArgumentNotValidException e) {
        String detalhe = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalhe);
    }
}
