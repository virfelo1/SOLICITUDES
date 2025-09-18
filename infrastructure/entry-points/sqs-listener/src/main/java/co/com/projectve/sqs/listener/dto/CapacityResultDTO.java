package co.com.projectve.sqs.listener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapacityResultDTO {
    private Integer idRequest;
    private String email;
    private String state;
}
