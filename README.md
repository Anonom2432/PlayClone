# 📱 PlayClone — Каталог-агрегатор приложений

Кроссплатформенное Android-приложение с интерфейсом в стиле Google Play Store. Реализует многопользовательскую синхронизацию через Firebase Firestore в реальном времени.

## 🎯 Возможности

- **Лента приложений** — LazyColumn с карточками в стиле Google Play
- **Поиск** — фильтрация в реальном времени с debounce 300ms
- **Добавление приложений** — форма с валидацией и загрузкой изображений
- **Детали приложения** — горизонтальный скролл скриншотов, метаданные
- **Offline-First** — Room кэш с автоматической синхронизацией
- **Real-time синхронизация** — Firebase Firestore snapshot listener
- **Material Design 3** — современная тема с поддержкой тёмного режима

## 🏗 Архитектура

```
Presentation Layer (UI)
    ├── Compose UI (Screens, Components)
    ├── Navigation Compose
    └── ViewModel (StateFlow)
    
Domain Layer (Business Logic)
    ├── Use Cases (GetApps, SearchApps, AddApp)
    └── Domain Models
    
Data Layer (Repository)
    ├── OfflineFirstAppRepository
    ├── Room (Local Cache)
    └── Firebase (Remote Sync)
```

## 🚀 Быстрый старт

### 1. Открыть в GitHub Codespaces

1. Перейдите на GitHub репозиторий проекта
2. Нажмите **Code** → **Codespaces** → **Create codespace on main**
3. Дождитесь инициализации среды (~2-3 минуты)
4. Терминал автоматически откроется в директории проекта

### 2. Настройка Firebase (для синхронизации)

#### Вариант A: Локальная разработка

1. Создайте проект в [Firebase Console](https://console.firebase.google.com/)
2. Добавьте Android-приложение с package name `com.example.playclone`
3. Скачайте `google-services.json` и поместите в `app/google-services.json`
4. Включите следующие Firebase сервисы:
   - **Firestore Database** — создайте базу в production режиме
   - **Storage** — для загрузки изображений
   - **Authentication** — включите анонимную авторизацию

#### Вариант B: CI/CD через GitHub Secrets

Для автоматической сборки без Firebase создайте placeholder:

```bash
# В GitHub Actions это делается автоматически
mkdir -p app && cat > app/google-services.json << 'EOF'
{"project_info":{"project_number":"000000000000","project_id":"placeholder"},"client":[{"client_info":{"package_name":"com.example.playclone"},"api_key":[{"current_key":"placeholder"}]}]}
EOF
```

Приложение автоматически переключится на локальный режим (LocalMockRepository).

### 3. Сборка и запуск

#### Локально (Android Studio)

```bash
# Клонировать репозиторий
git clone <repository-url>
cd playclone

# Открыть в Android Studio или запустить из терминала:
./gradlew assembleDebug

# Установить на подключённое устройство/эмулятор
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### В Codespaces

```bash
# Собрать debug APK
./gradlew assembleDebug

# APK будет доступен в:
# app/build/outputs/apk/debug/app-debug.apk
```

#### Запуск на эмуляторе Pixel 7 API 34

1. Откройте AVD Manager в Android Studio
2. Создайте эмулятор: **Pixel 7** + **API 34 (Android 14)**
3. Запустите эмулятор
4. Перетащите `app-debug.apk` на окно эмулятора или выполните:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## 🧪 Тестирование

### Unit-тесты

```bash
./gradlew testDebugUnitTest
```

### Инструментированные тесты

```bash
./gradlew connectedAndroidTest
```

### Проверка синхронизации

1. Запустите приложение на двух устройствах/эмуляторах
2. На устройстве A добавьте новое приложение через кнопку **+**
3. На устройстве B приложение появится автоматически в течение 1-2 секунд
4. Проверьте Pull-to-Refresh для принудительной синхронизации

## 🔧 CI/CD

Проект включает GitHub Actions workflow для автоматической сборки:

```yaml
# .github/workflows/build-apk.yml
on: push → build + test → upload APK artifact
```

APK артефакты доступны в разделе **Actions** → **Build APK** → **Artifacts**.

## 📁 Структура проекта

```
app/
├── src/main/
│   ├── java/com/example/playclone/
│   │   ├── data/           # Слой данных (Room, Firebase, Repository)
│   │   ├── domain/         # Бизнес-логика (Use Cases, Models)
│   │   ├── presentation/   # UI (Screens, ViewModel, Theme, Navigation)
│   │   ├── di/             # Dependency Injection
│   │   ├── util/           # Утилиты и константы
│   │   ├── MainActivity.kt
│   │   └── PlayCloneApp.kt
│   └── res/                # Ресурсы (strings, themes, drawables)
├── test/                   # Unit-тесты
└── build.gradle.kts
```

## 🛠 Технологии

| Компонент | Технология |
|-----------|------------|
| UI | Jetpack Compose + Material 3 |
| Навигация | Navigation Compose |
| Локальная БД | Room 2.6.1 |
| Синхронизация | Firebase Firestore |
| Изображения | Firebase Storage + Coil |
| Асинхронность | Kotlin Coroutines + Flow |
| DI | Ручной (AppContainer) |
| Мин. SDK | 24 (Android 7.0) |
| Целевой SDK | 34 (Android 14) |

## ⚙️ Конфигурация

### gradle.properties

```properties
org.gradle.jvmargs=-Xmx2048m
android.useAndroidX=true
kotlin.code.style=official
```

### Версии зависимостей

- Gradle: 8.2
- Kotlin: 1.9.22
- Compose BOM: 2024.02.00
- Firebase BOM: 32.7.2
- Room: 2.6.1

## 🔐 Безопасность

- **Не храните секреты в коде** — используйте `local.properties` или GitHub Secrets
- **Firebase Rules** — настройте правила Firestore для production:
  ```javascript
  rules_version = '2';
  service cloud.firestore {
    match /databases/{database}/documents {
      match /apps/{appId} {
        allow read: if true;
        allow write: if request.auth != null;
      }
    }
  }
  ```

## 📝 Лицензия

MIT License — свободное использование с указанием авторства.

---

**Статус сборки:** [![Build APK](https://github.com/yourusername/playclone/actions/workflows/build-apk.yml/badge.svg)]()

**Последняя версия:** 1.0.0 (API 34)
