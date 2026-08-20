package cz.listek.backend.account;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public final class AccountDtos {
    private AccountDtos() {}

    public record AccountResponse(UUID id, String ownerName, String accountNumber, BigDecimal balance, CurrencyCode currency) {}

    public record CreateAccountRequest(
            @NotBlank String ownerName,
            @NotBlank @Pattern(regexp = "[0-9]{9,34}") String accountNumber,
            BigDecimal initialBalance,
            CurrencyCode currency) {}
}
