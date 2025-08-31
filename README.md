🚀 Task-O! & BancoVirtual — Monorepo (Java · Swing · MySQL/MariaDB)

A two-app student project:

Task-O! – a kanban-style task manager with login, admin tools, boards, columns, tasks, and custom backgrounds/templates ✨

BancoVirtual – a simple banking GUI to create accounts and perform deposits, withdrawals, and transfers for practice and demos 🏦

📚 Table of Contents

Project Structure

Features

Tech Stack

Quick Start

Prerequisites

Database Setup (Task-O!)

Configuration

Build & Run

How Task-O! Works

How BancoVirtual Works

Troubleshooting

Roadmap

Contributing

License

🗂 Project Structure
Proyecto final equipo 1/
├─ Tasko/
│  └─ src/                   # Java Swing sources for Task-O!
│     ├─ Main.java           # Entry point (opens Login)
│     ├─ InicioSesion.java   # Login & Sign Up UI
│     ├─ AdminFrame.java     # Admin dashboard (users/projects)
│     ├─ TableroDeTrabajo.java / TaskBoardFrame.java  # Main board UI
│     ├─ ImageViewport.java  # Custom background viewing
│     ├─ Database.java       # JDBC connectivity helpers
│     ├─ *DAO.java           # UserDAO, ProjectDAO, ColumnDAO, TaskDAO, etc.
│     └─ utils/PasswordUtil  # SHA-256 password hashing
└─ BancoVirtual/
   └─ src/
      ├─ Main.java
      └─ SistemaBancarioGUI.java  # Banking GUI (accounts/operations)

✨ Features
🧩 Task-O!

Secure login & sign-up (passwords hashed via SHA-256)

Admin panel to manage users and projects/boards

Kanban boards with columns and tasks (CRUD via DAOs)

Custom backgrounds and template support (e.g., CustomTemplates.tsv)

Persistence with MySQL/MariaDB through JDBC

🏦 BancoVirtual

Create accounts and perform deposit, withdrawal, and transfer operations

Clear, classroom-friendly GUI flows

Runs standalone (no DB required by default)

🛠 Tech Stack

Language: Java (11/17 recommended)

UI: Swing

DB: MySQL or MariaDB (Task-O!)

Driver: MySQL Connector/J

⚡ Quick Start
✅ Prerequisites

Java 17 (or 11)

Git

MySQL or MariaDB running locally (for Task-O!)

MySQL Connector/J available to the build tool

🗄 Database Setup (Task-O!)

Create the DB and minimal tables:

-- schema.sql (starter for Task-O!)
CREATE DATABASE IF NOT EXISTS taskboard CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE taskboard;

CREATE TABLE IF NOT EXISTS users (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  username     VARCHAR(64)  UNIQUE NOT NULL,
  password_sha CHAR(64)     NOT NULL,            -- SHA-256 hex
  role         ENUM('ADMIN','USER') DEFAULT 'USER',
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS projects (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  owner_id     INT NOT NULL,
  name         VARCHAR(128) NOT NULL,
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS project_columns (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  project_id   INT NOT NULL,
  name         VARCHAR(128) NOT NULL,
  position     INT NOT NULL DEFAULT 0,
  FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tasks (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  column_id    INT NOT NULL,
  title        VARCHAR(200) NOT NULL,
  description  TEXT,
  position     INT NOT NULL DEFAULT 0,
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (column_id) REFERENCES project_columns(id) ON DELETE CASCADE
);

-- Optional: seed an admin (replace <SHA256> with your computed hash)
-- INSERT INTO users (username, password_sha, role) VALUES ('admin','<SHA256>','ADMIN');

🔧 Configuration

Use environment variables (recommended):

TASKO_DB_URL (default: jdbc:mysql://127.0.0.1:3306/taskboard)

TASKO_DB_USER (default: taskuser)

TASKO_DB_PASS (default: taskpass)

Or edit the defaults in Database.java / Main.java.

▶️ Build & Run

If your repo currently lacks build files, choose one option below and add the file to each app (Tasko/ and BancoVirtual/).

🧱 Option A — Gradle

Tasko/build.gradle:

plugins {
    id 'java'
    id 'application'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories { mavenCentral() }

dependencies {
    implementation 'mysql:mysql-connector-java:8.0.33'
    // implementation 'com.formdev:flatlaf:3.4' // optional: nicer Swing look
}

application {
    mainClass = 'Main' // adjust package if needed, e.g., com.tasko.Main
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
}


Run Task-O!:

cd Tasko
gradle run


Create a similar build.gradle for BancoVirtual with application.mainClass = 'Main', then:

cd BancoVirtual
gradle run

🧩 Option B — Maven

Tasko/pom.xml:

<project xmlns="http://maven.apache.org/POM/4.0.0"  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0  http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>edu.example</groupId>
  <artifactId>tasko</artifactId>
  <version>1.0.0</version>
  <properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <exec.mainClass>Main</exec.mainClass>
  </properties>
  <dependencies>
    <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <version>8.0.33</version>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>3.2.0</version>
        <configuration>
          <mainClass>${exec.mainClass}</mainClass>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>


Run Task-O!:

cd Tasko
mvn -q exec:java


Create a similar pom.xml for BancoVirtual, then:

cd BancoVirtual
mvn -q exec:java

🧭 How Task-O! Works

Login & Sign-Up – Users authenticate with username/password (SHA-256 hashed) 🔐

Projects & Boards – Each project is a kanban board with multiple columns 🗂️

Columns – Ordered left-to-right via position; rename and reorder as needed ↔️

Tasks (Cards) – Title, optional description, and an order within a column 📝

Admin Panel – Manage users and projects; enforce roles and clean up data 🧑‍💼

Custom Backgrounds & Templates – Load images and pre-seed content with TSV files 🖼️

Persistence – DAOs (UserDAO, ProjectDAO, ColumnDAO, TaskDAO) run CRUD against MySQL/MariaDB 💾

💳 How BancoVirtual Works

Accounts – Create accounts with an owner and starting balance 👤

Operations – Deposit, Withdraw, Transfer between accounts with validation ➕➖🔁

GUI Flow – Choose an operation, select accounts, set amount, confirm ✅

Data Storage – In-memory by default; you can wire a DB if you extend it 🗃️

🧰 Troubleshooting

Cannot connect to DB – Verify TASKO_DB_URL, user, password, and server reachability (port 3306)

JDBC driver not found – Ensure mysql-connector-j is on the classpath or configured in Gradle/Maven

Login fails – Seed an admin user or verify SHA-256 hashing matches the stored value

Swing layout oddities – Use consistent layout managers and a single Look & Feel

Compilation errors – Remove any placeholder ... and complete missing code blocks before building

🗺️ Roadmap

Migrate SHA-256 to BCrypt with per-user salts 🛡️

Drag-and-drop task movement with auto-persisted order 🧲

Activity log and per-project permissions 📒

Export boards to CSV/JSON for sharing 📤

Optional REST API layer for Task-O! 🌐

🤝 Contributing

Fork the repo and create a feature branch 🌱

Use Java 17, format code consistently, add brief Javadoc to public methods 🧼

Write small, focused commits with descriptive messages ✍️

Open a PR with a clear description; screenshots welcome 📸
