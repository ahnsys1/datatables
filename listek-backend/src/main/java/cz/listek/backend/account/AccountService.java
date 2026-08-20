package cz.listek.backend.account;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cz.listek.backend.account.AccountDtos.AccountResponse;
import cz.listek.backend.account.AccountDtos.CreateAccountRequest;
import cz.listek.backend.transaction.Transaction;
import cz.listek.backend.transaction.TransactionDtos.TransactionResponse;
import cz.listek.backend.transaction.TransactionRepository;
import cz.listek.backend.transaction.TransactionType;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        return accountRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        var initialBalance = request.initialBalance() == null ? BigDecimal.ZERO : request.initialBalance();
        if (initialBalance.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pocatecni zustatek nesmi byt zaporny");
        }
        var currency = request.currency() == null ? CurrencyCode.CZK : request.currency();
        return toResponse(accountRepository.save(new Account(request.ownerName(), request.accountNumber(), initialBalance, currency)));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> transactions(UUID accountId) {
        requireAccount(accountId);
        return transactionRepository.findTop20ByAccountIdOrderByCreatedAtDesc(accountId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void transfer(UUID fromId, UUID toId, BigDecimal amount, String description) {
        if (fromId.equals(toId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zdrojovy a cilovy ucet musi byt rozdilne");
        }
        var source = requireAccount(fromId);
        var target = requireAccount(toId);
        if (source.getCurrency() != target.getCurrency()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prevod mezi ruznymi menami neni podporovan");
        }
        if (source.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Na uctu neni dostatecny zustatek");
        }
        source.debit(amount);
        target.credit(amount);
        transactionRepository.save(new Transaction(source, amount.negate(), TransactionType.DEBIT, description));
        transactionRepository.save(new Transaction(target, amount, TransactionType.CREDIT, description));
    }

    private Account requireAccount(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ucet nebyl nalezen"));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getOwnerName(), account.getAccountNumber(), account.getBalance(), account.getCurrency());
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getAccount().getId(), transaction.getAmount(), transaction.getType(), transaction.getDescription(), transaction.getCreatedAt());
    }
}
