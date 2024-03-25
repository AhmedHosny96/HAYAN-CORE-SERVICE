package com.hayaan.flight.object.dto.booking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponse {

    private int status;
    private String message;

    private List<BookingDetails> bookingInfo;

    @Data
    @Builder
    public static class BookingDetails {
        private String pnrCode;
        private int version;
        private String status;
        private String transactionId;
        private LocalDateTime timeLimit;
    }
}
