package cz.listek.backend.loan;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    List<LoanApplication> findByAccount_IdOrderByCreatedAtDesc(UUID accountId);
}
