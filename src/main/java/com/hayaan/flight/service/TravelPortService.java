package com.hayaan.flight.service;

import com.hayaan.config.AsyncHttpConfig;
import com.hayaan.flight.object.dto.*;
import com.hayaan.flight.object.dto.flight.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.RequestBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelPortService {

    // this will call travel port service

    private final AsyncHttpConfig asyncHttp;


    private final MapperService mapperService;

    @Value("${travelPort.endpoint}")
    private String TRAVELPORT_URL;

    // SEARCH

    public FlightSearchResponse searchFlights(FlightSearchDto flightSearchDto) {
        // Construct passenger criteria array

        JSONArray passengerCriteriaArray = new JSONArray();

        for (PassengerCriteria passengerCriteria : flightSearchDto.passengerCriteria()) {

            JSONObject passengerCriteriaObj = new JSONObject();
            passengerCriteriaObj.put("value", passengerCriteria.type());
            passengerCriteriaObj.put("number", passengerCriteria.number());
            passengerCriteriaArray.put(passengerCriteriaObj);
        }

        // Construct search criteria array
        JSONArray searchCriteriaArray = new JSONArray();
        JSONObject searchCriteria = new JSONObject();
        searchCriteria.put("departureDate", flightSearchDto.departureDate());
        searchCriteria.put("from", flightSearchDto.from());
        searchCriteria.put("to", flightSearchDto.to());
        searchCriteriaArray.put(searchCriteria);

        // Construct main request body
        JSONObject mainRequestBody = new JSONObject();
        mainRequestBody.put("offersPerPage", flightSearchDto.pageNumber());
        mainRequestBody.put("passengerCriteria", passengerCriteriaArray);
        mainRequestBody.put("searchCriteria", searchCriteriaArray);

        // Log and send the request
        log.info("[SEARCHING FLIGHTS]");
        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(TRAVELPORT_URL + "/search")
                .setBody(mainRequestBody.toString());
        log.info("[SEARCH FLIGHTS FINISHED]");

        JSONObject flightSearchResponse = asyncHttp.sendRequest(requestBody);

        // RETURN ERROR RESPONSE IMMEDIATELY
        if (flightSearchResponse.has("status")) {
            FlightSearchResponse errorResponse = FlightSearchResponse.builder()
                    .status(400)
                    .message(flightSearchResponse.getString("details"))
                    .build();
            return errorResponse;
        }

        // Mapping response
        log.info("[MAPPING RESPONSE]");
        JSONArray airInfoResponse = flightSearchResponse.getJSONObject("airSegmentList")
                .getJSONArray("airSegment");

        List<AirInfoResponse> airInfoList = new ArrayList<>();
        for (int i = 0; i < airInfoResponse.length(); i++) {
            JSONObject segment = airInfoResponse.getJSONObject(i);
            AirInfoResponse airInfo = mapperService.mapToAirInfo(segment);
            airInfoList.add(airInfo);
        }

        JSONArray fairInfoResponse = flightSearchResponse.getJSONObject("fareInfoList")
                .getJSONArray("fareInfo");
        BaggageInfoResponse fairInfo = mapperService.mapToBaggageInfo(fairInfoResponse);

        JSONArray priceInfo = flightSearchResponse.getJSONObject("airPricePointList")
                .getJSONArray("airPricePoint");
        PriceInfoResponse priceInfoResponse = mapperService.mapToPriceInfo(priceInfo);


        FlightSearchResponse successResponse = FlightSearchResponse.builder()
                .status(200)
                .message("success")
                .airInfo(airInfoList)
                .fairInfo(fairInfo)
                .priceInfo(priceInfoResponse)
                .passengerInfo(flightSearchDto.passengerCriteria())
                .build();

        return successResponse;
    }

    // SEARCH FLIGHT + PRICES


    public AirPriceSolution searchFlightWithPrice(FlightPriceSearchDto flightPriceSearchDto) {
        JSONObject mainRequestBody = new JSONObject();

        // Construct passengerCriteria array
        JSONArray passengerCriteriaArray = new JSONArray();
        if (flightPriceSearchDto.getPassengersInfo() != null) {
            for (PassengerCriteria passengerCriteria : flightPriceSearchDto.getPassengersInfo()) {
                JSONObject passengerCriteriaObj = new JSONObject();
                passengerCriteriaObj.put("value", passengerCriteria.type());
                passengerCriteriaObj.put("number", passengerCriteria.number());
                passengerCriteriaArray.put(passengerCriteriaObj);
            }
        }

        // Construct airSegments array
        JSONArray airSegmentsArray = new JSONArray();
        if (flightPriceSearchDto.getAirInfo() != null) {
            for (AirInfoResponse airSegment : flightPriceSearchDto.getAirInfo()) {
                JSONObject airSegmentObj = new JSONObject();
                airSegmentObj.put("carrier", airSegment.getCarrier());
                airSegmentObj.put("flightNumber", airSegment.getFlightNumber());
                airSegmentObj.put("equipment", airSegment.getEquipment());
                airSegmentObj.put("departureDate", airSegment.getDepartureDate());
                airSegmentObj.put("departureTime", airSegment.getDepartureTime());
                airSegmentObj.put("arrivalDate", airSegment.getArrivalDate());
                airSegmentObj.put("arrivalTime", airSegment.getArrivalTime());
                airSegmentObj.put("origin", airSegment.getOrigin());
                airSegmentObj.put("destination", airSegment.getDestination());
                airSegmentsArray.put(airSegmentObj);
            }
        }

        mainRequestBody.put("passengerCriteria", passengerCriteriaArray);
        mainRequestBody.put("airSegments", airSegmentsArray);

        log.info("PRICE REQUEST : {}", mainRequestBody);

        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(TRAVELPORT_URL + "/price")
                .setBody(mainRequestBody.toString());

        JSONObject airPriceResponse = asyncHttp.sendRequest(requestBody);

        // THROW ERROR IMMEDIATELY
        if (airPriceResponse.has("status")) {
            AirPriceSolution errorResponse = AirPriceSolution.builder()
                    .status(400)
                    .message(airPriceResponse.getString("details"))
                    .build();
            return errorResponse;
        }

        JSONObject airItineraryObject = airPriceResponse.getJSONObject("airItinerary");
        log.info("AIRITINERARY : {}", airItineraryObject);


        JSONArray airSegment = airItineraryObject.getJSONArray("airSegment");

        // AIR SEGMENT MAPPING

        List<AirInfoResponse> airInfoResponseList = new ArrayList<>();

        for (int i = 0; i < airSegment.length(); i++) {
            JSONObject airSegmentObject = airSegment.getJSONObject(i);

            log.info("AIR SEGMENT : {}", airSegmentObject);

            AirInfoResponse airInfoResponse = mapperService.mapToAirInfo(airSegmentObject);

            airInfoResponseList.add(airInfoResponse);

        }

        var airPriceSolutionObject = airPriceResponse.getJSONArray("airPriceResult").getJSONObject(0).getJSONArray("airPricingSolution");
        log.info("AIRPICE SOLUTION : {}", airPriceSolutionObject);

        // PASSENGER OBJECT MAPPING

        List<PassengerType> passengerTypeList = new ArrayList<>();

        for (int i = 0; i < airPriceSolutionObject.length(); i++) {
            JSONObject airPricingInfoObj = airPriceSolutionObject.getJSONObject(i);
            JSONArray passengerTypes = airPricingInfoObj.getJSONArray("airPricingInfo");

            for (int j = 0; j < passengerTypes.length(); j++) {
                JSONObject passengerTypeObj = passengerTypes.getJSONObject(j);
                JSONArray passengerTypeArray = passengerTypeObj.getJSONArray("passengerType");

                for (int k = 0; k < passengerTypeArray.length(); k++) {
                    JSONObject passengerObj = passengerTypeArray.getJSONObject(k);
                    String code = passengerObj.getString("code");
                    // Use the code value as needed
                    log.info("PASSENGER CODE : {}", code);

                    PassengerType passengerType = PassengerType.builder()
                            .code(code)
                            .build();
                    passengerTypeList.add(passengerType);
                }
            }
        }


        var airPricing = airPriceSolutionObject.getJSONObject(0).getJSONArray("airPricingInfo").getJSONObject(0);

        String insideKey = airPricing.getString("key");

        log.info("INSIDE KEY: {}", insideKey);

        String outSideKey = airPriceSolutionObject.getJSONObject(0).getString("key");

        log.info("OUTSIDE KEY : {}", outSideKey);

        var hostTokenObject = airPriceSolutionObject.getJSONObject(0).getJSONArray("hostToken");

        log.info("HOST TOKEN : {}", hostTokenObject);

        List<HostTokenResponse> hostTokenResponse = new ArrayList<>();

        // HOST TOKEN MAPPING
        for (int i = 0; i < hostTokenObject.length(); i++) {
            JSONObject hostTokenObjectJSONObject = hostTokenObject.getJSONObject(i);
            HostTokenResponse hostTokenResponseInfo = mapperService.mapToHostToken(hostTokenObjectJSONObject);
            hostTokenResponse.add(hostTokenResponseInfo);
            log.info("HOST TOKEN : {}", hostTokenResponse);

        }

        BookingInfoResponse bookingInfoResponse = null;

        FareInfoResponse fareInfo = null;

        // FAREINFO AND BOOKINGINFO MAPPINGS

        for (int i = 0; i < airPriceSolutionObject.length(); i++) {
            JSONObject segment = airPriceSolutionObject.getJSONObject(i);

            JSONArray airPricingInfo = segment.getJSONArray("airPricingInfo");

            JSONObject bookingInfo = airPricingInfo.getJSONObject(0).getJSONArray("bookingInfo").getJSONObject(0);
            log.info("BOOKINGINFO :{}", bookingInfo);
            // MAP TO BOOKING INFO
            bookingInfoResponse = mapperService.mapToBookingInfo(bookingInfo);

            JSONObject fareInfoObj = airPricingInfo.getJSONObject(0).getJSONArray("fareInfo").getJSONObject(0);
            log.info("FAREINFO :{}", fareInfo);
            // MAP TO FAIR INFO
            fareInfo = mapperService.mapToFareInfo(fareInfoObj);


        }

        // CONSTRUCT AIRPRICINGINFO

        AirObjectInfo airPricingObj = AirObjectInfo.builder()
                .key(outSideKey)
                .bookingInfo(bookingInfoResponse)
                .fareInfo(fareInfo)
                .build();


        // CONSTRUCTING AIRPRICE RESPONSE

        AirPriceInfoResponse airPriceInfoResponse = AirPriceInfoResponse
                .builder()
                .key(insideKey)
//                .bookingInfo(bookingInfoResponse)
                .airInfo(airInfoResponseList)
                .hostTokenInfo(hostTokenResponse)
                .airPriceInfo(List.of(airPricingObj))
//                .fareInfo(fareInfo)
                .build();

        // CONSTRUCTING MAIN AIRPRICE RESPONSE

        return AirPriceSolution.builder()
                .status(200)
                .message("success")
//                .airInfo(airSegmentsArray)
                .airPriceInfo(List.of(airPriceInfoResponse))
                .passengerInfo(passengerTypeList)
                .build();
//                .passengerInfo()

//        return response;
    }

    // SEARCH WITH AIR LEG MODIFIER ✅
//        if (flightSearchDto.preferredCabin().eq("Business")) {
//
//            var modifiers = new JSONObject();
//
//            var preferredCabins = new JSONArray();
//            preferredCabins.put(flightSearchDto.preferredCabin());
//
//            modifiers.put("allowDirectAccess", true);
//            modifiers.put("maxConnectionTime", 24);
//            modifiers.put("preferredCabins", preferredCabins);
//
//            searchCriteriaArray.put(modifiers);
//
//        }
//    public void searchFlightWithPrice(FlightPriceSearchDto flightPriceSearchDto) {
//
//
//        JSONObject mainRequestBody = new JSONObject();
//
//        // Construct passengerCriteria array
//        JSONArray passengerCriteriaArray = new JSONArray();
//        for (PassengerCriteria passengerCriteria : flightPriceSearchDto.getPassengerCriteria()) {
//            JSONObject passengerCriteriaObj = new JSONObject();
//            passengerCriteriaObj.put("value", passengerCriteria.type());
//            passengerCriteriaObj.put("number", passengerCriteria.number());
//            passengerCriteriaArray.put(passengerCriteriaObj);
//        }
//
//        // Construct airSegments array
//        JSONArray airSegmentsArray = new JSONArray();
//        for (AirInfoResponse airSegment : flightPriceSearchDto.getAirSegment()) {
//            JSONObject airSegmentObj = new JSONObject();
//            airSegmentObj.put("carrier", airSegment.getCarrier());
//            airSegmentObj.put("flightNumber", airSegment.getFlightNumber());
//            airSegmentObj.put("equipment", airSegment.getEquipment());
//            airSegmentObj.put("departureDate", airSegment.getDepartureDate());
//            airSegmentObj.put("departureTime", airSegment.getDepartureTime());
//            airSegmentObj.put("arrivalDate", airSegment.getArrivalDate());
//            airSegmentObj.put("arrivalTime", airSegment.getArrivalTime());
//            airSegmentObj.put("origin", airSegment.getOrigin());
//            airSegmentObj.put("destination", airSegment.getDestination());
//            airSegmentsArray.put(airSegmentObj);
//        }
//
//        mainRequestBody.put("passengerCriteria", passengerCriteriaArray);
//        mainRequestBody.put("airSegments", airSegmentsArray);
//
//
//    }


    // search flight prices

    // book flight

    // cancel flight

    //
}
