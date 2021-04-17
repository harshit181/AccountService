package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.BalanceNotAvailableException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.service.AccountsService;
import javax.validation.Valid;

import com.db.awmd.challenge.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  private final NotificationService notificationService;

  @Autowired
  public AccountsController(AccountsService accountsService,NotificationService notificationService) {
    this.accountsService = accountsService;
    this.notificationService=notificationService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

  @PatchMapping(path="/transfer/from/{fromAccountId}/to/{toAccountId}/{amount}")
  public ResponseEntity transferMoney(@PathVariable String fromAccountId,@PathVariable String toAccountId,@PathVariable String amount)
  {
    log.info("Transferring amount {} from {} to {} ",amount,fromAccountId,toAccountId);
    if(fromAccountId.equals(toAccountId))
    {
      return new ResponseEntity<>("FromAccount and ToAccount are same",HttpStatus.BAD_REQUEST);
    }
    BigDecimal transferAmount=new BigDecimal(amount);
    try {
     Account result= this.accountsService.transferMoney(fromAccountId, toAccountId, transferAmount);
      this.notificationService.notifyAboutTransfer(this.accountsService.getAccount(fromAccountId),"transferred "+amount+" from your account to account "+toAccountId);
      this.notificationService.notifyAboutTransfer(this.accountsService.getAccount(toAccountId),"transferred "+amount+" from to your account from account "+fromAccountId);
      return ResponseEntity.ok(result);
    }
    catch(BalanceNotAvailableException e)
    {
      return new ResponseEntity<>(e.getMessage(),HttpStatus.FORBIDDEN);
    }
    catch(InvalidAccountException e)
    {
      return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
    }
  }

}
