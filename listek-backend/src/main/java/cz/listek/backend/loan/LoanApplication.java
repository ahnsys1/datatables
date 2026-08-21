package cz.listek.backend.loan;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import cz.listek.backend.account.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "loan_application")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private int repaymentMonths;

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal annualRate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(nullable = false, length = 80)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected LoanApplication() {
    }

    public LoanApplication(Account account, LoanType type, BigDecimal amount, int repaymentMonths,
            BigDecimal annualRate, BigDecimal monthlyPayment, String purpose) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.repaymentMonths = repaymentMonths;
        this.annualRate = annualRate;
        this.monthlyPayment = monthlyPayment;
        this.purpose = purpose;
        this.status = LoanStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public LoanType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public int getRepaymentMonths() {
        return repaymentMonths;
    }

    public BigDecimal getAnnualRate() {
        return annualRate;
    }

    public BigDecimal getMonthlyPayment() {
        return monthlyPayment;
    }

    public String getPurpose() {
        return purpose;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
