package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.BalanceNotAvailableException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

  }

  @Test
  public void transferMoney() throws Exception{
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    String uniqueId2 = "Id-" + System.currentTimeMillis();
    Account account2 = new Account(uniqueId2);
    account2.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account2);

    assertThat(this.accountsService.transferMoney(uniqueId,uniqueId2,new BigDecimal(20))).isEqualTo(account);

  }

  @Test
  public void transferMoney_invalidFromAccount() throws Exception{
    String uniqueId = "Idx-" + System.currentTimeMillis();

    String uniqueId2 = "Id-" + System.currentTimeMillis();
    Account account2 = new Account(uniqueId2);
    account2.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account2);
  try {
    this.accountsService.transferMoney(uniqueId, uniqueId2, new BigDecimal(20));
    fail("Should have failed when transferring from invalid account");
  }
  catch (InvalidAccountException e)
  {
    assertThat(e.getMessage()).isEqualTo("Invalid Account ID");
  }
  }

  @Test
  public void transferMoney_invalidToAccount() throws Exception{
    String uniqueId = "Idx-" + System.currentTimeMillis();

    String uniqueId2 = "Id-" + System.currentTimeMillis();
    Account account2 = new Account(uniqueId2);
    account2.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account2);
    try {
      this.accountsService.transferMoney(uniqueId2, uniqueId, new BigDecimal(20));
      fail("Should have failed when transferring to invalid account");
    }
    catch (InvalidAccountException e)
    {
      assertThat(e.getMessage()).isEqualTo("Invalid Account ID");
    }
  }

  @Test
  public void transferMoney_balanceLessThanAvailable() throws Exception{
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    account.setBalance(new BigDecimal(10));
    this.accountsService.createAccount(account);

    String uniqueId2 = "Id-" + System.currentTimeMillis();
    Account account2 = new Account(uniqueId2);
    account2.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account2);

    try {
      this.accountsService.transferMoney(uniqueId, uniqueId2, new BigDecimal(20));
      fail("Should have failed when transferring from account where balance is less than requested");
    }
    catch (BalanceNotAvailableException e)
    {
      assertThat(e.getMessage()).isEqualTo("Amount available is less the requested");
    }

  }

}
