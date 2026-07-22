# کدشیر </> — اپلیکیشن اشتراک‌گذاری حرفه‌ای کد

اپلیکیشن اندرویدی با **Kotlin + Jetpack Compose + Firebase** — کاملاً فارسی و راست‌چین.

## ✨ قابلیت‌ها (خلاصه)

- 🔐 ورود/ثبت‌نام با ایمیل و گوگل، بازیابی رمز، مسدودسازی کاربران
- 👑 سیستم نقش: مالک / ادمین / کاربر — فقط مالک و ادمین‌ها آپلود می‌کنند
- 📁 آپلود چند فایل، دسته‌بندی، تگ، کاور، نسخه‌بندی
- 👁️ نمایش کد با Syntax Highlighting رنگی + ۴ تم (Dark, Light, Dracula, Monokai)
- ⬇️ دانلود تکی یا ZIP کل پروژه + شمارنده دانلود و بازدید
- 💬 نظرات تو در تو، پرسش و پاسخ، لایک نظر، گزارش تخلف
- ❤️ لایک/دیسلایک، امتیاز ستاره‌ای، ذخیره (بوکمارک)
- 🔍 جستجوی پیشرفته، فیلتر زبان/دسته، مرتب‌سازی، ترندینگ 🔥
- 🏆 لیدربورد، سیستم امتیاز و سطح (تازه‌کار تا افسانه)
- 🛡️ پنل ادمین: مدیریت کاربران، ارتقا به ادمین، رسیدگی به گزارش‌ها
- 📊 داشبورد آماری مالک با نمودار توزیع زبان‌ها
- 🔔 اعلان‌ها + اعلامیه سراسری مالک
- 🌙 تم تاریک/روشن/سیستم + انیمیشن و صفحه Splash

---

## 🚀 راه‌اندازی (یک بار انجام بده)

### گام ۱: ساخت پروژه Firebase (رایگان)

1. برو به [console.firebase.google.com](https://console.firebase.google.com) و با حساب گوگل وارد شو
2. **Add project** → یک اسم بگذار (مثلاً `codeshare`) → Google Analytics را می‌توانی خاموش کنی → **Create**

### گام ۲: افزودن اپ اندروید

1. در صفحه پروژه روی آیکون **Android** کلیک کن
2. **Package name** را دقیقاً این بگذار: `com.codeshare.app`
3. **Register app** → فایل **google-services.json** را دانلود کن
4. فایل دانلودشده را جایگزین این فایل کن:
   `app/google-services.json`
   (فایل فعلی placeholder است و فقط برای کامپایل گذاشته شده — تا جایگزینش نکنی اپ به سرور وصل نمی‌شود)

### گام ۳: فعال‌سازی سرویس‌ها در کنسول Firebase

| سرویس | مسیر | تنظیم |
|---|---|---|
| **Authentication** | Build → Authentication → Get started | تب Sign-in method: **Email/Password** را Enable کن. برای ورود گوگل: **Google** را هم Enable کن |
| **Firestore** | Build → Firestore Database → Create database | حالت **Production** و نزدیک‌ترین ریجن |
| **Storage** | Build → Storage → Get started | حالت Production |

> ⚠️ برای ورود با گوگل باید **SHA-1** دستگاهت را هم اضافه کنی:
> در پوشه پروژه بزن: `gradlew.bat signingReport` و مقدار SHA-1 (debug) را در
> Firebase Console → Project settings → اپ اندروید → Add fingerprint وارد کن.
> بعد google-services.json را دوباره دانلود و جایگزین کن.

### گام ۴: قوانین امنیتی (خیلی مهم!)

**Firestore** → تب Rules → محتوای فایل [firestore.rules](firestore.rules) را کپی کن → Publish

**Storage** → تب Rules → محتوای فایل [storage.rules](storage.rules) را کپی کن → Publish

### گام ۵: بیلد و نصب

```bash
# در پوشه پروژه:
gradlew.bat assembleDebug
```

فایل APK اینجا ساخته می‌شود:
`app/build/outputs/apk/debug/app-debug.apk`

آن را به گوشی منتقل و نصب کن، یا با گوشی وصل‌شده:
```bash
gradlew.bat installDebug
```

### گام ۶: مالک شدن! 👑

**اولین کسی که در اپ ثبت‌نام کند، خودکار «مالک» (OWNER) می‌شود.**
پس اول خودت ثبت‌نام کن! بعد از آن می‌توانی از «پنل مدیریت» بقیه را ادمین کنی.

---

## 📂 ساختار پروژه

```
app/src/main/java/com/codeshare/app/
├── MainActivity.kt              ← نقطه ورود
├── CodeShareApp.kt              ← کلاس Application
├── data/
│   ├── model/Models.kt          ← مدل‌های داده (کاربر، فایل، نظر…)
│   └── repo/                    ← لایه ارتباط با Firebase
│       ├── AuthRepository.kt
│       ├── FileRepository.kt
│       ├── InteractionRepository.kt
│       └── AdminRepository.kt
└── ui/
    ├── AppViewModel.kt          ← منطق اصلی اپ
    ├── AppNavHost.kt            ← ناوبری بین صفحات
    ├── theme/                   ← رنگ‌ها و تم
    ├── components/              ← کامپوننت‌های مشترک + نمایشگر کد
    └── screens/                 ← ۱۱ صفحه اپ
```
