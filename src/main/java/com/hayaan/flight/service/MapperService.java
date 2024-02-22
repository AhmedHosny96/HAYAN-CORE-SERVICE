package com.hayaan.flight.service;

import com.hayaan.flight.object.dto.BookingInfoResponse;
import com.hayaan.flight.object.dto.FareInfoResponse;
import com.hayaan.flight.object.dto.HostTokenResponse;
import com.hayaan.flight.object.dto.flight.AirInfoResponse;
import com.hayaan.flight.object.dto.flight.BaggageInfoResponse;
import com.hayaan.flight.object.dto.flight.PriceInfoResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class MapperService {


    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");


    // FLIGHT SEARCH MAPPINGS

    public AirInfoResponse mapToAirInfo(JSONObject response) {

        String arrivalDate = response.optString("arrivalTime");
        String arrivalTimeString = arrivalDate.substring(arrivalDate.indexOf('T') + 1);

        String departureDate = response.optString("departureTime");
        String departureTimeString = departureDate.substring(departureDate.indexOf('T') + 1);

        return AirInfoResponse.builder()
                .key(response.getString("key"))
                .classOfService(response.has("classOfService") ? response.get("classOfService").toString() : null)
                .origin(response.getString("origin"))
                .destination(response.optString("destination"))
                .carrier(response.optString("carrier"))
                .flightNumber(response.optString("flightNumber"))
                .equipment(response.optString("equipment"))
                .departureDate(LocalDate.parse(response.optString("departureTime"), DATE_TIME_FORMATTER))
                .arrivalTime(arrivalTimeString)
                .departureTime(departureTimeString)
                .arrivalDate(LocalDate.parse(response.optString("arrivalTime"), DATE_TIME_FORMATTER))
                .FlightTime(response.optInt("flightTime"))
                .build();

    }


    public PriceInfoResponse mapToPriceInfo(JSONArray response) {
        JSONObject priceKeys = response.getJSONObject(0);
        String currency = priceKeys.getString("totalPrice").substring(0, 3);
        double originalPrice = Double.parseDouble(priceKeys.getString("basePrice").substring(3));
        double taxes = Double.parseDouble(priceKeys.getString("taxes").substring(3));
        double commission = 100.0;

        double priceAfterTaxAndMarkup = originalPrice + taxes + commission;

        PriceInfoResponse priceInfoResponse = PriceInfoResponse.builder()
                .originalPrice(originalPrice)
                .taxes(taxes)
                .commission(commission)
                .priceAfterTaxAndCommission(priceAfterTaxAndMarkup)
                .currency(currency)
                .build();

        return priceInfoResponse;
    }


    public BaggageInfoResponse mapToBaggageInfo(JSONArray response) {

        JSONObject baggageINfo = response.getJSONObject(0).getJSONObject("baggageAllowance").getJSONObject("maxWeight");

        return BaggageInfoResponse
                .builder()
                .value(baggageINfo.getInt("value"))
                .unit(baggageINfo.getString("unit"))
                .build();

    }

    // AIR PRICE SEARCH MAPPINGS

    public HostTokenResponse mapToHostToken(JSONObject response) {

        return HostTokenResponse.builder()
                .key(response.optString("key"))
                .value(response.optString("value"))
                .build();
    }

    public BookingInfoResponse mapToBookingInfo(JSONObject response) {

        return BookingInfoResponse.builder()
                .bookingCode(response.getString("bookingCode"))
                .cabinClass(response.getString("cabinClass"))
                .segmentRef(response.getString("segmentRef"))
                .fareInfoRef(response.getString("fareInfoRef"))
                .hostTokenRef(response.getString("hostTokenRef"))
                .build();
    }

    public FareInfoResponse mapToFareInfo(JSONObject fareInfoObj) {

        return FareInfoResponse.builder()
                .key(fareInfoObj.getString("key"))
                .fareBasis(fareInfoObj.getString("fareBasis"))
                .passengerTypeCode(fareInfoObj.getString("passengerTypeCode"))
                .origin(fareInfoObj.getString("origin"))
                .destination(fareInfoObj.getString("destination"))
                .build();
    }

    //

}
