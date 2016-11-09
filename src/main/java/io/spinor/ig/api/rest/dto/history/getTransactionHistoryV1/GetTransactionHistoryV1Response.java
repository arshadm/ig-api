package io.spinor.ig.api.rest.dto.history.getTransactionHistoryV1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
List of transactions
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetTransactionHistoryV1Response {

/*
List of transactions
*/
private java.util.List<TransactionsItem> transactions;

public java.util.List<TransactionsItem> getTransactions() { return transactions; }
public void setTransactions(java.util.List<TransactionsItem> transactions) { this.transactions=transactions; }
}
