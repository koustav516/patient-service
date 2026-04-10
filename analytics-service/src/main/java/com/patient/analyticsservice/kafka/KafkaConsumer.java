package com.patient.analyticsservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.google.protobuf.InvalidProtocolBufferException;

import patient.events.PatientEvent;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "patient", groupId = "analytics-service")
    public void consumeEvent(byte[] event) {
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);

            log.info("Received Patient Event: [Patient ID: {}, Event Type: {}, PatientEmail: {}]",
                    patientEvent.getPatientId(),
                    patientEvent.getEventType(),
                    patientEvent.getEmail());
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse patient event {}", e.getMessage());
        }
    }

}
