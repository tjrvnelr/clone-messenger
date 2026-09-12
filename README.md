# Java Messenger — Phase 1: Setup, Server, Registration & Login

This is the first incremental milestone of the Java Messenger build:

1. Project setup (Maven, package layout)
2. MySQL database
3. Server (accepts connections, one thread per client)
4. Client connection (JavaFX app + background socket listener)
5. Registration
6. Login

Search, real-time messaging, message history, online-status broadcasting,
unread counts, and the final UI pass are **not** in this build yet — they're
the next phases. What you have right now: two or more people can each
launch the client, create an account, and log in against a shared MySQL
database, all over a real socket connection to a multithreaded server.

## 1. Prerequisites

- **JDK 17** or later (`java -version`)
- **Maven 3.8+** (`mvn -version`)
- **MySQL Server 8.x** running locally (or reachable over the network)

## 2. Create the database

Run the schema script once:

```
mysql -u root -p < database/schema.sql
```

This creates the `java_messenger` database with the `users`, `conversations`,
and `messages` tables (the last two are ready for the next phase).

## 3. Configure the connection

Edit `src/main/resources/db.properties`:

```
db.url=jdbc:mysql://localhost:3306/java_messenger?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=CHANGE_ME
```

Set `db.password` to your MySQL root (or dedicated app-user) password. If
you change this file you'll need to rebuild before it takes effect.

## 4. Build

From the project root:

```
mvn clean compile
```

> **Note:** this project was written and organized without being compiled
> in the environment it was generated in (that sandbox has no route to
> Maven Central), so `mvn clean compile` on your own machine is the first
> real build/compile check it will get. If you hit a dependency-resolution
> or version error, tell me the exact message and I'll fix the `pom.xml`.

## 5. Run the server

In one terminal:

```
mvn exec:java
```

You should see:

```
Java Messenger Server starting on port 5555...
Server is listening for connections.
```

Leave this running.

## 6. Run the client

In a second terminal:

```
mvn javafx:run
```

The login screen should appear. Click **"Don't have an account? Sign up"**,
register a user, then log in with it. You'll land on a minimal messenger
shell showing your username and a Logout button.

To test messaging-readiness of the connection layer, run `mvn javafx:run`
a second time in a third terminal to open a second client and register a
second user — both should be able to log in at the same time, and the
server terminal will log both connections.

## 7. What to expect / how to test this phase

- **Duplicate username or email** → registration is rejected with a clear
  message, without touching the database twice.
- **Wrong password / unknown identifier** → login is rejected with a
  generic "Incorrect username/email or password" message (the server
  doesn't reveal which part was wrong).
- **Server not running** → the client shows "Can't reach the server. Is it
  running?" instead of hanging or crashing.
- **Password storage** → check the `users` table; `password_hash` should be
  a bcrypt hash (starts with `$2a$` or `$2b$`), never the plain password.
- **Logout** → click Logout, then check the `users` table; `status` should
  flip back to `OFFLINE` and `last_seen` should update.

## Project layout

```
src/main/java/com/javamessenger/
├── client/       MainApp (JavaFX entry point), ClientContext (shared session state)
├── controller/   LoginController, RegisterController, MessengerController
├── network/      NetworkClient (client-side socket + background listener thread)
├── server/       Server (accepts connections), ClientHandler (per-client thread),
│                 ServerRegistry (maps userId -> ClientHandler for later real-time routing)
├── database/     DBConnection (JDBC factory), UserDAO (prepared-statement queries)
├── model/        User, Packet, PacketType (the client/server wire protocol)
└── utils/        PasswordUtil (bcrypt), ValidationUtil (registration field checks)

src/main/resources/
├── css/style.css     white + baby-blue (#89CFF0) theme, plus message-bubble
│                     classes reserved for the messaging phase
├── fxml/             login.fxml, register.fxml, messenger.fxml
└── db.properties     MySQL connection settings

database/schema.sql   users, conversations, messages tables
```

## Next phase

Once this is confirmed working on your machine, the next build phase adds:
user search, opening a conversation, real-time message send/receive over
the existing socket connection, message history loading, and unread-count
badges — using the `SEARCH_USERS`, `SEND_MESSAGE`, `INCOMING_MESSAGE`, etc.
packet types already reserved in `PacketType.java`.

## Android APK

The JavaFX desktop client is not Android-compatible, so an installable native
Android client is provided in `android-client/`. Open that folder in Android
Studio and use **Build > Build APK(s)**. See `android-client/README.md` for
phone network setup; a physical phone must connect to the computer's LAN IP,
not `localhost`.
