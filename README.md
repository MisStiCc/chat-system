# Chat System

Client-server chat system with Swing UI (Telegram-like dark theme).

## Tech Stack
- Java 17
- Swing + FlatLaf (dark theme)
- Pure sockets (no frameworks)
- Maven multi-module

## Project Structure

```
chat-system/
├── .gitignore
├── pom.xml
├── README.md
├── server/
│   ├── pom.xml
│   └── src/
│       └── main/
│           └── java/
│               └── com/
│                   └── chat/
│                       └── server/
│                           ├── ChatServer.java
│                           ├── ClientHandler.java
│                           ├── model/
│                           │   └── User.java
│                           └── service/
│                               ├── AuthService.java
│                               └── SessionManager.java
└── client/
    ├── pom.xml
    └── src/
        └── main/
            └── java/
                └── com/
                    └── chat/
                        └── client/
                            ├── ClientApp.java
                            ├── network/
                            │   └── ServerConnection.java
                            └── ui/
                                ├── LoginFrame.java
                                └── ChatFrame.java
```

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build
```bash
mvn clean package
```

### Run Server
```bash
java -jar server/target/chat-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Run Client
```bash
java -jar client/target/chat-client-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Test Users

| Login | Password | Nickname |
|-------|----------|----------|
| alice | 123 | Alice |
| bob | 123 | Bob |
| charlie | 123 | Charlie |
| dmitry | 123 | Dmitry |
| elena | 123 | Elena |

## Features

- User authentication
- Private messaging (`/msg <user> <message>`)
- Public broadcast (just type message)
- Online users list (`/users`)
- Auto-update users list every 5 seconds
- Dark theme (Telegram style)
- Connection loss handling with reconnect option

## Commands

| Command | Description |
|---------|-------------|
| `/msg <user> <message>` | Send private message |
| `/users` | Show online users |
| `/help` | Show help |
| `/quit` | Disconnect |

## Architecture

### Server
- Multi-threaded socket server
- Session management
- Message routing (private/broadcast)

### Client
- Swing GUI with FlatLaf dark theme
- Asynchronous network communication
- Auto-update user list
- Reconnect on connection loss

## License
Educational project for test assignment.
