# Social media 

This is the backend code for a event sharing application, which enables users to share and manage events.
  
## Features:	
- Event Management: users can create, update, and delete events, as well as view event details.
- User Interaction: Users can join and leave events.
- Microservices Architecture: The application is designed with a microservices architecture, where there is a separate 'stats' service responsible for collecting data from the main service.

## Technologies
* Spring Boot
* Hibernate
* Docker
* Maven
* Lombok

## Database
- PostgreSQL
 
## Getting Started
To get started with this project:

1. Clone the [repository](https://github.com/vitaliibredun/Exploring.git) to your local machine.
2. Install Docker and Docker Compose if not already installed.
3. Navigate to the project directory and run `docker-compose up` to start the application and database containers.
4. Access the application via the provided [endpoints]().

## Dependencies

The list of dependencies here: [Dependencies]()

## Tests

The tests use a H2 database.
To run the tests, use the following command:

    mvn test

## ER diagram

The structure of a database here: [ER diagram]()

## API

The application exposes some of its functionality via an API. It is documented here: [API]()
