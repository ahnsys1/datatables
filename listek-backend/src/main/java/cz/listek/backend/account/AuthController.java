package cz.listek.backend.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.listek.backend.account.AccountDtos.AccountResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AccountResponse login(@Valid @RequestBody LoginRequest request) {
        return accountService.login(request.email(), request.password());
    }

    public record LoginRequest(@NotBlank
            @Email String email, @NotBlank String password) {

    }
}
