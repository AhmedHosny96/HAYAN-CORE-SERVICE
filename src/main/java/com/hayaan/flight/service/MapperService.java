package com.hayaan.flight.service;

import com.hayaan.config.UtilService;
import com.hayaan.flight.object.dto.*;
import com.hayaan.flight.object.dto.booking.TravelerResponse;
import com.hayaan.flight.object.dto.flight.*;
import com.hayaan.flight.object.entity.Airline;
import com.hayaan.flight.object.entity.Airport;
import com.hayaan.flight.repo.AirlineRepository;
import com.hayaan.flight.repo.AirportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class MapperService {


    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private final AirportRepository airportRepository;

    private final AirlineRepository airlineRepository;

    private final AirDetailsService airDetailsService;

    private final UtilService utilService;


    // FLIGHT SEARCH MAPPINGS

    public AirInfoResponse mapToAirInfo(JSONObject response) {

        String arrivalDate = response.optString("arrivalTime");
        String arrivalTimeString = arrivalDate.substring(arrivalDate.indexOf('T') + 1);

        String departureDate = response.optString("departureTime");
        String departureTimeString = departureDate.substring(departureDate.indexOf('T') + 1);

        return AirInfoResponse.builder()
                .key(response.optString("key"))
                .classOfService(response.optString("classOfService"))
                .origin(response.optString("origin"))
                .destination(response.optString("destination"))
                .airlineName(response.optString("carrier"))
                .flightNumber(response.optString("flightNumber"))
                .equipment(response.optString("equipment"))
                .departureDate(LocalDate.parse(response.optString("departureTime"), DATE_TIME_FORMATTER))
                .arrivalTime(arrivalTimeString)
                .departureTime(departureTimeString)
                .arrivalDate(LocalDate.parse(response.optString("arrivalTime"), DATE_TIME_FORMATTER))
                .flightDuration(response.optInt("flightTime") == 0 ? utilService.convertStringToDuration(response.optInt("travelTime")) :
                        utilService.convertStringToDuration(response.optInt("flightTime")))
                .build();

    }


    public PriceInfoResponse mapToPriceInfo(JSONArray response) {
        JSONObject priceKeys = response.getJSONObject(0);
        String currency = priceKeys.optString("basePrice").substring(0, 3);
        double originalPrice = Double.parseDouble(priceKeys.optString("basePrice").substring(3));
        double taxes = Double.parseDouble(priceKeys.optString("taxes").substring(3));
        double commission = 100.0;

        double priceAfterTaxAndMarkup = originalPrice + taxes + commission;

        PriceInfoResponse priceInfoResponse = PriceInfoResponse.builder()
                .originalPrice(originalPrice + taxes)
//                .taxes(taxes)
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
                .value(baggageINfo.optInt("value"))
                .unit(baggageINfo.optString("unit"))
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
                .bookingCode(response.optString("bookingCode"))
                .cabinClass(response.optString("cabinClass"))
                .segmentRef(response.optString("segmentRef"))
                .fareInfoRef(response.optString("fareInfoRef"))
                .hostTokenRef(response.optString("hostTokenRef"))
                .build();
    }

    public FareInfoResponse mapToFareInfo(JSONObject fareInfoObj) {

        return FareInfoResponse.builder()
                .key(fareInfoObj.optString("key"))
                .fareBasis(fareInfoObj.optString("fareBasis"))
                .passengerTypeCode(fareInfoObj.optString("passengerTypeCode"))
                .origin(fareInfoObj.optString("origin"))
                .destination(fareInfoObj.optString("destination"))
                .build();
    }

    // RETRIVE BOOKING MAPPINGS
    public List<TravelerResponse> mapToTravelerResponse(JSONArray travelerArray) {
        List<TravelerResponse> travelerResponses = new ArrayList<>();

        for (int i = 0; i < travelerArray.length(); i++) {
            JSONObject travelerObj = travelerArray.getJSONObject(i);

            TravelerResponse travelerResponse = TravelerResponse.builder()
                    .prefix(travelerObj.getJSONObject("bookingTravelerName").optString("prefix"))
                    .firstName(travelerObj.getJSONObject("bookingTravelerName").optString("first"))
                    .middleName(travelerObj.getJSONObject("bookingTravelerName").optString("middle"))
                    .lastName(travelerObj.getJSONObject("bookingTravelerName").optString("last"))
                    .suffix(travelerObj.getJSONObject("bookingTravelerName").optString("suffix"))
                    .location(travelerObj.getJSONArray("phoneNumber").getJSONObject(0).optString("location"))
                    .phoneNumber(travelerObj.getJSONArray("phoneNumber").getJSONObject(0).optString("number"))
                    .build();

            travelerResponses.add(travelerResponse);
        }

        return travelerResponses;
    }

    // MAP PASSENGER TYPE
    List<PassengerType> passengerTypes = new ArrayList<>();

    public List<PassengerType> mapPassengerTypes(JSONArray passengerTypeArray) {
        List<PassengerType> passengerTypes = new ArrayList<>();

        for (int i = 0; i < passengerTypeArray.length(); i++) {
            JSONObject passengerTypeObject = passengerTypeArray.getJSONObject(i);

            // Extract relevant fields from the JSON object
            String code = passengerTypeObject.optString("code");
            int age = passengerTypeObject.optInt("age"); // Assuming 'age' is present in the JSON object

            // Create PassengerType object using the builder pattern
            PassengerType passengerType = PassengerType.builder()
                    .code(code)
                    .age(age)
                    // Add other relevant fields here
                    .build();

            // Add the created PassengerType object to the list
            passengerTypes.add(passengerType);
        }

        return passengerTypes;
    }


    // FLIGHT LOGIC MAPPERS

    public FlightSearchResponse mapToFareItineraries(JSONArray fareItineraries, String sessionId, String origin, String destination) {
        FlightSearchResponse flightSearchResponse = new FlightSearchResponse();
        List<DepartFlightResponse> onwardFlightList = new ArrayList<>();
        List<ReturnFlightResponse> returnFlightList = new ArrayList<>();

        JSONObject flightSegment = null;
        JSONObject seatObject = null;

        for (int i = 0; i < fareItineraries.length(); i++) {
            JSONObject fareItinerary = fareItineraries.getJSONObject(i).getJSONObject("FareItinerary");
            JSONObject airItineraryFareInfo = fareItinerary.optJSONObject("AirItineraryFareInfo");
            JSONArray originDestinationOptions = fareItinerary.getJSONArray("OriginDestinationOptions");

            // Split the originDestinationOptions into onward and return flights
            for (int j = 0; j < originDestinationOptions.length(); j++) {
                JSONObject originDestinationOptionObj = originDestinationOptions.getJSONObject(j);
                JSONArray originDestinationOption = originDestinationOptionObj.getJSONArray("OriginDestinationOption");
                int totalStops = originDestinationOptionObj.getInt("TotalStops");

                List<AirInfoResponse> airInfoList = new ArrayList<>();
                AirInfoResponse airInfoResponse = new AirInfoResponse();
                ArrayList<TransitDetails> transitList = new ArrayList<>();
                ArrayList<Duration> durationList = new ArrayList<>();
                ArrayList<Duration> layoverDurationList = new ArrayList<>();

                int totalJourneyTime = 0;
                String flightNumber = "";
                String previousArrivalDateTime = "";

                for (int k = 0; k < originDestinationOption.length(); k++) {
                    flightSegment = originDestinationOption.getJSONObject(k).getJSONObject("FlightSegment");
                    seatObject = originDestinationOption.getJSONObject(k).getJSONObject("SeatsRemaining");

                    totalJourneyTime += flightSegment.getInt("JourneyDuration");

                    String layoverDuration = "";
                    if (k > 0 && !previousArrivalDateTime.isEmpty()) {
                        layoverDuration = calculateLayoverDuration(previousArrivalDateTime, flightSegment.getString("DepartureDateTime"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
                        layoverDurationList.add(parseDurationFromString(layoverDuration));
                    }

                    flightNumber += (k > 0 ? "->" : "") + flightSegment.getString("MarketingAirlineCode") + flightSegment.getString("FlightNumber");

                    previousArrivalDateTime = flightSegment.getString("ArrivalDateTime");

                    if (k > 0) {
                        TransitDetails transitDetails = extractTransitDetails(flightSegment, parseDurationFromString(layoverDuration));
                        transitList.add(transitDetails);
                    }

                    if (k == 0) {
                        airInfoResponse.setDepartureDate(LocalDate.parse(flightSegment.getString("DepartureDateTime").substring(0, 10)));
                        airInfoResponse.setDepartureTime(flightSegment.getString("DepartureDateTime").substring(11, 16));
                    }

                    if (k == originDestinationOption.length() - 1) {
                        airInfoResponse.setArrivalDate(LocalDate.parse(flightSegment.getString("ArrivalDateTime").substring(0, 10)));
                        airInfoResponse.setArrivalTime(flightSegment.getString("ArrivalDateTime").substring(11, 16));
                    }
                }

                durationList.add(utilService.convertStringToDuration(totalJourneyTime));
                Duration totalDuration = calculateTotalDuration(durationList, layoverDurationList);

                Airline airline = airlineRepository.findByAirLineCode(fareItinerary.getString("ValidatingAirlineCode")).get();

                airInfoResponse.setAirlineName(airline.getAirLineName());
                airInfoResponse.setRemainingSeats(seatObject.optInt("Number"));
                airInfoResponse.setFareSourceCode(airItineraryFareInfo.optString("FareSourceCode"));
                airInfoResponse.setFlightNumber(flightNumber);
                airInfoResponse.setSessionId(sessionId);
                airInfoResponse.setTotalStops(totalStops);
                airInfoResponse.setFlightDuration(totalDuration);
                airInfoResponse.setTransitFlight(transitList);
                airInfoResponse.setCurrency(fareItinerary.getJSONObject("AirItineraryFareInfo").getJSONObject("ItinTotalFares").getJSONObject("TotalFare").getString("CurrencyCode"));
                airInfoResponse.setFareInfo(extractFareInfo(fareItinerary.getJSONObject("AirItineraryFareInfo")));
                airInfoResponse.setBaggageInfo(extractBaggageInfo(fareItinerary.getJSONObject("AirItineraryFareInfo")));
                airInfoResponse.setAirlineLogoUrl(fareItinerary.optString("AirlineLogo"));

                airInfoList.add(airInfoResponse);

                // Determine if the flight is onward or return based on its position
                if (j == 0) {  // First segment is considered onward journey
                    DepartFlightResponse departFlightResponse = new DepartFlightResponse();
                    departFlightResponse.setAirInfo(airInfoList);
                    airInfoResponse.setOrigin(origin + "-" + airDetailsService.getAirportNameByCode(origin).getCity());
                    airInfoResponse.setDestination(destination + "-" + airDetailsService.getAirportNameByCode(destination).getCity());

                    // Set other fields like priceInfo, passengerInfo, fairInfo if needed
                    onwardFlightList.add(departFlightResponse);
                } else if (j == 1) {  // Second segment is considered return journey
                    ReturnFlightResponse returnFlightResponse = new ReturnFlightResponse();
                    returnFlightResponse.setAirInfo(airInfoList);
                    // Set other fields like priceInfo, passengerInfo, fairInfo if needed
                    airInfoResponse.setOrigin(destination + "-" + airDetailsService.getAirportNameByCode(destination).getCity());
                    airInfoResponse.setDestination(origin + "-" + airDetailsService.getAirportNameByCode(origin).getCity());

                    returnFlightList.add(returnFlightResponse);
                }
            }
        }

        flightSearchResponse.setDepartFlight(onwardFlightList);
        flightSearchResponse.setReturnFlight(returnFlightList);
        // Set other fields of flightSearchResponse if needed (status, message, key, etc.)

        return flightSearchResponse;
    }

// Helper methods to extract fare, baggage, and transit details should be defined to keep the logic clean and maintainable.

    private TransitDetails extractTransitDetails(JSONObject flightSegment, Duration layoverDuration) {
        TransitDetails transitDetails = new TransitDetails();
        Airport airport = airportRepository.findAirportByAirportCode(flightSegment.optString("DepartureAirportLocationCode")).orElseThrow(() -> new RuntimeException("Airport not found"));

        transitDetails.setAirportCode(airport.getAirportCode());
        transitDetails.setCity(airport.getCity());
        transitDetails.setCountry(airport.getCountry());
        transitDetails.setAirlineName(flightSegment.getString("MarketingAirlineName"));
        transitDetails.setArrivalDateTime(flightSegment.getString("ArrivalDateTime"));
        transitDetails.setDepartureDateTime(flightSegment.getString("DepartureDateTime"));
        transitDetails.setAirlineLogo(airlineRepository.findByAirLineCode(flightSegment.getString("MarketingAirlineCode")).orElseThrow(() -> new RuntimeException("Airline not found")).getAirLineLogo());

        transitDetails.setLayoverDuration(layoverDuration);
        return transitDetails;
    }

    private List<PriceInfoResponse> extractFareInfo(JSONObject airItineraryFareInfo) {
        JSONObject itinTotalFares = airItineraryFareInfo.getJSONObject("ItinTotalFares");
        JSONObject totalFare = itinTotalFares.getJSONObject("TotalFare");
        JSONObject baseFare = itinTotalFares.getJSONObject("BaseFare");
        JSONObject totalTax = itinTotalFares.getJSONObject("TotalTax");
        JSONObject penaltyDetails = airItineraryFareInfo.getJSONArray("FareBreakdown").getJSONObject(0).getJSONObject("PenaltyDetails");

        PriceInfoResponse priceInfo = PriceInfoResponse.builder()
                .taxAmount(totalTax.getDouble("Amount"))
                .baseFareAmount(baseFare.getDouble("Amount"))
                .totalAmount(totalFare.getDouble("Amount"))
                .changePenaltyAmount(penaltyDetails.getDouble("ChangePenaltyAmount"))
                .refundPenaltyAmount(penaltyDetails.getDouble("RefundPenaltyAmount"))
                .refundAllowed(penaltyDetails.getBoolean("RefundAllowed"))
                .changeAllowed(penaltyDetails.getBoolean("ChangeAllowed"))
                .build();

        return List.of(priceInfo);
    }

    private List<BaggageInfoResponse> extractBaggageInfo(JSONObject airItineraryFareInfo) {
        JSONObject fairBreakDown = airItineraryFareInfo.getJSONArray("FareBreakdown").getJSONObject(0);
        JSONArray baggage = fairBreakDown.getJSONArray("Baggage");
        JSONArray cabinBaggage = fairBreakDown.getJSONArray("CabinBaggage");

        BaggageInfoResponse baggageInfo = BaggageInfoResponse.builder()
                .allowedBaggage(baggage.getString(0) + " " + cabinBaggage.getString(0))
                .build();

        return List.of(baggageInfo);
    }


    public static String calculateLayoverDuration(String arrivalDateTime, String departureDateTime, SimpleDateFormat dateFormat) {
        try {
            Date arrivalTimePrev = dateFormat.parse(arrivalDateTime);
            Date departureTimeCurr = dateFormat.parse(departureDateTime);

            long layoverMillis = departureTimeCurr.getTime() - arrivalTimePrev.getTime();
            layoverMillis = Math.abs(layoverMillis);

            long layoverHours = TimeUnit.MILLISECONDS.toHours(layoverMillis);
            long layoverMinutes = TimeUnit.MILLISECONDS.toMinutes(layoverMillis) % 60;

            return String.format("%02d:%02d", layoverHours, layoverMinutes);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Error in calculating duration";
        }
    }

    public static Duration parseDurationFromString(String durationString) {
        String[] parts = durationString.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid duration format");
        }

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        return Duration.ofHours(hours).plusMinutes(minutes);
    }


    public static Duration calculateTotalDuration(List<Duration> durationList, List<Duration> layoverDurationList) {
        Duration totalDuration = Duration.ZERO;

        // calculate duration list total
        for (Duration duration : durationList) {
            totalDuration = totalDuration.plus(duration);
        }

        // calculate layoverdurationlist  total
        for (Duration duration : layoverDurationList) {
            totalDuration = totalDuration.plus(duration);
        }

        return totalDuration;
    }


}
