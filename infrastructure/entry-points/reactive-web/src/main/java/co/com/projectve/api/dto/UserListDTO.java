package co.com.projectve.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class UserListDTO {
    private String email;
    private String firstName;
    private BigDecimal baseSalary;
}
