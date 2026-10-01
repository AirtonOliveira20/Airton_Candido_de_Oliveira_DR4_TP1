package br.edu.infnet.ecommerce.integracao.pagamento;


public class PagamentoIndisponivelException extends RuntimeException {

    public PagamentoIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
