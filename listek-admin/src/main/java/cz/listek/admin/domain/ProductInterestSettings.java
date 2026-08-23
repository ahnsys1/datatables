package cz.listek.admin.domain;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_interest_settings")
public class ProductInterestSettings {

    @Id
    private Boolean id;
    private BigDecimal savingsRate;
    private BigDecimal overdraftRate;
    private BigDecimal personalLoanRate;
    private BigDecimal homeLoanRate;

    protected ProductInterestSettings() {
    }

    public BigDecimal getSavingsRate() {
        return savingsRate;
    }

    public BigDecimal getOverdraftRate() {
        return overdraftRate;
    }

    public BigDecimal getPersonalLoanRate() {
        return personalLoanRate;
    }

    public BigDecimal getHomeLoanRate() {
        return homeLoanRate;
    }

    public void update(BigDecimal savingsRate, BigDecimal overdraftRate, BigDecimal personalLoanRate, BigDecimal homeLoanRate) {
        this.savingsRate = savingsRate;
        this.overdraftRate = overdraftRate;
        this.personalLoanRate = personalLoanRate;
        this.homeLoanRate = homeLoanRate;
    }
}
