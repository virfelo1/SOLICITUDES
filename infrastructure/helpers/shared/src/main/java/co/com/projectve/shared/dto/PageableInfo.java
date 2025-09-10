package co.com.projectve.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageableInfo {
    private int pageNumber;
    private int pageSize;
    private long offset;
    private boolean paged;
    private boolean unpaged;
}




