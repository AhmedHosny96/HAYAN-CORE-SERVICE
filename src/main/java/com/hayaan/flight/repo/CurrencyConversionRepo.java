package com.hayaan.flight.repo;

import com.hayaan.flight.object.entity.CurrencyConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyConversionRepo extends JpaRepository<CurrencyConversion, Long> {

    Optional<CurrencyConversion> findByBaseCurrencyAndTargetCurrency(String baseCurrency, String targetCurrency);
}
