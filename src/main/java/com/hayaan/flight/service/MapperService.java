package com.hayaan.flight.service;

import com.hayaan.config.UtilService;
import com.hayaan.flight.object.dto.*;
import com.hayaan.flight.object.dto.booking.TravelerResponse;
import com.hayaan.flight.object.dto.flight.AirInfoResponse;
import com.hayaan.flight.object.dto.flight.BaggageInfoResponse;
import com.hayaan.flight.object.dto.flight.OnwardJourneyResponse;
import com.hayaan.flight.object.dto.flight.PriceInfoResponse;
import com.hayaan.flight.object.entity.Airport;
import com.hayaan.flight.repo.AirportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MapperService {


    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private final AirportRepository airportRepository;

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

    public List<AirInfoResponse> mapToFareItineraries(JSONArray fareItineraries, String sessionId, String origin, String destination) {
        List<AirInfoResponse> airInfoList = new ArrayList<>();

        for (int i = 0; i < fareItineraries.length(); i++) {
            JSONObject fareItinerary = fareItineraries.getJSONObject(i).getJSONObject("FareItinerary");

            String airlineLogoUrl = fareItinerary.optString("AirlineLogo");

            JSONObject airItineraryFareInfo = fareItinerary.getJSONObject("AirItineraryFareInfo");

            JSONObject totalFare = airItineraryFareInfo.optJSONObject("ItinTotalFares").optJSONObject("TotalFare");

            JSONArray originDestinationOptions = fareItinerary.optJSONArray("OriginDestinationOptions");
            JSONArray originDestinationOption = fareItinerary.optJSONArray("OriginDestinationOptions").optJSONObject(0).optJSONArray("OriginDestinationOption");

            int totalStops = originDestinationOptions.optJSONObject(0).optInt("TotalStops");

            AirInfoResponse airInfoResponse = new AirInfoResponse();
            ArrayList<TransitDetails> transitList = new ArrayList<>();

            int totalJourneyTime = 0; // Initialize total journey time

            for (int j = 0; j < originDestinationOption.length(); j++) {
                JSONObject flightSegment = originDestinationOption.optJSONObject(j).optJSONObject("FlightSegment");
                totalJourneyTime += flightSegment.getInt("JourneyDuration"); // Add current segment's journey time to total

                if (j > 0) {
                    Airport airportByAirportCode = airportRepository.findAirportByAirportCode(flightSegment.optString("DepartureAirportLocationCode")).get();

                    var transitDetail = new TransitDetails();
                    transitDetail.setAirportCode(airportByAirportCode.getAirportCode());
                    transitDetail.setCity(airportByAirportCode.getCity());
                    transitDetail.setCountry(airportByAirportCode.getCountry());

                    transitList.add(transitDetail);
                }

                if (j == originDestinationOption.length() - 1) {


                    airInfoResponse.setAirlineName(flightSegment.getString("MarketingAirlineName"));
                    airInfoResponse.setFlightNumber(flightSegment.getString("MarketingAirlineCode") + flightSegment.getString("FlightNumber"));
                    airInfoResponse.setEquipment(flightSegment.getJSONObject("OperatingAirline").getString("Equipment"));
                    airInfoResponse.setFlightDuration(utilService.convertStringToDuration(flightSegment.getInt("JourneyDuration")));
                    airInfoResponse.setDepartureDate(LocalDate.parse(flightSegment.getString("DepartureDateTime").substring(0, 10)));
                    airInfoResponse.setDepartureTime(flightSegment.getString("DepartureDateTime").substring(11, 16));
                    airInfoResponse.setArrivalDate(LocalDate.parse(flightSegment.getString("ArrivalDateTime").substring(0, 10)));
                    airInfoResponse.setArrivalTime(flightSegment.getString("ArrivalDateTime").substring(11, 16));
                    airInfoResponse.setClassOfService(flightSegment.getString("CabinClassCode"));

                    JSONObject seatsRemaining = originDestinationOption.optJSONObject(j).optJSONObject("SeatsRemaining");
                    airInfoResponse.setRemainingSeats(seatsRemaining.optInt("Number"));
                }
            }

            // get origin and destination fullname

            String originAirportName = airDetailsService.getAirportNameByCode(origin).getAirportName();
            String destinationAirportName = airDetailsService.getAirportNameByCode(destination).getAirportName();


            String originAirportCity = airDetailsService.getAirportNameByCode(origin).getCity();
            String destinationAirportCity = airDetailsService.getAirportNameByCode(destination).getCity();


            airInfoResponse.setTransitFlight(transitList);
            airInfoResponse.setFlightDuration(utilService.convertStringToDuration(totalJourneyTime)); // Set total journey time
            airInfoResponse.setSessionId(sessionId);
            airInfoResponse.setTotalStops(totalStops);
            airInfoResponse.setOrigin(originAirportCity);
            airInfoResponse.setDestination(destinationAirportCity);
            airInfoResponse.setFareSourceCode(airItineraryFareInfo.getString("FareSourceCode"));
            airInfoResponse.setIsRefundable(airItineraryFareInfo.getString("IsRefundable").equalsIgnoreCase("Yes"));
            airInfoResponse.setCurrency(totalFare.optString("CurrencyCode"));
            airInfoResponse.setTotalAmount(totalFare.optDouble("Amount"));
            airInfoResponse.setAirlineLogoUrl(airlineLogoUrl);

            airInfoList.add(airInfoResponse);
        }

        return airInfoList;
    }


}
