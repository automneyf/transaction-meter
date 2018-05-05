package com.weiqi.web.rest.controller;

import com.weiqi.service.ITransactionService;
import com.weiqi.web.rest.vm.Transaction;
import com.weiqi.web.rest.vm.TransactionStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by Weiqi on 5/4/2018.
 */
@RestController
public class TransactionController {

    @Autowired
    private ITransactionService transactionService;

    /**
     * POST /transactions : Create transaction
     * @param transaction transaction in request body
     * @return the ResponseEntity with status 201 in case of success, or 204 if transaction is older than 60 seconds
     */
    @RequestMapping(path = "/transactions", method = RequestMethod.POST , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Void> createTransaction(@Valid @RequestBody Transaction transaction) {
        boolean created = transactionService.createTransaction(transaction);
        HttpStatus status = created ? HttpStatus.CREATED : HttpStatus.NO_CONTENT;
        return new ResponseEntity<>(status);
    }

    /**
     * GET /statistics : Get transaction statistics
     * @return the ResponseEntity with status 200 and transaction statistics in response body
     */
    @RequestMapping(path = "/statistics", method = RequestMethod.GET , produces = "application/json; charset=UTF-8")
    public ResponseEntity<TransactionStats> getTransactionStats() {
        return new ResponseEntity<>(transactionService.getTransactionStats(), HttpStatus.OK);
    }

}
