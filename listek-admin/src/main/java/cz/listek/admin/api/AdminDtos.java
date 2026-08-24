package cz.listek.admin.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import cz.listek.admin.domain.ApplicationStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record LoginRequest(@jakarta.validation.constraints.NotBlank String username,
            @jakarta.validation.constraints.NotBlank String password) {

    }

    public record PasswordRequest(@jakarta.validation.constraints.NotBlank String password) {

    }

    public record CreateAdminRequest(@NotBlank
            @Size(max = 80) String username,
            @NotBlank
            @Size(max = 100) String firstName,
            @NotBlank
            @Size(max = 100) String lastName,
            @NotBlank
            @jakarta.validation.constraints.Pattern(regexp = "[0-9]{6}/?[0-9]{3,4}") String birthNumber,
            @jakarta.validation.constraints.Email
            @NotBlank
            @Size(max = 160) String email,
            @NotBlank
            @Size(max = 160) String street,
            @NotBlank
            @Size(max = 100) String city,
            @NotBlank
            @jakarta.validation.constraints.Pattern(regexp = "[0-9]{3} ?[0-9]{2}") String postalCode,
            @NotBlank
            @Size(min = 12, max = 200) String password) {

    }

    public record AdminUserResponse(String username) {

    }

    public record AuthResponse(String token, String username, boolean mustChangePassword) {

    }

    public record DashboardResponse(long clients, long pendingLoans, long pendingOverdrafts,
            BigDecimal deposits, long decidedToday) {

    }

    public record AccountResponse(UUID id, String ownerName, String email, String accountNumber,
            BigDecimal balance, String currency, String type) {

    }

    public record ApplicationResponse(UUID id, String category, String product, UUID accountId,
            String clientName, String accountNumber, BigDecimal amount, Integer repaymentMonths,
            BigDecimal monthlyIncome, BigDecimal monthlyPayment, String purpose,
            ApplicationStatus status, Instant createdAt, Instant decidedAt, String decisionNote) {

    }

    public record DecisionRequest(@NotNull ApplicationStatus status, @Size(max = 500) String note) {

    }

    public record CreateOverdraftRequest(@NotNull UUID accountId,
            @NotNull
            @DecimalMin("1000.00")
            @DecimalMax("250000.00") BigDecimal requestedLimit,
            @NotNull
            @DecimalMin("0.00") BigDecimal monthlyIncome) {

    }

    public record InterestSettingsResponse(BigDecimal savingsRate, BigDecimal overdraftRate,
            BigDecimal personalLoanRate, BigDecimal homeLoanRate) {

    }

    public record UpdateInterestSettingsRequest(@NotNull
            @DecimalMin("0.00") BigDecimal savingsRate,
            @NotNull
            @DecimalMin("0.00") BigDecimal overdraftRate,
            @NotNull
            @DecimalMin("0.00") BigDecimal personalLoanRate,
            @NotNull
            @DecimalMin("0.00") BigDecimal homeLoanRate) {

    }
}
