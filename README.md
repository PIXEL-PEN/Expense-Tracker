# 📒 Expense Tracker

A simple, offline Android app for daily expense tracking and budgeting.  
Designed for **fast entry and clear summaries**, with no ads, no accounts, and no internet connection required.

---

## ✨ Features

- ➕ **Add Expenses**  
  Enter description, amount, category, and date.  
  Default categories: Groceries, Rent, Utilities, Bills, Transport, Other.  
  Users can add their own (persist after reset, sorted alphabetically).

- 👀 **View Expenses** in multiple ways:
  - View All – full chronological list
  - By Date – grouped by day (banners like *10 Sep. 2025*)
  - By Month – grouped by month (banners like *September – 2025*)
  - By Category – alphabetized ledger
  - Day Detail View – drill-down into all expenses for a specific day

- 💰 **Totals everywhere** (day, month, and per-view totals with proper decimals & commas)

- 📤 **Export data**  
  - CSV → storage  
  - HTML → email

- 🗑 **Reset database** (expenses only — categories preserved)

- ⚙️ **Settings**
  - Currency preference (saved in `SharedPreferences`)
  - Date format spinner *(placeholder — not yet functional)*

---

## 📱 Screenshots

*(to be added by maintainer)*  
- Main Menu  
- Add Expense  
- Settings  
- Example Views  

---

## ⚡ Usage

1. Install on your Android device.  
2. Add expenses daily.  
3. View by date, month, or category to spot trends.  
4. Export data for backup or analysis.  

**Best results:** use it every day as your budgeting tool.

---

## ⚠️ Compatibility

- ✅ Tested on **modern Android devices (API 31+, Android 12 and above)**  
- ⚠️ On **older 16:9 devices** (e.g., Galaxy Note5, Android 7 / API 24):
  - Top bars and footers may appear deeper  
  - Buttons and banners may look oversized  
  - *This is cosmetic only — all functions work correctly*  
- Scaling adjustments for legacy devices are planned before public release.

---

## 🚀 Planned Enhancements

- Better scaling on older/smaller screens (API < 24).  
- Tagging system for categories (Fixed, Essential, Discretionary).  
- Export improvements (filtering, date ranges).  
- Improved Settings layout with more options.  
- Additional visual polish and theming.  

---

## 🛠 Development

- Built in **Java** with **Room (SQLite)** and classic Android XML layouts.  
- Active work on `main`; stable checkpoints live in `baseline-stable`.  
- Freeze tags are used to capture milestones and safe builds.  

### App Variants

- **ExpTrack-dev** → development/debug build  
  *(package id: `com.example.expensetracker.dev`)*  
- **ExpTracker** → release build  
  *(package id: `com.example.expensetracker`)*  

Both can be installed side-by-side.

---

## 📜 License

This project is licensed under the **MIT License**.  
See [LICENSE](LICENSE) for details.
