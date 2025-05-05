package com.hayaan.flight.service;

import com.hayaan.flight.object.dto.AllCountryResp;
import com.hayaan.flight.object.entity.Country;
import com.hayaan.flight.repo.CountryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepo countryRepo;


    public AllCountryResp getAllCountries() {

        List<Country> all = countryRepo.findAll();


        return AllCountryResp.builder()
                .status(200)
                .message("success")
                .countries(all)
                .build();
    }
}
