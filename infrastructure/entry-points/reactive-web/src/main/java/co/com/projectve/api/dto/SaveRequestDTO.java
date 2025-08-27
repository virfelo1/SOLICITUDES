package co.com.projectve.api.dto;

import java.math.BigDecimal;

public record SaveRequestDTO(String documentType, Long documentNumber, BigDecimal creditAmount, Integer creditTime, String typeCredit) {
}
