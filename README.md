# SubTrackr

> Stop paying for things you forgot about.

A full-stack subscription tracking mobile app built with React Native (Expo) + Node.js or high-performance native Android (Kotlin Compose).

## Project Structure

```
subtrackr/
├── apps/
│   ├── mobile/     React Native Expo app
│   └── api/        Node.js Express REST API
├── app/            Native high-performance Android Jetpack Compose App (Room, ViewModel)
└── packages/
    └── shared/     Shared TypeScript types
```

## Quick Start (Full-Stack React Native + Express API version)

### Prerequisites
- Node.js 20+
- PostgreSQL (or Supabase account)
- Expo CLI (`npm install -g expo-cli`)

### Setup

```bash
# 1. Clone and install
git clone https://github.com/yourname/subtrackr
cd subtrackr
npm install

# 2. Setup API
cd apps/api
cp .env.example .env
# Fill in .env values (see Environment Variables below)
npx prisma migrate dev
npx prisma db seed

# 3. Start API
npm run dev   # runs on http://localhost:3000

# 4. Start Mobile (new terminal)
cd apps/mobile
npx expo start
# Press 'a' for Android emulator, 'i' for iOS simulator
```

### Environment Variables

See `apps/api/.env.example` for all required variables.

Required services (all have free tiers):
- **Supabase** (PostgreSQL): supabase.com
- **Upstash** (Redis): upstash.com
- **Resend** (Email): resend.com
- **RevenueCat** (Payments): revenuecat.com

### Deploy

Backend: Railway (railway.app)
Mobile: Expo EAS (expo.dev/eas)

See DEPLOYMENT.md for step-by-step instructions.

## Quick Start (Native Android Version)

The repository also includes a production-ready, local-first Jetpack Compose native Android implementation in the `/app` folder containing:
- **Room Database** for lightweight persistence.
- **Material 3 UI** with high-contrast elements, custom icons, category segments, and dark canvas mode.
- **MainViewModel** logic implementing offline modes, calculations, and premium paywall views.

To run, open the repository in Android Studio and run `/app`.

## Tech Stack (Hybrid & Full Stack Native)

| Layer | Technology |
|-------|-----------|
| Mobile | React Native, Expo, TypeScript & Native Android Kotlin Compose |
| Navigation | expo-router & Typed Serializable Compose Navigation |
| State | React Query + Zustand & StateFlow with architectural ViewModels |
| Database | PostgreSQL (Prisma ORM) & SQLite (Android Room Database) |
| Cache | Redis (Upstash) |
| Auth | JWT (access + refresh tokens) |
| Payments | RevenueCat |
| Email | Resend |
| Push | Expo Notifications & Android NotificationHelper alarm alerts |
| Hosting | Railway (API) + EAS (Mobile) |

## Revenue Model

- Free tier: up to 5 subscriptions
- Premium: $3.99/month or $29.99/year
- Payment processing: RevenueCat (handles App Store + Google Play)

## License

MIT
