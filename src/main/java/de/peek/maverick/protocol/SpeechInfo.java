package de.peek.maverick.protocol;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@Builder
@ToString
public class SpeechInfo {

    private String speaker;
    private String theme;
    private Date date;
    private Integer words;

}
