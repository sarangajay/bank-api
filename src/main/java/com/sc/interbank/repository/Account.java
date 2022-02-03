package com.sc.interbank.repository;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Account {
    private int accountNumber;
    private double amount;
    private String name;
    private String currency;

}