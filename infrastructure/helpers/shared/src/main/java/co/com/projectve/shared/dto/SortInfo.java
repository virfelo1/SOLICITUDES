package co.com.projectve.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SortInfo {
    private boolean empty;
    private boolean sorted;
    private boolean unsorted;
}




