package cz.listek.admin.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cz.listek.admin.api.AdminDtos.AdminUserResponse;
import cz.listek.admin.api.AdminDtos.AuthResponse;
import cz.listek.admin.api.AdminDtos.CreateAdminRequest;
import cz.listek.admin.api.AdminDtos.LoginRequest;
import cz.listek.admin.domain.AdminUser;
import cz.listek.admin.repository.AdminUserRepository;

@Service
public class AdminAuthService {

    private final AdminUserRepository userRepository;
    private final ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public AdminAuthService(AdminUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse login(LoginRequest request) {
        String username = request.username().trim();
        AdminUser user = userRepository.findById(username).orElseThrow(() -> invalidCredentials());
        if (!MessageDigest.isEqual(user.getPasswordHash().getBytes(StandardCharsets.UTF_8),
                request.password().getBytes(StandardCharsets.UTF_8))) {
            throw invalidCredentials();
        }
        String token = HexFormat.of().formatHex(random.generateSeed(32));
        sessions.put(token, username);
        return new AuthResponse(token, username, user.mustChangePassword());
    }

    public void changePassword(String session, String password) {
        String username = sessions.get(session);
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Neplatná administrátorská relace");
        }
        userRepository.findById(username).ifPresentOrElse(user -> {
            user.changePassword(password);
            userRepository.save(user);
        }, () -> {
            throw invalidCredentials();
        });
    }

    public AdminUserResponse createAdmin(String session, CreateAdminRequest request) {
        requireSession(session);
        String username = request.username().trim();
        if (userRepository.existsById(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Administrátor s tímto uživatelským jménem již existuje");
        }
        AdminUser user = new AdminUser(username, request.firstName().trim(), request.lastName().trim(),
                request.birthNumber().trim(), request.email().trim(), request.street().trim(),
                request.city().trim(), request.postalCode().trim(), request.password(), false);
        userRepository.save(user);
        return toResponse(user);
    }

    public List<AdminUserResponse> listAdmins(String session) {
        requireSession(session);
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    private AdminUserResponse toResponse(AdminUser user) {
        return new AdminUserResponse(user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail());
    }

    private void requireSession(String session) {
        if (sessions.get(session) == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Neplatná administrátorská relace");
        }
    }

    private static ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nesprávné uživatelské jméno nebo heslo");
    }
}
