# BEAT 🎧 - Live Music Interactive Experience

**BEAT** היא אפליקציית אנדרואיד מבוססת מיקום (Location-Based Service) המיועדת ליצור אינטראקציה בזמן אמת בין אמנים לקהל במהלך הופעות חיות. האפליקציה פותחה כפרויקט גמר במסגרת לימודי מדעי המחשב.

---

## 🌟 פיצ'רים מרכזיים (Key Features)

### 👤 חוויית המשתמש (User/Artist)
* **מערכת רישום כפולה:** תהליכי Onboarding מותאמים אישית לאמנים (DJs, זמרים, קומיקאים) ולבליינים.
* **ניהול פרופיל:** העלאת תמונות פרופיל ל-Firebase Storage ועדכון ביוגרפיה וסגנונות מוזיקליים.

### 🗺️ מפה ומיקום (Maps & Location)
* **גילוי הופעות:** מפה אינטראקטיבית המציגה הופעות "לייב" סביב המשתמש בזמן אמת.
* **ניווט חכם:** הטמעת Google Places Autocomplete לחיפוש כתובות מדויק ו-Geocoding להמרת כתובות לקואורדינטות.
* **עדכוני GPS:** שימוש ב-FusedLocationProviderClient לקבלת מיקום מדויק וחסכוני בסוללה.

### 🎤 אינטראקציה בזמן אמת (Real-time Interaction)
* **בקשות שירים:** הקהל יכול לשלוח בקשות תוכן ישירות לאמן במהלך המופע.
* **מדד שביעות רצון:** מערכת לייקים/דיסלייקים המשקפת לאמן את הפידבק מהקהל בלייב.
* **לוח בקרה לאמן:** ניהול רשימת בקשות עם אפשרות לסימון "בוצע" (Played) ועדכון סטטוס המופע.

---

## 🛠 טכנולוגיות וספריות (Tech Stack)

* **Language:** Java (Android SDK).
* **Backend:** Firebase (Auth, Realtime Database, Storage).
* **Networking:** Retrofit 2 & GSON (קריאה ל-Google Cloud APIs).
* **Location:** Google Maps SDK & Google Places API.
* **Media Handling:** Glide (טעינת תמונות אסינכרונית וניהול Cache).
* **UI/UX:** Material Design Components, RecyclerView, Navigation Component.

---

## 🏗 ארכיטקטורה ודפוסי עיצוב (Design Patterns)

הפרויקט מיישם עקרונות הנדסת תוכנה מתקדמים:
1. **Singleton Pattern:** ניהול מופע יחיד של Retrofit Clients לייעול משאבי רשת.
2. **Observer Pattern:** סנכרון אוטומטי של ה-UI מול שינויים ב-Database בעזרת Firebase Listeners.
3. **Adapter & ViewHolder Pattern:** אופטימיזציה של הצגת רשימות ארוכות לחוויית גלילה חלקה.
4. **Delegation:** הפרדה ברורה בין שכבת התצוגה (Fragments) ללוגיקה העסקית והתקשורת (Activities & Models).




## ⚙️ התקנה והרצה (Setup)

1. שכפל את הפרויקט: `git clone [Your-Repo-URL]`
2. הוסף את קובץ ה-`google-services.json` לתיקיית ה-`app`.
3. הגדר את ה-`API_KEY` של Google Maps ב-`AndroidManifest.xml`.
4. בצע Build וסנכרון ל-Gradle.

---

## 👨‍💻 פותח על ידי
שיר מולקנדוב
נוי ליבוביץ

.

---
*פרויקט זה פותח תוך דגש על ביצועים, אבטחת נתונים וחוויית משתמש מודרנית.*
