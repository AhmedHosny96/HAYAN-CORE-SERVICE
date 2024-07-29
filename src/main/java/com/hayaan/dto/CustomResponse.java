package com.hayaan.dto;


import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomResponse(
        int status,
        String message,
        String token
) {
}
