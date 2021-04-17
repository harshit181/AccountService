package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.db.awmd.challenge.exception.InvalidAccountException;
import org.springframework.stereotype.Repository;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

  private final Map<String, Account> accounts = new ConcurrentHashMap<>();

  @Override
  public void createAccount(Account account) throws DuplicateAccountIdException {
    Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
    if (previousAccount != null) {
      throw new DuplicateAccountIdException(
        "Account id " + account.getAccountId() + " already exists!");
    }
  }

  @Override
  public Account getAccount(String accountId) {
    return accounts.get(accountId);
  }

  @Override
  public void clearAccounts() {
    accounts.clear();
  }

  @Override
  public boolean isAmountAvailable(String accountId, BigDecimal amount) {
    Account account=getAccount(accountId);

    if(account==null)
    {
      throw new InvalidAccountException("Invalid Account ID");
    }
    return account.getBalance().compareTo(amount)>=0;
  }

  @Override
  public boolean withdrawAmount(String accountId, BigDecimal amount) {
    Account account=getAccount(accountId);

    if(account==null)
    {
      throw new InvalidAccountException("Invalid Account ID");
    }
    if(isAmountAvailable(accountId,amount))
    {
      account.setBalance(account.getBalance().subtract(amount));
      return true;
    }
    return false;
  }

  @Override
  public boolean depositAmount(String accountId, BigDecimal amount) {
    Account account=getAccount(accountId);

    if(account==null)
    {
      throw new InvalidAccountException("Invalid Account ID");
    }

      account.setBalance(account.getBalance().add(amount));
      return true;
  }

}
