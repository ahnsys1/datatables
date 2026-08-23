package cz.listek.admin.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.listek.admin.api.AdminDtos.AccountResponse;
import cz.listek.admin.api.AdminDtos.ApplicationResponse;
import cz.listek.admin.api.AdminDtos.CreateOverdraftRequest;
import cz.listek.admin.api.AdminDtos.DashboardResponse;
import cz.listek.admin.api.AdminDtos.DecisionRequest;
import cz.listek.admin.api.AdminDtos.InterestSettingsResponse;
import cz.listek.admin.api.AdminDtos.UpdateInterestSettingsRequest;
import cz.listek.admin.service.AdminWorkflowService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminWorkflowService workflowService;

    public AdminController(AdminWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return workflowService.dashboard();
    }

    @GetMapping("/accounts")
    public List<AccountResponse> accounts() {
        return workflowService.accounts();
    }

    @GetMapping("/loans")
    public List<ApplicationResponse> loans() {
        return workflowService.loans();
    }

    @PatchMapping("/loans/{id}/decision")
    public ApplicationResponse decideLoan(@PathVariable UUID id, @Valid @RequestBody DecisionRequest request) {
        return workflowService.decideLoan(id, request);
    }

    @GetMapping("/overdrafts")
    public List<ApplicationResponse> overdrafts() {
        return workflowService.overdrafts();
    }

    @PostMapping("/overdrafts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse createOverdraft(@Valid @RequestBody CreateOverdraftRequest request) {
        return workflowService.createOverdraft(request);
    }

    @PatchMapping("/overdrafts/{id}/decision")
    public ApplicationResponse decideOverdraft(@PathVariable UUID id, @Valid @RequestBody DecisionRequest request) {
        return workflowService.decideOverdraft(id, request);
    }

    @GetMapping("/settings/interest")
    public InterestSettingsResponse interestSettings() {
        return workflowService.interestSettings();
    }

    @PatchMapping("/settings/interest")
    public InterestSettingsResponse updateInterestSettings(@Valid @RequestBody UpdateInterestSettingsRequest request) {
        return workflowService.updateInterestSettings(request);
    }
}
