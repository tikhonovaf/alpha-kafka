package ru.alfabank.ufr.onespace.kafka.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alfabank.ufr.onespace.kafka.model.CallDetailsDto;
import ru.alfabank.ufr.onespace.kafka.model.MlmDto;
import ru.alfabank.ufr.onespace.kafka.service.CallDetailsKafkaProducerService;
import ru.alfabank.ufr.onespace.kafka.service.MlmInfoKafkaProducerService;

import javax.validation.Valid;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(
        value = "/api",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class CallController {

    private final CallDetailsKafkaProducerService kafkaProducerService;
    private final MlmInfoKafkaProducerService mlmInfoKafkaProducerService;

    @PostMapping("/callDetails")
    public ResponseEntity<Void> receiveCallDetails(@Valid @RequestBody CallDetailsDto callDetailsDto) {
        log.info("Receive callDetails, pomSessionId: {}", callDetailsDto.getPomSessionId());
        CompletableFuture.runAsync(() -> {
            kafkaProducerService.sendMessage(callDetailsDto);
        });
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/mlm-call-info")
    public ResponseEntity<Void> receiveCalMlmCallInfo(@Valid @RequestBody MlmDto mlmDto) {
        log.info("Receive mlmCallInfo, UCID: {}", mlmDto.UCID());
        CompletableFuture.runAsync(() -> {
            mlmInfoKafkaProducerService.sendMessage(mlmDto);
        });
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
