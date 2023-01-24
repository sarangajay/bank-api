package com.bank.banking.accounts.connector;

import com.bank.banking.accounts.domain.AccountNo;
import com.bank.banking.accounts.dto.AccountMovementRequest;
import com.bank.banking.accounts.dto.CreateAccountRequest;
import com.bank.banking.accounts.dto.FundTransferRequest;
import com.bank.banking.accounts.error.InsufficientFoundsException;
import com.bank.banking.accounts.error.InvalidAccountNumberException;
import com.bank.banking.accounts.error.InvalidAmountException;
import com.bank.banking.accounts.error.RequiredFieldException;
import com.bank.banking.accounts.service.BankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class BankConnector {

    private static final Logger LOG = LoggerFactory.getLogger(BankConnector.class);

    @Autowired
    private BankService bankService;

    @PostMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAccount(@RequestBody CreateAccountRequest createAccountRequest) throws InvalidAccountNumberException, InvalidAmountException, RequiredFieldException {
        LOG.info(String.format("Account creation Request Received %s", createAccountRequest.toString()));
        AccountNo accountNo = bankService.createAccount(createAccountRequest);
        LOG.info(String.format("Account is created with AccountNo : %s", accountNo));
        return new ResponseEntity(accountNo.toString(), HttpStatus.CREATED);
    }

    @PostMapping(value = "/account/deposit")
    public ResponseEntity<Object> deposit(@RequestBody AccountMovementRequest movementRequest) throws InvalidAccountNumberException, InvalidAmountException, RequiredFieldException {
        LOG.info(String.format("Account deposit Request Received %s", movementRequest.toString()));
        AccountNo accountNo = bankService.doDeposit(movementRequest);
        LOG.info(String.format("deposit is performed on account : %s", accountNo));
        return new ResponseEntity(accountNo.toString(), HttpStatus.OK);
    }

    @PostMapping(value = "/account/withdraw")
    public ResponseEntity<Object> withdraw(@RequestBody AccountMovementRequest movementRequest) throws InvalidAccountNumberException, InvalidAmountException, RequiredFieldException, InsufficientFoundsException {
        LOG.info(String.format("Account withdraw Request Received %s", movementRequest.toString()));
        AccountNo accountNo = bankService.doWithdraw(movementRequest);
        LOG.info(String.format("withdraw is performed on : %s", accountNo));
        return new ResponseEntity(accountNo.toString(), HttpStatus.OK);
    }

    @GetMapping(value = "/account/{accountId}/balance")
    @ResponseBody
    public ResponseEntity<Object> getBalance(@PathVariable String accountId) throws InvalidAccountNumberException {
        LOG.info(String.format("Get balance for %s", accountId));
        return ResponseEntity.ok().body(bankService.getAccount(accountId));
    }

    @GetMapping(value = "/account/{accountId}/statement")
    @ResponseBody
    public ResponseEntity<Object> getTransactions(@PathVariable String accountId,
                                                  @RequestParam(defaultValue = "10") int limit) throws InvalidAccountNumberException {
        LOG.info(String.format("Get transactions for %s", accountId));
        return ResponseEntity.ok().body(bankService.getTransactions(accountId, limit));
    }

}
