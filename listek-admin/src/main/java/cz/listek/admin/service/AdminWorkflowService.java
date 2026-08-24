package cz.listek.admin.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cz.listek.admin.api.AdminDtos.AccountResponse;
import cz.listek.admin.api.AdminDtos.ApplicationResponse;
import cz.listek.admin.api.AdminDtos.CreateOverdraftRequest;
import cz.listek.admin.api.AdminDtos.DashboardResponse;
import cz.listek.admin.api.AdminDtos.DecisionRequest;
import cz.listek.admin.api.AdminDtos.InterestSettingsResponse;
import cz.listek.admin.api.AdminDtos.UpdateInterestSettingsRequest;
import cz.listek.admin.domain.AdminAccount;
import cz.listek.admin.domain.AdminLoanApplication;
import cz.listek.admin.domain.AdminStandingOrder;
import cz.listek.admin.domain.AdminTransaction;
import cz.listek.admin.domain.ApplicationStatus;
import static cz.listek.admin.domain.ApplicationStatus.APPROVED;
import static cz.listek.admin.domain.ApplicationStatus.PENDING;
import static cz.listek.admin.domain.ApplicationStatus.REJECTED;
import cz.listek.admin.domain.OverdraftApplication;
import cz.listek.admin.domain.ProductInterestSettings;
import cz.listek.admin.repository.AdminAccountRepository;
import cz.listek.admin.repository.AdminLoanApplicationRepository;
import cz.listek.admin.repository.AdminStandingOrderRepository;
import cz.listek.admin.repository.AdminTransactionRepository;
import cz.listek.admin.repository.OverdraftApplicationRepository;
import cz.listek.admin.repository.ProductInterestSettingsRepository;

@Service
public class AdminWorkflowService {

    private final AdminAccountRepository accountRepository;
    private final AdminLoanApplicationRepository loanRepository;
    private final OverdraftApplicationRepository overdraftRepository;
    private final AdminTransactionRepository transactionRepository;
    private final AdminStandingOrderRepository standingOrderRepository;
    private final ProductInterestSettingsRepository interestSettingsRepository;
    private final String loanRepaymentAccountNumber;
    private final String loanVariableSymbol;
    private final String loanSpecificSymbol;
    private final int loanRepaymentDayOfMonth;

    public AdminWorkflowService(AdminAccountRepository accountRepository,
            AdminLoanApplicationRepository loanRepository,
            OverdraftApplicationRepository overdraftRepository,
            AdminTransactionRepository transactionRepository,
            AdminStandingOrderRepository standingOrderRepository,
            ProductInterestSettingsRepository interestSettingsRepository,
            @Value("${app.loan.repayment-account-number:LOAN-REPAYMENT}") String loanRepaymentAccountNumber,
            @Value("${app.loan.variable-symbol:0}") String loanVariableSymbol,
            @Value("${app.loan.specific-symbol:0}") String loanSpecificSymbol,
            @Value("${app.loan.repayment-day-of-month:15}") int loanRepaymentDayOfMonth) {
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
        this.overdraftRepository = overdraftRepository;
        this.transactionRepository = transactionRepository;
        this.standingOrderRepository = standingOrderRepository;
        this.interestSettingsRepository = interestSettingsRepository;
        this.loanRepaymentAccountNumber = loanRepaymentAccountNumber;
        this.loanVariableSymbol = loanVariableSymbol;
        this.loanSpecificSymbol = loanSpecificSymbol;
        this.loanRepaymentDayOfMonth = loanRepaymentDayOfMonth;
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        BigDecimal deposits = accountRepository.findAll().stream()
                .map(AdminAccount::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        Instant startOfToday = LocalDate.now(ZoneId.of("Europe/Prague")).atStartOfDay(ZoneId.of("Europe/Prague")).toInstant();
        long decidedToday = loanRepository.findAll().stream().filter(item -> item.getDecidedAt() != null && item.getDecidedAt().isAfter(startOfToday)).count()
                + overdraftRepository.findAll().stream().filter(item -> item.getDecidedAt() != null && item.getDecidedAt().isAfter(startOfToday)).count();
        return new DashboardResponse(accountRepository.count(), loanRepository.countByStatus(PENDING),
                overdraftRepository.countByStatus(PENDING), deposits, decidedToday);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> accounts() {
        return accountRepository.findAll().stream().map(account -> new AccountResponse(account.getId(), account.getOwnerName(),
                account.getEmail(), account.getAccountNumber(), account.getBalance(), account.getCurrency(), account.getType())).toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> loans() {
        return loanRepository.findAllByOrderByCreatedAtDesc().stream().map(this::loanResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> overdrafts() {
        return overdraftRepository.findAllByOrderByCreatedAtDesc().stream().map(this::overdraftResponse).toList();
    }

    @Transactional
    public ApplicationResponse decideLoan(UUID id, DecisionRequest request) {
        validateDecision(request.status());
        AdminLoanApplication application = loanRepository.findById(id).orElseThrow(() -> notFound("Žádost o půjčku"));
        if (application.getStatus() != PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O žádosti již bylo rozhodnuto");
        }
        if (request.status() == APPROVED) {
            AdminAccount account = accountRepository.findWithLockById(application.getAccount().getId())
                    .orElseThrow(() -> notFound("Účet"));
            account.credit(application.getAmount());
            transactionRepository.save(new AdminTransaction(account, application.getAmount(), "CREDIT", "Čerpání půjčky"));
            LocalDate dueDate = dueDate(application.getRepaymentMonths(), loanRepaymentDayOfMonth);
            application.configureRepayment(loanRepaymentAccountNumber, loanVariableSymbol, loanSpecificSymbol,
                    loanRepaymentDayOfMonth, dueDate);
            standingOrderRepository.save(new AdminStandingOrder(account, loanRepaymentAccountNumber,
                    application.getMonthlyPayment(), "Splátka půjčky", loanRepaymentDayOfMonth,
                    loanVariableSymbol, loanSpecificSymbol));
        }
        application.decide(request.status(), normalizeNote(request.note()));
        return loanResponse(application);
    }

    @Transactional
    public ApplicationResponse decideOverdraft(UUID id, DecisionRequest request) {
        validateDecision(request.status());
        OverdraftApplication application = overdraftRepository.findById(id).orElseThrow(() -> notFound("Žádost o kontokorent"));
        if (request.status() == APPROVED && application.getStatus() == PENDING) {
            AdminAccount account = accountRepository.findWithLockById(application.getAccount().getId())
                    .orElseThrow(() -> notFound("Účet"));
            account.credit(application.getRequestedLimit());
            transactionRepository.save(new AdminTransaction(account, application.getRequestedLimit(), "CREDIT", "Schválený kontokorent"));
        }
        application.decide(request.status(), normalizeNote(request.note()));
        return overdraftResponse(application);
    }

    @Transactional
    public ApplicationResponse createOverdraft(CreateOverdraftRequest request) {
        AdminAccount account = accountRepository.findById(request.accountId()).orElseThrow(() -> notFound("Účet"));
        if (overdraftRepository.existsByAccount_Id(request.accountId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Účet již má kontokorent");
        }
        return overdraftResponse(overdraftRepository.save(new OverdraftApplication(account,
                request.requestedLimit(), request.monthlyIncome(), settings().getOverdraftRate())));
    }

    @Transactional(readOnly = true)
    public InterestSettingsResponse interestSettings() {
        return settingsResponse(settings());
    }

    @Transactional
    public InterestSettingsResponse updateInterestSettings(UpdateInterestSettingsRequest request) {
        ProductInterestSettings settings = settings();
        settings.update(request.savingsRate(), request.overdraftRate(), request.personalLoanRate(), request.homeLoanRate());
        return settingsResponse(interestSettingsRepository.save(settings));
    }

    private ProductInterestSettings settings() {
        return interestSettingsRepository.findById(true).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nastavení sazeb nebylo nalezeno"));
    }

    private InterestSettingsResponse settingsResponse(ProductInterestSettings settings) {
        return new InterestSettingsResponse(settings.getSavingsRate(), settings.getOverdraftRate(), settings.getPersonalLoanRate(), settings.getHomeLoanRate());
    }

    private void validateDecision(ApplicationStatus status) {
        if (status != APPROVED && status != REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rozhodnutí musí být APPROVED nebo REJECTED");
        }
    }

    private String normalizeNote(String note) {
        return note == null || note.isBlank() ? null : note.trim();
    }

    private ResponseStatusException notFound(String subject) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, subject + " nebyla nalezena");
    }

    private LocalDate dueDate(int repaymentMonths, int repaymentDayOfMonth) {
        YearMonth month = YearMonth.now(ZoneId.of("Europe/Prague")).plusMonths(repaymentMonths);
        return month.atDay(Math.min(repaymentDayOfMonth, month.lengthOfMonth()));
    }

    private ApplicationResponse loanResponse(AdminLoanApplication item) {
        return new ApplicationResponse(item.getId(), "LOAN", item.getType(), item.getAccount().getId(),
                item.getAccount().getOwnerName(), item.getAccount().getAccountNumber(), item.getAmount(),
                item.getRepaymentMonths(), null, item.getMonthlyPayment(), item.getPurpose(), item.getStatus(),
                item.getCreatedAt(), item.getDecidedAt(), item.getDecisionNote(), item.getRepaymentAccountNumber(),
                item.getVariableSymbol(), item.getSpecificSymbol(), item.getRepaymentDayOfMonth(), item.getRepaidAmount(),
                item.getRemainingInstallments(), item.getDueDate(), item.getAnnualRate());
    }

    private ApplicationResponse overdraftResponse(OverdraftApplication item) {
        return new ApplicationResponse(item.getId(), "OVERDRAFT", "Kontokorent", item.getAccount().getId(),
                item.getAccount().getOwnerName(), item.getAccount().getAccountNumber(), item.getRequestedLimit(),
                null, item.getMonthlyIncome(), null, "Provozní rezerva", item.getStatus(), item.getCreatedAt(),
                item.getDecidedAt(), item.getDecisionNote(), null, null, null, null, null, null, null, item.getAnnualRate());
    }
}
