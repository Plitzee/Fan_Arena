# FanArena

FanArena is an Android app for real-time sports fan engagement. It brings live match context, AI-assisted analysis, prediction, community interaction, missions, XP, tokens, and reward redemption into one mobile experience.

The project was built as a course application/demo to show how a sports fan platform can move beyond passive score tracking and turn each match into an interactive arena.

## Key Features

- **Live sports dashboard**: follow upcoming and live match contexts across supported sports.
- **Match detail experience**: view overview, prediction, AI tactical insight, room discussion, and stats in one place.
- **AI strategic prediction**: use Gemini-powered explanations to support fan predictions and tactical reasoning.
- **Community match room**: chat, vote in room polls, and discuss match moments with other fans.
- **Missions, XP, and leaderboard**: reward consistent participation and make engagement measurable.
- **Token shop and vault**: earn tokens from activity and connect engagement to redeemable value.
- **Demo-friendly fallback data**: the app can still run with seeded fixtures when external APIs are unavailable.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose, Material 3, Navigation Compose
- **Architecture**: ViewModel + Repository pattern
- **Local storage**: Room
- **Backend services**: Firebase Auth, Firestore, Firebase Realtime Database
- **AI**: Google Gemini API
- **Sports data**: API-Sports via Retrofit, OkHttp, Moshi
- **Images**: Coil
- **Build system**: Gradle Kotlin DSL

## App Flow

1. User logs in or uses the quick demo account.
2. Dashboard shows match contexts, XP, token balance, missions, and rankings.
3. Match Detail lets users inspect stats, ask for AI insight, make predictions, and enter the match room.
4. Missions and prediction results update XP/tokens.
5. Users can spend earned tokens in the shop and return for new matches.

## Demo Account

Use this account for quick local testing:

```text
Email: plitzee@gmail.com
Password: huy123
```

## Demo Video

Watch the recorded project demo here:

[FanArena demo video](docs/demo/fanarena-demo.webm)

## Run Locally

### Prerequisites

- [Android Studio](https://developer.android.com/studio)
- Android SDK with an emulator or a physical Android device
- JDK 11 or newer
- Firebase configuration for Android
- Gemini API key
- API-Sports key

### Setup

1. Clone the repository.

```bash
git clone https://github.com/Plitzee/Fan_Arena.git
cd Fan_Arena
```

2. Create a `.env` file in the project root.

```env
GEMINI_API_KEY="YOUR_GEMINI_API_KEY"
APISPORTS_API_KEY="YOUR_APISPORTS_API_KEY"
```

3. Add your Firebase Android configuration file.

```text
app/google-services.json
```

4. Open the project in Android Studio and let Gradle sync.

5. Run the app on an emulator or physical device.

You can also build from the command line:

```bash
./gradlew assembleDebug
```

On Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

## Project Structure

```text
app/src/main/java/com/example/
|-- ai/             # Gemini integration
|-- data/           # Room entities, DAO, repository, API services
|-- ui/screens/     # Compose screens
|-- viewmodel/      # App state and screen logic
`-- MainActivity.kt # Android entry point
```

## Notes

- `.env`, `local.properties`, keystores, and generated build outputs should not be committed.
- If API keys are missing, the app uses demo fixtures where possible instead of blocking the whole experience.
- Firebase features require a valid `google-services.json` and matching Firebase project setup.

## Repository

GitHub: https://github.com/Plitzee/Fan_Arena
