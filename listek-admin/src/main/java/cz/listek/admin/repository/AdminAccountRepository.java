package cz.listek.admin.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.listek.admin.domain.AdminAccount;

public interface AdminAccountRepository extends JpaRepository<AdminAccount, UUID> {
}