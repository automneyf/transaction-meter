package com.weiqi.service;

import com.weiqi.web.rest.vm.Transaction;
import com.weiqi.web.rest.vm.TransactionStats;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static com.weiqi.constant.Constants.TRANSACTION_WINDOW_IN_MILLISECONDS;
import static org.junit.Assert.assertEquals;

/**
 * Created by Weiqi on 5/4/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionServiceTest {

    @Autowired
    private ITransactionService transactionService;

    @Test
    public void whenCreateExpiredTransaction_thenTransactionNotCreated() {
        transactionService.reset();
        Transaction transaction = new Transaction(0.01, new Date().getTime() - TRANSACTION_WINDOW_IN_MILLISECONDS - 1);
        assertEquals(false, transactionService.createTransaction(transaction));
        assertEquals(new TransactionStats(), transactionService.getTransactionStats());
    }

    @Test
    public void whenCreateFutureTransaction_thenTransactionNotCreated() {
        transactionService.reset();
        Transaction transaction = new Transaction(0.01, new Date().getTime() + 1);
        assertEquals(false, transactionService.createTransaction(transaction));
        assertEquals(new TransactionStats(), transactionService.getTransactionStats());
    }

    @Test
    public void whenCreateTransactions_thenGetStatistics() {
        transactionService.reset();

        double amount1 = 0.01;
        Transaction transaction1 = new Transaction(amount1, new Date().getTime());
        assertEquals(true, transactionService.createTransaction(transaction1));
        assertEquals(new TransactionStats(0.01, 0.01, 0.01, 0.01, 1), transactionService.getTransactionStats());

        double amount2 = 0.03;
        Transaction transaction2 = new Transaction(amount2, new Date().getTime());
        assertEquals(true, transactionService.createTransaction(transaction2));
        assertEquals(new TransactionStats(0.04, 0.02, 0.03, 0.01, 2), transactionService.getTransactionStats());
    }

    @Test
    public void whenResetTransactionStats_thenTransactionStatsReset() {
        transactionService.reset();

        double amount1 = 0.01;
        Transaction transaction1 = new Transaction(amount1, new Date().getTime());
        assertEquals(true, transactionService.createTransaction(transaction1));
        assertEquals(new TransactionStats(0.01, 0.01, 0.01, 0.01, 1), transactionService.getTransactionStats());

        double amount2 = 0.03;
        Transaction transaction2 = new Transaction(amount2, new Date().getTime());
        assertEquals(true, transactionService.createTransaction(transaction2));
        assertEquals(new TransactionStats(0.04, 0.02, 0.03, 0.01, 2), transactionService.getTransactionStats());

        transactionService.reset();
        assertEquals(new TransactionStats(0.0, 0.0, 0.0, 0.0, 0), transactionService.getTransactionStats());
    }

}
