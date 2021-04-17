package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.BalanceNotAvailableException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public Account transferMoney(String fromAccountId, String toAccountId, BigDecimal amount)
  {
    if(this.accountsRepository.isAmountAvailable(fromAccountId,amount)){
      withdrawAmount(fromAccountId,amount);
      depositAmount(toAccountId,amount);
      return getAccount(fromAccountId);
    }
    else
    {
      throw new BalanceNotAvailableException("Amount available is less the requested");
    }

  }

  public void withdrawAmount(String fromAccountId, BigDecimal amount)
  {
    this.accountsRepository.withdrawAmount(fromAccountId,amount);
  }

  public void depositAmount(String toAccountId, BigDecimal amount)
  {
    this.accountsRepository.depositAmount(toAccountId,amount);
  }
}
