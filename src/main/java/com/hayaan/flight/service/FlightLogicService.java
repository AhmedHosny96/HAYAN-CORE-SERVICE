package com.hayaan.flight.service;


import com.hayaan.auth.object.entity.User;
import com.hayaan.auth.repo.UserRepository;
import com.hayaan.auth.service.UserService;
import com.hayaan.config.AsyncHttpConfig;
import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.FlightType;
import com.hayaan.flight.object.dto.*;
import com.hayaan.flight.object.dto.booking.*;
import com.hayaan.flight.object.dto.flight.*;
import com.hayaan.flight.object.entity.*;
import com.hayaan.flight.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.RequestBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightLogicService {

    private final UserRepository userRepository;
    private final AgentRepo agentRepo;
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
    private final UserService userService;


    // FLIGHT SEARCH ONE WAY / TWO WAY

    //    @Cacheable("flightLogicSearch")
    public FlightSearchResponse searchFlight(FlightSearchDto flightSearchDto) {


        log.info("searchFlight : {}", flightSearchDto);

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

        RequestBuilder requestBody = new RequestBuilder("POST").setUrl(FLIGHT_LOGIC_API).setBody(mainSearchRequest.toString());

        JSONObject flightSearchResponse = asyncHttp.sendRequest(requestBody);

        if (flightSearchResponse.has("Errors")) {
            var errorObject = flightSearchResponse.optJSONObject("Errors");

            return FlightSearchResponse.builder().status(400).message(errorObject.optString("ErrorMessage")).build();

        }

        log.info("FLIGHT LOGIC SEARCH RESPONSE : {}");

        JSONObject airSearchResponseObject = flightSearchResponse.optJSONObject("AirSearchResponse");

        if (airSearchResponseObject == null) {
            var errorObject = flightSearchResponse.optJSONObject("Errors");

            return FlightSearchResponse.builder().status(400).message("Error occured on flight logic service , Kindly contact the system developer").build();

        }

        JSONArray fareItineraries = airSearchResponseObject.optJSONObject("AirSearchResult").optJSONArray("FareItineraries");

        String sessionId = airSearchResponseObject.optString("session_id");

        FlightSearchResponse searchResponse = mapperService.mapToFareItineraries(fareItineraries, sessionId, flightSearchDto.from(), flightSearchDto.to(), flightSearchDto.currency());

//        OnwardJourneyResponse onwardJourneyResponse = mapperService.mapOnWardFlight(fareItineraries, sessionId, flightSearchDto.from(), flightSearchDto.to());


//        var onWardJourney = new OnwardJourneyResponse();
//        onWardJourney.setAirInfo(airInfoResponses);

        return FlightSearchResponse.builder().status(200).message("success").onwardFlight(searchResponse.getOnwardFlight()).returnFlight(searchResponse.getReturnFlight())
//                .airInfo(airInfoResponses)
                .build();
    }

    // BOOK FLIGHT
    public BookingResponse bookFlight(BookingRequestDto bookingRequestDto) {


        AirInfoResponse airInfoRequest = bookingRequestDto.getAirPriceInfo().get(0).getAirSegment().get(0);

        // validate before booking

        JSONObject validateFareRuleResponse = validateFareRules(airInfoRequest.getSessionId(), airInfoRequest.getFareSourceCode());

        if (!validateFareRuleResponse.optBoolean("IsValid")) {
            return BookingResponse.builder().status(400).message("Fare rule validation failed. Please refresh your browser").build();
        }

        JSONObject mainBookingRequest = new JSONObject();
        JSONObject flightBookingInfo = new JSONObject().put("flight_session_id", bookingRequestDto.getAirPriceInfo().get(0).getAirSegment().get(0).getSessionId()).put("fare_source_code", bookingRequestDto.getAirPriceInfo().get(0).getAirSegment().get(0).getFareSourceCode()).put("IsPassportMandatory", "false").put("fareType", "Public").put("areaCode", bookingRequestDto.getTravelers().get(0).getPhoneNumbers().get(0).getAreaCode()).put("countryCode", "251");

        JSONObject paxInfo = new JSONObject();
        paxInfo.put("clientRef", UUID.randomUUID().toString());
        paxInfo.put("postCode", bookingRequestDto.getTravelers().get(0).getAddress().getPostalCode());
        paxInfo.put("customerEmail", bookingRequestDto.getTravelers().get(0).getEmail());
        paxInfo.put("customerPhone", bookingRequestDto.getTravelers().get(0).getPhoneNumbers().get(0).getPhoneNumber());
        paxInfo.put("fareType", "WebFare");
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
            travelerJson.put("passportExpiryDate", traveler.getPassportExpiryDate());
            travelerJson.put("frequentFlyrNum", traveler.getFrequentFlyerNumber());

            Passenger passenger = Passenger.builder().firstName(traveler.getFirstName()).middleName(traveler.getMiddleName()).lastName(traveler.getLastName()).email(traveler.getEmail()).phoneNumber(traveler.getPhoneNumbers().get(0).getPhoneNumber()).document("Passport").documentIdNumber(traveler.getIdNo()).dateOfIssue(LocalDateTime.now()) // Replace with actual issue date
                    .expiryDate(LocalDateTime.of(2023, 12, 25, 0, 0)) // Replace with actual expiry date
                    .passengerType(traveler.getTravelerType().getCode()).build();

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

        RequestBuilder requestBody = new RequestBuilder("POST").setUrl(this.FLIGHT_LOGIC_API).setBody(mainBookingRequest.toString());

        JSONObject flightBookingResponse = this.asyncHttp.sendRequest(requestBody);

        if (flightBookingResponse.has("Errors") && flightBookingResponse.optString("Errors") != "") {
//            JSONObject errorObject = flightBookingResponse.optJSONObject("Errors");
            return BookingResponse.builder().status(400).message(flightBookingResponse.optString("Errors")).build();
        }

        log.info("FLIGHT LOGIC BOOKING RESPONSE : {}", flightBookingResponse);

        JSONObject bookFlightResult = flightBookingResponse.optJSONObject("BookFlightResponse").optJSONObject("BookFlightResult");
        log.info("FLIGHT LOGIC BOOKING RESPONSE : {}", flightBookingResponse);

//        if (bookFlightResult.has("Errors") && bookFlightResult.optJSONObject("Errors") != null) {
//            JSONObject errorsObject = bookFlightResult.optJSONObject("Errors");
//            JSONObject errorObject = errorsObject.optJSONObject("Error");
//            return BookingResponse.builder().status(400).message(errorObject.optString("ErrorMessage")).build();
//        }

        if (!bookFlightResult.optBoolean("Success")) {
            JSONObject errorsObject = bookFlightResult.optJSONObject("Errors");
            JSONObject errorObject = errorsObject != null ? errorsObject.optJSONObject("Error") : null;
            String errorMessage = "Booking failed";

            if (errorObject != null) {
                errorMessage = errorObject.optString("ErrorMessage", errorMessage);
            } else if (errorsObject != null) {
                errorMessage = errorsObject.optString("ErrorMessage", errorMessage);
            }

            return BookingResponse.builder().status(400).message(errorMessage).build();
        }


        String pnr = bookFlightResult.optString("UniqueID");
        BookingResponse.BookingDetails bookingDetails = BookingResponse.BookingDetails.builder().status(bookFlightResult.optString("Status")).pnrCode(pnr).build();

        JSONObject fareItinerary = validateFareRuleResponse.optJSONObject("FareItineraries").optJSONObject("FareItinerary");

        JSONObject airItineraryFareInfo = fareItinerary.optJSONObject("AirItineraryFareInfo");

        JSONArray originDestinationOptions = fareItinerary.optJSONArray("OriginDestinationOptions");

        JSONObject totalFare = airItineraryFareInfo.optJSONObject("ItinTotalFares").optJSONObject("TotalFare");


        Double originalTicketPrice = Double.valueOf(totalFare.optString("Amount"));

        log.info("totalFare : {}", originalTicketPrice);


        String origin;
        String destination;
        String flightNumber;

        LocalDateTime departureDateTime;
        LocalDateTime arrivalDateTime;


        for (int i = 0; i < originDestinationOptions.length(); i++) {
            JSONObject originDestinationOption = originDestinationOptions.getJSONObject(i);
            int totalStops = originDestinationOption.optInt("TotalStops");

            JSONArray flightSegments = originDestinationOption.optJSONArray("OriginDestinationOption");

            if (flightSegments != null && flightSegments.length() > 0) {
                // First flight segment
                JSONObject firstSegment = flightSegments.getJSONObject(0).getJSONObject("FlightSegment");
                departureDateTime = LocalDateTime.parse(firstSegment.optString("DepartureDateTime"));
                origin = firstSegment.optString("DepartureAirportLocationCode");

                flightNumber = firstSegment.optString("MarketingAirlineCode").trim() + firstSegment.optString("FlightNumber").trim();
                System.out.println("First Flight Segment:");

                // Last flight segment
                JSONObject lastSegment = flightSegments.getJSONObject(flightSegments.length() - 1).getJSONObject("FlightSegment");
                arrivalDateTime = LocalDateTime.parse(lastSegment.optString("ArrivalDateTime"));
                destination = lastSegment.optString("ArrivalAirportLocationCode");


                boolean isInternationalFlight = airDetailsService.isInternationalFlight(origin, destination);

                FlightType flightType = isInternationalFlight ? FlightType.International : FlightType.Domestic;

                log.info("flight type : {}", flightType);

                log.info("original ticket price :{}", originalTicketPrice);
                double rate = currencyService.convertCurrency("USD", bookingRequestDto.getCurrency());

                double totalPrice = airDetailsService.roundNumber(originalTicketPrice * rate);

                double convertedFareAmount = originalTicketPrice * rate;

                double commission = commissionService.calculateCommission(convertedFareAmount, flightType, "ADMIN");

                User user = null;
                if (bookingRequestDto.getUserId() != null) {
                    user = userRepository.findById(bookingRequestDto.getUserId()).orElse(null);
                }

                Agent agent = null;
                if (bookingRequestDto.getAgentId() != null) {
                    agent = agentRepo.findById(bookingRequestDto.getAgentId()).orElse(null);
                }

                TicketHistory ticketHistory = TicketHistory.builder()
                        .pnr(pnr)
                        .origin(origin)
                        .destination(destination)
                        .departureDateTime(departureDateTime)
                        .arrivalDateTime(arrivalDateTime)
                        .flightNumber(flightNumber)
                        .ticketAmount(roundToTwoDecimalPlaces(convertedFareAmount)) // Round to two decimal places
                        .commissionAmount(roundToTwoDecimalPlaces(commission)) // Round to two decimal places
                        .totalAmount(roundToTwoDecimalPlaces(convertedFareAmount + commission)) // Round to two decimal places
                        .totalNoOfPassengers(passengers.size())
                        .createdDate(LocalDateTime.now())
                        .currency("ETB")
                        .status(0) // pending
                        .statusDesc("PENDING")
                        .user(user)
                        .agent(agent)
                        .build();

                TicketHistory saveTicketHistoryWithPassengers = transactionService.saveTicketHistoryWithPassengers(ticketHistory, passengers);

                PaymentStageDto paymentStageDto = PaymentStageDto.builder().amount(saveTicketHistoryWithPassengers.getTotalAmount()).pnr(bookingDetails.getPnrCode()).build();

                log.info("payment stage dto : {}", paymentStageDto);

                transactionService.stagePayment(paymentStageDto);

                InsertConversionHistoryDb insertConversionHistoryDb = InsertConversionHistoryDb.builder().originalAmount(originalTicketPrice).baseCurrency("USD").targetCurrency(bookingRequestDto.getCurrency()).rate(rate).amountAfter(totalPrice).bookingReference(bookingDetails.getPnrCode()).build();

                currencyService.logTicketConversionHistoryToDb(insertConversionHistoryDb);

            }
        }
        BookingResponse bookingResponse = BookingResponse.builder()


                .status(200).message("success").bookingInfo(List.of(bookingDetails)).build();

        return bookingResponse;
    }

    public JSONObject validateFareRules(String sessionId, String fareSourceCode) {

        var validationRequest = new JSONObject();
        validationRequest.put("operation", "ValidateFare");
        validationRequest.put("session_id", sessionId);
        validationRequest.put("fare_source_code", fareSourceCode);

        log.info("FLIGHT LOGIC VALIDATE FARE REQUEST : {}", validationRequest);

        RequestBuilder requestBody = new RequestBuilder("POST").setUrl(FLIGHT_LOGIC_API).setBody(validationRequest.toString());

        JSONObject validateFareResponse = asyncHttp.sendRequest(requestBody);

        JSONObject airRevalitateResponse = validateFareResponse.optJSONObject("AirRevalidateResponse").optJSONObject("AirRevalidateResult");


        log.info("FLIGHT LOGIC FARE VALIDATION RESPONSE : {}", airRevalitateResponse);

        return airRevalitateResponse;

    }

    public static double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }


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

        JSONObject validationRequest = new JSONObject();
        validationRequest.put("operation", "TripDetails");
        validationRequest.put("UniqueID", bookingRef);

        log.info("FLIGHT LOGIC TRIP DETAILS REQUEST : {}", validationRequest);
        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(FLIGHT_LOGIC_API)
                .setBody(validationRequest.toString());
        JSONObject tripDetailsResponse = asyncHttp.sendRequest(requestBody);
        log.info("FLIGHT LOGIC TRIP DETAILS RESPONSE : {}", tripDetailsResponse);

        if (tripDetailsResponse.has("Errors")) {
            return FlightByPnrCodeResponse.builder()
                    .status(400)
                    .message(tripDetailsResponse.optJSONObject("Errors").optString("ErrorMessage"))
                    .build();
        }

        JSONObject tripDetailsResult = tripDetailsResponse
                .optJSONObject("TripDetailsResponse")
                .optJSONObject("TripDetailsResult");

        JSONObject travelItinerary = tripDetailsResult
                .optJSONObject("TravelItinerary");

        JSONArray reservationItems = travelItinerary.optJSONObject("ItineraryInfo")
                .optJSONArray("ReservationItems");

        String destination = travelItinerary.optString("Destination");
        String origin = travelItinerary.optString("Origin");

        log.info("origin : {}", origin);
        log.info("destination : {}", destination);

        List<JSONObject> onwardSegments = new ArrayList<>();
        List<JSONObject> returnSegments = new ArrayList<>();

        for (int i = 0; i < reservationItems.length(); i++) {
            JSONObject segment = reservationItems.getJSONObject(i).optJSONObject("ReservationItem");
            String departureCode = segment.optString("DepartureAirportLocationCode");
            String arrivalCode = segment.optString("ArrivalAirportLocationCode");

            if (departureCode.equalsIgnoreCase(origin) && arrivalCode.equalsIgnoreCase(destination)) {
                // This is an ONWARD flight
                onwardSegments.add(segment);
            } else if (departureCode.equalsIgnoreCase(destination) && arrivalCode.equalsIgnoreCase(origin)) {
                // This is a RETURN flight
                returnSegments.add(segment);
            } else {
                // It is part of onward if onward hasn't completed yet
                if (onwardSegments.isEmpty()) {
                    onwardSegments.add(segment);
                } else {
                    returnSegments.add(segment);
                }
            }
        }


        PriceInfoResponse priceInfo = PriceInfoResponse.builder()
                .currency(ticketHistory.getCurrency())
                .baseFareAmount(ticketHistory.getTicketAmount())
                .commissionAmount(ticketHistory.getCommissionAmount())
                .totalAmount(ticketHistory.getTotalAmount())
                .build();

        List<DepartFlightResponse> onwardFlightList = new ArrayList<>();
        List<ReturnFlightResponse> returnFlightList = new ArrayList<>();

        if (!onwardSegments.isEmpty()) {
            onwardFlightList.add(DepartFlightResponse.builder()
                    .airInfo(List.of(buildAirInfoWithTransit(onwardSegments, origin, destination)))
                    .priceInfo(priceInfo)
                    .passengerInfo(Collections.emptyList())
                    .fairInfo(null)
                    .build());
        }

        if (!returnSegments.isEmpty()) {
            returnFlightList.add(ReturnFlightResponse.builder()
                    .airInfo(List.of(buildAirInfoWithTransit(returnSegments, destination, origin)))
                    .priceInfo(priceInfo)
                    .passengerInfo(Collections.emptyList())
                    .fairInfo(null)
                    .build());
        }

        List<TravelerResponse> travelerResponses = ticketHistory.getPassengers().stream().map(p -> TravelerResponse.builder()
                .passengerType(p.getPassengerType())
                .firstName(p.getFirstName())
                .middleName(p.getMiddleName())
                .lastName(p.getLastName())
                .phoneNumber(p.getPhoneNumber())
                .build()).collect(Collectors.toList());

        return FlightByPnrCodeResponse.builder()
                .status(200)
                .message("Success")
                .pnrCode(ticketHistory.getPnr())
                .version(1)
                .bookingStatus(ticketHistory.getStatusDesc())
                .travelers(travelerResponses)
                .onwardFlight(onwardFlightList)
                .returnFlight(returnFlightList)
                .build();
    }

    private AirInfoResponse buildAirInfoWithTransit(List<JSONObject> segments, String origin, String destination) {
        List<TransitDetails> transitDetails = new ArrayList<>();
        JSONObject lastSegment = segments.get(segments.size() - 1);

        StringBuilder flightNumberBuilder = new StringBuilder();
        int totalJourneyDuration = 0;

        for (int i = 0; i < segments.size(); i++) {
            JSONObject curr = segments.get(i);
            String currentFlightNumber = curr.optString("MarketingAirlineCode") + curr.optString("FlightNumber");
            if (i > 0) flightNumberBuilder.append("->");
            flightNumberBuilder.append(currentFlightNumber);

            totalJourneyDuration += curr.optInt("JourneyDuration");

            if (i < segments.size() - 1) {
                JSONObject next = segments.get(i + 1);
                LocalDateTime arrivalTime = LocalDateTime.parse(curr.optString("ArrivalDateTime"));
                LocalDateTime nextDepartureTime = LocalDateTime.parse(next.optString("DepartureDateTime"));

                Duration layover = Duration.between(arrivalTime, nextDepartureTime);

                String transitCode = curr.optString("ArrivalAirportLocationCode");
                Airline airline = airlineRepository.findByAirLineCode(curr.optString("MarketingAirlineCode")).orElse(null);
                Airport airport = airportRepository.findAirportByAirportCode(transitCode).orElse(null);

                TransitDetails transit = new TransitDetails();
                transit.setAirportCode(transitCode);
                transit.setCity(airport != null ? airport.getCity() : "");
                transit.setCountry(airport != null ? airport.getCountry() : "");
                transit.setAirlineName(airline != null ? airline.getAirLineName() : curr.optString("MarketingAirlineCode"));
                transit.setAirlineLogo(airline != null ? airline.getAirLineLogo() : "");
                transit.setArrivalDateTime(arrivalTime.toString());
                transit.setDepartureDateTime(nextDepartureTime.toString());
                transit.setLayoverDuration(layover);

                transitDetails.add(transit);
            }
        }

        LocalDateTime departureDateTime = LocalDateTime.parse(segments.get(0).optString("DepartureDateTime"));
        LocalDateTime arrivalDateTime = LocalDateTime.parse(lastSegment.optString("ArrivalDateTime"));
        String depCode = segments.get(0).optString("DepartureAirportLocationCode");
        String arrCode = lastSegment.optString("ArrivalAirportLocationCode");

        Airline airline = airlineRepository.findByAirLineCode(lastSegment.optString("MarketingAirlineCode")).orElse(null);
        String airlineName = airline != null ? airline.getAirLineName() : lastSegment.optString("MarketingAirlineCode");
        String airlineLogo = airline != null ? airline.getAirLineLogo() : "";

        Airport depAirport = airportRepository.findAirportByAirportCode(origin).orElse(null);
        Airport arrAirport = airportRepository.findAirportByAirportCode(destination).orElse(null);

        return AirInfoResponse.builder()
                .airlineName(airlineName)
                .flightNumber(flightNumberBuilder.toString())
                .flightDuration(Duration.ofMinutes(totalJourneyDuration))
                .departureDate(departureDateTime.toLocalDate())
                .departureTime(departureDateTime.toLocalTime().toString())
                .arrivalDate(arrivalDateTime.toLocalDate())
                .arrivalTime(arrivalDateTime.toLocalTime().toString())
                .origin(origin + "-" + (depAirport != null ? depAirport.getCity() : ""))
                .destination(destination + "-" + (arrAirport != null ? arrAirport.getCity() : ""))
                .transitFlight(transitDetails)
                .totalStops(transitDetails.size())
                .airlineLogoUrl(airlineLogo)
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

        var orderTicketRequest = new JSONObject().put("operation", "OrderTicket").put("UniqueID", pnr);

        RequestBuilder requestBody = new RequestBuilder("POST").setUrl(FLIGHT_LOGIC_API).setBody(orderTicketRequest.toString());

        JSONObject confirmTicketResponse = asyncHttp.sendRequest(requestBody);

        log.info("CONFIRMATION RESPONSE : {}", confirmTicketResponse);
        if (confirmTicketResponse.has("Errors") && confirmTicketResponse.optJSONObject("Errors") != null) {

            String errorMessage = confirmTicketResponse.optJSONObject("Errors").optString("ErrorMessage");
            return new CustomResponse(400, errorMessage, null);
        }

        JSONObject fetchTripDetailsResponse = fetchTripDetails(pnr);

        JSONObject itineraryInfo = fetchTripDetailsResponse.optJSONObject("ItineraryInfo");
        JSONObject customerInfo = itineraryInfo.optJSONArray("CustomerInfos").optJSONObject(0).optJSONObject("CustomerInfo");
        JSONObject reservationItem = itineraryInfo.optJSONArray("ReservationItems").optJSONObject(0).optJSONObject("ReservationItem");

        TicketHistory existingTicketHistory = ticketHistoryRepo.findByPnr(pnr).get();
        existingTicketHistory.setStatus(1);
        existingTicketHistory.setPaymentReference(payment.getPaymentReference());
        existingTicketHistory.setUniqueId(reservationItem.optString("AirlinePNR"));
        existingTicketHistory.setEticketNumber(customerInfo.optString("eTicketNumber"));
        existingTicketHistory.setStatusDesc("COMPLETED");
        ticketHistoryRepo.save(existingTicketHistory);

        return new CustomResponse(200, "Ticket confirmed successfully", null);

    }

    public JSONObject fetchTripDetails(String pnr) {

        var tripDetailsRequest = new JSONObject();
        tripDetailsRequest.put("operation", "TripDetails");
        tripDetailsRequest.put("UniqueID", pnr);

        log.info("FLIGHT LOGIC TRIP DETAILS REQUEST : {}", tripDetailsRequest);

        RequestBuilder requestBody = new RequestBuilder("POST").setUrl(FLIGHT_LOGIC_API).setBody(tripDetailsRequest.toString());

        JSONObject tripDetailsResponse = asyncHttp.sendRequest(requestBody);
//
        JSONObject itineraryInfoResp = tripDetailsResponse.optJSONObject("TripDetailsResponse").optJSONObject("TripDetailsResult").optJSONObject("TravelItinerary");

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

    /// /            airport.setLongitude(jsonAirport.getDouble("Longitude"));
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

        return AirportListResp.builder().status(200).message("success").airPortList(airportList).build();

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

        return AirlineListResp.builder().status(200).message("success").airLineList(all).build();
    }

    public CustomResponse cancelFlight(String pnrCode, Long userId) {
        Optional<TicketHistory> ticketHistoryOpt = ticketHistoryRepo.findByPnr(pnrCode);

        if (!ticketHistoryOpt.isPresent()) {
            return new CustomResponse(400, "PNR not found", null);
        }

        TicketHistory ticketHistory = ticketHistoryOpt.get();

        if (ticketHistory.getStatus() == 3) {
            return new CustomResponse(400, "Ticket already cancelled", null);
        }

        // Fetch the requester user
        Optional<User> requesterOpt = userRepository.findById(userId);
        if (!requesterOpt.isPresent()) {
            return new CustomResponse(400, "User not found", null);
        }

        User requester = requesterOpt.get();
        String requesterRole = requester.getRole().getName(); // or requester.getRoles() if it's a list/set

//        Optional<User> userRepositoryById = userRepository.findById(ticketHistory.getUser().getId());

        // Allow if requester is ADMIN or booked the ticket
        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = ticketHistory.getUser().getId() != null && ticketHistory.getUser().getId().equals(userId);

        if (!isAdmin && !isOwner) {
            return new CustomResponse(403, "Only the ticket owner or an admin can cancel this ticket.", null);
        }

        // Build cancel request
        var requestBody = new JSONObject();
        requestBody.put("UniqueID", pnrCode);
        requestBody.put("operation", "CancelTrip");
        log.info("FLIGHT LOGIC CANCEL FLIGHT REQUEST: {}", requestBody);

        RequestBuilder requestBuilder = new RequestBuilder("POST").setUrl(FLIGHT_LOGIC_API).setBody(requestBody.toString());
        JSONObject cancelFlightResponse = asyncHttp.sendRequest(requestBuilder);
        log.info("CANCEL FLIGHT RESPONSE : {}", cancelFlightResponse);

        // Handle errors
        if (cancelFlightResponse.has("Errors") && cancelFlightResponse.optJSONObject("Errors") != null) {
            String errorMessage = cancelFlightResponse.optJSONObject("Errors").optString("ErrorMessage");
            return new CustomResponse(400, errorMessage, null);
        }

        // Update ticket
        ticketHistory.setStatus(3);
        ticketHistory.setStatusDesc("CANCELLED");
        ticketHistoryRepo.save(ticketHistory);

        // Update payment
        paymentRepository.findByPnr(ticketHistory.getPnr()).ifPresent(payment -> {
            payment.setPaymentStatus(3);
            payment.setPaymentStatusDesc("CANCELLED");
            paymentRepository.save(payment);
        });

        return new CustomResponse(200, "Flight cancelled successfully", null);
    }


    @Cacheable("airportByCode")
    public Airport getAirportByCode(String airportCode) {
        return airportRepository.findAirportByAirportCode(airportCode).get();
    }


    public ReissueTicketResponse reissueTicket(String uniqueReference, LocalDate departureDate) {


        Optional<TicketHistory> ticketHistoryRepoByPnr = ticketHistoryRepo.findByPnr(uniqueReference);

        if (!ticketHistoryRepoByPnr.isPresent()) {
            return ReissueTicketResponse.builder()
                    .status(400)
                    .message("Pnr not found")
                    .build();
        }

        TicketHistory ticketHistory = ticketHistoryRepoByPnr.get();

        // STEP 1: Call TripDetails
        JSONObject tripDetailsRequest = new JSONObject()
                .put("operation", "TripDetails")
                .put("UniqueID", uniqueReference);

        log.info("FLIGHT LOGIC TRIP DETAILS REQUEST: {}", tripDetailsRequest);

        RequestBuilder tripRequest = new RequestBuilder("POST")
                .setUrl(FLIGHT_LOGIC_API)
                .setBody(tripDetailsRequest.toString());

        JSONObject tripDetailsResponse = asyncHttp.sendRequest(tripRequest);
        log.info("FLIGHT LOGIC TRIP DETAILS RESPONSE: {}", tripDetailsResponse);

        if (tripDetailsResponse.has("Errors")) {
            return ReissueTicketResponse.builder()
                    .status(400)
                    .message(tripDetailsResponse.optJSONObject("Errors").optString("ErrorMessage", "Trip details error"))
                    .build();
        }

        JSONObject tripResult = tripDetailsResponse
                .optJSONObject("TripDetailsResponse")
                .optJSONObject("TripDetailsResult");

        JSONObject itineraryInfo = tripResult
                .optJSONObject("TravelItinerary")
                .optJSONObject("ItineraryInfo");

        JSONArray customerInfos = itineraryInfo.optJSONArray("CustomerInfos");
        JSONArray reservationItems = itineraryInfo.optJSONArray("ReservationItems");

        // STEP 2: Prepare paxDetails for ReissueQuote
        JSONArray paxDetailsArray = new JSONArray();
        for (int i = 0; i < customerInfos.length(); i++) {
            JSONObject ci = customerInfos.getJSONObject(i).getJSONObject("CustomerInfo");

            JSONObject pax = new JSONObject();
            pax.put("type", ci.optString("PassengerType", "ADT"));
            pax.put("title", ci.optString("PassengerTitle", ""));
            pax.put("firstName", ci.optString("PassengerFirstName", ""));
            pax.put("lastName", ci.optString("PassengerLastName", ""));
            pax.put("eTicket", ci.optString("eTicketNumber", ""));

            paxDetailsArray.put(pax);
        }

        // STEP 3: Prepare OriginDestinationInfo
        JSONArray originDestArray = new JSONArray();
        for (int i = 0; i < reservationItems.length(); i++) {
            JSONObject ri = reservationItems.getJSONObject(i).getJSONObject("ReservationItem");

            JSONObject originDest = new JSONObject();
            originDest.put("airportOriginCode", ri.optString("DepartureAirportLocationCode"));
            originDest.put("airportDestinationCode", ri.optString("ArrivalAirportLocationCode"));
            originDest.put("cabinPreference", "Y");
            originDest.put("departureDate", departureDate);
            originDest.put("flightNumber", ri.optString("FlightNumber"));
            originDest.put("airlineCode", ri.optString("MarketingAirlineCode"));

            originDestArray.put(originDest);
        }

        // STEP 4: Call ReissueQuote API
        JSONObject reissueQuoteRequest = new JSONObject()
                .put("operation", "ReissueQuote")
                .put("UniqueID", uniqueReference)
                .put("paxDetails", paxDetailsArray)
                .put("OriginDestinationInfo", originDestArray);

        log.info("FLIGHT LOGIC REISSUE QUOTE REQUEST: {}", reissueQuoteRequest);

        RequestBuilder quoteRequest = new RequestBuilder("POST")
                .setUrl(FLIGHT_LOGIC_API)
                .setBody(reissueQuoteRequest.toString());

        JSONObject quoteResponse = asyncHttp.sendRequest(quoteRequest);
        log.info("FLIGHT LOGIC REISSUE QUOTE RESPONSE: {}", quoteResponse);

        if (quoteResponse.has("Errors")) {
            return ReissueTicketResponse.builder()
                    .status(400)
                    .message(quoteResponse.optJSONObject("Errors").optString("ErrorMessage", "Quote error"))
                    .build();
        }

        JSONObject quoteResult = quoteResponse
                .optJSONObject("ReissueQuoteResponse")
                .optJSONObject("ReissueQuoteResult");

        Long ptrUniqueID = quoteResult.optLong("ptrUniqueID");

        // STEP 5: Call ReissueTicket API
        JSONObject reissueTicketRequest = new JSONObject()
                .put("operation", "ReissueTicket")
                .put("UniqueID", uniqueReference)
                .put("ptrUniqueID", ptrUniqueID)
                .put("PreferenceOption", 1)
                .put("remark", "");

        log.info("FLIGHT LOGIC REISSUE TICKET REQUEST: {}", reissueTicketRequest);

        RequestBuilder ticketRequest = new RequestBuilder("POST")
                .setUrl(FLIGHT_LOGIC_API)
                .setBody(reissueTicketRequest.toString());

        JSONObject reissueResponse = asyncHttp.sendRequest(ticketRequest);
        log.info("FLIGHT LOGIC REISSUE TICKET RESPONSE: {}", reissueResponse);

        if (reissueResponse.has("Errors")) {
            return ReissueTicketResponse.builder()
                    .status(400)
                    .message(reissueResponse.optJSONObject("Errors").optString("ErrorMessage", "Reissue ticket error"))
                    .uniqueReference(uniqueReference)
                    .ptrUniqueID(ptrUniqueID)
                    .ptrStatus("FAILED")
                    .build();
        }

        JSONObject reissueResult = reissueResponse
                .optJSONObject("ReissueResponse")
                .optJSONObject("ReissueResult");

        Long ptrUniqueId = reissueResult.optLong("ptrUniqueID");

        log.info("ptrUniqueId : {}", ptrUniqueId);

        ticketHistory.setPtrUniqueID(ptrUniqueId);

        ticketHistoryRepo.save(ticketHistory);
        // Final success response
        return ReissueTicketResponse.builder()
                .status(200)
                .message(reissueResult.optString("Message", "Reissue successful"))
                .uniqueReference(reissueResult.optString("UniqueID"))
                .ptrUniqueID(ptrUniqueId)
                .ptrStatus(reissueResult.optString("Status", "InProcess"))
                .build();
    }
    // post ticket status

    public ReissueTicketResponse getReissueTicketResponse(String pnr, Long ptrUniqueId) {

        Optional<TicketHistory> existingRecord = ticketHistoryRepo.findByPnrAndPtrUniqueID(pnr, ptrUniqueId);

        // Reverse condition: only return error if NOT found
        if (!existingRecord.isPresent()) {
            return ReissueTicketResponse.builder()
                    .status(400)
                    .message("Pnr / ptrUniqueId not found")
                    .build();
        }

        var postTicketRequest = new JSONObject()
                .put("operation", "PostTicketStatus")
                .put("UniqueID", pnr)
                .put("ptrUniqueID", ptrUniqueId);

        log.info("FLIGHT LOGIC POST TICKET STATUS REQUEST: {}", postTicketRequest);

        RequestBuilder ticketRequest = new RequestBuilder("POST")
                .setUrl(FLIGHT_LOGIC_API)
                .setBody(postTicketRequest.toString());

        JSONObject postTicketResponse = asyncHttp.sendRequest(ticketRequest);

        log.info("FLIGHT LOGIC POST TICKET STATUS RESPONSE: {}", postTicketResponse);

        if (postTicketResponse.has("Errors")) {
            return ReissueTicketResponse.builder()
                    .status(400)
                    .message(postTicketResponse.optJSONObject("Errors").optString("ErrorMessage", "Reissue ticket error"))
                    .build();
        }

        JSONObject ptrResult = postTicketResponse
                .optJSONObject("PtrResponse")
                .optJSONObject("PtrResult");

        JSONArray ptrDetails = ptrResult.optJSONArray("PtrDetails");

        if (ptrDetails == null) {
            return ReissueTicketResponse.builder()
                    .status(400)
                    .message("PtrDetails not found")
                    .build();
        }

        JSONObject ptrDetailsObject = ptrDetails.getJSONObject(0);

        // Optional: extract passenger info if needed
        JSONArray paxDetails = ptrDetailsObject.optJSONArray("PaxDetails");
//        List<PassengerDto> passengers = new ArrayList<>();
//
//        if (paxDetails != null) {
//            for (int i = 0; i < paxDetails.length(); i++) {
//                JSONObject pax = paxDetails.getJSONObject(i);
//                passengers.add(PassengerDto.builder()
//                        .type(pax.optString("PassengerType"))
//                        .title(pax.optString("Title"))
//                        .firstName(pax.optString("FirstName"))
//                        .lastName(pax.optString("LastName"))
//                        .eTicket(pax.optString("ETicket"))
//                        .build());
//            }
//        }

        return ReissueTicketResponse.builder()
                .status(200)
                .message("success")
                .uniqueReference(ptrDetailsObject.optString("UniqueID"))
                .ptrUniqueID(Long.valueOf(ptrDetailsObject.optString("PtrUniqueID")))
                .ptrStatus(ptrDetailsObject.optString("PtrStatus"))
                .ptrType(ptrDetailsObject.optString("PtrType"))
                .resolution(ptrDetailsObject.optString("Resolution"))
                //.passengers(passengers)
                .build();
    }


}
