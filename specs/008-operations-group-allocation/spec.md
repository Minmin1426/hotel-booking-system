# Feature Specification: 008-operations-group-allocation

**Feature Branch**: `008-operations-group-allocation`  
**Created**: 2026-07-22  
**Status**: Approved & Completed

---

# User Scenarios & Testing

## User Story 1 - Hotel Dashboard (Priority: P1)

As a Hotel Partner, I want to view the hotel operation dashboard so that I can monitor occupancy, revenue, and upcoming group bookings.

### Why this priority

The dashboard provides an overview of hotel operations and helps partners make operational decisions. Without this feature, partners cannot efficiently monitor hotel performance.

### Independent Test

- Login as Hotel Partner.
- Open Dashboard.
- Verify occupancy rate, revenue, available rooms and upcoming groups are displayed correctly.

### Acceptance Scenarios

#### Scenario: Display dashboard successfully

**Given** the partner has permission to access the hotel

**When** opening the dashboard

**Then**

- Occupancy rate is displayed.
- Revenue summary is displayed.
- Upcoming group bookings are displayed.
- Available room statistics are displayed.

---

## User Story 2 - Manage Hotel Registration & Restaurant Information (Priority: P1)

As a Hotel Partner, I want to register hotel information and restaurant facilities so that the hotel can provide accommodation and meal services.

### Why this priority

Hotel registration is required before rooms and restaurant services can be configured.

### Independent Test

- Create a new hotel profile.
- Update restaurant information.
- Verify data is saved successfully.

### Acceptance Scenarios

#### Scenario: Register hotel successfully

**Given** a partner has not registered a hotel

**When** submitting valid information

**Then**

- Hotel information is saved.
- Restaurant information is saved.
- Hotel status becomes Pending Approval.

---

## User Story 3 - Manage Room Inventory & Room Matrix (Priority: P1)

As a Hotel Partner, I want to configure room quotas and monitor room status so that I can manage room availability efficiently.

### Why this priority

Room inventory directly affects booking availability and hotel operation.

### Independent Test

- Configure room allotment.
- View Room Matrix.
- Change room status.

### Acceptance Scenarios

#### Scenario: Update room status

**Given** a room exists

**When** changing room status

**Then**

- Room status is updated.
- Room Matrix refreshes correctly.

---

## User Story 4 - Group Room Allocation (Priority: P1)

As a Hotel Partner, I want to allocate rooms automatically for group bookings so that manual room assignment is minimized.

### Why this priority

Group bookings often contain many guests, making manual assignment inefficient.

### Independent Test

- Select a group booking.
- Execute Auto Allocate.
- Verify all guests receive valid rooms.

### Acceptance Scenarios

#### Scenario: Auto allocate rooms

**Given** sufficient available rooms exist

**When** clicking Allocate

**Then**

- Guests are assigned rooms.
- Capacity validation passes.
- No duplicated room assignment exists.

---

## User Story 5 - Restaurant Meal Package Management (Priority: P2)

As a Restaurant Manager, I want to manage meal packages and verify meal tickets so that restaurant services are controlled effectively.

### Why this priority

Meal packages are optional services and can be implemented after core hotel operations.

### Independent Test

- Create meal package.
- Generate QR ticket.
- Scan QR ticket.

### Acceptance Scenarios

#### Scenario: Scan meal ticket

**Given** a valid QR ticket

**When** restaurant staff scans the ticket

**Then**

- Ticket status changes to Used.
- Remaining quantity decreases.
- Duplicate scan is rejected.

---

## User Story 6 - Dynamic Pricing & Group Discount (Priority: P2)

As a Hotel Partner, I want to configure pricing rules so that room prices change automatically based on business policies.

### Why this priority

Flexible pricing helps maximize hotel revenue.

### Independent Test

- Configure weekend surcharge.
- Configure group discount.
- Simulate room pricing.

### Acceptance Scenarios

#### Scenario: Calculate room price

**Given** pricing rules exist

**When** requesting price calculation

**Then**

- Weekend surcharge is applied.
- Group discount is calculated.
- Final room price is returned correctly.

---

## User Story 7 - Cancellation Approval (Priority: P2)

As a Hotel Partner, I want to review cancellation requests so that refund decisions follow hotel policies.

### Why this priority

Cancellation approval affects hotel revenue but is secondary to booking operations.

### Independent Test

- Open pending requests.
- Approve cancellation.
- Reject cancellation.

### Acceptance Scenarios

#### Scenario: Approve cancellation

**Given** a cancellation request exists

**When** approving the request

**Then**

- Booking status becomes Cancelled.
- Refund request is generated.
- Customer receives notification.

---

## User Story 8 - Hotel Approval (Priority: P2)

As an Admin, I want to approve hotel registration so that only verified hotels appear on the platform.

### Why this priority

Hotel verification ensures service quality and legal compliance.

### Independent Test

- Review hotel information.
- Approve hotel.
- Reject hotel.

### Acceptance Scenarios

#### Scenario: Approve hotel

**Given** a hotel is pending approval

**When** admin approves

**Then**

- Hotel status becomes Approved.
- Hotel becomes visible to customers.

---

# Edge Cases

- Room quota exceeds hotel capacity.
- Auto allocation cannot find enough rooms.
- Duplicate QR meal ticket scanning.
- Hotel registration missing required certificates.
- Pricing rules overlap.
- Cancellation after check-in.
- Room status changed while allocation is processing.
- Admin rejects hotel due to invalid documents.