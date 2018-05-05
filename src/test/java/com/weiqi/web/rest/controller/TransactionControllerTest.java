package com.weiqi.web.rest.controller;

import com.google.gson.Gson;
import com.weiqi.service.ITransactionService;
import com.weiqi.web.rest.vm.Transaction;
import com.weiqi.web.rest.vm.TransactionStats;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Weiqi on 5/4/2018.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    private static final String STATISTICS_ENDPOINT_URL = "/statistics";
    private static final String TRANSACTION_ENDPOINT_URL = "/transactions";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ITransactionService transactionService;

    @Test
    public void givenStatistics_thenReadStatistics() throws Exception {
        TransactionStats stats = new TransactionStats(0.01, 0.01, 0.01, 0.01, 1);
        given(transactionService.getTransactionStats()).willReturn(stats);
        mvc.perform(get(STATISTICS_ENDPOINT_URL))
                .andExpect(status().isOk())
                .andExpect(content().json(new Gson().toJson(stats)));
    }

    @Test
    public void whenCreateEmptyAmountTransaction_thenBadRequest() throws Exception {
        Transaction transaction = new Transaction(null, new Date().getTime());
        mvc.perform(post(TRANSACTION_ENDPOINT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(transaction)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenCreateEmptyTimestampTransaction_thenBadRequest() throws Exception {
        Transaction transaction = new Transaction(0.01, null);
        mvc.perform(post(TRANSACTION_ENDPOINT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(transaction)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenCreateNegativeAmountTransaction_thenBadRequest() throws Exception {
        Transaction transaction = new Transaction(-0.01, new Date().getTime());
        mvc.perform(post(TRANSACTION_ENDPOINT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(transaction)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenCreateNegativeTimestampTransaction_thenBadRequest() throws Exception {
        Transaction transaction = new Transaction(0.01, -1l);
        mvc.perform(post(TRANSACTION_ENDPOINT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(transaction)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenCreateExpiredTransaction_thenTransactionNotCreated() throws Exception {
        Transaction transaction = new Transaction(0.01, 0l);
        given(transactionService.createTransaction(transaction)).willReturn(false);
        mvc.perform(post(TRANSACTION_ENDPOINT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(transaction)))
                .andExpect(status().isNoContent());
    }


    @Test
    public void whenCreateValidTransaction_thenTransactionCreated() throws Exception {
        Transaction transaction = new Transaction(0.01, new Date().getTime());
        given(transactionService.createTransaction(transaction)).willReturn(true);
        mvc.perform(post(TRANSACTION_ENDPOINT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(transaction)))
                .andExpect(status().isCreated());
    }

}
