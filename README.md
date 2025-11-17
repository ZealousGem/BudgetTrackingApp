# BudgetTrackingApp
Open Source Project

# Overview

Budget Boet is a personal budgeting and expense tracking Android application designed to help users manage their finances effectively, understand their spending habits, and achieve their financial goals. The app provides a user-friendly platform for logging daily expenses, visualizing spending through graphs, and setting up financial goals and categories.

## Key Features

* **User Authentication:** Secure registration and login.
* **Expense Management:** Log detailed expense entries.
* **Expense Review:** View a comprehensive list of all recorded expenses.
* **Category Customization:** Create and manage new spending categories.
* **Category Analysis:** Track spending per category.
* **Financial Goal Setting:** Set and monitor personal financial goals.
* **Progress Visualization:** View spending trends using graphical representations like Bar Graphs and Line Graphs.
* **Rewards System:** Engage users with a rewards mechanism (e.g., for meeting goals).
* **Main Interface:** A central HomeScreen serves as the primary entry point after launching the app.

## Design Considerations

The design of Budget Boet focuses on usability, security, and performance.

### 1. User Experience (UX)

* **Intuitive Navigation:** The primary user flow is simple: log in/register, enter expenses, and view reports. The HomeScreen is the main hub for accessing all features.
* **Data Visualization:** The inclusion of graphs is a key design choice to make financial data immediately understandable and actionable.
* **Encouragement:** The Rewards system is designed to gamify the budgeting process, providing positive reinforcement for good financial habits.

### 2. Technical Architecture

* **Security:** All core feature activities (like ExpenseEntryActivity, Goals, Login) are set to `android:exported="false"` in the manifest. This securely prevents external applications from directly launching or accessing these private components, enhancing app security. The app requires the Internet permission, indicating a design that relies on external data synchronization, cloud storage, or an API service for persistent, cross-device data access.
* **Permissions:** The app requests permission for the camera and features the `androidx.core.content.FileProvider`. This allows users to capture images (e.g., receipts) and securely share content (e.g., exported reports or receipt images) with other applications using the FileProvider.

## Utilisation of GitHub and GitHub Actions

This project leverages GitHub for robust version control and collaboration, and utilizes GitHub Actions to implement a Continuous Integration/Continuous Deployment (CI/CD) pipeline.

### GitHub (Version Control)

* **Repository Management:** All source code is hosted on this GitHub repository, allowing for seamless tracking of changes, branching for feature development, and merging reviewed code.
* **Collaboration:** Pull Requests ensure that all code changes are reviewed by team members before being merged into the main branch, maintaining code quality and stability.

### GitHub Actions (CI/CD)

* **Continuous Integration (CI):**
    * **Automated Builds:** A workflow automatically compiles the Android application every time code is pushed or a Pull Request is opened.
    * **Linting/Static Analysis:** The workflow runs code quality checks to catch potential bugs, style violations, and performance issues early in the development cycle.
    * **Automated Testing:** Unit and instrumentation tests are executed to verify that new code changes have not introduced regressions.
* **Continuous Deployment (CD):**
    * **Release Management:** Upon merging code into the main branch or tagging a new release version, a workflow can be triggered to automatically generate a signed APK/AAB and publish it to a distribution platform.

# Install Instructions 

Download the project as a ZIP file or clone the repository.

Extract the folder.

Open **Android Studio**.

Select **Open Project**.

Open the **"BudgetBoet Folder"** in the file explorer.

Allow Android Studio to **synchronize the Gradle files**.

Then **Run** the app to deploy it to an emulator or a connected physical device.
