package br.edu.infnet.ecommerce.integracao.pagamento;

/*BRANCH BY ABSTRACTION - a ABSTRACAO.*/
public interface GatewayDePagamento {

    ResultadoPagamento processar(SolicitacaoPagamento solicitacao);
}
