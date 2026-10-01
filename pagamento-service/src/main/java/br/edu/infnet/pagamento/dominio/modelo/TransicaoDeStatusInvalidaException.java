package br.edu.infnet.pagamento.dominio.modelo;

/** Violacao de invariante: tentativa de mudar o status de forma nao permitida. */
public class TransicaoDeStatusInvalidaException extends RuntimeException {

    public TransicaoDeStatusInvalidaException(String mensagem) {
        super(mensagem);
    }
}
