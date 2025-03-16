package ru.alfabank.ufr.onespace.kafka.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallDetailsDto {

    @NotNull
    private String typeBr;
    @NotNull
    private String userContactId;
    @NotNull
    private String callResultName;
    @NotNull
    private String callDateTime;
    private String callbackPhone;
    private String callbackDateTime;
    private String campaignName;
    private String comment;
    @NotNull
    private String pomSessionId;

    private Long talkTime;
    private Long acwTime;
    private String operatorId;
    private String phoneNumber;

}
