# SPA Session Manager (Android)

An offline-first front-desk app for managing spa visits, treatment rooms, therapist rotation, treatment timers, bookings, and client waiting estimates.

## Current scope

- **Local-first:** Room/SQLite is the source of truth on the device. Core workflows do not require sign-in, internet, or a backend.
- **Branch-aware:** Rooms, treatments, therapist rosters, bookings, settings, and sessions are attached to a branch.
- **Front desk:** Check clients in, assign a card number, choose one or more treatments, select a therapist and room, and record pre-treatment or between-treatment steam.
- **Live session timing:** Start/finish a treatment when the therapist calls the desk; the front desk displays elapsed and remaining time.
- **Room turnover:** A room enters cleaning status after completion and becomes available after the configured local turnover buffer.
- **Daily therapist roster:** Add therapists, adjust their turn order, and mark them available or on break.
- **Configuration:** Add room names/types, treatment names/categories/durations, and branch card/steam/turnover settings.

The app does **not** sync to the spa management system yet. The local repository/data layer is a deliberate boundary for adding a sync adapter later; no sync service is configured now.

## First-launch setup

A clean install creates a generic **Main Branch** and default operating settings only. It does not invent room names, treatment names/durations, or staff identities.

1. Open **Catalog → Rooms Configuration → Add Room**. Add each real room and choose Single, Couple, or Steam Bath.
2. Open **Catalog → Treatments Catalog → Add Treatment**. Enter the real treatment name, category, planned duration in minutes, and optional price/description/steam marker.
3. Open **Therapists**. Add today's therapists, then use the up/down controls to set the manager's rotation order. Set anyone not working today to unavailable/on break as appropriate.
4. Open **Settings**. Confirm the client card prefix/range, the steam interval, the pre-steam duration, and room cleaning buffer for this branch.
5. Use **Front Desk → Check In Client** to begin operating. Check-in requires a suitable configured room, an available therapist, and at least one configured treatment.

Additional branches can be added from the branch selector. Each branch has its own catalog, roster and settings.

## Core implementation

- Kotlin + Jetpack Compose UI
- Room database in `app/src/main/java/com/example/data/local/`
- Entities in `app/src/main/java/com/example/data/model/`
- Repository/business rules in `app/src/main/java/com/example/data/repository/`
- ViewModel and screens under `app/src/main/java/com/example/ui/`

No cloud synchronization or server API should be treated as active. Keep current business operations functional offline; future synchronization should be added as a separate, conflict-aware layer rather than making the local workflows depend on connectivity.

## Build

The repository currently contains Gradle wrapper configuration but not the Gradle wrapper scripts/JAR. To build in Android Studio, open this repository with an Android SDK/Gradle environment and sync the project. The sandbox used for this refinement did not have Gradle or an Android SDK installed, so a local APK build could not be run here.

## Data note

The updated clean-install flow does not seed demo rooms, demo treatments, or demo therapists. Existing installations that already have local draft data retain their database; back up any real data before clearing app storage or reinstalling.
