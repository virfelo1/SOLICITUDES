package co.com.projectve.r2dbc.entity;


import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
import lombok.*;
import org.springframework.data.annotation.Id;
//import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.math.BigInteger;

@Entity
@Table(name = "users_info")
@Data
public class CreditApplicationEntity {
    @Id
    private BigInteger id;
    private String documentType;
    //@Column(unique =true)
    private Long documentNumber;
    private BigDecimal creditAmount;
    private Integer creditTime;
    private String typeCredit;
    private String creditStatus;
}
