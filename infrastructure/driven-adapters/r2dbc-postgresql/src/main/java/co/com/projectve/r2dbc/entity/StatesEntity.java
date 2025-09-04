package co.com.projectve.r2dbc.entity;

import jakarta.persistence.Entity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "states")
@Data
public class StatesEntity {

    @Id
    @Column("id_state")
    private short idState;

    @Column("name_state")
    private String nameState;

    @Column("description")
    private String description;

}
