package co.com.projectve.r2dbc.entity;

import jakarta.persistence.Entity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "loan_type")
@Data
public class LoanTypeEntity {
    @Id
    @Column("id_loan_type")
    private short idLoanType;

    @Column("name_loan")
    private String nameLoanType;

    @Column("max_amount")
    private BigDecimal maxAmount;

    @Column("min_amount")
    private BigDecimal minAmount;

    @Column("interest_rate")
    private double interestRate;

    @Column("auto_validation")
    private Boolean autoValidation;
}
