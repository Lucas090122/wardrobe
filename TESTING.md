# Unit Testing Documentation

## 1. Overview

This document describes the unit testing strategy, tools, scope, and coverage for the **Wardrobe** Android project.  
Our testing focuses on verifying **business logic**, **state management**, and **non-UI components**, following MVVM architecture best practices.

---

## 2. Testing Goals

Our testing verifies:

- Correct **state handling** across ViewModels  
- Accurate **business logic outputs**  
- Proper **Repository interactions** through mocks  
- Deterministic coroutine behavior  
- Behavior of **pure logic utilities**

We intentionally exclude Android framework code and components that require instrumentation tests.

---

## 3. Tools & Frameworks

| Tool | Purpose |
|------|---------|
| **JUnit4** | Core testing framework |
| **MockK** | Mocking and verifying interactions |
| **kotlinx-coroutines-test** | Controlling coroutine execution |
| **Truth** | Fluent assertion library |
| **Jacoco** | Code coverage reporting |

---

## 4. Included Scope (What We Test)

### ViewModels — Core Logic
Tested classes:

- `MainViewModel`
- `MemberViewModel`
- `StatisticsViewModel`
- `WardrobeViewModel`

Covered behavior includes:

- StateFlow emissions  
- NFC state machine  
- Transfer operations  
- Repository calls  
- Statistics calculations  
- Navigation request logic  
- Outdated item counting  

---

### Utilities — Pure Business Logic

- `WeatherRecommender.kt`

Covered:

- Temperature-to-clothing recommendation logic  
- Boundary values  
- Seasonal adjustments  

---

## 5. Excluded Scope (Not Unit-Tested)

### UI (Jetpack Compose)
Screens such as:

- `HomeScreen.kt`
- `SettingsScreen.kt`
- `ClothingCard.kt`
- `ItemDetailScreen.kt`

Reason: Compose UI requires **instrumented UI tests** (`androidTest`).

---

### Activity & Navigation
- `MainActivity.kt`
- `Navigation.kt`

Reason: Android lifecycle & NavHost depend on Android runtime.

---

### Database / Room / DAO / DataStore
- `AppDatabase.kt`
- `ClothesDao.kt`
- `SettingsRepository.kt`

Reason: Require integration-level testing.

---

### Remote Services / APIs
- `GeminiAiService.kt`
- `WeatherRepository.kt`

Reason: Depend on network, Android system services, or API stubs.

---

## 6. Test Structure

```
 com.example.wardrobe
 ├── MainViewModelTest.kt
 ├── MemberViewModelTest.kt
 ├── StatisticsViewModelTest.kt
 ├── WardrobeViewModelTest.kt
 └── util/
     └── WeatherRecommenderTest.kt
```

---

## 7. Running Tests

Run all JVM unit tests:

```bash
./gradlew testDebugUnitTest
```

---

## 8. Jacoco Coverage Report

Generate the report:
```bash
./gradlew jacocoTestReport
```
Report location:
```bash
app/build/reports/jacoco/jacocoTestReport/html/index.html
```

---

## 9. Coverage Resuls

### Overall Logic Coverage Summary
<img width="1114" height="182" alt="截屏2025-12-03 23 18 58" src="https://github.com/user-attachments/assets/b5027bad-fdbe-4410-8010-6b418e6451d9" />

---

### 10. Summary

Our testing strategy:
 - Tests all logic-bearing components
 - Excludes UI, Activity, Room, and remote APIs
 - Ensures predictable and verifiable state management
 - Uses Jacoco to report coverage only for testable modules
 - Produces clean, maintainable tests aligned with Android best practices

This ensures meaningful test coverage and high reliability for the project’s business logic.
