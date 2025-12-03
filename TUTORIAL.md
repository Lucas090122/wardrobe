# Wardrobe App — User Tutorial

This guide explains how to use the main features of the **Wardrobe** application, including:

- Member selection and management  
- Adding clothing items  
- NFC-based navigation  
- AI-powered auto-fill  
- Statistics and transfer history  

---

# 1. App Overview

The Wardrobe app is designed for families who share clothing storage.  
Main features include:

- Multiple members  
- NFC-based smart storage  
- AI-powered auto-filling of clothing details  
- Tags, categories, seasons, and item management  
- Statistics & analytics  
- Item transfer between members  
- Weather-based clothing recommendation  

---

# 2. Member Selection Screen (First Screen)

When you open the app for the first time, you see the **Member Selection** screen.  
If there are no members yet, the app shows a message like:

> “No members yet. Add one above!”

At the top you will see:

- Page title: **Home**  
- A large **“Add New Member”** button  
- Weather summary on the top bar (temperature + icon)  
- Navigation drawer icon (top left)

<p align="center">
  <img src="https://github.com/user-attachments/assets/a62a333f-0583-40a1-8368-66e8252e5c13" width="240">
</p>

From here you can:

- Add a new family member  
- Select an existing member (once created)  
- Open the drawer to navigate to other features

---

## 2.1 Adding a New Member

Tap **“Add New Member”**:

You can enter:

- Name  
- Gender  
- Age  
- (Optional) Birthday  

If the age is under 18, the birthday may be required (depending on validation rules).

After saving:

- The new member appears in the list  
- You can select them as the **current active member**

<p align="center">
  <img src="https://github.com/user-attachments/assets/2c40c2b3-045f-4b30-8d63-d1d147e39c72" width="240">
</p>

---

## 2.2 Selecting the Active Member

From the Member Selection screen:

1. Tap on a member in the list  
2. That member becomes the **current active member**  
3. The app navigates to the main wardrobe view for that member

The currently selected member is used across:

- Home / Inventory  
- Statistics  
- Recommendations  
- Transfers  

---

# 3. Drawer Navigation (Side Menu)

The app includes a **navigation drawer**, accessible from the top-left icon on almost every screen.

The drawer provides quick access to all major sections of the app:

- **Home** – Clothing inventory for the selected member  
- **Member** – Member selection and management  
- **Statistics** – Charts and analytics  
- **Settings** – Configuration panel  
- **Admin Mode** toggle  
- **AI Mode** toggle  
- **Dark Mode** toggle  

<p align="center">
  <img src="https://github.com/user-attachments/assets/33e0fe53-2090-436b-b09a-f1df744eaa68" width="240">
</p>

### 3.1 Drawer Sections Explained

#### Home
Takes you to the clothing inventory for the currently selected member.

#### Member
Opens the **member selection** screen.  
This is useful if you want to quickly switch between children or family members.

#### Statistics
Opens the analytics page where you can see distributions of:

- transfer history
- clothing Inventory

#### Settings
Contains advanced configuration and NFC binding workflow (explained later).

---

### 3.2 Drawer Toggles

These global toggles live **inside the drawer**, not inside Settings:

#### Admin Mode
Allows privileged operations such as deleting locations that still contain items.  
Normal users cannot do these operations.

#### AI Mode
Enables or disables AI-powered auto-fill when adding clothing items.  
If turned off, AI requests will not be made.

#### Dark Mode
Switches the entire UI theme between light and dark.

---

After choosing a member and exploring sections via the drawer, the Home Screen becomes the main working area of the app.

# 4. Home / Inventory Screen

After selecting a member, you are taken to the **Home / Clothing Inventory** screen.  
Here you can:

- View all clothing items for the current member  
- Filter or scroll through the list  
- Open the side drawer for navigation  
- Add new clothing items

<p align="center">
  <img src="https://github.com/user-attachments/assets/8ec24cc9-ccce-49bc-bdf3-e4f5ea5ef79c" width="240">
</p>

---

## 4.1 Adding a Clothing Item

On the Home screen, tap the **Add Item** button (or equivalent action).

You can specify:

- Description  
- Category  
- Warmth level  
- Occasion  
- Color  
- Size label  
- Season  
- Favorite flag  
- Tags  
- (Optional) Location

<p align="center">
  <img src="https://github.com/user-attachments/assets/20fc4cd3-2e8e-44f8-bff6-23155c72467f" width="240">
</p>

---

## 4.2 AI Auto-Fill (Optional)

If AI is enabled in Settings:

1. Add or take a photo of the clothing item  
2. The app sends the image to Gemini  
3. Detected attributes such as **category**, **warmth level**, **color**, or **season** are automatically suggested

The user can still edit all fields before saving.

---

# 5. NFC Smart Storage

Wardrobe allows you to associate **physical storage locations** (e.g. boxes, shelves) with **NFC stickers**.

## 5.1 Binding an NFC Sticker to a Location

1. Open the **Settings** screen from the drawer  
2. Enter **“Bind Location”** mode (e.g. “Add new NFC sticker”)  
3. Scan an NFC tag with your phone  
4. A dialog appears asking you to choose which Location this tag should represent  
5. Confirm to bind the tag to that Location

<p align="center">
  <img src="https://github.com/user-attachments/assets/fd08227b-388a-42d8-9f77-405fec629e79" width="240">
</p>

After binding, the tag is stored in the database and linked to that Location.

---

## 5.2 Using NFC to Navigate to a Location

When the app is running (or in foreground) and **NFC mode is Idle**:

1. Scan a known NFC tag  
2. The app resolves the location ID associated with the tag  
3. The `MainViewModel` sets a navigation request  
4. The UI navigates directly to that Location’s screen, showing all stored items there

---

# 6. Location Screen

A Location screen shows:

- Location name / description  
- Thumbnails of clothing stored there  

You can:

- See where each piece of clothing is physically stored  

<p align="center">
  <img src="https://github.com/user-attachments/assets/2e2b23cf-8bb9-4852-9581-afc25c83b582" width="240">
</p>

---

# 7. Item Details & Transfer Between Members

From the clothing list or location view:

1. Tap an item → opens the **Item Detail** screen  
2. You can edit the item information, change favorite status, or move it

To **transfer an item to another member**:

1. Open the Item Detail  
2. Tap the **Transfer** button  
3. Select the target member  
4. Confirm

The app:

- Updates the item owner  
- Records the transfer in **TransferHistory**  
- Shows it later in the transfer history screen

<p align="center">
  <img src="https://github.com/user-attachments/assets/9c91f5d9-f2e8-4339-82b7-bcd27b879338" width="240">
  <img src="https://github.com/user-attachments/assets/b50291cf-2517-4fa9-84e4-a60d84bd832c" width="240">
</p>

---

# 8. Statistics Screen

The **Statistics** screen provides an overview of the current member’s wardrobe:

- Distribution by category (tops, bottoms, outerwear, etc.)  
- Seasonal distribution (summer, winter, etc.)  
- Member distribution

Charts are generated using chart libraries and updated automatically from the database.

<p align="center">
  <img src="https://github.com/user-attachments/assets/22297ece-d5da-40b0-81c8-186a0f037ec5" width="240">
  <img src="https://github.com/user-attachments/assets/679117cd-b0a5-47c2-a6f4-9722473c89ea" width="240">
  <img src="https://github.com/user-attachments/assets/9597b302-b843-464a-ba65-0b1d5d4b5db9" width="240">
</p>

This helps users see imbalances (e.g. too many coats, not enough summer clothes).

---

# 9. Settings Screen

The **Settings** screen contains configuration options such as:

- Change admin mode PIN (If exists)
- Toggle **NFC bind mode**  

<p align="center">
  <img src="https://github.com/user-attachments/assets/b65ebde3-6190-4752-adce-d536ab4110c0" width="240">
</p>

---

# 10. Tips & Best Practices

- When starting the app, always choose the correct **active member** on the Member Selection screen.  
- Use NFC stickers on boxes, drawers, or bags to quickly jump to a location in the app.  
- Use AI auto-fill to speed up adding large numbers of items.  
- Transfer items instead of deleting/recreating them when they are passed from one child to another.  
- Use statistics to decide what clothing is missing before going shopping.

---

# 11. Related Documents

For more technical details:

- **Project README:** [README.md](./README.md)  
- **Unit Testing & Coverage:** [TESTING.md](./TESTING.md)  

This tutorial focuses on how to use the app from a user’s perspective, starting from the **Member Selection screen** and covering all key features.
