package cz.listek.backend.payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class PaymentDtos {

    private PaymentDtos() {
    }

    public record CreateStandingOrderRequest(@NotBlank String targetAccountNumber, @NotNull
            @DecimalMin("0.01") BigDecimal amount, @NotBlank String description, @Min(1)
            @Max(28) int dayOfMonth) {

    }

    public record UpdateStandingOrderRequest(@NotBlank String targetAccountNumber, @NotNull
            @DecimalMin("0.01") BigDecimal amount, @NotBlank String description, @Min(1)
            @Max(28) int dayOfMonth) {

    }

    public record StandingOrderResponse(UUID id, UUID accountId, String targetAccountNumber, BigDecimal amount, String description, int dayOfMonth, boolean active, OffsetDateTime createdAt) {

    }

    public record CreatePaymentTemplateRequest(@NotBlank String name, @NotBlank String targetAccountNumber, @NotNull
            @DecimalMin("0.01") BigDecimal amount, @NotBlank String description) {

    }

    public record PaymentTemplateResponse(UUID id, UUID accountId, String name, String targetAccountNumber, BigDecimal amount, String description, OffsetDateTime createdAt) {

    }
}
