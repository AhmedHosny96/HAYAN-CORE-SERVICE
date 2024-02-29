package com.hayaan.flight.service;

import com.hayaan.config.AsyncHttpConfig;
import com.hayaan.flight.object.dto.*;
import com.hayaan.flight.object.dto.booking.*;
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

    private final AsyncHttpConfig asyncHttp;

    private final MapperService mapperService;

    @Value("${travelPort.endpoint}")
    private String TRAVELPORT_URL;

    //        for (PassengerCriteria passengerCriteria : flightSearchDto.passengerCriteria()) {
//
//            passengerCriteriaObj.put("value", passengerCriteria.type());
//            passengerCriteriaObj.put("number", passengerCriteria.number());
//            passengerCriteriaArray.put(passengerCriteriaObj);
//        }


    // SEARCH FLIGHT + PRICES ✅
    public FlightSearchResponse searchFlights(FlightSearchDto flightSearchDto) {
        // Construct passenger criteria array

        JSONArray passengerCriteriaArray = new JSONArray();

        // Add adults
        for (int i = 0; i < flightSearchDto.noOfAdult(); i++) {
            JSONObject adultPassenger = new JSONObject();
            adultPassenger.put("value", "ADT");
            adultPassenger.put("number", 1);
            passengerCriteriaArray.put(adultPassenger);
        }

        // Add child passengers
        for (int i = 0; i < flightSearchDto.noOfChildren(); i++) {
            JSONObject childPassenger = new JSONObject();
            childPassenger.put("value", "CHD");
            childPassenger.put("number", 1);
            passengerCriteriaArray.put(childPassenger);
        }

        // Add infant passengers
        for (int i = 0; i < flightSearchDto.noOfInfant(); i++) {
            JSONObject infantPassenger = new JSONObject();
            infantPassenger.put("value", "INF");
            infantPassenger.put("number", 1);
            passengerCriteriaArray.put(infantPassenger);
        }

        // Construct search criteria array
        JSONArray searchCriteriaArray = new JSONArray();

        // ONE WAY
        JSONObject searchCriteria1 = new JSONObject();
        searchCriteria1.put("departureDate", flightSearchDto.departureDate());
        searchCriteria1.put("from", flightSearchDto.from());
        searchCriteria1.put("to", flightSearchDto.to());
        searchCriteriaArray.put(searchCriteria1);

        // TWO WAY
        if (flightSearchDto.returnDate() != null) {
            JSONObject searchCriteria2 = new JSONObject();
            searchCriteria2.put("departureDate", flightSearchDto.returnDate());
            searchCriteria2.put("from", flightSearchDto.to());
            searchCriteria2.put("to", flightSearchDto.from());
            searchCriteriaArray.put(searchCriteria2);
        }

        // CABIN SELECTION
        if (flightSearchDto.preferredCabin() != null) {
            var modifiers = new JSONObject();
            modifiers.put("allowDirectAccess", true);
            modifiers.put("maxConnectionTime", 24);

            var preferredCabins = new JSONArray();
            preferredCabins.put(flightSearchDto.preferredCabin());

            log.info("PREFERRED CABIN : {}", flightSearchDto.preferredCabin());
            modifiers.put("preferredCabins", preferredCabins);
            searchCriteria1.put("modifiers", modifiers);

        }

        // INCLUDE ALL FLIGHT DETAILS
        var searchModifiers = new JSONObject();
        searchModifiers.put("includeFlightDetails", true);

        // Construct main request body
        JSONObject mainRequestBody = new JSONObject();
        mainRequestBody.put("offersPerPage", 5);
        mainRequestBody.put("passengerCriteria", passengerCriteriaArray);
        mainRequestBody.put("searchCriteria", searchCriteriaArray);
        mainRequestBody.put("searchModifiers", searchModifiers);

        log.info("REQUEST : {}", mainRequestBody);

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
        log.info("[MAPPING RESPONSE] : {}", flightSearchResponse);
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


        List<PassengerCriteria> passengerCriteriaList = new ArrayList<>();

        for (int i = 0; i < passengerCriteriaArray.length(); i++) {

            JSONObject passengerObj = passengerCriteriaArray.getJSONObject(i);

            var passengerCriteria = new PassengerCriteria(
                    passengerObj.getString("value"),
                    passengerObj.optInt("number")
            );
            passengerCriteriaList.add(passengerCriteria);
        }

        FlightSearchResponse successResponse = FlightSearchResponse.builder()
                .status(200)
                .message("success")
                .airInfo(airInfoList)
                .fairInfo(fairInfo)
                .priceInfo(priceInfoResponse)
                .passengerInfo(passengerCriteriaList)
                .build();

        return successResponse;
    }


    // SEARCH  PRICES ✅
    public AirPriceSolution searchFlightWithPrice(FlightPriceSearchDto flightPriceSearchDto) {
        JSONObject mainRequestBody = new JSONObject();

        // Construct passengerCriteria array
        JSONArray passengerCriteriaArray = new JSONArray();
        if (flightPriceSearchDto.getPassengerInfo() != null) {
            for (PassengerCriteria passengerCriteria : flightPriceSearchDto.getPassengerInfo()) {
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
                .bookingInfo(List.of(bookingInfoResponse))
                .fareInfo(List.of(fareInfo))
                .build();

        // CONSTRUCTING AIRPRICE RESPONSE

        AirPriceInfoResponse airPriceInfoResponse = AirPriceInfoResponse
                .builder()
                .key(insideKey)
                .airSegment(airInfoResponseList)
                .hostToken(hostTokenResponse)
                .airPricingInfo(List.of(airPricingObj))
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

    // BOOK FLIGHT ✅

    public BookingResponse bookFlight(BookingRequestDto bookingRequestDto) {
        JSONObject mainRequestBody = new JSONObject();

        // Construct travelers array
        JSONArray travelersArray = new JSONArray();
        for (TravelersDto traveler : bookingRequestDto.getTravelers()) {
            JSONObject travelerObject = new JSONObject();
            travelerObject.put("firstName", traveler.getFirstName());
            travelerObject.put("lastName", traveler.getLastName());
            travelerObject.put("gender", traveler.getGender());
//            travelerObject.put("email", traveler.getEmail());
            travelerObject.put("dateOfBirth", traveler.getDateOfBirth());

            // Construct phoneNumber array for each traveler
            JSONArray phoneNumberArray = new JSONArray();
            for (TravelersDto.PhoneNumber phoneNumber : traveler.getPhoneNumber()) {
                JSONObject phoneNumberObject = new JSONObject();
                phoneNumberObject.put("phoneNumber", phoneNumber.getPhoneNumber());
                phoneNumberObject.put("areCode", phoneNumber.getAreCode());
                phoneNumberObject.put("cityCode", phoneNumber.getCityCode());
                phoneNumberObject.put("countryArea", phoneNumber.getCountryArea());
                phoneNumberArray.put(phoneNumberObject);
            }
            travelerObject.put("phoneNumber", phoneNumberArray);

            // Construct address object for each traveler
            JSONObject addressObject = new JSONObject();
            TravelersDto.Address address = traveler.getAddress();
            addressObject.put("city", address.getCity());
            addressObject.put("country", address.getCountry());
            addressObject.put("street", address.getStreet());
            addressObject.put("postalCode", address.getPostalCode());
            travelerObject.put("address", addressObject);

            travelerObject.put("travelerType", traveler.getTravelerType());
            travelersArray.put(travelerObject);
        }
        mainRequestBody.put("travelers", travelersArray);

        log.info("AIR PRIC SOLUTION", bookingRequestDto.getAirPriceInfo());


        mainRequestBody.put("airPricingSolution", bookingRequestDto.getAirPriceInfo());

        // Construct formOfPayment array
        JSONArray formOfPaymentArray = new JSONArray();
        for (FormOfPaymentDto formOfPayment : bookingRequestDto.getFormOfPayment()) {
            JSONObject formOfPaymentObject = new JSONObject();
            formOfPaymentObject.put("isCash", formOfPayment.getIsCash());
            formOfPaymentObject.put("key", formOfPayment.getKey());
            formOfPaymentArray.put(formOfPaymentObject);
        }

        mainRequestBody.put("formOfPayment", formOfPaymentArray);

        log.info("BOOKING FULL REQUEST : {}", mainRequestBody);

        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(TRAVELPORT_URL + "/book")
                .setBody(mainRequestBody.toString());

        JSONObject flightBookingResponse = asyncHttp.sendRequest(requestBody);

        if (flightBookingResponse.has("status")) {
            return BookingResponse.builder()
                    .status(400)
                    .message(flightBookingResponse.getString("details"))
                    .build();

        }

        String transactionId = flightBookingResponse.optString("transactionId");

        JSONObject universalRecordObj = flightBookingResponse.getJSONObject("universalRecord");

        BookingResponse.BookingDetails bookingDetails = BookingResponse.BookingDetails.builder()
                .pnrCode(universalRecordObj.optString("locatorCode"))
                .transactionId(transactionId)
                .version(universalRecordObj.optInt("version"))
                .status(universalRecordObj.optString("status"))
                .build();

        return BookingResponse.builder()
                .status(200)
                .message("success")
                .bookingInfo(List.of(bookingDetails))
                .build();


    }

    // GET BOOKING BY PNR CODE

    public FlightByPnrCodeResponse findFlightByPnr(String pnrCode) {

        var requestBody = new JSONObject();
        requestBody.put("locatorCode", pnrCode);

        log.info("RETRIEVE FLIGHT BY PNR : {}", requestBody);

        RequestBuilder requestBuilder = new RequestBuilder("POST")
                .setUrl(TRAVELPORT_URL + "/retrieve")
                .setBody(requestBody.toString());

        JSONObject flightRetrieveResponse = asyncHttp.sendRequest(requestBuilder);

        log.info("RETRIEVE FLIGHT RESPONSE : {}", flightRetrieveResponse);

        // MAP ERROR RESPONSE

        if (flightRetrieveResponse.has("status")) {

            return FlightByPnrCodeResponse.builder()
                    .status(400)
                    .message(flightRetrieveResponse.optString("details"))
                    .build();
        }
        // MAP SUCCESS RESPONSE

        JSONObject universalRecord = flightRetrieveResponse.getJSONObject("universalRecord");

        String pnr = universalRecord.optString("locatorCode");
        String bookingStatus = universalRecord.optString("status");
        int version = universalRecord.optInt("version");

        JSONArray bookingTraveler = universalRecord.getJSONArray("bookingTraveler");

        // MAP TRAVELERS TO LIST
        List<TravelerResponse> travelerResponseList = mapperService.mapToTravelerResponse(bookingTraveler);

        // MAP AIR INFO

        JSONArray airReservation = universalRecord.getJSONArray("airReservation");

        JSONObject airSegment = airReservation.getJSONObject(0).getJSONArray("airSegment").getJSONObject(0);


        AirInfoResponse airInfoResponse = mapperService.mapToAirInfo(airSegment);

        log.info("AIR RESERVATION: {}", airReservation);

//        JSONArray airPricingInfoObj = airReservation.getJSONArray("airPricingInfoObj");

        JSONArray airPricingInfoObj = airReservation.getJSONObject(0).getJSONArray("airPricingInfo");


        JSONArray bookingInfo = airPricingInfoObj.getJSONObject(0).getJSONArray("bookingInfo");

//        JSONObject airPriceInfo = airPricingInfoObj.getJSONObject(0);

        PriceInfoResponse priceInfoResponse = mapperService.mapToPriceInfo(airPricingInfoObj);

        // map the bookin info
        BookingInfoResponse bookingInfoResponse = mapperService.mapToBookingInfo(bookingInfo.getJSONObject(0));

        // map the passengerType

        JSONArray passengerType = airPricingInfoObj.getJSONObject(0).getJSONArray("passengerType");

        log.info("PASSENGER INFO : {}", passengerType);

        List<PassengerType> passengerTypeList = mapperService.mapPassengerTypes(passengerType);

//        PriceInfoResponse priceInfoResponse = mapperService.mapToPriceInfo(airPricingInfoObj);

        return FlightByPnrCodeResponse.builder()
                .status(200)
                .message("success")
                .bookingStatus(bookingStatus)
                .pnrCode(pnr)
                .version(version)
                .travelers(travelerResponseList)
                .bookingInfo(List.of(bookingInfoResponse))
                .priceInfo(priceInfoResponse)
                .airInfo(List.of(airInfoResponse))
                .passengerInfo(passengerTypeList)
                .build();
    }
}
