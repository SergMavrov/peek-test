package de.peek.maverick.protocol;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalyticResponse {

    private String mostSpeeches;
    private String mostSecurity;
    private String leastWordy;

}
