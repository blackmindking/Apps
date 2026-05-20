import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

// Helper to convert other cycles to standard monthly equivalent amount
function toMonthlyAmount(amount: number, billingCycle: string): number {
  switch (billingCycle.toLowerCase()) {
    case 'weekly':
      return amount * 4.33;
    case 'monthly':
      return amount;
    case 'yearly':
    case 'annual':
      return amount / 12;
    default:
      return amount;
  }
}

// Helper to calculate days remaining until a renewal timestamp date
function daysUntil(dateString: string): number {
  const diffTime = new Date(dateString).getTime() - new Date().getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays;
}

// Helper to calculate previous 6 calendar months
function getLast6Months() {
  const months = [];
  const monthLabels = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const today = new Date();
  for (let i = 5; i >= 0; i--) {
    const d = new Date(today.getFullYear(), today.getMonth() - i, 1);
    months.push({
      year: d.getFullYear(),
      month: d.getMonth(),
      label: monthLabels[d.getMonth()]
    });
  }
  return months;
}

export class SubscriptionsService {
  async getAnalyticsSummary(userId: string) {
    const subscriptions = await prisma.subscription.findMany({
      where: { userId, isActive: true }
    });

    // 1. Total monthly (convert all to monthly equivalent)
    const totalMonthly = subscriptions.reduce((sum, s) => {
      return sum + toMonthlyAmount(Number(s.amount), s.billingCycle.toLowerCase());
    }, 0);

    // 2. Spend by category
    const categoryMap = new Map<string, number>();
    subscriptions.forEach(s => {
      const monthly = toMonthlyAmount(Number(s.amount), s.billingCycle.toLowerCase());
      categoryMap.set(s.category, (categoryMap.get(s.category) || 0) + monthly);
    });
    const spendByCategory = Array.from(categoryMap.entries())
      .map(([category, amount]) => ({
        category,
        amount,
        percentage: totalMonthly > 0 ? (amount / totalMonthly) * 100 : 0
      }))
      .sort((a, b) => b.amount - a.amount);

    // 3. Monthly history (last 6 months — estimate based on active subs)
    const monthlyHistory = getLast6Months().map(({ year, month, label }) => ({
      month: label,
      amount: totalMonthly // simplified — use actual billing dates in v2
    }));

    // 4. Top 5 most expensive
    const topExpensive = [...subscriptions]
      .sort((a, b) => toMonthlyAmount(Number(b.amount), b.billingCycle.toLowerCase())
        - toMonthlyAmount(Number(a.amount), a.billingCycle.toLowerCase()))
      .slice(0, 5);

    // 5. Due this week
    const dueThisWeek = subscriptions.filter(s => {
      const days = daysUntil(s.nextRenewalDate.toISOString());
      return days >= 0 && days <= 7;
    });

    // 6. Dynamic insight
    const topCategory = spendByCategory[0];
    const topPct = topCategory ? Math.round(topCategory.percentage) : 0;
    let insight = `You're spending $${totalMonthly.toFixed(2)}/month on ${subscriptions.length} subscriptions.`;
    if (topCategory && topPct > 40) {
      insight = `${topCategory.category} is eating ${topPct}% of your subscription budget — $${topCategory.amount.toFixed(2)}/month.`;
    }
    if (subscriptions.length === 0) {
      insight = "Add your first subscription to start seeing insights.";
    }

    return {
      totalMonthly,
      totalYearly: totalMonthly * 12,
      activeCount: subscriptions.length,
      dueThisWeek: dueThisWeek.length,
      dueThisWeekAmount: dueThisWeek.reduce((sum, s) => sum + Number(s.amount), 0),
      spendByCategory,
      monthlyHistory,
      topExpensive,
      insight
    };
  }
}
export default new SubscriptionsService();
