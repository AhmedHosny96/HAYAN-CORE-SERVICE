# Hayan Travel - Core Flight Booking System

Hayan Travel is a comprehensive flight booking system designed to integrate seamlessly with Flight Logic and Travelport, as well as various payment systems. Built using Java Spring Boot and leveraging MSSQL for data management, Hayan Travel provides robust APIs for searching, booking, and managing flight reservations.

## Features

- **Integration with Flight Logic and Travelport:** Provides real-time flight availability and booking capabilities.
- **Payment Systems Integration:** Supports multiple payment methods for seamless transactions.
- **Flight Search and Booking:** Easily search for and book flights with detailed traveler information.
- **Ticket Confirmation and Retrieval:** Confirm bookings and retrieve ticket details with ease.

## Technology Stack

- **Backend:** Java, Spring Boot
- **Database:** MSSQL
- **Integration:** Flight Logic, Travelport

## Prerequisites

- **Java 11** or higher
- **Spring Boot 2.5.4** or higher
- **MSSQL Server**
- **Maven** (for build automation)

## Installation

1. **Clone the Repository:**

    ```bash
    git clone https://github.com/yourusername/hayan-travel.git
    cd hayan-travel
    ```

2. **Configure the Database:**

    - Set up an MSSQL database.
    - Update the `application.properties` file with your MSSQL database connection details.
    
    ```properties
    spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=hayan_travel
    spring.datasource.username=yourusername
    spring.datasource.password=yourpassword
    ```

3. **Build the Project:**

    ```bash
    mvn clean install
    ```

4. **Run the Application:**

    ```bash
    mvn spring-boot:run
    ```

## API Endpoints

The Hayan Travel system provides the following REST API endpoints:

1. **Search Flight**

    - **Endpoint:** `POST /api/v1/flight/availability`
    - **Request Payload:**
    ```json
    {
        "noOfAdult": 1,
        "noOfChildren": 1,
        "noOfInfant": 0,
        "departureDate": "2024-06-15",
        "from": "DXB",
        "to": "NBO",
        "preferredCabin": "Economy"
    }
    ```

2. **Book Flight**

    - **Endpoint:** `POST /api/v1/book`
    - **Request Payload:**
    ```json
    {
        "travelers": [
            {
                "title": "Mr",
                "firstName": "AHMED",
                "middleName": "ABDI",
                "lastName": "ALI",
                "gender": "M",
                "email": "aahosny1@raysmfi.com",
                "dateOfBirth": "1992-10-12",
                "idNo": "464444131",
                "nationality": "ET",
                "phoneNumber": "915783511",
                "address": "addis abeba, Ethiopia, Bole, 1165",
                "travelerType": "ADT"
            },
            {
                "title": "Miss",
                "firstName": "Janet",
                "middleName": "",
                "lastName": "Thei",
                "gender": "F",
                "email": "janet@gmail.com",
                "dateOfBirth": "2010-09-25",
                "idNo": "232324454332",
                "nationality": "KE",
                "phoneNumber": "915783512",
                "address": "Nyeri, Kenya, Main Street, 10100",
                "travelerType": "CHD"
            }
        ],
        "airPriceInfo": [
            {
                "airSegment": [
                    {
                        "sessionId": "MTcxNjk4MjgwMF83OTg3MDM=",
                        "fareSourceCode": "aHNHenpVUVRDUFpTVk5GTzhWT3FFWWErWWpiU3dkMk12a2ZDWEhDKy8wVUNWZEdWTWd3cHhmRU5Jd2QvN1hhb2srWlZ5TURVL1VQdjcyQmtaKzF5L0dxZTVmeHdCcFZ5aER3djhjNnpnbk4zL2YrZ2R6MVZsQVF3VzZreEQ2blpJWlVTckFxSjZIVTQxSC9UeXdmeHFtQkZwYXFtOXdYczNjclJWTmZTbjkwPQ=="
                    }
                ]
            }
        ]
    }
    ```

3. **Payment**

    - **Endpoint:** `POST /api/v1/payment`
    - **Request Payload:**
    ```json
    {
        "pnr": "1WY78O",
        "paymentMethod": "SAHAY"
    }
    ```

4. **Confirm Ticket**

    - **Endpoint:** `GET /api/v1/flight/confirm-ticket`
    - **Query Parameter:** `pnrCode=TR78002024`

5. **Fetch Ticket Details**

    - **Endpoint:** `GET /api/v1/flight`
    - **Query Parameter:** `pnrCode=1WODS7`

## Usage

### Searching for Flights

To search for flights, make a POST request to the flight availability endpoint with the required parameters.

### Booking a Flight

To book a flight, make a POST request to the booking endpoint with the traveler and pricing information.

### Making a Payment

To make a payment for a booked flight, make a POST request to the payment endpoint with the PNR and payment method.

### Confirming a Ticket

To confirm a ticket, make a GET request to the confirm ticket endpoint with the PNR code.

### Fetching Ticket Details

To fetch ticket details, make a GET request to the fetch ticket details endpoint with the PNR code.


## Acknowledgments

Special thanks to the developers of Spring Boot, Flight Logic, and Travelport for their excellent tools and documentation.
