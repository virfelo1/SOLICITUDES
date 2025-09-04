package co.com.projectve.r2dbc.entity;


import jakarta.persistence.Entity;
import org.springframework.data.annotation.Transient;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "users_info")
@Data
public class CreditApplicationEntity {
    @Id
    @Column("id_request")
    private Integer idRequest;

    @Column("document_type")
    private String documentType;

    @Column("document_number")
    private String documentNumber;

    @Column("credit_amount")
    private BigDecimal creditAmount;

    @Column("credit_time")
    private Integer creditTime;

    @Column("email")
    private String email;

    @Column("id_state") //esta asociado a la tabla: "loan_type"
    private Short idState;

    @Column("id_loan_type") //esta asociado a la tabla: "states"
    private Short idLoanType;
}
