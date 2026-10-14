# Public Transport Management System (PTMS)

A modular, framework-free Java 17 console application engineered for academic demonstration of five classic Gang of Four (GoF) design patterns: **Prototype**, **Memento**, **Template Method**, **Builder**, and **Mediator**.

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/24waghelay-prog/public-transport-management-system)
[![Deploy on Railway](https://railway.app/button.svg)](https://railway.app/new/template?template=https%3A%2F%2Fgithub.com%2F24waghelay-prog%2Fpublic-transport-management-system)
[![Deploy to Koyeb](https://www.koyeb.com/static/images/deploy/button.svg)](https://app.koyeb.com/deploy?type=git&repository=github.com/24waghelay-prog/public-transport-management-system&branch=main)

---

## 1. Project Overview

The Public Transport Management System simulates the core operations of a municipal multi-modal transport network (Buses, Metro lines, and Regional Trains). Built strictly using standard Java 17 constructs (Records, Streams, Deques, Atomic counters), it demonstrates how classic design patterns solve real-world architectural challenges:

- **Builder**: Eliminates telescoping constructors and enforces multi-rule validation when constructing multi-stop transit routes and passenger tickets.
- **Prototype**: Clones vehicle specifications and schedules via deep copy without reliance on Java's problematic `Cloneable` or `Object.clone()`.
- **Template Method**: Enforces an invariant ticket booking algorithm (`validate` → `calculateFare` → `reserveSeat` → `processPayment` → `issueTicket` → `afterBooking` hook) while allowing transit modes to customize pricing and seat reservations.
- **Mediator**: Completely decouples buses, train stations, and drivers by routing all events (delays, mechanical breakdowns, standby vehicle dispatch) through a central control mediator.
- **Memento**: Implements snapshot-based state capture using immutable Java records, with a two-stack Caretaker enabling undo and redo of schedule mutations.

---

## 2. Pattern to Class Mapping

| Design Pattern | GoF Role | Class / Component | Package | Responsibility |
| :--- | :--- | :--- | :--- | :--- |
| **Builder** | Builder | `RouteBuilder` | `transport.builder` | Fluent builder validating route integrity (>= 2 stops, positive fare, frequency). |
| | Product | `Route` | `transport.builder` | Immutable product storing ordered stops, timing slots, and fare rules. |
| | Builder | `TicketBuilder` | `transport.builder` | Fluent builder with sensible defaults (auto ID, cash mode, 0% discount). |
| | Product | `Ticket` | `transport.builder` | Passenger travel ticket supporting cancellation. |
| **Prototype** | Prototype | `Vehicle` | `transport.prototype` | Abstract base specifying `public abstract Vehicle copy()`. |
| | Concrete Prototype | `BusVehicle` | `transport.prototype` | Deep copy of features list and bus specifications. |
| | Concrete Prototype | `Schedule` | `transport.prototype` | Deep copy of departure timings and route stops for cloning. |
| | Prototype Registry | `VehicleRegistry` | `transport.prototype` | Stores named prototypes (`AC Bus`, `Non-AC Bus`, `Electric Bus`). |
| **Template Method** | Abstract Class | `BookingProcess` | `transport.template` | Defines `final book()` workflow, common validation, and `afterBooking()` hook. |
| | Concrete Class | `BusBooking` | `transport.template` | Per-stop distance pricing with window/aisle seat assignment. |
| | Concrete Class | `MetroBooking` | `transport.template` | Distance slab pricing (0-5km, 5-15km, etc.) and unreserved standing allocation. |
| | Concrete Class | `TrainBooking` | `transport.template` | Class-tiered pricing (Sleeper, 3AC, 1AC) and coach berth assignment. |
| **Mediator** | Mediator (Interface) | `ControlCenter` | `transport.mediator` | Contract defining `notify(sender, event, data)` and registration hooks. |
| | Concrete Mediator | `TransportControlCenter`| `transport.mediator` | Coordinates delays, dispatches replacement buses to drivers, and updates station boards. |
| | Colleague | `Bus` | `transport.mediator` | Communicates incidents (delays, breakdowns) exclusively through mediator. |
| | Colleague | `Station` | `transport.mediator` | Receives real-time electronic passenger display board announcements. |
| | Colleague | `Driver` | `transport.mediator` | Receives emergency vehicle assignments from control center. |
| **Memento** | Memento | `ScheduleMemento` | `transport.memento` | Immutable Java record snapshot capturing schedule state. |
| | Originator | `Schedule` | `transport.prototype` | Originates snapshots via `createMemento()` and restores via `restore()`. |
| | Caretaker | `ScheduleHistory` | `transport.memento` | Manages undo/redo history using two `Deque` stacks; clears redo on new edit. |

---

## 3. Package Structure

```
transport/
├── builder/
│   ├── Route.java              # Product (Route)
│   ├── RouteBuilder.java       # Fluent Builder with integrity validation
│   ├── Ticket.java             # Product (Ticket)
│   └── TicketBuilder.java      # Fluent Builder with sensible defaults
├── prototype/
│   ├── Vehicle.java            # Abstract Prototype with copy()
│   ├── BusVehicle.java         # Concrete Prototype
│   ├── VehicleRegistry.java    # Prototype Registry
│   └── Schedule.java           # Concrete Prototype & Memento Originator
├── template/
│   ├── BookingRequest.java     # Request parameter record
│   ├── BookingProcess.java     # Abstract Template Method skeleton
│   ├── BusBooking.java         # Concrete per-stop booking
│   ├── MetroBooking.java       # Concrete distance-slab booking
│   └── TrainBooking.java       # Concrete class-tiered booking
├── mediator/
│   ├── ControlCenter.java      # Mediator interface
│   ├── TransportControlCenter.java # Concrete Mediator coordinating colleagues
│   ├── Bus.java                # Colleague (Bus)
│   ├── Station.java            # Colleague (Station display)
│   └── Driver.java             # Colleague (Driver dispatch)
├── memento/
│   ├── ScheduleMemento.java    # Immutable snapshot record
│   └── ScheduleHistory.java    # Caretaker (undo/redo Deque stacks)
├── TransportSystem.java        # In-memory service & seed data manager
├── MenuHandler.java            # Console UI handlers (Builder & Prototype)
├── SimulationHandler.java      # Console UI handlers (Template, Memento & Mediator)
└── Main.java                   # Application entry point
```

---

## 4. How to Build and Run

### Prerequisites
- **Java Development Kit (JDK) 17+** (tested and verified on Java 17 and Java 27)
- **Apache Maven 3.8+**

### Compile and Package
```bash
mvn clean package
```

### Run All Unit Tests
```bash
mvn test
```

### Run the Application
Execute the packaged standalone JAR:
```bash
java -jar target/public-transport-management-system-1.0.0.jar
```
When launched, PTMS runs both the interactive console menu in your terminal and starts an embedded web dashboard:
- **Interactive Web Dashboard**: [http://localhost:8085](http://localhost:8085)
- **Interactive Terminal UI**: Console menu options 1–7.

### Containerized Deployment (Docker)
A production multi-stage `Dockerfile` and `docker-compose.yml` are provided.
```bash
# Build and run with Docker Compose
docker compose up --build -d

# Or build and run standalone container
docker build -t ptms-app .
docker run -d -p 8085:8085 --name ptms-container ptms-app
```



---

## 5. Sample Console Output

```text
=============================================================
     PUBLIC TRANSPORT MANAGEMENT SYSTEM (PTMS)
       GoF Design Patterns Demonstration in Java 17
=============================================================

----------------- MAIN MENU -----------------
1. Add / View Routes (Builder Pattern)
2. Clone Route or Schedule (Prototype Pattern)
3. Add Vehicles from Templates (Prototype Registry)
4. Book / Cancel Ticket (Template Method Pattern)
5. Edit Schedule with Undo / Redo (Memento Pattern)
6. Simulate Delay / Breakdown (Mediator Pattern)
7. Exit
---------------------------------------------
Enter choice (1-7): 1

--- [Builder Pattern] Route Management ---
1. View Existing Routes
2. Add New Route (using RouteBuilder)
Select choice: 1
 • [RT-101] 'Airport Express' | Stops (4): [Central Station, Tech Park, Airport T1, Airport T2] | BaseFare: $20.00 | +$5.00/stop | Every 30 min
 • [RT-202] 'Metro Blue Line' | Stops (4): [North Terminal, Market Square, City Center, South Gate] | BaseFare: $15.00 | +$3.50/stop | Every 10 min

----------------- MAIN MENU -----------------
Enter choice (1-7): 2

--- [Prototype Pattern] Prototype Cloning ---
1. Clone Schedule (e.g. Weekday -> Weekend)
2. Clone Existing Fleet Vehicle
Select choice: 1
Available Schedules:
 - SCH-101-WD : [SCH-101-WD] Route: 'Airport Express' | Day: Weekday | Vehicle: BUS-VOLVO-01 | Times: [06:00, 09:00, 14:00, 18:00] | Stops: [Central Station, Tech Park, Airport T1, Airport T2] | Active: true
Enter Schedule ID to clone: SCH-101-WD
Enter new Schedule ID: SCH-101-WE
Enter new Day Type (e.g. Weekend): Weekend
 Schedule cloned via Prototype copy() [Deep Copy Verified]:
 Original: [SCH-101-WD] Route: 'Airport Express' | Day: Weekday | Vehicle: BUS-VOLVO-01 | Times: [06:00, 09:00, 14:00, 18:00] | Stops: [Central Station, Tech Park, Airport T1, Airport T2] | Active: true
 Cloned  : [SCH-101-WE] Route: 'Airport Express' | Day: Weekend | Vehicle: BUS-VOLVO-01 | Times: [06:00, 09:00, 14:00, 18:00, 20:30] | Stops: [Central Station, Tech Park, Airport T1, Airport T2] | Active: true

----------------- MAIN MENU -----------------
Enter choice (1-7): 4

--- [Template Method & Builder] Ticket Booking ---
1. Book Ticket
2. Cancel Ticket
3. View All Tickets
Select choice: 1
Transport Mode (BUS / METRO / TRAIN): BUS
Passenger Name: Diana Prince
Source Station: Central Station
Destination Station: Tech Park
 Ticket issued successfully via Template Method: [TKT-1001] Type: BUS | Passenger: Diana Prince | Route: Central Station -> Tech Park | Seat: Bus-Seat-2 (Window) | Fare: $22.50 | Mode: CARD | Status: CONFIRMED

----------------- MAIN MENU -----------------
Enter choice (1-7): 5

--- [Memento Pattern] Schedule Editor (Undo/Redo) ---
Actions: 1. Edit Departure Time | 2. Undo Last Edit | 3. Redo Undone Edit
Choice: 1
Enter Schedule ID: SCH-101-WD
Enter new departure time to add (e.g. 19:45): 21:15
 Edited schedule. Snapshot pushed to Memento Caretaker.
 Current State: [SCH-101-WD] Route: 'Airport Express' | Day: Weekday | Vehicle: BUS-VOLVO-01 | Times: [06:00, 09:00, 14:00, 18:00, 21:15] | Stops: [Central Station, Tech Park, Airport T1, Airport T2] | Active: true

Choice: 2
 Undo successful! Restored state from Memento: [SCH-101-WD] Route: 'Airport Express' | Day: Weekday | Vehicle: BUS-VOLVO-01 | Times: [06:00, 09:00, 14:00, 18:00] | Stops: [Central Station, Tech Park, Airport T1, Airport T2] | Active: true

Choice: 3
 Redo successful! Restored state from Memento: [SCH-101-WD] Route: 'Airport Express' | Day: Weekday | Vehicle: BUS-VOLVO-01 | Times: [06:00, 09:00, 14:00, 18:00, 21:15] | Stops: [Central Station, Tech Park, Airport T1, Airport T2] | Active: true

----------------- MAIN MENU -----------------
Enter choice (1-7): 6

--- [Mediator Pattern] Transport Control Center Simulation ---
1. Simulate Bus Delay
2. Simulate Bus Breakdown & Emergency Replacement
3. View Station Boards & Alerts
Choice: 1
Enter Bus ID (e.g. BUS-VOLVO-01): BUS-VOLVO-01
Enter Delay in minutes: 15
 Bus reported delay through ControlCenter Mediator.

Choice: 2
Enter Bus ID (e.g. BUS-VOLVO-01): BUS-VOLVO-01
Enter Breakdown Reason: Engine overheat
 Breakdown reported! Mediator dispatched replacement vehicle and notified driver.

Choice: 3
Station Electronic Display Boards:
 [Central Station Board]
    ALERT: Bus BUS-VOLVO-01 (Route RT-101) delayed by 15 mins.
    SERVICE UPDATE: Bus BUS-VOLVO-01 broken down (Engine overheat). Replacement Bus EV-STANDBY-01 deployed.
 [Airport T1 Board]
    ALERT: Bus BUS-VOLVO-01 (Route RT-101) delayed by 15 mins.
    SERVICE UPDATE: Bus BUS-VOLVO-01 broken down (Engine overheat). Replacement Bus EV-STANDBY-01 deployed.

Passenger Push Notification Feed:
    [Push Notification] ALERT: Bus BUS-VOLVO-01 (Route RT-101) delayed by 15 mins.
    [Passenger Alert] SERVICE UPDATE: Bus BUS-VOLVO-01 broken down (Engine overheat). Replacement Bus EV-STANDBY-01 deployed.
```

---

## 6. Verification & Quality Assurance

- **Unit Test Coverage**: Comprehensive JUnit 5 suites for all five patterns (`BuilderTest`, `PrototypeTest`, `TemplateMethodTest`, `MediatorTest`, `MementoTest`).
- **Clean Architecture**: Every Java class is strictly below 150 lines, adhering to single responsibility.
- **Deep Immutability & Defensive Copying**: Prevents state pollution across prototypes and mementos.

---

## 7. Persistent Accessibility & Cloud Deployment

PTMS is architected to remain **100% accessible even when you close your terminal, exit Antigravity IDE, or deploy to the cloud**.

### Option A: Local Background Daemon (Survives Terminal & Antigravity Exit)

Run the application as an independent, decoupled Windows background service with an automated Cloudflare public tunnel:

```powershell
# Start Java server and public tunnel in background
.\start-service.bat
# or in PowerShell:
.\start-service.ps1
```

- **Independent Life Cycle**: Uses Windows WMI detached process creation (`javaw.exe`). You can safely **close the terminal and close Antigravity IDE**; the app and tunnel stay running!
- **Check Status & Public Link**:
  ```powershell
  .\status-service.bat
  ```
- **Stop Service**:
  ```powershell
  .\stop-service.bat
  ```

---

### Option B: 24/7 Cloud Deployment (Runs Even When Computer Is Off)

To make PTMS permanently accessible on the web without keeping your computer on:

#### 1. Deploy on Render.com (100% Free)
1. Push this repository to GitHub.
2. Sign in to [Render.com](https://render.com) and click **New** > **Blueprint** (or **Web Service**).
3. Connect your GitHub repository.
4. Render automatically detects `render.yaml` or `Dockerfile`.
5. Click **Apply / Deploy**.
6. Render builds the container and provides a permanent, free 24/7 HTTPS URL (e.g., `https://ptms-service.onrender.com`).

#### 2. Deploy on Railway.app
1. Push repository to GitHub.
2. In [Railway.app](https://railway.app), click **New Project** > **Deploy from GitHub repo**.
3. Railway automatically detects `Dockerfile` and `railway.toml`.
4. Add a public domain under **Settings > Networking > Generate Domain**.

#### 3. Run in Docker (Detached Background Daemon)
```bash
docker compose up -d
```
The container runs in the background (`restart: unless-stopped`) and remains accessible at `http://localhost:8085`.

