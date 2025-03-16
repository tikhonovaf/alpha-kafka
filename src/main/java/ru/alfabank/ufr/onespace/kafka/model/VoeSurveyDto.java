package ru.alfabank.ufr.onespace.kafka.model;


import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VoeSurveyDto {

    public static final String ACTION_CREATE = "create";

    @NotNull
    private String userLogin;
    @NotNull
    private String surveyId;
    private String action;
}
