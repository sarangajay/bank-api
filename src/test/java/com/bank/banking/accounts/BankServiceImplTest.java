package com.bank.banking.accounts;


import com.bank.banking.accounts.domain.AccountMovementType;
import com.bank.banking.accounts.domain.AccountNo;
import com.bank.banking.accounts.domain.AccountType;
import com.bank.banking.accounts.dto.AccountMovementRequest;
import com.bank.banking.accounts.dto.CreateAccountRequest;
import com.bank.banking.accounts.dto.FundTransferRequest;
import com.bank.banking.accounts.error.*;
import com.bank.banking.accounts.domain.Account;
import com.bank.banking.accounts.repository.AccountTransaction;
import com.bank.banking.accounts.repository.BankRepository;
import com.bank.banking.accounts.service.BankService;

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
import java.util.UUID;

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
    private ArgumentCaptor<String> accountIdCaptor;

    @Captor
    private ArgumentCaptor<Double> amountCaptor;

    @Test
    public void get_Account_Details_By_Account_Number_Successfully() throws InvalidAccountNumberException, InvalidAmountException {
        String accountId = UUID.randomUUID().toString();
        Account account = new Account("SJA", AccountType.SAVINGS);
        when(bankRepository.getAccount(new AccountNo(accountId)))
                .thenReturn(account);

        Account accountF = bankService.getAccount(accountId);
        assertThat(accountF.getName()).isEqualTo("SJA");
        assertThat(account.getBalance().getAmount()).isEqualTo(0.00);
    }

   @Test()
   public void get_Account_Details_By_Account_Number_Account_Not_Found() {
       String accountId = UUID.randomUUID().toString();
       ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bankService.getAccount(accountId));

        Assertions.assertEquals("Account not found for " + accountId, exception.getMessage());
    }

    @Test
    public void create_New_Account_Successfully() throws InvalidAccountNumberException, InvalidAmountException, RequiredFieldException {
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name("NewAccount")
                .accountType(AccountType.SAVINGS.name())
                .balance(100.00)
                .build();
        AccountNo accountNo = new AccountNo(UUID.randomUUID().toString());
        when(bankService.createAccount(createAccountRequest))
                .thenReturn(accountNo);

        AccountNo accountNoRet = bankService.createAccount(createAccountRequest);
        assertThat(accountNoRet).isEqualTo(accountNo);
    }

    @Test
    public void create_New_Account_Required_Field_Exception() {
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .accountType(AccountType.SAVINGS.name())
                .balance(100.00)
                .build();

        RequiredFieldException exception = assertThrows(
                RequiredFieldException.class,
                () -> bankService.createAccount(createAccountRequest));

        Assertions.assertEquals("Name is required for creation of a new account", exception.getMessage());
    }


    @Test
    public void create_Deposit_Successfully() throws InvalidAccountNumberException, InvalidAmountException, RequiredFieldException {
        String accountId = UUID.randomUUID().toString();
        AccountNo accountNo = new AccountNo(accountId);

        Double amount = 500.00;
        when(bankRepository.doDeposit(accountId, amount))
                .thenReturn(accountNo);

        AccountMovementRequest accountMovementRequest = AccountMovementRequest.builder()
                .accountId(accountId)
                .accountMovementType(AccountMovementType.DEPOSIT.name())
                .amount(amount)
                .build();

        AccountNo accountNoRet = bankService.doDeposit(accountMovementRequest);

        verify(bankRepository, times(1)).doDeposit(accountIdCaptor.capture(), amountCaptor.capture());

        assertThat(new AccountNo(accountIdCaptor.getAllValues().get(0))).isEqualTo(accountNoRet);
    }


    @Test
    public void create_Deposit_Required_Field_Exception() throws InvalidAccountNumberException, InvalidAmountException, RequiredFieldException {
        String accountId = UUID.randomUUID().toString();
        AccountNo accountNo = new AccountNo(accountId);

        Double amount = 500.00;
        when(bankRepository.doDeposit(accountId, amount))
                .thenReturn(accountNo);

        AccountMovementRequest accountMovementRequest = AccountMovementRequest.builder()
                .accountId(accountId)
                .amount(amount)
                .build();

        RequiredFieldException exception = assertThrows(
                RequiredFieldException.class,
                () -> bankService.doDeposit(accountMovementRequest));

        Assertions.assertEquals("Account movement type is missing", exception.getMessage());
    }

    @Test
    public void create_Withdraw_Successfully() throws InvalidAccountNumberException, InvalidAmountException, RequiredFieldException, InsufficientFoundsException {
        String accountId = UUID.randomUUID().toString();
        AccountNo accountNo = new AccountNo(accountId);

        Double amount = 500.00;
        when(bankRepository.doWithdraw(accountId, amount))
                .thenReturn(accountNo);

        AccountMovementRequest accountMovementRequest = AccountMovementRequest.builder()
                .accountId(accountId)
                .accountMovementType(AccountMovementType.WITHDRAW.name())
                .amount(amount)
                .build();

        AccountNo accountNoRet = bankService.doWithdraw(accountMovementRequest);

        verify(bankRepository, times(1)).doWithdraw(accountIdCaptor.capture(), amountCaptor.capture());

        assertThat(new AccountNo(accountIdCaptor.getAllValues().get(0))).isEqualTo(accountNoRet);
    }

    @Test
    public void create_Withdraw_Required_Field_Exception() throws InvalidAccountNumberException, InvalidAmountException, RequiredFieldException, InsufficientFoundsException {
        String accountId = UUID.randomUUID().toString();
        AccountNo accountNo = new AccountNo(accountId);

        Double amount = 500.00;
        when(bankRepository.doWithdraw(accountId, amount))
                .thenReturn(accountNo);

        AccountMovementRequest accountMovementRequest = AccountMovementRequest.builder()
                .accountId(accountId)
                .amount(amount)
                .build();

        RequiredFieldException exception = assertThrows(
                RequiredFieldException.class,
                () -> bankService.doWithdraw(accountMovementRequest));

        Assertions.assertEquals("Account movement type is missing", exception.getMessage());
    }

    @Test
    public void get_Transaction_Records_For_Account_Number_Successfully() throws Exception {
        List<AccountTransaction> expectedResponse = objectMapper.readValue(
                IOUtils.toString(resourceFile.getInputStream(), StandardCharsets.UTF_8),
                new TypeReference<List<AccountTransaction>>() {
                });

        String accountId = "48bb49c8-d979-477b-9a64-d55bd5c16c8d";
        when(bankRepository.getTransactions(anyObject(), anyInt()))
                .thenReturn(expectedResponse);

        Assertions.assertEquals(bankService.getTransactions(accountId, 4).size(), expectedResponse.size());
    }

   /*  @Test()
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
*/
}
