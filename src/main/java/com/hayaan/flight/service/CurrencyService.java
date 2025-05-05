package com.hayaan.flight.service;

import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.dto.CurrencyConversionListResp;
import com.hayaan.flight.object.dto.CurrencyListResp;
import com.hayaan.flight.object.dto.InsertConversionHistoryDb;
import com.hayaan.flight.object.entity.Currency;
import com.hayaan.flight.object.entity.CurrencyConversion;
import com.hayaan.flight.object.entity.TicketConversionHistory;
import com.hayaan.flight.repo.CurrencyConversionRepo;
import com.hayaan.flight.repo.CurrencyRepository;
import com.hayaan.flight.repo.TicketConversionHistoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    private final CurrencyConversionRepo currencyConversionRepo;

    private final TicketConversionHistoryRepo ticketConversionHistoryRepo;

    // get all currencies
    public CurrencyListResp getAllCurrencies() {
        List<Currency> currencies = currencyRepository.findAll();

        if (currencies.isEmpty()) {
            return CurrencyListResp.builder()
                    .status(400)
                    .message("No currencies defined in the system.")
                    .currencies(List.of())
                    .build();
        }

        return CurrencyListResp.builder()
                .status(200)
                .message("Success")
                .currencies(currencies)
                .build();
    }

    // create new currency
    public CustomResponse createCurrency(Currency currency) {

        Optional<Currency> byNameOrCode = currencyRepository.findByNameOrCode(currency.getName(), currency.getCode());

        if (byNameOrCode.isPresent()) {
            return new CustomResponse(400, "Currency name or code already exists", null);

        }

        var newCurrency = Currency.builder()
                .code(currency.getCode())
                .name(currency.getName())
                .build();

        currencyRepository.save(newCurrency);
        return new CustomResponse(200, "Currency created successfully", null);

    }

    public CustomResponse updateCurrency(Long id, Currency currencyDetails) {
        Optional<Currency> currencyOptional = currencyRepository.findById(id);
        if (currencyOptional.isPresent()) {
            Currency currency = currencyOptional.get();
            currency.setName(currencyDetails.getName());
            currency.setCode(currencyDetails.getCode());
            currency.setUpdatedAt(LocalDateTime.now());
            currencyRepository.save(currency);
            return new CustomResponse(200, "Currency updated successfully", null);
        } else {
            return new CustomResponse(404, "Currency not found", null);
        }

    }

    // TODO: 8/1/2024 currency conversion
    public CurrencyConversionListResp getAllCurrencyConversions() {
        List<CurrencyConversion> conversions = currencyConversionRepo.findAll();


        if (conversions.isEmpty()) {
            return CurrencyConversionListResp.builder()
                    .status(400)
                    .message("No currency conversions defined in the system.")
                    .currencyConversions(List.of())
                    .build();
        }

        return CurrencyConversionListResp.builder()
                .status(200)
                .message("Success")
                .currencyConversions(conversions)
                .build();
    }

    public CustomResponse createCurrencyConversion(CurrencyConversion conversion) {


        Optional<CurrencyConversion> byBaseCurrencyAndTargetCurrency = currencyConversionRepo.findByBaseCurrencyAndTargetCurrency(conversion.getBaseCurrency(), conversion.getTargetCurrency());

        if (byBaseCurrencyAndTargetCurrency.isPresent()) {
            String message = String.format("Currency conversion of %s and %s is already defined", conversion.getBaseCurrency(), conversion.getTargetCurrency());
            return new CustomResponse(400, message, null);
        }

        CurrencyConversion newConversion = new CurrencyConversion();

        newConversion.setBaseCurrency(conversion.getBaseCurrency());
        newConversion.setTargetCurrency(conversion.getTargetCurrency());
        newConversion.setRate(conversion.getRate());
        newConversion.setSource("MARKET");
        newConversion.setCreatedAt(LocalDateTime.now());
        newConversion.setDate(LocalDate.now());
        currencyConversionRepo.save(newConversion);
        return new CustomResponse(200, "Currency conversion created successfully", null);

    }

    public CustomResponse updateCurrencyConversion(Long id, CurrencyConversion conversionDetails) {
        Optional<CurrencyConversion> conversionOptional = currencyConversionRepo.findById(id);
        if (conversionOptional.isPresent()) {
            CurrencyConversion conversion = conversionOptional.get();
            conversion.setBaseCurrency(conversionDetails.getBaseCurrency());
            conversion.setTargetCurrency(conversionDetails.getTargetCurrency());
            conversion.setRate(conversionDetails.getRate());
            conversion.setDate(conversionDetails.getDate());
            conversion.setSource(conversionDetails.getSource());
            currencyConversionRepo.save(conversion);

            return new CustomResponse(200, "Currency conversion created successfully", null);

        } else {
            return new CustomResponse(400, "Currency conversion not found", null);
        }
    }


    public double convertCurrency(String from, String to) {
        Optional<CurrencyConversion> byBaseCurrencyAndTargetCurrency = currencyConversionRepo.findByBaseCurrencyAndTargetCurrency(from, to);

        if (!byBaseCurrencyAndTargetCurrency.isPresent()) {
            throw new RuntimeException("Currencies conversion not found");
        }

        CurrencyConversion currencyConversion = byBaseCurrencyAndTargetCurrency.get();
        double conversionRate = currencyConversion.getRate();

        return conversionRate;
    }


    public void logTicketConversionHistoryToDb(InsertConversionHistoryDb insertConversionHistoryDb) {

        var ticketConversionHis = TicketConversionHistory.builder()
                .rate(insertConversionHistoryDb.getRate())
                .baseCurrency(insertConversionHistoryDb.getBaseCurrency())
                .targetCurrency(insertConversionHistoryDb.getTargetCurrency())
                .originalAmount(insertConversionHistoryDb.getOriginalAmount())
                .bookingReference(insertConversionHistoryDb.getBookingReference())
                .amountAfter(insertConversionHistoryDb.getAmountAfter())
                .build();

        ticketConversionHistoryRepo.save(ticketConversionHis);
    }

}
