# Picture-transformation-encrypt App

## Setup the application:

### 1
Before we can start the application it is necessary to create the pictures table in the postgreSQL DB.

CREATE TABLE pictures (\
id SERIAL PRIMARY KEY,\
file_name VARCHAR(255),\
picture_data BYTEA\
);

### 2
After creating the table we must configure the application.properties for the database connection

spring.datasource.url=jdbc:postgresql://localhost:8888/localhost\
spring.datasource.username=postgres\
spring.datasource.password=test1234\

### 3
After configuring the DB connection it is time to start the application with the command:\
./mvnw spring-boot:run

Before the main program starts it creates a secretkey file into the specified folder (key),
if the key already exists then this step is skipped.

### 4
After seeing the\
Started PictureTransformationEncyptApplication line\
the Application is up and running!


