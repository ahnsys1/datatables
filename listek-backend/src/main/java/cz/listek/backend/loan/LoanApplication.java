package cz.listek.backend.loan;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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

    @Column(length = 34)
    private String repaymentAccountNumber;

    @Column(length = 10)
    private String variableSymbol;

    @Column(length = 10)
    private String specificSymbol;

    private Integer repaymentDayOfMonth;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal repaidAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal remainingAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalBalance = BigDecimal.ZERO;

    private Integer remainingInstallments;

    private LocalDate dueDate;

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
        this.remainingInstallments = repaymentMonths;
        this.remainingAmount = monthlyPayment.multiply(BigDecimal.valueOf(repaymentMonths));
        this.principalBalance = amount;
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

    public String getRepaymentAccountNumber() {
        return repaymentAccountNumber;
    }

    public String getVariableSymbol() {
        return variableSymbol;
    }

    public String getSpecificSymbol() {
        return specificSymbol;
    }

    public Integer getRepaymentDayOfMonth() {
        return repaymentDayOfMonth;
    }

    public BigDecimal getRepaidAmount() {
        return repaidAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public Integer getRemainingInstallments() {
        return remainingInstallments;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal calculateEarlyRepaymentAmount() {
        return this.principalBalance.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public void configureRepayment(String repaymentAccountNumber, String variableSymbol, String specificSymbol,
            int repaymentDayOfMonth, LocalDate dueDate) {
        this.repaymentAccountNumber = repaymentAccountNumber;
        this.variableSymbol = variableSymbol;
        this.specificSymbol = specificSymbol;
        this.repaymentDayOfMonth = repaymentDayOfMonth;
        this.dueDate = dueDate;
        this.remainingAmount = monthlyPayment.multiply(BigDecimal.valueOf(repaymentMonths));
        this.principalBalance = amount;
    }

    public void recordRepayment(BigDecimal amount, boolean earlyRepayment) {
        if (amount.compareTo(calculateEarlyRepaymentAmount()) >= 0) {
            this.repaidAmount = this.repaidAmount.add(amount);
            this.remainingAmount = BigDecimal.ZERO.setScale(2);
            this.remainingInstallments = 0;
            return;
        }
        this.repaidAmount = this.repaidAmount.add(amount);
        if (earlyRepayment) {
            this.principalBalance = this.principalBalance.subtract(amount).max(BigDecimal.ZERO);
        } else {
            BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), 12, java.math.RoundingMode.HALF_UP);
            BigDecimal interest = this.principalBalance.multiply(monthlyRate).setScale(2, java.math.RoundingMode.HALF_UP);
            this.principalBalance = this.principalBalance.subtract(amount.subtract(interest)).max(BigDecimal.ZERO);
            this.remainingInstallments = Math.max(0, remainingInstallments - 1);
        }
        this.remainingAmount = calculateRemainingAmount()
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRemainingAmount() {
        if (principalBalance.signum() == 0 || remainingInstallments == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), 12, java.math.RoundingMode.HALF_UP);
        double rate = monthlyRate.doubleValue();
        double principal = principalBalance.doubleValue();
        double months = remainingInstallments;
        double payment = rate == 0 ? principal : principal * rate / (1 - Math.pow(1 + rate, -months));
        return BigDecimal.valueOf(payment * months);
    }
}
