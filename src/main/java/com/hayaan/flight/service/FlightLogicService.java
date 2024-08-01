package com.hayaan.flight.service;


import com.hayaan.config.AsyncHttpConfig;
import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.FlightType;
import com.hayaan.flight.object.dto.*;
import com.hayaan.flight.object.dto.booking.*;
import com.hayaan.flight.object.dto.flight.AirInfoResponse;
import com.hayaan.flight.object.dto.flight.FlightSearchDto;
import com.hayaan.flight.object.dto.flight.FlightSearchResponse;
import com.hayaan.flight.object.dto.flight.PriceInfoResponse;
import com.hayaan.flight.object.entity.*;
import com.hayaan.flight.repo.AirlineRepository;
import com.hayaan.flight.repo.AirportRepository;
import com.hayaan.flight.repo.PaymentRepository;
import com.hayaan.flight.repo.TicketHistoryRepo;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightLogicService {

    @Value("${flightLogic.endpoint}")
    private String FLIGHT_LOGIC_API;

    private final AsyncHttpConfig asyncHttp;

    private final MapperService mapperService;

    private final TicketHistoryRepo ticketHistoryRepo;

    private final AirDetailsService airDetailsService;
    private final CommissionService commissionService;

    private final FlightSearchRequestContext flightSearchRequestContext;

    private final TransactionService transactionService;

    private final CurrencyService currencyService;

    private final PaymentRepository paymentRepository;
    private final AirportRepository airportRepository;
    private final AirlineRepository airlineRepository;


    // FLIGHT SEARCH ONE WAY / TWO WAY

    //    @Cacheable("flightLogicSearch")
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

        flightSearchRequestContext.setCurrentRequest(flightSearchDto);

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

        FlightSearchResponse searchResponse = mapperService.mapToFareItineraries(fareItineraries, sessionId, flightSearchDto.from(), flightSearchDto.to());

//        OnwardJourneyResponse onwardJourneyResponse = mapperService.mapOnWardFlight(fareItineraries, sessionId, flightSearchDto.from(), flightSearchDto.to());


//        var onWardJourney = new OnwardJourneyResponse();
//        onWardJourney.setAirInfo(airInfoResponses);

        return FlightSearchResponse.builder()
                .status(200)
                .message("success")
                .onwardFlight(searchResponse.getOnwardFlight())
                .returnFlight(searchResponse.getReturnFlight())
//                .airInfo(airInfoResponses)
                .build();
    }

    // BOOK FLIGHT
    public BookingResponse bookFlight(BookingRequestDto bookingRequestDto) {
        log.info("DTO : {}", bookingRequestDto);

        JSONObject mainBookingRequest = new JSONObject();
        JSONObject flightBookingInfo = new JSONObject()
                .put("flight_session_id", bookingRequestDto.getAirPriceInfo().get(0).getAirSegment().get(0).getSessionId())
                .put("fare_source_code", bookingRequestDto.getAirPriceInfo().get(0).getAirSegment().get(0).getFareSourceCode())
                .put("IsPassportMandatory", "false")
                .put("fareType", "Public")
                .put("areaCode", bookingRequestDto.getTravelers().get(0).getPhoneNumbers().get(0).getAreCode())
                .put("countryCode", "251");

        JSONObject paxInfo = new JSONObject();
        paxInfo.put("clientRef", "BOOK001");
        paxInfo.put("postCode", bookingRequestDto.getTravelers().get(0).getAddress().getPostalCode());
        paxInfo.put("customerEmail", bookingRequestDto.getTravelers().get(0).getEmail());
        paxInfo.put("customerPhone", bookingRequestDto.getTravelers().get(0).getPhoneNumbers().get(0).getPhoneNumber());
        paxInfo.put("bookingNote", "test");

        JSONObject paxDetails = new JSONObject();
        JSONArray adultArray = new JSONArray();
        JSONArray childArray = new JSONArray();
        JSONArray infantArray = new JSONArray();

        List<Passenger> passengers = new ArrayList<>();

        for (TravelersDto traveler : bookingRequestDto.getTravelers()) {
            JSONObject travelerJson = new JSONObject();
            travelerJson.put("title", traveler.getTitle());
            travelerJson.put("firstName", traveler.getFirstName());
            travelerJson.put("lastName", traveler.getLastName());
            travelerJson.put("passportNo", traveler.getIdNo());
            travelerJson.put("nationality", traveler.getNationality());
            travelerJson.put("passportIssueCountry", traveler.getNationality());
            travelerJson.put("dob", traveler.getDateOfBirth().toString());
            travelerJson.put("passportExpiryDate", "2023-12-25");

            Passenger passenger = Passenger.builder()
                    .firstName(traveler.getFirstName())
                    .middleName(traveler.getMiddleName())
                    .lastName(traveler.getLastName())
                    .email(traveler.getEmail())
                    .phoneNumber(traveler.getPhoneNumbers().get(0).getPhoneNumber())
                    .document("Passport")
                    .documentIdNumber(traveler.getIdNo())
                    .dateOfIssue(LocalDateTime.now()) // Replace with actual issue date
                    .expiryDate(LocalDateTime.of(2023, 12, 25, 0, 0)) // Replace with actual expiry date
                    .passengerType(traveler.getTravelerType().getCode())
                    .build();

            passengers.add(passenger);

            switch (traveler.getTravelerType().getCode()) {
                case "ADULT":
                    adultArray.put(travelerJson);
                    break;
                case "CHILD":
                    childArray.put(travelerJson);
                    break;
                case "INFANT":
                    infantArray.put(travelerJson);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown traveler type: " + traveler.getTravelerType().getCode());
            }
        }

        paxDetails.put("adult", adultArray);
        paxDetails.put("child", childArray);
        paxDetails.put("infant", infantArray);

        paxInfo.put("paxDetails", paxDetails);

        mainBookingRequest.put("operation", "BookFlight");
        mainBookingRequest.put("flightBookingInfo", flightBookingInfo);
        mainBookingRequest.put("paxInfo", paxInfo);

        log.info("FLIGHT LOGIC BOOKING REQUEST : {}", mainBookingRequest);

        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(this.FLIGHT_LOGIC_API)
                .setBody(mainBookingRequest.toString());

        JSONObject flightBookingResponse = this.asyncHttp.sendRequest(requestBody);

        if (flightBookingResponse.has("Errors")) {
            JSONObject errorObject = flightBookingResponse.optJSONObject("Errors");
            return BookingResponse.builder()
                    .status(400)
                    .message(errorObject.optString("ErrorMessage"))
                    .build();
        }

        JSONObject bookFlightResult = flightBookingResponse.optJSONObject("BookFlightResponse").optJSONObject("BookFlightResult");
        log.info("FLIGHT LOGIC BOOKING RESPONSE : {}", flightBookingResponse);

        if (bookFlightResult.has("Errors") && bookFlightResult.optJSONObject("Errors") != null) {
            JSONObject errorsObject = bookFlightResult.optJSONObject("Errors");
            JSONObject errorObject = errorsObject.optJSONObject("Error");
            return BookingResponse.builder()
                    .status(400)
                    .message(errorObject.optString("ErrorMessage"))
                    .build();
        }

        if (bookFlightResult == null) {
            return BookingResponse.builder()
                    .status(400)
                    .message("Error occurred on flight logic service, kindly contact the system developer")
                    .build();
        }

        String pnr = bookFlightResult.optString("UniqueID");
        BookingResponse.BookingDetails bookingDetails = BookingResponse.BookingDetails.builder()
                .status(bookFlightResult.optString("Status"))
                .pnrCode(pnr)
                .build();

        JSONObject tripDetailsResponse = fetchTripDetails(pnr);
        log.info("TRIP DETAILS RESPONSE : {}", tripDetailsResponse);


//        JSONObject reservationItem = tripDetailsResponse.optJSONArray("ReservationItems").optJSONObject(0).optJSONObject("ReservationItem");

        JSONObject itineraryInfo = tripDetailsResponse.optJSONObject("ItineraryInfo");

        JSONArray reservationItems = itineraryInfo.optJSONArray("ReservationItems");

        JSONObject airPriceItem = itineraryInfo.optJSONObject("ItineraryPricing").optJSONObject("TotalFare");
        JSONObject customerInfo = itineraryInfo.optJSONArray("CustomerInfos").optJSONObject(0).optJSONObject("CustomerInfo");

        if (reservationItems != null && reservationItems.length() > 0) {

            String origin = tripDetailsResponse.optString("Origin");
            String destination = tripDetailsResponse.optString("Destination");


            LocalDateTime departureDateTime = null;
            LocalDateTime arrivalDateTime = null;
            String goFlightNumber = "";
            String returnFlightNumber = "";

            for (int i = 0; i < reservationItems.length(); i++) {
                JSONObject reservationItem = reservationItems.optJSONObject(i).optJSONObject("ReservationItem");

                if (reservationItem != null) {
                    LocalDateTime currentDepartureDateTime = LocalDateTime.parse(reservationItem.optString("DepartureDateTime"));
                    LocalDateTime currentArrivalDateTime = LocalDateTime.parse(reservationItem.optString("ArrivalDateTime"));
                    String currentFlightNumber = reservationItem.optString("MarketingAirlineCode").trim() + reservationItem.optString("FlightNumber").trim();

                    // Set origin and departureDateTime for the first leg of the journey
                    if (i == 0) {
                        departureDateTime = currentDepartureDateTime;
                        goFlightNumber = currentFlightNumber;
                    }

                    // Update destination and arrivalDateTime for the last leg of the journey
                    if (i == reservationItems.length() - 1) {
                        arrivalDateTime = currentArrivalDateTime;
                        returnFlightNumber = currentFlightNumber;
                    }
                }
            }

            Double originalTicketPrice = Double.valueOf(airPriceItem.optString("Amount"));


            // calculate commission

            boolean isInternationalFlight = airDetailsService.isInternationalFlight(origin, destination);

            FlightType flightType = isInternationalFlight ? FlightType.International : FlightType.Domestic;

            double commission = commissionService.calculateCommission(originalTicketPrice, flightType);

            // convert currency

            double rate = currencyService.convertCurrency("USD", "ETB");

            double totalPrice = airDetailsService.roundNumber(originalTicketPrice * rate);

            TicketHistory ticketHistory = TicketHistory.builder()
                    .pnr(pnr)
                    .origin(origin)
                    .destination(destination)
                    .departureDateTime(departureDateTime)
                    .arrivalDateTime(arrivalDateTime)
                    .flightNumber(goFlightNumber)
//                    .returnFlightNumber(returnFlightNumber)
                    .ticketAmount(totalPrice)
                    .commissionAmount(commission)
                    .totalAmount(totalPrice + commission)
                    .totalNoOfPassengers(passengers.size())
                    .createdDate(LocalDateTime.now())
                    .currency("ETB")
                    .status(0) // pending
                    .statusDesc("PENDING")
                    .build();

            TicketHistory saveTicketHistoryWithPassengers = transactionService.saveTicketHistoryWithPassengers(ticketHistory, passengers);

            PaymentStageDto paymentStageDto = PaymentStageDto.builder()
                    .amount(saveTicketHistoryWithPassengers.getTicketAmount())
                    .pnr(bookingDetails.getPnrCode())
                    .build();
            transactionService.stagePayment(paymentStageDto);

        }
        BookingResponse bookingResponse = BookingResponse.builder()
                .status(200)
                .message("success")
                .bookingInfo(List.of(bookingDetails))
                .build();

        return bookingResponse;
    }

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
                .optJSONObject("TravelItinerary");

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


    public FlightByPnrCodeResponse getTripDetails(String pnr) {
        var tripDetailsRequest = new JSONObject();
        tripDetailsRequest.put("operation", "TripDetails");
        tripDetailsRequest.put("UniqueID", pnr);

        log.info("FLIGHT LOGIC TRIP DETAILS REQUEST : {}", tripDetailsRequest);

        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(FLIGHT_LOGIC_API)
                .setBody(tripDetailsRequest.toString());

        JSONObject tripDetailsResponse = asyncHttp.sendRequest(requestBody);

        if (tripDetailsResponse.has("Errors") && tripDetailsResponse.optJSONObject("Errors") != null) {
            var errorsObject = tripDetailsResponse.optJSONObject("Errors");
            JSONObject errorObject = errorsObject.optJSONObject("Error");

            return FlightByPnrCodeResponse.builder()
                    .status(400)
                    .message(errorObject.optString("ErrorMessage"))
                    .build();
        }

        JSONObject tripDetailsResult = tripDetailsResponse.optJSONObject("TripDetailsResponse").optJSONObject("TripDetailsResult");
        JSONObject travelItinerary = tripDetailsResult.optJSONObject("TravelItinerary");

        String bookingStatus = travelItinerary.optString("BookingStatus");

        JSONObject itineraryInfoResp = travelItinerary.optJSONObject("ItineraryInfo");

        // Mapping traveler information
        List<TravelerResponse> travelers = new ArrayList<>();
        JSONArray customerInfos = itineraryInfoResp.optJSONArray("CustomerInfos");
        if (customerInfos != null) {
            for (int i = 0; i < customerInfos.length(); i++) {
                JSONObject customerInfo = customerInfos.optJSONObject(i).optJSONObject("CustomerInfo");
                if (customerInfo != null) {
                    travelers.add(TravelerResponse.builder()
                            .prefix(customerInfo.optString("PassengerTitle"))
                            .firstName(customerInfo.optString("PassengerFirstName"))
                            .lastName(customerInfo.optString("PassengerLastName"))
                            .location(customerInfo.optString("PassengerNationality"))
                            .phoneNumber(customerInfo.optString("PhoneNumber"))
                            .build());
                }
            }
        }
        BookingInfoResponse bookingInfoResponse = new BookingInfoResponse();


        // Mapping price information
        JSONObject itineraryPricing = itineraryInfoResp.optJSONObject("ItineraryPricing");
        PriceInfoResponse priceInfoResponse = null;
        if (itineraryPricing != null) {
            priceInfoResponse = PriceInfoResponse.builder()
                    .currency(itineraryPricing.optJSONObject("TotalFare").optString("CurrencyCode"))
                    .originalPrice(itineraryPricing.optJSONObject("EquiFare").optDouble("Amount"))
                    .taxAmount(itineraryPricing.optJSONObject("Tax").optDouble("Amount"))
                    .totalAmount(itineraryPricing.optJSONObject("TotalFare").optDouble("Amount"))
                    .build();
        }

        // Mapping air information
        List<AirInfoResponse> airInfoResponses = new ArrayList<>();
        JSONArray reservationItems = itineraryInfoResp.optJSONArray("ReservationItems");
        if (reservationItems != null) {
            for (int i = 0; i < reservationItems.length(); i++) {
                JSONObject reservationItem = reservationItems.optJSONObject(i).optJSONObject("ReservationItem");
                if (reservationItem != null) {


                    bookingInfoResponse.setBookingCode(reservationItem.optString("AirlinePNR"));

//                    airInfoResponses.add(AirInfoResponse.builder()
//                            .airlinePNR(reservationItem.optString("AirlinePNR"))
//                            .arrivalAirportLocationCode(reservationItem.optString("ArrivalAirportLocationCode"))
//                            .arrivalDateTime(reservationItem.optString("ArrivalDateTime"))
//                            .baggage(reservationItem.optString("Baggage"))
//                            .cabinClassText(reservationItem.optString("CabinClassText"))
//                            .departureAirportLocationCode(reservationItem.optString("DepartureAirportLocationCode"))
//                            .departureDateTime(reservationItem.optString("DepartureDateTime"))
//                            .flightNumber(reservationItem.optString("FlightNumber"))
//                            .journeyDuration(reservationItem.optInt("JourneyDuration"))
//                            .marketingAirlineCode(reservationItem.optString("MarketingAirlineCode"))
//                            .operatingAirlineCode(reservationItem.optString("OperatingAirlineCode"))
//                            .stopQuantity(reservationItem.optInt("StopQuantity"))
//                            .build());
                }
            }
        }

        // Mapping booking information (if available)
//        List<BookingInfoResponse> bookingInfoResponses = new ArrayList<>();
        // Add logic to map BookingInfoResponse if the data is available

        // Mapping passenger type information
        List<PassengerType> passengerInfoResponses = new ArrayList<>();
        JSONArray ptcFareBreakdowns = itineraryInfoResp.optJSONArray("TripDetailsPTC_FareBreakdowns");
        if (ptcFareBreakdowns != null) {
            for (int i = 0; i < ptcFareBreakdowns.length(); i++) {
                JSONObject fareBreakdown = ptcFareBreakdowns.optJSONObject(i).optJSONObject("TripDetailsPTC_FareBreakdown");
                if (fareBreakdown != null) {
                    JSONObject passengerTypeQuantity = fareBreakdown.optJSONObject("PassengerTypeQuantity");
                    if (passengerTypeQuantity != null) {
                        passengerInfoResponses.add(PassengerType.builder()
                                .code(passengerTypeQuantity.optString("Code"))
                                .age(passengerTypeQuantity.optInt("Quantity"))
                                .build());
                    }
                }
            }
        }


        return FlightByPnrCodeResponse.builder()
                .status(200)
                .message("success")
                .bookingStatus(bookingStatus)
                .travelers(travelers)
                .priceInfo(priceInfoResponse)
                .airInfo(airInfoResponses)
                .bookingInfo(List.of(bookingInfoResponse))
                .passengerInfo(passengerInfoResponses)
                .build();
    }
    // confirm ticket


    public FlightByPnrCodeResponse getTripInfo(String bookingRef) {

        Optional<TicketHistory> ticketHistoryRepoByPnr = ticketHistoryRepo.findByPnr(bookingRef);

        if (!ticketHistoryRepoByPnr.isPresent()) {
            return FlightByPnrCodeResponse.builder()
                    .status(400)
                    .message(String.format("Flight with reference %s not found", bookingRef))
                    .build();
        }

        TicketHistory ticketHistory = ticketHistoryRepoByPnr.get();

        if (ticketHistory.getStatus() == 2) {
            return FlightByPnrCodeResponse.builder()
                    .status(400)
                    .message(String.format("Booking with reference %s expired, please book flight again", bookingRef))
                    .build();
        }

        // Mapping AirInfoResponse
        List<AirInfoResponse> airInfoResponses = ticketHistory.getPassengers().stream()
                .map(passenger -> AirInfoResponse.builder()
//                        .pnr(ticketHistory.getPnr())
                        .origin(ticketHistory.getOrigin())
                        .destination(ticketHistory.getDestination())
                        .flightNumber(ticketHistory.getFlightNumber())
                        .departureDateTime(ticketHistory.getDepartureDateTime())
                        .arrivalDateTime(ticketHistory.getArrivalDateTime())
                        .build())
                .collect(Collectors.toList());

        // Mapping TravelerResponse (assuming similar structure to Passenger)
        List<TravelerResponse> travelerResponses = ticketHistory.getPassengers().stream()
                .map(passenger -> TravelerResponse.builder()
                        .firstName(passenger.getFirstName())
                        .middleName(passenger.getMiddleName())
                        .lastName(passenger.getFirstName())
                        .phoneNumber(passenger.getPhoneNumber())
                        .build())
                .collect(Collectors.toList());

        // Mapping PriceInfoResponse
        PriceInfoResponse priceInfoResponse = PriceInfoResponse.builder()
                .currency(ticketHistory.getCurrency().trim())
                .baseFareAmount(ticketHistory.getTicketAmount())
                .totalAmount(ticketHistory.getTotalAmount())
                .commissionAmount(ticketHistory.getCommissionAmount())
                // Add other relevant fields for price information
                .build();

        // Assuming BookingInfoResponse and PassengerType are also mappable
//        List<BookingInfoResponse> bookingInfoResponses = //... map these appropriately
//                List<PassengerType> passengerTypes = //... map these appropriately

        // Building the final response
        return FlightByPnrCodeResponse.builder()
                .status(200)
                .message("Success")
                .pnrCode(ticketHistory.getPnr())
                .version(1) // Example version, you might want to determine this differently
                .bookingStatus(ticketHistory.getStatusDesc())
                .travelers(travelerResponses)
                .priceInfo(priceInfoResponse)
                .airInfo(airInfoResponses)
//                .bookingInfo(bookingInfoResponses)
//                .passengerInfo(passengerTypes)
                .build();
    }

    public CustomResponse confirmTicket(String pnr) {
        // check the payment
        Optional<Payment> byPnr = paymentRepository.findByPnr(pnr);

        if (!byPnr.isPresent()) {
            return new CustomResponse(400, "Pnr not found", null);
        }

        Payment payment = byPnr.get();

        if (payment.getPaymentStatus() == 0) {
            return new CustomResponse(400, "Ticket cannot be confirmed please pay the ticket using payment gateways", null);
        }

        if (payment.getPaymentStatus() == 1 || payment.getPaymentStatus() == 3) {
            return new CustomResponse(400, "Ticket payment is still pending or failed", null);
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
            return new CustomResponse(400, errorMessage, null);
        }

        return new CustomResponse(200, "Ticket successfully confirmed", null);
    }


    //        TicketHistoryDto ticketHistory = TicketHistoryDto.builder()
//                .pnr(pnr)
//                .origin(reservationItem.optString("DepartureAirportLocationCode"))
//                .destination(reservationItem.optString("ArrivalAirportLocationCode"))
//                .airlineId(reservationItem.optString("OperatingAirlineCode"))
//                .departureDateTime(LocalDateTime.parse(reservationItem.optString("DepartureDateTime")))
//                .ticketAmount(Double.valueOf(airPriceItem.optString("Amount")))
//                .firstName(customerInfo.optString("PassengerFirstName"))
//                .middleName("")
//                .lastName(customerInfo.optString("PassengerLastName"))
//                .documentIdNumber(customerInfo.optString("PassportNumber"))
//                .phoneNumber(customerInfo.optString("PhoneNumber"))
//                .email(customerInfo.optString("email"))
//                .build();
//        this.transactionService.saveTicket(ticketHistory);


    //FETCH AIRPORTS FROM API AND INSERT INTO AIRPORT TABLE
//    @Cacheable("GetAirports")
//    public AirportListResp getAllAirports() {
//        var airPortListRequest = new JSONObject()
//                .put("operation", "AirportList");
//
//        RequestBuilder requestBody = new RequestBuilder("POST")
//                .setUrl(FLIGHT_LOGIC_API)
//                .setBody(airPortListRequest.toString());
//
//        JSONObject airPortListResponse = asyncHttp.sendRequest(requestBody);
//
//        JSONArray jsonArray = airPortListResponse.getJSONArray("airportlist");
//
//        // Convert JSONArray to List<Airport>
//        List<Airport> airportList = new ArrayList<>();
//        for (int i = 0; i < jsonArray.length(); i++) {
//            JSONObject jsonAirport = jsonArray.getJSONObject(i);
//            Airport airport = new Airport();
//            airport.setAirportCode(jsonAirport.getString("AirportCode"));
//            airport.setAirportName(jsonAirport.getString("AirportName"));
//            airport.setCountry(jsonAirport.getString("Country"));
////            airport.setLatitude(jsonAirport.getDouble("Latitude"));
//            airport.setCity(jsonAirport.getString("City"));
////            airport.setLongitude(jsonAirport.getDouble("Longitude"));
//            airportList.add(airport);
//        }
//
//        airportRepository.saveAll(airportList);
//
//        return AirportListResp.builder()
//                .status(200)
//                .message("success")
//                .airPortList(Collections.singletonList(airportList))
//                .airLineList(List.of())
//                .build();
//    }

    @Cacheable("GetAirports")
    public AirportListResp getAllAirports() {

        List<Airport> airportList = airportRepository.findAll();

        return AirportListResp.builder()
                .status(200)
                .message("success")
                .airPortList(airportList)
                .build();

    }

    @Cacheable("GetAirlines")
    public AirlineListResp getAllAirlines() {

//        var airPortListRequest = new JSONObject()
//                .put("operation", "AirlineList");
//
//        RequestBuilder requestBody = new RequestBuilder("POST")
//                .setUrl(FLIGHT_LOGIC_API)
//                .setBody(airPortListRequest.toString());
//
//        JSONObject airPortListResponse = asyncHttp.sendRequest(requestBody);
//
//        JSONArray jsonArray = airPortListResponse.getJSONArray("airlines");
//
//
//        List<Airline> airportLists = new ArrayList<>();
//        for (int i = 0; i < jsonArray.length(); i++) {
//            JSONObject jsonAirport = jsonArray.getJSONObject(i);
//            Airline airport = new Airline();
//            airport.setAirLineCode(jsonAirport.getString("AirLineCode"));
//            airport.setAirLineName(jsonAirport.getString("AirLineName"));
//            airport.setAirLineLogo(jsonAirport.getString("AirLineLogo"));
//            airportLists.add(airport);
//        }
//
//        airlineRepository.saveAll(airportLists);

        // Convert JSONArray to List
//        List<Object> airportList = jsonArray.toList();

        List<Airline> all = airlineRepository.findAll();

        return AirlineListResp.builder()
                .status(200)
                .message("success")
                .airLineList(all)
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
            return new CustomResponse(400, errorMessage, null);
        }

        return new CustomResponse(200, "Flight Cancelled successfully", null);
    }


}
