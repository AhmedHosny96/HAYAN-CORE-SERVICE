package com.hayaan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class ResponseEntityExceptionHandler {

    //    @ExceptionHandler(CustomBadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(CustomBadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CustomBadRequestException.class)
    public ResponseEntity<String> handleForbiddenException(CustomBadRequestException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
