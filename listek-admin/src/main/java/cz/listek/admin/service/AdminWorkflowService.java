package cz.listek.admin.service;

import static cz.listek.admin.domain.ApplicationStatus.APPROVED;
import static cz.listek.admin.domain.ApplicationStatus.PENDING;
import static cz.listek.admin.domain.ApplicationStatus.REJECTED;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cz.listek.admin.api.AdminDtos.AccountResponse;
import cz.listek.admin.api.AdminDtos.ApplicationResponse;
import cz.listek.admin.api.AdminDtos.CreateOverdraftRequest;
import cz.listek.admin.api.AdminDtos.DashboardResponse;
import cz.listek.admin.api.AdminDtos.DecisionRequest;
import cz.listek.admin.domain.AdminAccount;
import cz.listek.admin.domain.AdminLoanApplication;
import cz.listek.admin.domain.ApplicationStatus;
import cz.listek.admin.domain.OverdraftApplication;
import cz.listek.admin.repository.AdminAccountRepository;
import cz.listek.admin.repository.AdminLoanApplicationRepository;
import cz.listek.admin.repository.OverdraftApplicationRepository;

@Service
public class AdminWorkflowService {

    private final AdminAccountRepository accountRepository;
    private final AdminLoanApplicationRepository loanRepository;
    private final OverdraftApplicationRepository overdraftRepository;

    public AdminWorkflowService(AdminAccountRepository accountRepository,
            AdminLoanApplicationRepository loanRepository,
            OverdraftApplicationRepository overdraftRepository) {
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
        this.overdraftRepository = overdraftRepository;
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
                account.getEmail(), account.getAccountNumber(), account.getBalance(), account.getCurrency())).toList();
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
        application.decide(request.status(), normalizeNote(request.note()));
        return loanResponse(application);
    }

    @Transactional
    public ApplicationResponse decideOverdraft(UUID id, DecisionRequest request) {
        validateDecision(request.status());
        OverdraftApplication application = overdraftRepository.findById(id).orElseThrow(() -> notFound("Žádost o kontokorent"));
        application.decide(request.status(), normalizeNote(request.note()));
        return overdraftResponse(application);
    }

    @Transactional
    public ApplicationResponse createOverdraft(CreateOverdraftRequest request) {
        AdminAccount account = accountRepository.findById(request.accountId()).orElseThrow(() -> notFound("Účet"));
        return overdraftResponse(overdraftRepository.save(new OverdraftApplication(account,
                request.requestedLimit(), request.monthlyIncome())));
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

    private ApplicationResponse loanResponse(AdminLoanApplication item) {
        return new ApplicationResponse(item.getId(), "LOAN", item.getType(), item.getAccount().getId(),
                item.getAccount().getOwnerName(), item.getAccount().getAccountNumber(), item.getAmount(),
                item.getRepaymentMonths(), null, item.getMonthlyPayment(), item.getPurpose(), item.getStatus(),
                item.getCreatedAt(), item.getDecidedAt(), item.getDecisionNote());
    }

    private ApplicationResponse overdraftResponse(OverdraftApplication item) {
        return new ApplicationResponse(item.getId(), "OVERDRAFT", "Kontokorent", item.getAccount().getId(),
                item.getAccount().getOwnerName(), item.getAccount().getAccountNumber(), item.getRequestedLimit(),
                null, item.getMonthlyIncome(), null, "Provozní rezerva", item.getStatus(), item.getCreatedAt(),
                item.getDecidedAt(), item.getDecisionNote());
    }
}