package com.sc.interbank.connector;

import com.sc.interbank.dto.FundTransferRequest;
import com.sc.interbank.service.BankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class BankConnector {

    private static final Logger LOG = LoggerFactory.getLogger(BankConnector.class);

    @Autowired
    private BankService bankService;

    @PostMapping(value = "/transfer")
    public ResponseEntity<Object> doTransfer(@RequestBody FundTransferRequest fundTransferRequest) {
        LOG.info(String.format("Transfer Request Received %s", fundTransferRequest.toString()));
        bankService.doTransfer(fundTransferRequest);
        return new ResponseEntity<>("Transaction completed", HttpStatus.OK);
    }

    @GetMapping(value = "/{accountId}/balance")
    @ResponseBody
    public ResponseEntity<Object> getBalance(@PathVariable Integer accountId) {
        LOG.info(String.format("Get balance for %d", accountId));
        return ResponseEntity.ok().body(bankService.getAccount(accountId));
    }

    @GetMapping(value = "/{accountId}/statements/mini")
    @ResponseBody
    public ResponseEntity<Object> getTransactions(@PathVariable Integer accountId,
                                                  @RequestParam(defaultValue = "5") int limit) {
        LOG.info(String.format("Get transactions for %d", accountId));
        return ResponseEntity.ok().body(bankService.getTransactions(accountId, limit));
    }

}
