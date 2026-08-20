package cz.listek.backend.transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class TransactionDtos {
    private TransactionDtos() {}

    public record TransferRequest(
            @NotNull UUID fromAccountId,
            @NotNull UUID toAccountId,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank String description) {}

    public record TransactionResponse(UUID id, UUID accountId, BigDecimal amount, TransactionType type, String description, OffsetDateTime createdAt) {}
}
