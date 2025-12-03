#  Wardrobe – Smart Family Clothing Organizer

###  Overview
**Wardrobe** is a mobile application designed to help families manage and organize their clothes efficiently.  
Users can create multiple members, upload or photograph clothing items, categorize them with tags, and track seasonal storage.  
The app also supports weather-based outfit recommendations and localization for different languages.

---

##  Key Features

-  **Multi-member support** – each family member has their own wardrobe.  
-  **Add & edit clothes** – upload or take photos of clothing items.  
-  **Global tag system** – reusable tags for quick filtering and organization.  
-  **Weather integration** – suggest outfits based on local weather conditions.  
-  **Seasonal organization** – mark and store clothes by season.  
-  **Localization support** – automatic language selection based on system language.  
-  **Drawer menu** – access app settings, dark mode, and member management.

---

##  Tech Stack

| Category | Technology |
|-----------|-------------|
| **Frontend** | Kotlin, Jetpack Compose, Material 3 |
| **Backend / Database** | Room / SQLite (local) |
| **Architecture** | MVVM (Model–View–ViewModel) |
| **Tools** | Android Studio, GitHub, Figma, Trello |
| **APIs** | OpenWeatherMap API (weather integration), Gemini 2.5 flash API |

---

##  System Design

### **ER Diagram**
 <img width="808" height="631" alt="截屏2025-11-26 23 24 33" src="https://github.com/user-attachments/assets/5287b87b-19cc-4bde-b731-1510d0c564e2" />

#### **Entity Overview**
- **Member** – stores user info such as name, gender, age, and optional birth date.  
- **ClothingItem** – represents a clothing record (description, image URI, created time), including extended attributes such as category, warmth level, occasions, waterproof flag, color, season, last worn time, favorite status, and optional size label.  
- **Tag** – global shared tags (e.g., “Winter”, “Formal”, “Sports”).  
- **Location** – stores physical storage information, such as `locationId` and `name` (e.g., “Main Wardrobe”, “Storage Box A”).  
- **TransferHistory** – records the transfer of a clothing item between members, including source member, target member, and transfer time.  
- **NfcTag** – represents a physical NFC tag bound to a specific storage location, using a unique hardware ID.

**Relationships:**  
  - Each **Member** can own multiple **ClothingItem** records.  
  - Each **ClothingItem** belongs to exactly one **Member**.  
  - Each **ClothingItem** can have multiple **Tag**s (many-to-many relationship via `ClothingTagCrossRef`).  
  - Each **Tag** can be linked to multiple **ClothingItem**s.  
  - Each **ClothingItem** can optionally belong to one **Location** (one-to-many relationship from `Location` to `ClothingItem`).  
  - Each **Location** can store multiple **ClothingItem**s.  
  - **TransferHistory** links clothing items with both source and target members to record ownership transfers.  
  - **NfcTag** references a `Location`, allowing users to bind NFC stickers to physical storage spaces for quick lookup.  
  - **Tag**s are globally shared across all members.  
  - When a **Member** is deleted, all related **ClothingItem** and **TransferHistory** entries are also removed.  
  - When a **Location** is deleted, related clothing items’ `locationId` is set to `null`, and related NFC tags are removed.  
  - When a **Tag** or **ClothingItem** is deleted, the corresponding records in `ClothingTagCrossRef` are removed automatically.


---

##  Project Management

### **Trello Board**
[🔗 Trello Backlog & Sprint Plan](https://trello.com/b/ymbal9w5/backlog))

Each sprint includes clear tasks and acceptance criteria following the Agile Scrum methodology.

#### **Sprint 1 Focus**
- Establish basic family member system (weak login)
- Improve search & filter logic
- Implement tag statistics
- Add in-use vs stored item categorization
- Create initial Figma design prototype

#### **Sprint 2 Focus**
- Responsive UI layout & light/dark theme
- Transfer items between members with history
- Bluetooth sharing as a generated image
- Weather integration with location handling
- Storage location tracking
- Implement navigation drawer (MD3)

#### **Sprint 3 Focus**
- Research image recognition for auto-tagging
- Weather-based outfit recommendations
- Smart growth notifications for children
- Statistics and charts
- Photo AI prototype for auto-tag & category suggestions

#### **Sprint 4 Focus**
- Research Android NFC integration methods
- Research Material Design 3 UI patterns
- NFC-based smart storage prototype
- Localization & multilingual support
- Overall app optimization (UI, recommendation accuracy, user flow)

---

##  UI / UX Design

### **Figma Prototype**
[Figma Design Board](https://www.figma.com/design/PwMYy5MikBidqkola0tFQ3/Wardrobe?node-id=0-1&t=gcwxdkpzBlOvIgVo-1)

- Based on **Material 3 Guidelines**  
- Includes light/dark mode  
- Responsive layouts tested on multiple screen sizes  

---

## Unit Testing Summary

We implemented unit tests for all core business logic components (ViewModels and utilities).  
Android framework code, UI Composables, Room DAO, and remote API components were intentionally excluded because they belong to instrumented tests.

➡️ **Full testing documentation is available here: [TESTING.md](./TESTING.md)**  

---

##  Team

| Role | Member | Responsibilities |
|------|---------|------------------|
| Project Owner | *Wang Qingyun* | Sprint planning, coordination |
| Scrum Master | *Jia Ke* | Member & Tag modules |
| Developer | *Yang Yang* | Weather & AI integration |
| Designer | *Hooda Himanshu* | Figma design, UI assets |

---

##  How to Run

1. Clone the repository:  
   ```bash
   git clone https://github.com/Lucas090122/wardrobe.git
   cd wardrobe
2.	Open the project in Android Studio.
3.	Sync Gradle and run on an emulator or physical device.
4.	(Optional) Add your OpenWeather API key in local.properties or environment variables.

---

## License

This project is developed for educational purposes at Metropolia University of Applied Sciences (ICT23-SW).
© 2025 Wardrobe Team – All rights reserved.
