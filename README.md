# Lab5_Alarm_Fitness

## Overview

This repository contains the Lab 5 Android mobile development projects.

Unlike previous labs that focus on a single application, this repository is organized into five separate Android project folders:

- `1.AS`
- `2.ASL`
- `3.Recap`
- `4.Alarm`
- `5.Track`

The main purpose of this lab is to practice asynchronous tasks, loaders, background execution, Android services, alarm scheduling, notifications, local storage, and fitness tracking application development.

---

## Folder Summary

| Folder | Description |
|---|---|
| `1.AS` | AsyncTask practice project |
| `2.ASL` | AsyncTaskLoader and Google Books search project |
| `3.Recap` | Recap project for background task, foreground service, bound service, and AsyncTask |
| `4.Alarm` | Alarm application with scheduling, notification, storage, and receiver logic |
| `5.Track` | Fitness tracking application with services, local data layer, AI coach structure, and Wear sync structure |

---

## Features

- Five separate Android practice projects
- AsyncTask demonstration
- AsyncTaskLoader demonstration
- Google Books API search
- Background task practice
- Foreground service practice
- Bound service practice
- Alarm scheduling
- Alarm notification
- Alarm enable / disable function
- Alarm repeat day support
- Local alarm storage
- Fitness dashboard
- Fitness calculation task
- Local fitness data layer
- Step repository and database structure
- AI fitness coach structure
- Wear sync manager structure

---

## Tech Stack

- Java
- Kotlin
- Android Studio
- Gradle / Gradle Kotlin DSL
- XML Layout
- Jetpack Compose
- Material 3
- AsyncTask
- AsyncTaskLoader
- LoaderManager
- Coroutine
- Android Service
- Foreground Service
- Bound Service
- BroadcastReceiver
- Notification
- Alarm scheduling
- Local database / DAO / Repository structure
- Wear sync structure

---

## Project Structure

```text
Lab5_Alarm_Fitness/
│
├── 1.AS/
│   ├── app/
│   │   └── src/main/java/...
│   │       ├── MainActivity.java
│   │       └── SimpleAsyncTask.java
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradlew
│   └── gradlew.bat
│
├── 2.ASL/
│   ├── app/
│   │   └── src/main/java/...
│   │       ├── BookLoader.java
│   │       ├── BookSearchResult.java
│   │       ├── MainActivity.java
│   │       └── NetworkUtils.java
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradle.properties.example
│   ├── gradlew
│   └── gradlew.bat
│
├── 3.Recap/
│   ├── app/
│   │   └── src/main/java/...
│   │       ├── BackgroundTaskService.kt
│   │       ├── CountingBoundService.kt
│   │       ├── CountingForegroundService.kt
│   │       └── MainActivity.kt
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradlew
│   └── gradlew.bat
│
├── 4.Alarm/
│   ├── app/
│   │   └── src/main/java/...
│   │       ├── AlarmItem.kt
│   │       ├── AlarmNotifications.kt
│   │       ├── AlarmReceiver.kt
│   │       ├── AlarmScheduler.kt
│   │       ├── AlarmStorage.kt
│   │       ├── BootReceiver.kt
│   │       └── MainActivity.kt
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradlew
│   └── gradlew.bat
│
└── 5.Track/
    ├── app/
    │   └── src/main/java/...
    │       ├── ai/
    │       │   └── GeminiFitnessCoach.kt
    │       ├── async/
    │       │   └── FitnessCalculationTask.kt
    │       ├── data/
    │       │   ├── FitnessDatabase.kt
    │       │   ├── StepDao.kt
    │       │   ├── StepEntity.kt
    │       │   └── StepRepository.kt
    │       ├── model/
    │       │   ├── Badge.kt
    │       │   └── FitnessResult.kt
    │       ├── service/
    │       │   ├── FitnessBoundService.kt
    │       │   ├── StepBackgroundService.kt
    │       │   └── StepForegroundService.kt
    │       ├── ui/
    │       │   └── FitnessDashboardScreen.kt
    │       ├── wear/
    │       │   └── WearSyncManager.kt
    │       ├── FitnessViewModel.kt
    │       └── MainActivity.kt
    ├── build.gradle.kts
    ├── settings.gradle.kts
    ├── gradlew
    └── gradlew.bat
```

---

## Main Components

## 1. AS - AsyncTask Project

The `1.AS` folder is a basic AsyncTask practice project.

### MainActivity.java

`MainActivity.java` is the main screen of the app.

It starts the asynchronous task and updates the UI while the task is running.

### SimpleAsyncTask.java

`SimpleAsyncTask.java` demonstrates how AsyncTask works.

It simulates a background task by sleeping for a random amount of time and then updates the TextView when the task finishes.

Main responsibilities:

- Run background work
- Simulate a waiting task
- Return a result to the UI thread
- Update the TextView after completion

---

## 2. ASL - AsyncTaskLoader Project

The `2.ASL` folder is an AsyncTaskLoader project.

This project searches book information from the Google Books API and displays the result in the Android app.

### MainActivity.java

`MainActivity.java` handles the search UI.

Main responsibilities:

- Get user input
- Check whether the search query is empty
- Check network connection
- Start the loader
- Display loading state
- Display book title and author result
- Display error messages when no result is found

### BookLoader.java

`BookLoader.java` extends `AsyncTaskLoader`.

It loads book information in the background by calling `NetworkUtils`.

### NetworkUtils.java

`NetworkUtils.java` builds the Google Books API URL and sends the network request.

### BookSearchResult.java

`BookSearchResult.java` stores the parsed search result, such as book title and author.

---

## 3. Recap - Background and Service Recap Project

The `3.Recap` folder is a recap project for Android background execution.

It demonstrates different ways to run background or service-based tasks in Android.

### BackgroundTaskService.kt

`BackgroundTaskService.kt` demonstrates a background task service.

It uses a coroutine to count numbers in the background and stops itself after the task is completed.

### CountingForegroundService.kt

`CountingForegroundService.kt` demonstrates a foreground service.

A foreground service can keep running while showing a persistent notification to the user.

### CountingBoundService.kt

`CountingBoundService.kt` demonstrates a bound service.

It allows the activity to bind to the service and receive service data.

### MainActivity.kt

`MainActivity.kt` provides the user interface to test different background execution methods.

It may include actions such as:

- Start background task
- Start foreground service
- Bind to bound service
- Unbind from bound service
- Run AsyncTask demo

---

## 4. Alarm - Alarm Application

The `4.Alarm` folder is an alarm scheduling application.

The app allows users to create, edit, enable, disable, and delete alarms.

### AlarmItem.kt

`AlarmItem.kt` defines the alarm data model.

It stores information such as:

- Alarm ID
- Hour
- Minute
- Label
- Repeat days
- Enabled status

### AlarmScheduler.kt

`AlarmScheduler.kt` handles alarm scheduling logic.

Main responsibilities:

- Schedule alarms
- Cancel alarms
- Reschedule alarms when needed

### AlarmReceiver.kt

`AlarmReceiver.kt` receives alarm events.

When an alarm time is reached, this receiver handles the alarm trigger and shows a notification.

### AlarmNotifications.kt

`AlarmNotifications.kt` manages alarm notification logic.

It creates notification channels and displays alarm notifications.

### AlarmStorage.kt

`AlarmStorage.kt` manages local alarm storage.

It stores and retrieves alarm data locally.

### BootReceiver.kt

`BootReceiver.kt` helps restore alarms after the device restarts.

### MainActivity.kt

`MainActivity.kt` contains the main alarm UI.

Main responsibilities:

- Display alarm list
- Add new alarm
- Edit alarm
- Delete alarm
- Enable or disable alarm
- Select alarm time
- Select repeat days
- Save alarm information

---

## 5. Track - Fitness Tracking Application

The `5.Track` folder is a fitness tracking application.

It includes a fitness dashboard, step tracking data structure, service-based tracking, local data storage, AI fitness coach structure, and Wear sync structure.

### MainActivity.kt

`MainActivity.kt` is the main entry point of the fitness tracking app.

It initializes the UI and connects the main dashboard with the application logic.

### FitnessViewModel.kt

`FitnessViewModel.kt` manages fitness-related UI state and business logic.

Main responsibilities may include:

- Load step data
- Update dashboard state
- Calculate fitness results
- Connect repository data with UI
- Manage service-related data

### FitnessDashboardScreen.kt

`FitnessDashboardScreen.kt` contains the main Jetpack Compose dashboard UI.

It may display:

- Step count
- Fitness result
- Badge information
- Tracking status
- AI coach result

### FitnessCalculationTask.kt

`FitnessCalculationTask.kt` handles fitness calculation logic asynchronously.

### FitnessDatabase.kt

`FitnessDatabase.kt` defines the local database structure for fitness data.

### StepDao.kt

`StepDao.kt` defines database operations for step records.

### StepEntity.kt

`StepEntity.kt` defines the step data entity.

### StepRepository.kt

`StepRepository.kt` connects the local data layer with the ViewModel.

### FitnessBoundService.kt

`FitnessBoundService.kt` demonstrates bound service usage in the fitness tracking context.

### StepBackgroundService.kt

`StepBackgroundService.kt` demonstrates background step tracking logic.

### StepForegroundService.kt

`StepForegroundService.kt` demonstrates foreground step tracking logic.

### GeminiFitnessCoach.kt

`GeminiFitnessCoach.kt` contains the AI fitness coach structure.

It can be used to generate fitness suggestions or feedback if the required API setup is configured.

### WearSyncManager.kt

`WearSyncManager.kt` contains the structure for syncing fitness data with a wearable device.

---

## How to Run

## Requirements

Before running the projects, make sure you have installed:

- Android Studio
- Android SDK
- JDK 17 or higher
- Android Emulator or a real Android device
- Internet connection for Gradle sync
- Internet connection for the `2.ASL` Google Books search project
- Notification permission for alarm and foreground service projects on newer Android versions

---

## Important Note

This repository contains **five separate Android projects**.

Do not treat the root folder as only one single app.

Open one folder at a time in Android Studio:

```text
Lab5_Alarm_Fitness/1.AS
Lab5_Alarm_Fitness/2.ASL
Lab5_Alarm_Fitness/3.Recap
Lab5_Alarm_Fitness/4.Alarm
Lab5_Alarm_Fitness/5.Track
```

---

## Run with Android Studio

1. Clone this repository:

```bash
git clone https://github.com/izjoe/Lab5_Alarm_Fitness.git
```

2. Open **Android Studio**.

3. Choose **Open an Existing Project**.

4. Select one project folder, for example:

```text
Lab5_Alarm_Fitness/1.AS
```

or:

```text
Lab5_Alarm_Fitness/4.Alarm
```

5. Wait for Android Studio to sync Gradle.

6. Select an Android emulator or connect a real Android device.

7. Click the **Run** button.

8. The selected project will be built and launched on the device.

---

## Run with Terminal

### macOS or Linux

Go to the folder you want to run.

Example for `1.AS`:

```bash
cd Lab5_Alarm_Fitness/1.AS
./gradlew build
./gradlew installDebug
```

Example for `4.Alarm`:

```bash
cd Lab5_Alarm_Fitness/4.Alarm
./gradlew build
./gradlew installDebug
```

Example for `5.Track`:

```bash
cd Lab5_Alarm_Fitness/5.Track
./gradlew build
./gradlew installDebug
```

---

### Windows

Example for `1.AS`:

```bash
cd Lab5_Alarm_Fitness\1.AS
gradlew.bat build
```

Example for `4.Alarm`:

```bash
cd Lab5_Alarm_Fitness\4.Alarm
gradlew.bat build
```

Example for `5.Track`:

```bash
cd Lab5_Alarm_Fitness\5.Track
gradlew.bat build
```

---

## Optional API Key Setup

The `5.Track` project contains an AI fitness coach structure.

If the AI coach requires an API key, do not hardcode the real key directly in source code.

A safer approach is to store the key in `local.properties`:

```properties
GEMINI_API_KEY=your_api_key_here
```

Then load it through Gradle `BuildConfig`.

Example:

```kotlin
val apiKey = BuildConfig.GEMINI_API_KEY
```

Do not commit real API keys to GitHub.

---

## How to Use

## 1. AS

1. Open the `1.AS` project.
2. Run the app.
3. Tap the button to start the AsyncTask.
4. The app shows a waiting message.
5. After the background task finishes, the result is displayed on the screen.

---

## 2. ASL

1. Open the `2.ASL` project.
2. Run the app.
3. Enter a book name.
4. Tap the search button.
5. The app sends a request to the Google Books API.
6. The app displays the book title and author if a result is found.

---

## 3. Recap

1. Open the `3.Recap` project.
2. Run the app.
3. Use the available buttons to test:
   - Background task
   - Foreground service
   - Bound service
   - AsyncTask recap
4. Watch the UI or log output to understand how each method works.

---

## 4. Alarm

1. Open the `4.Alarm` project.
2. Run the app.
3. Add a new alarm.
4. Choose the alarm time.
5. Enter a label if needed.
6. Select repeat days if needed.
7. Save the alarm.
8. Enable or disable the alarm from the alarm list.
9. Edit or delete alarms when needed.
10. When the alarm triggers, the app displays a notification.

---

## 5. Track

1. Open the `5.Track` project.
2. Run the app.
3. View the fitness dashboard.
4. Track or simulate step and fitness data.
5. View fitness result or badge information.
6. Test background, foreground, or bound service behavior if available.
7. Use AI coach features if the API key is configured.
8. Use Wear sync features if supported by the testing environment.

---

## Demo

Demo folder:

```text
https://drive.google.com/drive/u/0/folders/1OWWOfUnb6ZgYrqEV3iq92Jf93EvwMupE
```

---

## Notes

- This repository is for Lab 5 Android practice.
- It contains five separate Android projects.
- Each folder should be opened separately in Android Studio.
- `AsyncTask` is deprecated in modern Android, but it is included here for learning purposes.
- `2.ASL` requires internet connection to search books.
- `4.Alarm` may require notification permission on newer Android versions.
- Alarm behavior may vary depending on emulator/device settings.
- A real Android device is recommended for testing alarms, notifications, and services.
- API keys should never be pushed to GitHub.

---

## Future Improvements

- Add separate README files for each folder
- Add screenshots for each project
- Add demo video for each project
- Replace AsyncTask with Kotlin Coroutine in modern versions
- Improve error handling for Google Books search
- Improve alarm UI design
- Add exact alarm permission handling
- Add alarm ringtone selection
- Add snooze function
- Add real fitness sensor integration
- Connect fitness tracking with Health Connect
- Improve AI fitness coach response quality
- Add unit tests and UI tests
- Improve code documentation

---

## Author

**Nguyễn Bảo Châu**

- University: University of Information Technology – VNUHCM
- Major: Information System
- Email: baochaune21@gmail.com

---

## License

This project is used for educational purposes.
