package com.weiqi.service;

import com.weiqi.web.rest.vm.Transaction;
import com.weiqi.web.rest.vm.TransactionStats;

/**
 * The service to handler all transaction related operations
 * Created by Weiqi on 5/4/2018.
 */
public interface ITransactionService {

    /**
     * Create a new transaction
     * @param transaction transaction vm
     * @return true if transaction is successfully created and stats is updated
     */
    boolean createTransaction(Transaction transaction);

    /**
     * Get transaction statistics based on transactions in last @see com.weiqi.constant.Constants#TRANSACTION_WINDOW_IN_MILLISECONDS milliseconds
     * @return statistics based on transactions in last @see com.weiqi.constant.Constants#TRANSACTION_WINDOW_IN_MILLISECONDS milliseconds
     */
    TransactionStats getTransactionStats();

    /**
     * Reset transaction stats. Only used for testing
     */
    void reset();
}
