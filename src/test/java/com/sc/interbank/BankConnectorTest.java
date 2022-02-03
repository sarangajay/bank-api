package com.sc.interbank;

import com.sc.interbank.dto.FundTransferRequest;
import com.sc.interbank.error.ResourceNotFoundException;
import com.sc.interbank.repository.Account;
import com.sc.interbank.repository.AccountTransaction;
import com.sc.interbank.service.BankService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyInt;

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
    public void get_Account_Details_By_Id_Successfully() throws Exception {
        when(bankService.getAccount(anyInt()))
                .thenReturn(new Account(113, 150.00, "SJA", "DKK"));

        this.mockMvc.perform(get("/accounts/113/balance")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("SJA"))
                .andExpect(jsonPath("$.currency").value("DKK"))
                .andExpect(jsonPath("$.amount").value(150));
    }

    @Test
    public void get_Account_Details_By_Id_Account_Not_Found() throws Exception {
        when(bankService.getAccount(anyInt()))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        this.mockMvc.perform(get("/accounts/113/balance")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    public void get_Transactions_For_Invalid_Account_Number() throws Exception {
        when(bankService.getTransactions(anyInt(), anyInt()))
                .thenThrow(new ResourceNotFoundException("Invalid account number"));

        this.mockMvc.perform(get("/accounts/555/statements/mini")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    public void get_Transactions_For_Account_Number_Successfully() throws Exception {
        List<AccountTransaction> expectedResponse = objectMapper.readValue(
                IOUtils.toString(resourceFile.getInputStream(), StandardCharsets.UTF_8),
                new TypeReference<List<AccountTransaction>>() {
                });

        when(bankService.getTransactions(anyInt(), anyInt()))
                .thenReturn(expectedResponse);

        this.mockMvc.perform(get("/accounts/111/statements/mini")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andDo(print())
                .andExpect(jsonPath("$.[0].accountId").value(111))
                .andExpect(jsonPath("$.[0].currency").value("DKK"))
                .andExpect(jsonPath("$.[0].amount").value(40.0))
                .andExpect(jsonPath("$.[0].type").value("DEBIT"))
                .andExpect(jsonPath("$.[0].transactionDate").value("2022-02-01T02:03:52.4234971"));
    }

    @Test()
    public void do_Transfer_Successfully() throws Exception {
        FundTransferRequest fundTransferRequest = FundTransferRequest.builder()
                .amount(100.00)
                .fromAccountNumber(113)
                .toAccountNumber(114)
                .build();

        this.mockMvc.perform( MockMvcRequestBuilders
                .post("/accounts/transfer")
                .content(asJsonString(fundTransferRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
