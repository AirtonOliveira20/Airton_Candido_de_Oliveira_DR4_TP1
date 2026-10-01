package br.edu.infnet.pagamento.aplicacao;

public class PagamentoNaoEncontradoException extends RuntimeException {

    public PagamentoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
