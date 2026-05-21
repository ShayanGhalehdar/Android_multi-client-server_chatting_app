# Multi-Client Android Chat App with Java Socket Server

An end-to-end real-time chat system: an Android client app and a Java socket server that supports multiple concurrent users.

## Features

- **Authentication:** Sign up, log in, persistent username / password storage.
- **User discovery & filtering:** Browse other users; filter the list by **gender** and **age range**.
- **Real-time chat:** One-to-one messaging over a TCP socket connection to the server.
- **Auto-reconnect:** Client recovers from transient network drops without losing the conversation context.
- **Multi-client server:** Server handles many simultaneous client sessions on separate threads.

## Architecture

```
            ┌────────────┐         TCP socket        ┌──────────────┐
  Android   │  MyChatApp │ ◀──────────────────────▶  │   Server     │  Java app
  device    │  (Android  │     framed JSON / text    │  (multi-     │  running on a host
            │   client)  │                            │   threaded)  │
            └────────────┘                            └──────────────┘
```

- **MyChatApp/** — Android Studio / Gradle project (Java).
- **Server/** — IntelliJ / `src/` Java project that listens on a configured TCP port and spawns a worker thread per connected client.

## Repository Structure

```
MyChatApp/                 — Android client (Gradle)
  app/
  build.gradle
  settings.gradle
  ...
Server/                    — Java socket server
  src/
  ProjectServer.iml
  out/
```

## How to Build and Run

**Server:**
```bash
cd Server/src
javac -d ../out **/*.java
java -cp ../out Main   # adjust to the actual main class
```

**Client:** open `MyChatApp/` in Android Studio, set the server's host/port in the client's connection config, and run on an emulator or device.
