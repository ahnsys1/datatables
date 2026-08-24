package cz.listek.backend.loan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cz.listek.backend.account.Account;
import cz.listek.backend.account.AccountRepository;
import cz.listek.backend.loan.LoanDtos.CreateLoanApplicationRequest;
import cz.listek.backend.loan.LoanDtos.LoanApplicationResponse;
import cz.listek.backend.settings.ProductInterestSettings;
import cz.listek.backend.settings.ProductInterestSettingsRepository;
import cz.listek.backend.transaction.TransactionRepository;

@Service
public class LoanApplicationService {

    private static final BigDecimal PERSONAL_RATE = new BigDecimal("6.9000");
    private static final BigDecimal HOME_RATE = new BigDecimal("5.4000");
    private static final BigDecimal PERSONAL_MAX = new BigDecimal("800000.00");
    private static final BigDecimal HOME_MAX = new BigDecimal("1500000.00");

    private final AccountRepository accountRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final ProductInterestSettingsRepository interestSettingsRepository;
    private final TransactionRepository transactionRepository;

    public LoanApplicationService(AccountRepository accountRepository, LoanApplicationRepository loanApplicationRepository,
            ProductInterestSettingsRepository interestSettingsRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.interestSettingsRepository = interestSettingsRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> findByAccount(UUID accountId) {
        requireAccount(accountId);
        return loanApplicationRepository.findByAccount_IdOrderByCreatedAtDesc(accountId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public LoanApplicationResponse create(UUID accountId, CreateLoanApplicationRequest request) {
        Account account = requireAccount(accountId);
        BigDecimal maximum = request.type() == LoanType.PERSONAL ? PERSONAL_MAX : HOME_MAX;
        if (request.amount().compareTo(maximum) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Castka pujcky presahuje povoleny limit");
        }
        ProductInterestSettings settings = interestSettingsRepository.findById(true).orElse(null);
        BigDecimal annualRate = settings == null
                ? request.type() == LoanType.PERSONAL ? PERSONAL_RATE : HOME_RATE
                : request.type() == LoanType.PERSONAL ? settings.getPersonalLoanRate() : settings.getHomeLoanRate();
        BigDecimal monthlyPayment = monthlyPayment(request.amount(), request.repaymentMonths(), annualRate);
        LoanApplication application = new LoanApplication(account, request.type(), request.amount(), request.repaymentMonths(),
                annualRate, monthlyPayment, request.purpose().trim());
        return toResponse(loanApplicationRepository.save(application));
    }

    private BigDecimal monthlyPayment(BigDecimal amount, int months, BigDecimal annualRate) {
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), 12, RoundingMode.HALF_UP);
        double payment = amount.doubleValue() * monthlyRate.doubleValue()
                / (1 - Math.pow(1 + monthlyRate.doubleValue(), -months));
        return BigDecimal.valueOf(payment).setScale(2, RoundingMode.HALF_UP);
    }

    private Account requireAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ucet nebyl nalezen"));
    }

    private LoanApplicationResponse toResponse(LoanApplication application) {
        return new LoanApplicationResponse(application.getId(), application.getType(), application.getAmount(),
                application.getRepaymentMonths(), application.getAnnualRate(), application.getMonthlyPayment(),
                application.getPurpose(), application.getStatus(), application.getCreatedAt(),
                application.getRepaymentAccountNumber(), application.getVariableSymbol(), application.getSpecificSymbol(),
                application.getRepaymentDayOfMonth(), application.getRepaidAmount(), application.getRemainingAmount(),
                application.getRemainingInstallments(),
                application.getDueDate());
    }
}
