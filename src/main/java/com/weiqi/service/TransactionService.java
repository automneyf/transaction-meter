package com.weiqi.service;

import com.weiqi.util.NumberUtil;
import com.weiqi.web.rest.vm.Transaction;
import com.weiqi.web.rest.vm.TransactionStats;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.weiqi.constant.Constants.TRANSACTION_WINDOW_IN_MILLISECONDS;

/**
 * Created by Weiqi on 5/4/2018.
 */
@Service
public class TransactionService implements ITransactionService {

    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private TransactionStats stats = new TransactionStats();
    private PriorityQueue<Double> minTransactionHeap = new PriorityQueue<>();
    private PriorityQueue<Double> maxTransactionHeap = new PriorityQueue<>(Comparator.reverseOrder());

    @Override
    public boolean createTransaction(Transaction transaction) {
        if (!isValidTransaction(transaction)) {
            return false;
        }

        updateTransactionStatsWhenCreated(transaction.getAmount());
        scheduleTransactionExpiration(transaction);
        return true;
    }

    @Override
    public TransactionStats getTransactionStats() {
        lock.readLock().lock();
        try {
            return stats;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void reset() {
        lock.writeLock().lock();
        try {
            stats = new TransactionStats();
            minTransactionHeap = new PriorityQueue<>();
            maxTransactionHeap = new PriorityQueue<>(Comparator.reverseOrder());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void updateTransactionStatsWhenCreated(double transactionAmount) {
        lock.writeLock().lock();
        try {
            stats.setSum(NumberUtil.getMoneyAmount(stats.getSum() + transactionAmount));
            stats.setCount(stats.getCount() + 1);
            minTransactionHeap.add(transactionAmount);
            maxTransactionHeap.add(transactionAmount);
            stats.setAvg(NumberUtil.getMoneyAmount(stats.getSum() / stats.getCount()));
            stats.setMax(maxTransactionHeap.peek());
            stats.setMin(minTransactionHeap.peek());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void updateTransactionStatsWhenExpired(double transactionAmount) {
        lock.writeLock().lock();
        try {
            stats.setSum(NumberUtil.getMoneyAmount(stats.getSum() - transactionAmount));
            stats.setCount(stats.getCount() - 1);
            minTransactionHeap.remove(transactionAmount);
            maxTransactionHeap.remove(transactionAmount);
            if (stats.getCount() > 0) {
                stats.setAvg(NumberUtil.getMoneyAmount(stats.getSum() / stats.getCount()));
            } else {
                stats.setAvg(0.0);
            }
            stats.setMax(maxTransactionHeap.peek() != null ? maxTransactionHeap.peek() : 0);
            stats.setMin(minTransactionHeap.peek() != null ? minTransactionHeap.peek() : 0);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void scheduleTransactionExpiration(Transaction transaction) {
        long timeDiff = transaction.getTimestamp() - new Date().getTime() + TRANSACTION_WINDOW_IN_MILLISECONDS;
        executorService.schedule(new RunUpdateTransactionStats(transaction.getAmount()), timeDiff, TimeUnit.MILLISECONDS);
    }

    private boolean isValidTransaction(Transaction transaction) {
        long timeDiff = new Date().getTime() - transaction.getTimestamp();
        // Do not allow future transactions based on requirements
        return timeDiff >= 0 && timeDiff <= TRANSACTION_WINDOW_IN_MILLISECONDS;
    }

    private class RunUpdateTransactionStats extends TimerTask {

        private double transactionAmount;

        public RunUpdateTransactionStats(double transactionAmount) {
            this.transactionAmount = transactionAmount;
        }

        @Override
        public void run() {
            updateTransactionStatsWhenExpired(transactionAmount);
        }
    }
}
