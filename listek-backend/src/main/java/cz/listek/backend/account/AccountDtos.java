package cz.listek.backend.account;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AccountDtos {

    private AccountDtos() {
    }

    public record AccountResponse(UUID id, String ownerName, String email, String address, String accountNumber, BigDecimal balance, CurrencyCode currency, AccountType type) {

    }

    public record CreateAccountRequest(
            @NotBlank String ownerName,
            @NotBlank
            @jakarta.validation.constraints.Email String email,
            @NotBlank
            @Size(max = 240) String address,
            @NotBlank
            @Size(min = 8, max = 120) String password,
            @NotBlank
            @Pattern(regexp = "[0-9]{9,34}") String accountNumber,
            BigDecimal initialBalance,
            CurrencyCode currency) {

    }

    public record UpdateAccountRequest(@NotBlank
            @Size(max = 120) String ownerName,
            @NotBlank
            @jakarta.validation.constraints.Email String email,
            @NotBlank
            @Size(max = 240) String address,
            @Size(min = 8, max = 120) String password) {

    }

    public record RegisterAccountRequest(
            @NotBlank
            @Size(max = 120) String ownerName,
            @NotBlank
            @jakarta.validation.constraints.Email String email,
            @NotBlank
            @Size(max = 240) String address,
            @NotBlank
            @Size(min = 8, max = 120) String password) {

    }
}
