import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcrypt';

const prisma = new PrismaClient();

async function main() {
  // Create a demo user
  const passwordHash = await bcrypt.hash('Demo1234!', 12);
  
  const user = await prisma.user.upsert({
    where: { email: 'demo@subtrackr.app' },
    update: {},
    create: {
      email: 'demo@subtrackr.app',
      name: 'Demo User',
      passwordHash,
      planTier: 'FREE',
      settings: {
        create: {
          defaultCurrency: 'USD',
          onboardingCompleted: false,
        }
      }
    }
  });

  // Create 3 demo subscriptions
  const today = new Date();
  const demoSubs = [
    {
      name: 'Netflix',
      category: 'ENTERTAINMENT',
      amount: 15.99,
      billingCycle: 'MONTHLY',
      color: '#FF6B6B',
      nextRenewalDate: new Date(today.getTime() + 12 * 86400000),
    },
    {
      name: 'Spotify',
      category: 'MUSIC',
      amount: 9.99,
      billingCycle: 'MONTHLY',
      color: '#6C63FF',
      nextRenewalDate: new Date(today.getTime() + 5 * 86400000),
    },
    {
      name: 'iCloud',
      category: 'PRODUCTIVITY',
      amount: 2.99,
      billingCycle: 'MONTHLY',
      color: '#5352ED',
      nextRenewalDate: new Date(today.getTime() + 18 * 86400000),
    },
  ];

  for (const sub of demoSubs) {
    await prisma.subscription.create({
      data: {
        ...sub,
        userId: user.id,
        currency: 'USD',
        startDate: new Date(today.getTime() - 30 * 86400000),
        notes: '',
        reminderEnabled: true,
        reminderDaysBefore: 3,
        isActive: true,
      }
    });
  }

  console.log('✅ Seed complete. Demo user: demo@subtrackr.app / Demo1234!');
}

main().catch(console.error).finally(() => prisma.$disconnect());
