# 🏃 CodeAlpha Fit — Fitness Tracker App

A modern Android fitness tracking application developed as part of my **App Development Internship at CodeAlpha**.

CodeAlpha Fit helps users track daily fitness activities, manage workouts, monitor goals, and view their progress through a clean and user-friendly interface.

---

## 📱 Features

### 🏠 Home Dashboard

The home screen provides a quick overview of the user's daily fitness activity.

- 👣 Daily step tracking
- 🔥 Calories burned
- ⏱️ Workout duration
- 🏋️ Workout/session count
- 📊 Daily progress indicators
- ➕ Quick workout logging

---

### 🏋️ Workout Management

Users can record and manage their fitness activities.

- Add workouts
- Record workout duration
- Record calories burned
- Track workout sessions
- View today's workouts
- Manage workout records

---

### 📈 Weekly Progress

The Progress screen provides a weekly overview of fitness activity.

- 🎯 Weekly step goals
- 🏋️ Weekly workout goals
- 🔥 Weekly calorie goals
- 📊 Weekly summary
- ⏱️ Average workout duration
- 📈 Activity visualization

---

### 📜 Workout History

Users can review their previous fitness activities.

- 🔎 Search workouts
- 📅 View workout history
- Filter by:
  - All
  - Today
  - This Week
  - This Month

---

### 🏆 Achievements

The application includes an achievement system to encourage consistent activity.

Examples include:

- First Workout
- Workout milestones
- Step milestones
- Calorie milestones
- Consistency achievements

---

### ⚙️ Settings

Users can customize their fitness experience.

#### Appearance

- System theme
- Light theme
- Dark theme

#### Fitness Goals

- Daily steps goal
- Weekly workout goal
- Weekly calorie goal

#### Notifications

- Daily fitness reminders

---

## 💾 Local Data Storage

The application uses local data storage to maintain fitness information.

Users can store:

- Workout records
- Workout duration
- Calories
- Fitness goals
- Progress information
- Workout history

The application is designed to work without requiring an external AI API or backend service.

---

## 🛠️ Technologies Used

- **Kotlin**
- **Android Studio**
- **Jetpack Compose**
- **Material 3**
- **Room Database**
- **ViewModel**
- **Repository Pattern**
- **Kotlin Coroutines**
- **Flow / StateFlow**
- **Android Navigation**

---

## 🏗️ Architecture

The application follows a structured Android architecture to separate UI, business logic, and data management.

```text
UI Layer
   ↓
ViewModel
   ↓
Repository
   ↓
Room Database
   ↓
Local Data
