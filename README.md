# Weather Information by Pincode  

This project provides weather information based on pincode and date. It can be built and run both with and without Docker.  

## 🌟 Features  
- Get current weather data by pincode  
- Fetch historical weather data by date  
- Built with Java/Spring Boot  
- Uses PostgreSQL for data storage  
- Redis for caching  
- Docker support for easy deployment  

## 🛠️ Prerequisites  

### With Docker:  
- Docker installed  
- Docker Compose installed  
- `.env` file configured  

### Without Docker:  
- Java 17+  
- Maven 3.x+  
- PostgreSQL database  
- Redis service  
- `.env` file configured  

## ⚙️ Environment Configuration  

Create a `.env` file in the root directory with these variables:  

```bash
# Database
DB_NAME=weather_db
DB_USERNAME=user
DB_PASSWORD=password
DB_URL=jdbc:postgresql://postgres:5432/weather_db
DB_HOST=postgres

# API Keys
GEO_CODING_API_KEY=your_geocoding_api_key_here
GEO_CODING_API_URL=http://api.openweathermap.org/geo/1.0/zip
CURRENT_WEATHER_API_URL=https://api.open-meteo.com/v1/forecast
OLD_WEATHER_API_URL=https://archive-api.open-meteo.com/v1/archive

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# App
APP_PORT=8080
```

## 🐳 Docker Setup  

### 1. Build and Run  
```bash
docker-compose build
docker-compose up
```

### 2. Access the Application  
The app will be available at:  
[http://localhost:8080](http://localhost:8080)  

### 3. Stop Containers  
```bash
docker-compose down
```

## 💻 Local Setup (Without Docker)  

### 1. Build the Project  
```bash
mvn clean package -DskipTests
```

### 2. Run PostgreSQL & Redis  
Ensure these services are running (or use Docker just for these):  
```bash
docker-compose -f compose.yml up postgres redis
```

### 3. Run the Application  
```bash
java -jar target/weather-info-pincode-0.0.1-SNAPSHOT.jar
```

### 4. Access the Application  
[http://localhost:8080](http://localhost:8080)  

---

## 📌 Notes  
- Change `APP_PORT` in `.env` if port 8080 is occupied  
- The `compose.yml` file contains just PostgreSQL and Redis configurations for local development  

Enjoy using the Weather Information Service! 🌤️