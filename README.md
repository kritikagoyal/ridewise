# Ride-Sharing LLD Project Brief

## 1. Learning Objectives

### OOP & SOLID

- **SRP (Single Responsibility Principle):** Use single-purpose classes such as `RideService`, `DriverService`, and `FareCalculator`.
- **OCP (Open/Closed Principle):** Support adding new ride-selection or pricing strategies without modifying core business logic.
- **LSP (Liskov Substitution Principle):** Any strategy implementation must remain interchangeable through its interface.
- **ISP (Interface Segregation Principle):** Keep interfaces small and focused, such as `RideMatchingStrategy` and `FareStrategy`.
- **DIP (Dependency Inversion Principle):** Services depend on abstractions/interfaces rather than concrete implementations.

### Design Principles

- **DRY:** Eliminate duplication, especially in ride allocation logic.
- **KISS:** Keep entity modeling and business logic simple.
- **YAGNI:** Build the MVP before introducing unnecessary features.
- **Law of Demeter:** Services should communicate directly with their collaborators and avoid deep method chains.

### LLD Skills

- Model domain entities and their relationships.
- Separate concerns across appropriate layers.
- Design scalable extension points using interfaces and composition.

---

## 2. Project Requirements

### A. Functional Requirements

The application must support:

1. Register riders.
2. Register drivers.
3. Show available drivers.
4. Request a ride.
5. Match a ride to a driver using a configurable strategy.
6. Calculate the fare using a configurable pricing strategy.
7. Track ride status:
   - `REQUESTED`
   - `ASSIGNED`
   - `COMPLETED`
   - `CANCELLED`

### B. Non-Functional Requirements

The design should provide:

- Easily extendable pricing algorithms.
- Easily replaceable driver-matching logic.
- Low coupling between services.
- Maintainable and readable code.

---

## 3. Domain Model

**Package:** `model/`

### Core Classes

#### Rider

Attributes:

- `id`
- `name`
- `location`

#### Driver

Attributes:

- `id`
- `name`
- `currentLocation`
- `available` — boolean

#### Ride

Attributes:

- `id`
- `rider`
- `driver`
- `distance`
- `status`

#### FareReceipt

Attributes:

- `rideId`
- `amount`
- `generatedAt`

### Enums

#### RideStatus

- `REQUESTED`
- `ASSIGNED`
- `COMPLETED`
- `CANCELLED`

#### VehicleType

- `BIKE`
- `AUTO`
- `CAR`

---

## 4. Strategy & Composition Design

The application should use the **Strategy Pattern** to make ride matching and fare calculation independently replaceable.

### 4.1 Ride Matching Strategy

```java
public interface RideMatchingStrategy {

    Driver findDriver(Rider rider, List<Driver> drivers);

}
```

#### Implementations

- `NearestDriverStrategy`
- `LeastActiveDriverStrategy`

The selected strategy determines which available driver is assigned to a rider.

### 4.2 Fare Calculation Strategy

```java
public interface FareStrategy {

    double calculateFare(Ride ride);

}
```

#### Implementations

- `DefaultFareStrategy`
- `PeakHourFareStrategy`

### Dependency Injection

Both strategies should be injected into `RideService` through its constructor.

For example:

```java
public RideService(
        RideMatchingStrategy matchingStrategy,
        FareStrategy fareStrategy) {
    // ...
}
```

This design demonstrates:

- **DIP** — `RideService` depends on interfaces.
- **OCP** — new strategies can be added without changing `RideService`.
- **Composition over inheritance** — behavior is composed through injected strategies.

---

## 5. Service Layer

**Package:** `service/`

### RiderService

Responsibilities:

- Register riders.
- Retrieve a rider by ID.

### DriverService

Responsibilities:

- Register drivers.
- Update driver availability.
- List available drivers.

### RideService

Responsibilities:

- Request a ride.
- Assign a driver using `RideMatchingStrategy`.
- Calculate fare using `FareStrategy`.
- Complete a ride.
- Maintain/update ride status.

The service layer should contain business logic. The console/UI layer should only handle user interaction and delegate operations to services.

---

## 6. Console Application

**Entry point:** `Main.java`

### Main Menu

```text
1. Add Rider
2. Add Driver
3. View Available Drivers
4. Request Ride
5. Complete Ride
6. View Rides
7. Exit
```

### Console Application Rules

Each menu option must:

- Use the service layer rather than directly manipulating domain objects or repositories.
- Catch and handle invalid user input.
- Keep presentation/input logic separate from business logic.
- Avoid tightly coupled logic.
- Provide clear feedback to the user.

---

## 7. Expected Architecture

A simple layered structure is recommended:

```text
src/
└── ...
    ├── model/
    │   ├── Rider.java
    │   ├── Driver.java
    │   ├── Ride.java
    │   ├── FareReceipt.java
    │   ├── RideStatus.java
    │   └── VehicleType.java
    │
    ├── strategy/
    │   ├── RideMatchingStrategy.java
    │   ├── NearestDriverStrategy.java
    │   ├── LeastActiveDriverStrategy.java
    │   ├── FareStrategy.java
    │   ├── DefaultFareStrategy.java
    │   └── PeakHourFareStrategy.java
    │
    ├── service/
    │   ├── RiderService.java
    │   ├── DriverService.java
    │   └── RideService.java
    │
    └── Main.java
```

---

## 8. Design Goals

The final implementation should demonstrate that:

- Domain models represent business entities rather than business workflows.
- Services own business operations and orchestration.
- Strategies encapsulate algorithms that are likely to change.
- Interfaces provide extension points.
- Dependencies are injected rather than instantiated deep inside services.
- Adding a new matching or pricing strategy requires minimal or no changes to existing service logic.
- The console application remains thin and focused on input/output.

---

## 9. MVP Scope

Focus on the core ride-sharing workflow first:

```text
Register Rider
      ↓
Register Driver
      ↓
View Available Drivers
      ↓
Request Ride
      ↓
Find Driver using Strategy
      ↓
Calculate Fare using Strategy
      ↓
Ride Assigned
      ↓
Complete Ride
      ↓
Fare Receipt
```

Avoid introducing unnecessary features until the MVP is complete. Potential future enhancements such as persistence, authentication, payments, notifications, surge pricing rules, and real-time location tracking are outside the initial scope.
