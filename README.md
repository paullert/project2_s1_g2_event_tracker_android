# Event Tracker Android

Android app for browsing events, viewing invitations, and responding to invitations.
Managers can create events. Google sign-in uses AppAuth with OAuth2 and PKCE.

## Team

Paul Conner, Sarah Wafa, Ann Jojo, and Billy Provins.

## Tech stack

Kotlin, Jetpack Compose / Material 3, AppAuth, Retrofit, OkHttp, and Gradle.

## Links

- [Backend repository and local database setup](https://github.com/paullert/README.md)

## Local setup

Use Android Studio with Android SDK 37, the project's JDK 25 Gradle toolchain,
Git, and an emulator or device running Android 12L / API 32 or newer.
Commands below use PowerShell; macOS/Linux users can use `./gradlew` instead.

1. Clone this repository and open the folder in Android Studio:

   ```powershell
   git clone https://github.com/paullert/project2_s1_g4_event_tracker_android.git
   cd project2_s1_g4_event_tracker_android
   ```

2. Clone Backend API that this app uses to retrieve information from. Guide is on the backend github [here](https://github.com/paullert/project2_s1_g4_event_tracker_api#local-setup)

3. Configure Google sign-in. Run `.\gradlew.bat signingReport` and create an
   **Android** OAuth client for package `com.example.event_tracker` with the build's
   signing SHA-1. Enable **Custom URI scheme** in the client's Advanced Settings
   and add your Google account as a consent-screen test user while in Testing.
   Set `GOOGLE_CLIENT_ID` in [gradle.properties](gradle.properties). The API must
   trust the same ID. Gradle generates the redirect automatically; no secret is used.

3. Keep `API_BASE_URL` in `gradle.properties` pointed at the live API, or set it to
   `http://10.0.2.2:8080/` for a local API. A physical phone needs the computer's
   reachable LAN address instead. HTTP is supported in debug builds only.

4. Start the emulator and click **Run** in Android Studio, or build and install:

   ```powershell
   .\gradlew.bat :app:installDebug
   ```

5. Select **Continue with Google**. New accounts register as `USER`; the backend
   database controls manager/admin permissions. The app sends Google ID tokens
   as bearer credentials and refreshes them when needed.

### Optional local API with Docker

Install Docker Desktop and complete the backend README's PostgreSQL/schema and
`.env` setup first. From the **backend** repository, run:

```powershell
docker build -t event-tracker-api .
docker run -d --name event-tracker-api --network event-tracker --env-file .env -e PORT=8080 -p 8080:8080 event-tracker-api
docker logs -f event-tracker-api
```

Then use `http://10.0.2.2:8080/` in Android's `API_BASE_URL`, sync Gradle, and rebuild.
For containers already created during backend setup, use
`docker start event-tracker-db event-tracker-api` instead of creating them again.

## Tests

```powershell
.\gradlew.bat :app:testDebugUnitTest
# Requires a connected, authorized emulator/device:
.\gradlew.bat :app:connectedDebugAndroidTest
```

If sign-in fails, filter Logcat by `EventTrackerAuth`. A 401 indicates a rejected
or missing token; a 403 indicates insufficient database permissions. A timeout
indicates a network/server wait; the hosted API may need time to wake from idle.
