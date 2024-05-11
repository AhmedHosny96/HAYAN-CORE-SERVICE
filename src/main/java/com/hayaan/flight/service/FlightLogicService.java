package com.hayaan.flight.service;


import com.hayaan.config.AsyncHttpConfig;
import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.dto.AirportListResp;
import com.hayaan.flight.object.dto.PaymentStageDto;
import com.hayaan.flight.object.dto.TicketHistoryDto;
import com.hayaan.flight.object.dto.booking.BookingRequestDto;
import com.hayaan.flight.object.dto.booking.BookingResponse;
import com.hayaan.flight.object.dto.booking.TravelersDto;
import com.hayaan.flight.object.dto.flight.AirInfoResponse;
import com.hayaan.flight.object.dto.flight.FlightSearchDto;
import com.hayaan.flight.object.dto.flight.FlightSearchResponse;
import com.hayaan.flight.object.dto.flight.OnwardJourneyResponse;
import com.hayaan.flight.object.entity.Airline;
import com.hayaan.flight.object.entity.Airport;
import com.hayaan.flight.object.entity.Payment;
import com.hayaan.flight.repo.AirlineRepository;
import com.hayaan.flight.repo.AirportRepository;
import com.hayaan.flight.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.RequestBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.hayaan.flight.object.dto.booking.BookingResponse.BookingDetails;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightLogicService {

    @Value("${flightLogic.endpoint}")
    private String FLIGHT_LOGIC_API;

    private final AsyncHttpConfig asyncHttp;

    private final MapperService mapperService;

    private final TransactionService transactionService;

    private final PaymentRepository paymentRepository;
    private final AirportRepository airportRepository;
    private final AirlineRepository airlineRepository;


    // FLIGHT SEARCH ONE WAY / TWO WAY

    @Cacheable("flightLogicSearch")
    public FlightSearchResponse searchFlight(FlightSearchDto flightSearchDto) {


        var mainSearchRequest = new JSONObject();

        mainSearchRequest.put("operation", "FlightAvailability");
        mainSearchRequest.put("journeyType", flightSearchDto.returnDate() == null ? "OneWay" : "Return");
        mainSearchRequest.put("departureDate", flightSearchDto.departureDate().toString());
        if (flightSearchDto.returnDate() != null) {
            mainSearchRequest.put("returnDate", flightSearchDto.returnDate().toString());
        }
        mainSearchRequest.put("airportOriginCode", flightSearchDto.from());
        mainSearchRequest.put("airportDestinationCode", flightSearchDto.to());
        mainSearchRequest.put("class", flightSearchDto.preferredCabin());
        mainSearchRequest.put("airlineCode", flightSearchDto.airLineCode() != null ? flightSearchDto.airLineCode() : "");
        mainSearchRequest.put("adults", flightSearchDto.noOfAdult());
        mainSearchRequest.put("childs", flightSearchDto.noOfChildren());
        mainSearchRequest.put("infants", flightSearchDto.noOfInfant());

        log.info("FLIGHT LOGIC SEARCH REQUEST : {}", mainSearchRequest);

        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(FLIGHT_LOGIC_API)
                .setBody(mainSearchRequest.toString());

        JSONObject flightSearchResponse = asyncHttp.sendRequest(requestBody);

        if (flightSearchResponse.has("Errors")) {
            var errorObject = flightSearchResponse.optJSONObject("Errors");

            return FlightSearchResponse.builder()
                    .status(400)
                    .message(errorObject.optString("ErrorMessage"))
                    .build();

        }

        log.info("FLIGHT LOGIC SEARCH RESPONSE : {}");

        JSONObject airSearchResponseObject = flightSearchResponse.optJSONObject("AirSearchResponse");

        if (airSearchResponseObject == null) {
            var errorObject = flightSearchResponse.optJSONObject("Errors");

            return FlightSearchResponse.builder()
                    .status(400)
                    .message("Error occured on flight logic service , Kindly contact the system developer")
                    .build();

        }


        JSONArray fareItineraries = airSearchResponseObject.optJSONObject("AirSearchResult").optJSONArray("FareItineraries");

        String sessionId = airSearchResponseObject.optString("session_id");

        List<AirInfoResponse> airInfoResponses = mapperService.mapToFareItineraries(fareItineraries, sessionId, flightSearchDto.from(), flightSearchDto.to());

//        OnwardJourneyResponse onwardJourneyResponse = mapperService.mapOnWardFlight(fareItineraries, sessionId, flightSearchDto.from(), flightSearchDto.to());


        var onWardJourney = new OnwardJourneyResponse();
        onWardJourney.setAirInfo(airInfoResponses);

        return FlightSearchResponse.builder()
                .status(200)
                .message("success")
//                .sessionId(sessionId)
                .onwardFlight(List.of(onWardJourney))
                .returnFlight(List.of())
//                .airInfo(airInfoResponses)
                .build();
    }

    // BOOK FLIGHT
    public BookingResponse bookFlight(BookingRequestDto bookingRequestDto) {

        var mainBookingRequest = new JSONObject();

        var flightBookingInfo = new JSONObject()
                .put("flight_session_id", bookingRequestDto.getAirPriceInfo().get(0).getAirSegment().get(0).getSessionId())
                .put("fare_source_code", bookingRequestDto.getAirPriceInfo().get(0).getAirSegment().get(0).getFareSourceCode())
                .put("IsPassportMandatory", "false")
                .put("fareType", "Public")
                .put("areaCode", bookingRequestDto.getTravelers().get(0).getPhoneNumber().get(0).getCountryArea())
                .put("countryCode", "251");


        JSONObject paxInfo = new JSONObject();
        paxInfo.put("clientRef", "BOOK001");
        paxInfo.put("postCode", bookingRequestDto.getTravelers().get(0).getAddress().getPostalCode());
        paxInfo.put("customerEmail", bookingRequestDto.getTravelers().get(0).getEmail());
        paxInfo.put("customerPhone", bookingRequestDto.getTravelers().get(0).getPhoneNumber().get(0).getPhoneNumber());
        paxInfo.put("bookingNote", "test");

        JSONObject paxDetails = new JSONObject();

        JSONArray adultArray = new JSONArray();
        for (TravelersDto adult : bookingRequestDto.getTravelers()) {
            JSONObject adultJson = new JSONObject();
            adultJson.put("title", adult.getTitle());
            adultJson.put("firstName", adult.getFirstName());
            adultJson.put("lastName", adult.getLastName());
            adultJson.put("passportNo", adult.getIdNo()); // Sample passport number, you may need to update this
            adultJson.put("nationality", adult.getNationality());
            adultJson.put("passportIssueCountry", adult.getNationality());
            adultJson.put("dob", adult.getDateOfBirth().toString());
            adultJson.put("passportExpiryDate", "2023-12-25"); // Sample expiry date, you may need to update this
            adultArray.put(adultJson);
        }

        JSONArray childArray = new JSONArray();
        JSONArray infantArray = new JSONArray();

        paxDetails.put("adult", adultArray);
        paxDetails.put("child", childArray); // Include the empty array for child passengers
        paxDetails.put("infant", infantArray); // Include the empty array for infant passengers

        paxInfo.put("paxDetails", paxDetails);

        // Add flight booking info and pax info to main booking request
        mainBookingRequest.put("operation", "BookFlight");
        mainBookingRequest.put("flightBookingInfo", flightBookingInfo);
        mainBookingRequest.put("paxInfo", paxInfo);

        log.info("FLIGHT LOGIC BOOKING REQUEST : {}", mainBookingRequest);

        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(FLIGHT_LOGIC_API)
                .setBody(mainBookingRequest.toString());

        JSONObject flightBookingResponse = asyncHttp.sendRequest(requestBody);

        if (flightBookingResponse.has("Errors")) {
            var errorObject = flightBookingResponse.optJSONObject("Errors");

            return BookingResponse.builder()
                    .status(400)
                    .message(errorObject.optString("ErrorMessage"))
                    .build();

        }

        JSONObject bookFlightResult = flightBookingResponse.optJSONObject("BookFlightResponse").optJSONObject("BookFlightResult");

        log.info("FLIGHT LOGIC BOOKING RESPONSE : {}", flightBookingResponse);

        if (bookFlightResult.has("Errors") && bookFlightResult.optJSONObject("Errors") != null) {
            var errorsObject = bookFlightResult.optJSONObject("Errors");


            JSONObject errorObject = errorsObject.optJSONObject("Error");

            return BookingResponse.builder()
                    .status(400)
                    .message(errorObject.optString("ErrorMessage"))
                    .build();

        }

        if (bookFlightResult == null) {
//            var errorObject = bookFlightResult.optJSONObject("Errors");
            return BookingResponse.builder()
                    .status(400)
                    .message("Error occurred on flight logic service, kindly contact the system developer")
                    .build();

        }

        String pnr = bookFlightResult.optString("UniqueID");

        BookingDetails bookingDetails = BookingDetails.builder()
                .status(bookFlightResult.optString("Status"))
                .pnrCode(pnr)
//                .timeLimit(LocalDateTime.parse(bookFlightResult.optString("TktTimeLimit"), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();

        // GET TICKET FULL DETAILS

        JSONObject tripDetailsResponse = fetchTripDetails(pnr);

        log.info("TRIP DETAILS RESPONSE : {}", tripDetailsResponse);

        JSONObject reservationItem = tripDetailsResponse.optJSONArray("ReservationItems").optJSONObject(0).optJSONObject("ReservationItem");

        JSONObject airPriceItem = tripDetailsResponse.optJSONObject("ItineraryPricing").optJSONObject("TotalFare");
        JSONObject customerInfo = tripDetailsResponse.optJSONArray("CustomerInfos").optJSONObject(0).optJSONObject("CustomerInfo");

        TicketHistoryDto ticketHistory = TicketHistoryDto.builder()
                .pnr(pnr)
                .origin(reservationItem.optString("DepartureAirportLocationCode"))
                .destination(reservationItem.optString("ArrivalAirportLocationCode"))
                .airlineId(reservationItem.optString("OperatingAirlineCode"))
                .travelDate(LocalDateTime.parse(reservationItem.optString("DepartureDateTime")))
                .ticketAmount(Double.valueOf(airPriceItem.optString("Amount")))
                .firstName(customerInfo.optString("PassengerFirstName"))
                .middleName("")

                .lastName(customerInfo.optString("PassengerLastName"))
                .documentIdNumber(customerInfo.optString("PassportNumber"))
                .phoneNumber(customerInfo.optString("PhoneNumber"))
                .email(customerInfo.optString("email"))
//                .expireDate(bookingDetails.getTimeLimit())
                .build();

        transactionService.saveTicket(ticketHistory);

        PaymentStageDto paymentStageDto = PaymentStageDto.builder()
                .amount(ticketHistory.getTicketAmount())
                .pnr(bookingDetails.getPnrCode())
                .build();

        transactionService.stagePayment(paymentStageDto);

        BookingResponse bookingResponse = BookingResponse.builder()
                .status(200)
                .message("success")
                .bookingInfo(List.of(bookingDetails))
                .build();

        return bookingResponse;
    }
    // retrieve trip details

    public JSONObject fetchTripDetails(String pnr) {

        var tripDetailsRequest = new JSONObject();
        tripDetailsRequest.put("operation", "TripDetails");
        tripDetailsRequest.put("UniqueID", pnr);

        log.info("FLIGHT LOGIC TRIP DETAILS REQUEST : {}", tripDetailsRequest);

        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(FLIGHT_LOGIC_API)
                .setBody(tripDetailsRequest.toString());

        JSONObject tripDetailsResponse = asyncHttp.sendRequest(requestBody);
//
        JSONObject itineraryInfoResp = tripDetailsResponse.optJSONObject("TripDetailsResponse").optJSONObject("TripDetailsResult")
                .optJSONObject("TravelItinerary").optJSONObject("ItineraryInfo");

//        JSONObject travelItinerary = tripDetailsResponse.optJSONObject("TripDetailsResult")
//                .optJSONObject("TravelItinerary");

//        String bookingStatus = travelItinerary.optString("BookingStatus");
//
//
//        JSONObject reservationItem = itineraryInfoResp.optJSONArray("ReservationItems").optJSONObject(0).optJSONObject("ReservationItem");
//
//        JSONObject airPriceItem = itineraryInfoResp.optJSONObject("ItineraryPricing").optJSONObject("TotalFare");
//        JSONObject customerInfo = itineraryInfoResp.optJSONArray("CustomerInfos").optJSONObject(0).optJSONObject("CustomerInfo");
//
//
//        PriceInfoResponse priceInfoResponse = mapperService.mapToPriceInfo(new JSONArray(airPriceItem));

        return itineraryInfoResp;


    }


//    public FlightByPnrCodeResponse fetchTripDetails(String pnr) {
//
//        var tripDetailsRequest = new JSONObject();
//        tripDetailsRequest.put("operation", "TripDetails");
//        tripDetailsRequest.put("UniqueID", pnr);
//
//        log.info("FLIGHT LOGIC TRIP DETAILS REQUEST : {}", tripDetailsRequest);
//
//        RequestBuilder requestBody = new RequestBuilder("POST")
//                .setUrl(FLIGHT_LOGIC_API)
//                .setBody(tripDetailsRequest.toString());
//
//        JSONObject tripDetailsResponse = asyncHttp.sendRequest(requestBody);
//
//        JSONObject itineraryInfoResp = tripDetailsResponse.optJSONObject("TripDetailsResponse").optJSONObject("TripDetailsResult")
//                .optJSONObject("TravelItinerary").optJSONObject("ItineraryInfo");
//
//        JSONObject travelItinerary = tripDetailsResponse.optJSONObject("TripDetailsResult")
//                .optJSONObject("TravelItinerary");
//
//        String bookingStatus = travelItinerary.optString("BookingStatus");
//
//
//        JSONObject reservationItem = itineraryInfoResp.optJSONArray("ReservationItems").optJSONObject(0).optJSONObject("ReservationItem");
//
//        JSONObject airPriceItem = itineraryInfoResp.optJSONObject("ItineraryPricing").optJSONObject("TotalFare");
//        JSONObject customerInfo = itineraryInfoResp.optJSONArray("CustomerInfos").optJSONObject(0).optJSONObject("CustomerInfo");
//
//
//        PriceInfoResponse priceInfoResponse = mapperService.mapToPriceInfo(new JSONArray(airPriceItem));
//
//
//        return FlightByPnrCodeResponse.builder()
//                .status(200)
//                .message("success")
//                .bookingStatus(bookingStatus)
//                .priceInfo(priceInfoResponse)
//                .build();
//
//
//    }

    // confirm ticket

    public CustomResponse confirmTicket(String pnr) {
        // check the payment
        Optional<Payment> byPnr = paymentRepository.findByPnr(pnr);

        if (!byPnr.isPresent()) {
            return new CustomResponse(400, "Pnr not found");
        }

        Payment payment = byPnr.get();

        if (payment.getPaymentStatus() == 0) {
            return new CustomResponse(400, "Ticket cannot be confirmed please pay the ticket using payment gateways");
        }

        if (payment.getPaymentStatus() == 1 || payment.getPaymentStatus() == 3) {
            return new CustomResponse(400, "Ticket payment is still pending or failed");
        }

        var orderTicketRequest = new JSONObject()
                .put("operation", "OrderTicket")
                .put("UniqueID", pnr);

        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(FLIGHT_LOGIC_API)
                .setBody(orderTicketRequest.toString());

        JSONObject confirmTicketResponse = asyncHttp.sendRequest(requestBody);

        log.info("CONFIRMATION RESPONSE : {}", confirmTicketResponse);
        if (confirmTicketResponse.has("Errors") && confirmTicketResponse.optJSONObject("Errors") != null) {

            String errorMessage = confirmTicketResponse.optJSONObject("Errors").optString("ErrorMessage");
            return new CustomResponse(400, errorMessage);
        }

        return new CustomResponse(200, "Ticket successfully confirmed");
    }

    // AIRPORT LIST API
    @Cacheable("GetAirports")
    public AirportListResp getAllAirports() {
        var airPortListRequest = new JSONObject()
                .put("operation", "AirportList");

        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(FLIGHT_LOGIC_API)
                .setBody(airPortListRequest.toString());

        JSONObject airPortListResponse = asyncHttp.sendRequest(requestBody);

        JSONArray jsonArray = airPortListResponse.getJSONArray("airportlist");

        // Convert JSONArray to List<Airport>
        List<Airport> airportList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonAirport = jsonArray.getJSONObject(i);
            Airport airport = new Airport();
            airport.setAirportCode(jsonAirport.getString("AirportCode"));
            airport.setAirportName(jsonAirport.getString("AirportName"));
            airport.setCountry(jsonAirport.getString("Country"));
//            airport.setLatitude(jsonAirport.getDouble("Latitude"));
            airport.setCity(jsonAirport.getString("City"));
//            airport.setLongitude(jsonAirport.getDouble("Longitude"));
            airportList.add(airport);
        }

        return AirportListResp.builder()
                .status(200)
                .message("success")
                .airPortList(Collections.singletonList(airportList))
                .airLineList(List.of())
                .build();
    }

    @Cacheable("GetAirlines")
    public AirportListResp getAllAirlines() {

        var airPortListRequest = new JSONObject()
                .put("operation", "AirlineList");

        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(FLIGHT_LOGIC_API)
                .setBody(airPortListRequest.toString());

        JSONObject airPortListResponse = asyncHttp.sendRequest(requestBody);

        JSONArray jsonArray = airPortListResponse.getJSONArray("airlines");


        List<Airline> airportLists = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonAirport = jsonArray.getJSONObject(i);
            Airline airport = new Airline();
            airport.setAirLineCode(jsonAirport.getString("AirLineCode"));
            airport.setAirLineName(jsonAirport.getString("AirLineName"));
            airportLists.add(airport);
        }

//        airlineRepository.saveAll(airportLists);
//
        // Convert JSONArray to List
        List<Object> airportList = jsonArray.toList();


        return AirportListResp.builder()
                .status(200)
                .message("success")
                .airLineList(airportList)
                .airPortList(List.of())
                .build();
    }

    public CustomResponse cancelFlight(String pnrCode) {

        var requestBody = new JSONObject();
        requestBody.put("UniqueID", pnrCode);
        requestBody.put("operation", "CancelTrip");

        log.info("FLIGHT LOGIC CANCEL FLIGHT REQUEST: {}", requestBody);

        RequestBuilder requestBuilder = new RequestBuilder("POST")
                .setUrl(FLIGHT_LOGIC_API)
                .setBody(requestBody.toString());

        JSONObject cancelFlightResponse = asyncHttp.sendRequest(requestBuilder);

        log.info("CANCEL FLIGHT RESPONSE : {}", cancelFlightResponse);

        // MAP ERROR RESPONSE

        if (cancelFlightResponse.has("Errors") && cancelFlightResponse.optJSONObject("Errors") != null) {

            String errorMessage = cancelFlightResponse.optJSONObject("Errors").optString("ErrorMessage");
            return new CustomResponse(400, errorMessage);
        }

        return new CustomResponse(200, "Flight Cancelled successfully");
    }


}
