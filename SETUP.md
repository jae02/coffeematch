# COFFEEMATCH Setup Guide

## System Requirements
- **Java**: JDK 17+ (Required for Backend)
- **Maven**: 3.x+ (Required for Backend)
- **Node.js**: 18+ (Required for Frontend)

## Quick Start
### Frontend (Vue.js)
The frontend has been pre-configured. To start:
1. Open a terminal in `frontend/`.
2. Run `npm run dev`.
3. Open the link provided (usually `http://localhost:5173`).

### Backend (Spring Boot)
**Note**: Maven (`mvn`) is currently missing on this system.

### Docker Support (Windows/Mac)
This project includes Docker support for cross-platform compatibility.
- Ensure you have Docker Installed (Docker Desktop for Mac/Windows).
- **Apple Silicon (M1/M2/M3)**: The default `mysql:8.0` image usually works with emulation. If you encounter issues, try adding `platform: linux/amd64` to the db service in `docker-compose.yml`.


1. **Install Maven**: Download and install Apache Maven, or ensure `mvn` is in your PATH.
2. Open a terminal in `backend/`.
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run the server:
   ```bash
   mvn spring-boot:run
   ```

## Development Notes
- The Frontend expects the Backend to be running for API calls.
- Run both concurrently in separate terminals.
