package ru.alfabank.ufr.onespace.kafka.model;

import javax.validation.constraints.NotEmpty;

public record MlmDto(
        String phoneNumber,
        @NotEmpty
        String UCID,
        String CUS,
        String startTime,
        String endTime,
        @NotEmpty
        String operatorId
) {
}
