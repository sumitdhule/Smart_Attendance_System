# Smart Attendance System - Face Recognition Based

A comprehensive face recognition-based smart attendance system built with Spring Boot, React.js, Oracle Database, and OpenCV.

## 🚀 Features

- **Face Recognition**: Automatic attendance marking using OpenCV face detection
- **Real-time Processing**: Live camera feed with instant face recognition
- **User Management**: Complete CRUD operations for user registration
- **Daily Reports**: Comprehensive attendance reports with export functionality
- **Multi-face Registration**: Register multiple face images per user for better accuracy
- **Status Tracking**: Automatic status assignment (Present, Late, Half Day)

## 📋 Technology Stack

### Backend
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Oracle Database 19c/21c
- OpenCV 4.7.0 (Java binding)

### Frontend
- React.js 18
- Bootstrap 5
- Axios for API calls
- React Webcam for camera access

## 🏗️ Project Structure

```
SmartAttendanceSystem/
├── src/main/java/com/attendance/
│   ├── SmartAttendanceApplication.java    # Main application
│   ├── config/                            # Configuration classes
│   │   ├── OpenCVConfig.java
│   │   └── WebConfig.java
│   ├── controller/                        # REST Controllers
│   │   ├── UserController.java
│   │   └── AttendanceController.java
│   ├── entity/                           # JPA Entities
│   │   ├── User.java
│   │   ├── FaceData.java
│   │   └── Attendance.java
│   ├── repository/                       # Data Access Layer
│   │   ├── UserRepository.java
│   │   ├── FaceDataRepository.java
│   │   └── AttendanceRepository.java
│   ├── service/                          # Business Logic
│   │   ├── UserService.java
│   │   ├── FaceRecognitionService.java
│   │   └── AttendanceService.java
│   ├── dto/                              # Data Transfer Objects
│   │   ├── UserDTO.java
│   │   ├── AttendanceDTO.java
│   │   └── ApiResponse.java
│   └── exception/                        # Exception Handling
│       ├── ResourceNotFoundException.java
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.properties            # Application configuration
│   ├── schema.sql                        # Database schema
│   ├── data.sql                          # Sample data
│   └── haarcascade_frontalface_default.xml
├── frontend/                             # React Frontend
│   ├── src/
│   │   ├── components/
│   │   │   ├── attendance/
│   │   │   ├── users/
│   │   │   └── reports/
│   │   ├── services/
│   │   └── App.js
│   └── package.json
└── pom.xml                               # Maven dependencies
```

## ⚙️ Setup Instructions

### Prerequisites
- Java JDK 17+
- Maven 3.8+
- Oracle Database 19c/21c
- Node.js 18+
- Eclipse IDE (recommended)

### Database Setup

1. Connect to Oracle as SYSDBA:
```sql
sqlplus / as sysdba
```

2. Create user and grant permissions:
```sql
CREATE USER attendance_user IDENTIFIED BY your_password;
GRANT CONNECT, RESOURCE, CREATE TABLE, CREATE SEQUENCE TO attendance_user;
```

3. Update `application.properties` with your credentials.

### Backend Setup (Eclipse IDE)

1. Import the project:
   - File → Import → Maven → Existing Maven Projects
   - Select the `SmartAttendanceSystem` folder
   - Click Finish

2. Wait for Maven to download dependencies.

3. Run the application:
   - Right-click on `SmartAttendanceApplication.java`
   - Run As → Java Application

4. Verify at: `http://localhost:8080/api/users`

### Frontend Setup

```bash
cd frontend
npm install
npm start
```

The React app will start at `http://localhost:3000`

## 📡 API Endpoints

### User Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create new user |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |
| POST | `/api/users/{id}/face` | Register face images |

### Attendance Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/attendance/mark` | Mark attendance (face image) |
| GET | `/api/attendance/today` | Today's attendance |
| GET | `/api/attendance/daily?date=YYYY-MM-DD` | Daily attendance |
| GET | `/api/attendance/report` | Date range report |
| POST | `/api/attendance/checkout/{userId}` | Manual checkout |

## 🔧 Configuration

### application.properties
```properties
# Database
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=attendance_user
spring.datasource.password=your_password

# Face Recognition
face.recognition.threshold=0.6
face.recognition.images.per.user=5
```

## 🎯 How to Use

1. **Register Users**: Go to "Register User" and fill in the details
2. **Capture Faces**: During registration, capture 3-5 face images
3. **Mark Attendance**: Click "Mark Attendance" and face the camera
4. **View Reports**: Check daily attendance and export reports

## 📝 Notes

- Make sure camera permissions are granted in the browser
- For better accuracy, capture face images in good lighting
- OpenCV native library must be properly loaded (handled by openpnp dependency)

## 📄 License

This project is for educational purposes.

## 👨‍💻 Author

Your Name - Student Project
