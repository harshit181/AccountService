package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;

import com.db.awmd.challenge.service.NotificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Before
  public void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  public void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  @Test
  public void transferMoneyFromValidAccountToValidAccount() throws Exception{
    String uniqueFromAccountId = "Idx3-" + System.currentTimeMillis();
    Account account1 = new Account(uniqueFromAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account1);
    String uniqueToAccountId = "Idx4-" + System.currentTimeMillis();
    Account account2 = new Account(uniqueToAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account2);

    this.mockMvc.perform(patch("/v1/accounts/transfer/from/"+uniqueFromAccountId+"/to/"+uniqueToAccountId+"/20"))
    .andExpect(status().isOk())
    .andExpect(
            content().string("{\"accountId\":\"" + uniqueFromAccountId + "\",\"balance\":103.45}")
    );

  }
  @Test
  public void transferMoneyFromInvalidAccount() throws Exception{
    String uniqueToAccountId = "Idx2-" + System.currentTimeMillis();
    Account account2 = new Account(uniqueToAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account2);

    this.mockMvc.perform(patch("/v1/accounts/transfer/from/"+"invalidAccount"+"/to/"+uniqueToAccountId+"/20"))
            .andExpect(status().isBadRequest())
            .andExpect(
                    content().string("Invalid Account ID")
            );

  }
@Test
  public void tranferMoneyToInvalidAccount() throws Exception{
    String uniqueToAccountId = "Idx1-" + System.currentTimeMillis();
    Account account2 = new Account(uniqueToAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account2);

    this.mockMvc.perform(patch("/v1/accounts/transfer/from/"+uniqueToAccountId+"/to/"+"invalidAccountId"+"/20"))
            .andExpect(status().isBadRequest())
            .andExpect(
                    content().string("Invalid Account ID")
            );

  }

  @Test
  public void transferMoneyWithBalanceThanTransferAmount() throws Exception{
    String uniqueFromAccountId = "Idz-" + System.currentTimeMillis();
    Account account1 = new Account(uniqueFromAccountId, new BigDecimal("19"));
    this.accountsService.createAccount(account1);
    String uniqueToAccountId = "Idz2-" + System.currentTimeMillis();
    Account account2 = new Account(uniqueToAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account2);

    this.mockMvc.perform(patch("/v1/accounts/transfer/from/"+uniqueFromAccountId+"/to/"+uniqueToAccountId+"/20"))
            .andExpect(status().isForbidden())
            .andExpect(
                    content().string("Amount available is less the requested")
            );

  }

  @Test
  public void transferMoneyFromSameAccount() throws  Exception{
    String uniqueFromAccountId = "Idx6-" + System.currentTimeMillis();
    Account account1 = new Account(uniqueFromAccountId, new BigDecimal("100"));
    this.accountsService.createAccount(account1);

    this.mockMvc.perform(patch("/v1/accounts/transfer/from/"+uniqueFromAccountId+"/to/"+uniqueFromAccountId+"/20"))
            .andExpect(status().isBadRequest())
            .andExpect(
                    content().string("FromAccount and ToAccount are same")
            );

  }
}
