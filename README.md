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
| **APIs** | OpenWeatherMap API (weather integration), OpenAI API (AI outfit suggestion, optional) |

---

##  System Design

### **ER Diagram**
 <img width="796" height="682" alt="截屏2025-11-12 10 43 13" src="https://github.com/user-attachments/assets/19e0665d-b835-445d-9b9c-5c501a06b582" />
 
#### **Entity Overview**
- **Member** – stores user info such as name, gender, and age.
- **ClothingItem** – represents a clothing record (description, image URI, created time).  
- **Tag** – global shared tags (e.g., “Winter”, “Formal”, “Sports”).
- **Location** – stores physical storage information, such as `locationId`, `name`, and `description` (e.g., “Main Wardrobe”, “Storage Box A”).
- **TransferHistory** - records the transfer of a clothing item between members, including source member, target member, and transfer time.
- **Relationships:**  
  - Each Member can own multiple ClothingItem records.
  - Each ClothingItem belongs to exactly one Member.
  - Each ClothingItem can have multiple Tags (many-to-many relationship via ClothingTagCrossRef).
  - Each Tag can be linked to multiple ClothingItems.
  - Each ClothingItem can optionally belong to one Location (one-to-many relationship from Location to ClothingItem).
  - Each Location can store multiple ClothingItems.
  - TransferHistory links clothing items with both source and target members to record ownership transfers.
  - Tags are globally shared across all members.
  - When a Member is deleted, all related ClothingItem and TransferHistory entries are also removed (cascade delete).
  - When a Location is deleted, related clothing items’ locationId is set to null (set null).
  - When a Tag or ClothingItem is deleted, the corresponding records in ClothingTagCrossRef are removed automatically (cascade delete).


---

##  Project Management

### **Trello Board**
[🔗 Trello Backlog & Sprint Plan](https://trello.com/b/ymbal9w5/backlog))

Each sprint includes clear tasks and acceptance criteria following the Agile Scrum methodology.

#### **Sprint 1 Focus**
- Implement Member system  
- CRUD for ClothingItem  
- Tag linking  
- ER diagram & database testing  

---

##  UI / UX Design

### **Figma Prototype**
[Figma Design Board](https://www.figma.com/design/PwMYy5MikBidqkola0tFQ3/Wardrobe?node-id=0-1&t=gcwxdkpzBlOvIgVo-1)

- Based on **Material 3 Guidelines**  
- Includes light/dark mode  
- Responsive layouts tested on multiple screen sizes  

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
