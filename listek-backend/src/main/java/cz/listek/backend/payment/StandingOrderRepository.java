package cz.listek.backend.payment;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StandingOrderRepository extends JpaRepository<StandingOrder, UUID> {

    List<StandingOrder> findByAccount_IdOrderByCreatedAtDesc(UUID accountId);
}
