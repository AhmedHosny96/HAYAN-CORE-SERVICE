package com.hayaan.flight.controller;


import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.dto.CurrencyConversionListResp;
import com.hayaan.flight.object.dto.CurrencyListResp;
import com.hayaan.flight.object.entity.Currency;
import com.hayaan.flight.object.entity.CurrencyConversion;
import com.hayaan.flight.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})

public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping("/currency")
    public ResponseEntity<CurrencyListResp> getAllCurrencies() {
        CurrencyListResp response = currencyService.getAllCurrencies();
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }

    @PostMapping("/currency")
    public ResponseEntity<CustomResponse> createCurrency(@RequestBody Currency currency) {
        CustomResponse response = currencyService.createCurrency(currency);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.status()));
    }

    @PutMapping("/currency/{id}")
    public ResponseEntity<CustomResponse> updateCurrency(@PathVariable Long id, @RequestBody Currency currencyDetails) {
        CustomResponse response = currencyService.updateCurrency(id, currencyDetails);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.status()));
    }

    @GetMapping("/currency-conversions")
    public ResponseEntity<CurrencyConversionListResp> getAllCurrencyConversions() {
        CurrencyConversionListResp response = currencyService.getAllCurrencyConversions();
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }

    @PostMapping("/currency-conversions")
    public ResponseEntity<CustomResponse> createCurrencyConversion(@RequestBody CurrencyConversion conversion) {
        CustomResponse response = currencyService.createCurrencyConversion(conversion);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.status()));
    }

    @PutMapping("/currency-conversions/{id}")
    public ResponseEntity<CustomResponse> updateCurrencyConversion(@PathVariable Long id, @RequestBody CurrencyConversion conversionDetails) {
        CustomResponse response = currencyService.updateCurrencyConversion(id, conversionDetails);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.status()));
    }

}
