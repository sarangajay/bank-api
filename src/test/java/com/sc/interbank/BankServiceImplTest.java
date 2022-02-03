package com.sc.interbank;


import com.sc.interbank.dto.FundTransferRequest;
import com.sc.interbank.error.ResourceNotFoundException;
import com.sc.interbank.repository.Account;
import com.sc.interbank.repository.AccountTransaction;
import com.sc.interbank.repository.BankRepository;
import com.sc.interbank.service.BankService;

import io.micrometer.core.instrument.util.IOUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class BankServiceImplTest {

    @MockBean
    private BankRepository bankRepository;

    @Autowired
    private BankService bankService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("classpath:transaction-results-mock.json")
    private Resource resourceFile;

    @Captor
    private ArgumentCaptor<Integer> accountIdCaptor;

    @Captor
    private ArgumentCaptor<Double> amountCaptor;

    @Test
    public void get_Account_Details_By_Account_Number_Successfully() {
        when(bankRepository.getAccount(anyInt()))
                .thenReturn(new Account(113, 150.00, "SJA", "DKK"));

        Account account = bankService.getAccount(113);

        assertThat(account.getAccountNumber()).isEqualTo(113);
        assertThat(account.getCurrency()).isEqualTo("DKK");
        assertThat(account.getName()).isEqualTo("SJA");
        assertThat(account.getAmount()).isEqualTo(150.00);
    }

    @Test()
    public void get_Account_Details_By_Account_Number_Account_Not_Found() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bankService.getAccount(23));

        Assertions.assertEquals("Account not found for 23", exception.getMessage());
    }

    @Test
    public void get_Transaction_Records_For_Account_Number_Successfully() throws Exception {
        List<AccountTransaction> expectedResponse = objectMapper.readValue(
                IOUtils.toString(resourceFile.getInputStream(), StandardCharsets.UTF_8),
                new TypeReference<List<AccountTransaction>>() {
                });

        when(bankRepository.getTransactions(anyInt(), anyInt()))
                .thenReturn(expectedResponse);

        Assertions.assertEquals(bankService.getTransactions(123, 4).size(), expectedResponse.size());
    }

    @Test()
    public void get_Transaction_NO_Records_For_Account_Number() {
        when(bankRepository.getAccount(anyInt()))
                .thenReturn(new Account(5, 150.00, "SJA", "DKK"));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bankService.getTransactions(5, 2));

        Assertions.assertEquals("Invalid account number 5", exception.getMessage());
    }

    @Test()
    public void get_Transaction_Records_For_Invalid_Account_Number() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bankService.getTransactions(1, 2));

        Assertions.assertEquals("Invalid account number 1", exception.getMessage());
    }

    @Test()
    public void do_Transfer_Invalid_From_Account() {
        FundTransferRequest fundTransferRequest = FundTransferRequest.builder()
                .amount(12.00)
                .toAccountNumber(1)
                .build();
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bankService.doTransfer(fundTransferRequest));

        Assertions.assertEquals("Invalid [FROM] account number", exception.getMessage());
    }

    @Test()
    public void do_Transfer_Invalid_To_Account() {
        when(bankRepository.getAccount(113))
                .thenReturn(new Account(113, 150.00, "SJA", "DKK"));

        FundTransferRequest fundTransferRequest = FundTransferRequest.builder()
                .amount(12.00)
                .fromAccountNumber(113)
                .build();

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bankService.doTransfer(fundTransferRequest));

        Assertions.assertEquals("Invalid [TO] account number", exception.getMessage());
    }

    @Test()
    public void do_Transfer_Insufficient_Funds() {
        when(bankRepository.getAccount(113))
                .thenReturn(new Account(113, 150.00, "SJA", "DKK"));

        when(bankRepository.getAccount(114))
                .thenReturn(new Account(114, 150.00, "SJA", "DKK"));

        FundTransferRequest fundTransferRequest = FundTransferRequest.builder()
                .amount(1000.00)
                .fromAccountNumber(113)
                .toAccountNumber(114)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bankService.doTransfer(fundTransferRequest));

        Assertions.assertEquals("Insufficient funds available", exception.getMessage());

    }

    @Test()
    public void do_Transfer_Successfully() {
        when(bankRepository.getAccount(113))
                .thenReturn(new Account(113, 150.00, "SJA", "DKK"));

        when(bankRepository.getAccount(114))
                .thenReturn(new Account(114, 150.00, "ABC", "DKK"));

        FundTransferRequest fundTransferRequest = FundTransferRequest.builder()
                .amount(100.00)
                .fromAccountNumber(113)
                .toAccountNumber(114)
                .build();

        bankService.doTransfer(fundTransferRequest);

        verify(bankRepository, times(2)).setAccountBalance(accountIdCaptor.capture(), amountCaptor.capture());

        assertThat(accountIdCaptor.getAllValues().get(0)).isEqualTo(113);
        assertThat(amountCaptor.getAllValues().get(0)).isEqualTo(50);

        assertThat(accountIdCaptor.getAllValues().get(1)).isEqualTo(114);
        assertThat(amountCaptor.getAllValues().get(1)).isEqualTo(250);
    }

}
