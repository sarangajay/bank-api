package com.bank.banking.accounts;

import com.bank.banking.accounts.domain.AccountMovementType;
import com.bank.banking.accounts.domain.AccountNo;
import com.bank.banking.accounts.domain.AccountType;
import com.bank.banking.accounts.dto.AccountMovementRequest;
import com.bank.banking.accounts.error.ResourceNotFoundException;
import com.bank.banking.accounts.domain.Account;
import com.bank.banking.accounts.repository.AccountTransaction;
import com.bank.banking.accounts.service.BankService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BankConnectorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankService bankService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("classpath:transaction-results-mock.json")
    private Resource resourceFile;

    @Test
    public void get_Account_Balance_Successfully() throws Exception {
        Account account = new Account("SJA", AccountType.SAVINGS);
        when(bankService.getAccount(anyString()))
                .thenReturn(account);

        this.mockMvc.perform(get("/accounts/account/48bb49c8-d979-477b-9a64-d55bd5c16c8d/balance")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("SJA"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance.amount").value(0.00));
    }

    @Test
    public void get_Account_Balance_Account_Not_Found() throws Exception {
        String accountId = UUID.randomUUID().toString();
        when(bankService.getAccount(accountId))
                .thenThrow(new ResourceNotFoundException(String.format("Account not found for %s", accountId)));

        this.mockMvc.perform(get("/accounts/account/48bb49c8-d979-477b-9a64-d55bd5c16c8d/balance/balance")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    public void get_Transactions_For_Account_Number_Successfully() throws Exception {
        List<AccountTransaction> expectedResponse = objectMapper.readValue(
                IOUtils.toString(resourceFile.getInputStream(), StandardCharsets.UTF_8),
                new TypeReference<List<AccountTransaction>>() {
                });

        when(bankService.getTransactions(anyString(), anyInt()))
                .thenReturn(expectedResponse);

        this.mockMvc.perform(get("/accounts/account/111/statement")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andDo(print())
                .andExpect(jsonPath("$.[0].accountId").value("48bb49c8-d979-477b-9a64-d55bd5c16c8d"))
                .andExpect(jsonPath("$.[0].amount.amount").value(40.0))
                .andExpect(jsonPath("$.[0].type").value("DEBIT"))
                .andExpect(jsonPath("$.[0].transactionDate").value("2023-01-24T14:40:28.671501"));
    }

    @Test()
    public void do_Deposite_Successfully() throws Exception {
        String accountId = UUID.randomUUID().toString();
        Double amount = 500.00;
        AccountNo accountNo = new AccountNo(accountId);

        AccountMovementRequest accountMovementRequest = AccountMovementRequest.builder()
                .accountId(accountId)
                .accountMovementType(AccountMovementType.DEPOSIT.name())
                .amount(amount)
                .build();


        when(bankService.doDeposit(accountMovementRequest))
                .thenReturn(accountNo);


        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/accounts/account/deposit")
                .content(asJsonString(accountMovementRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test()
    public void do_Withdraw_Successfully() throws Exception {
        String accountId = UUID.randomUUID().toString();
        Double amount = 500.00;
        AccountNo accountNo = new AccountNo(accountId);

        AccountMovementRequest accountMovementRequest = AccountMovementRequest.builder()
                .accountId(accountId)
                .accountMovementType(AccountMovementType.WITHDRAW.name())
                .amount(amount)
                .build();

        when(bankService.doWithdraw(accountMovementRequest))
                .thenReturn(accountNo);


        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/accounts/account/withdraw")
                .content(asJsonString(accountMovementRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
