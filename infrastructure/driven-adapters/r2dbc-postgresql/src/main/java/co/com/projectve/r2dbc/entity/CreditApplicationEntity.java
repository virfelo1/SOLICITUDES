package co.com.projectve.r2dbc.entity;


import jakarta.persistence.Entity;
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
    @Column("id")
    private Integer id;

    @Column("document_type")
    private String documentType;
    //@Column(unique =true)
    @Column("document_number")
    private String documentNumber;

    @Column("credit_amount")
    private BigDecimal creditAmount;

    @Column("credit_time")
    private Integer creditTime;

    @Column("type_credit")
    private String typeCredit;

    @Column("credit_status")
    private String creditStatus;
}
