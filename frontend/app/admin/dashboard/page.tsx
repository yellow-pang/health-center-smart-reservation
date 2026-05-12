'use client';

import { useState, useEffect } from 'react';
import { Users, Clock, UserX, TrendingUp, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { PageHeader } from '@/src/components/common/page-header';
import { MetricCard } from '@/src/components/common/metric-card';
import { LoadingState } from '@/src/components/common/loading-state';
import { ErrorState } from '@/src/components/common/error-state';
import {
  getDashboardStats,
  getHourlyVisitors,
  getServiceWaitTimes,
  getVisitTypeRatio,
} from '@/src/lib/mock-services';
import type { 
  DashboardStats, 
  HourlyVisitors, 
  ServiceWaitTime, 
  VisitTypeRatio 
} from '@/src/lib/mock-data';
import { 
  LineChart, 
  Line, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import { cn } from '@/lib/utils';

type LoadState = 'loading' | 'success' | 'error';

export default function AdminDashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [hourlyData, setHourlyData] = useState<HourlyVisitors[]>([]);
  const [waitTimeData, setWaitTimeData] = useState<ServiceWaitTime[]>([]);
  const [visitTypeData, setVisitTypeData] = useState<VisitTypeRatio[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [isRefreshing, setIsRefreshing] = useState(false);

  const loadData = async (showRefreshing = false) => {
    if (showRefreshing) {
      setIsRefreshing(true);
    } else {
      setLoadState('loading');
    }

    try {
      const [statsData, hourly, waitTimes, visitTypes] = await Promise.all([
        getDashboardStats(),
        getHourlyVisitors(),
        getServiceWaitTimes(),
        getVisitTypeRatio(),
      ]);
      
      setStats(statsData);
      setHourlyData(hourly);
      setWaitTimeData(waitTimes);
      setVisitTypeData(visitTypes);
      setLoadState('success');
    } catch {
      setLoadState('error');
    } finally {
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const pieColors = ['hsl(var(--chart-1))', 'hsl(var(--chart-2))'];

  return (
    <div>
      <PageHeader 
        title="관리자 대시보드" 
        description="보건소 운영 현황을 한눈에 확인하세요"
        actions={
          <Button 
            variant="outline" 
            onClick={() => loadData(true)}
            disabled={isRefreshing}
          >
            <RefreshCw className={cn('h-4 w-4 mr-2', isRefreshing && 'animate-spin')} />
            새로고침
          </Button>
        }
      />

      {loadState === 'loading' && <LoadingState />}
      
      {loadState === 'error' && <ErrorState onRetry={() => loadData()} />}

      {loadState === 'success' && stats && (
        <>
          {/* KPI Cards */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
            <MetricCard
              title="오늘 방문자"
              value={stats.todayVisitors}
              subtitle="명"
              icon={Users}
              trend={{ value: 12, isPositive: true }}
            />
            <MetricCard
              title="현재 대기"
              value={stats.currentWaiting}
              subtitle="명"
              icon={Clock}
            />
            <MetricCard
              title="평균 대기시간"
              value={`${stats.avgWaitMinutes}분`}
              icon={TrendingUp}
              trend={{ value: 5, isPositive: false }}
            />
            <MetricCard
              title="노쇼율"
              value={`${stats.noShowRate}%`}
              icon={UserX}
              trend={{ value: 2, isPositive: false }}
            />
          </div>

          {/* Charts Row 1 */}
          <div className="grid lg:grid-cols-2 gap-6 mt-6">
            {/* Hourly Visitors Line Chart */}
            <Card>
              <CardHeader>
                <CardTitle className="text-base">시간대별 방문자 수</CardTitle>
                <CardDescription>오늘 시간대별 방문자 추이</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-64">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={hourlyData}>
                      <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                      <XAxis 
                        dataKey="hour" 
                        tick={{ fontSize: 12 }}
                        tickLine={false}
                        axisLine={false}
                      />
                      <YAxis 
                        tick={{ fontSize: 12 }}
                        tickLine={false}
                        axisLine={false}
                      />
                      <Tooltip 
                        contentStyle={{ 
                          backgroundColor: 'hsl(var(--card))',
                          border: '1px solid hsl(var(--border))',
                          borderRadius: '8px',
                        }}
                        formatter={(value: number) => [`${value}명`, '방문자']}
                      />
                      <Line 
                        type="monotone" 
                        dataKey="count" 
                        stroke="hsl(var(--primary))"
                        strokeWidth={2}
                        dot={{ fill: 'hsl(var(--primary))', strokeWidth: 2 }}
                        activeDot={{ r: 6 }}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card>

            {/* Service Wait Times Bar Chart */}
            <Card>
              <CardHeader>
                <CardTitle className="text-base">업무별 평균 대기시간</CardTitle>
                <CardDescription>업무 유형별 평균 대기시간 (분)</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-64">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={waitTimeData} layout="vertical">
                      <CartesianGrid strokeDasharray="3 3" className="stroke-muted" horizontal={true} vertical={false} />
                      <XAxis 
                        type="number"
                        tick={{ fontSize: 12 }}
                        tickLine={false}
                        axisLine={false}
                      />
                      <YAxis 
                        type="category"
                        dataKey="serviceType" 
                        tick={{ fontSize: 12 }}
                        tickLine={false}
                        axisLine={false}
                        width={80}
                      />
                      <Tooltip 
                        contentStyle={{ 
                          backgroundColor: 'hsl(var(--card))',
                          border: '1px solid hsl(var(--border))',
                          borderRadius: '8px',
                        }}
                        formatter={(value: number) => [`${value}분`, '평균 대기']}
                      />
                      <Bar 
                        dataKey="avgMinutes" 
                        fill="hsl(var(--chart-2))"
                        radius={[0, 4, 4, 0]}
                      />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Charts Row 2 */}
          <div className="grid lg:grid-cols-3 gap-6 mt-6">
            {/* Visit Type Ratio Pie Chart */}
            <Card>
              <CardHeader>
                <CardTitle className="text-base">방문 유형 비율</CardTitle>
                <CardDescription>예약 vs 현장 방문</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-48">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={visitTypeData}
                        cx="50%"
                        cy="50%"
                        innerRadius={50}
                        outerRadius={70}
                        paddingAngle={2}
                        dataKey="count"
                        nameKey="type"
                        label={({ type, percentage }) => `${type} ${percentage}%`}
                        labelLine={false}
                      >
                        {visitTypeData.map((_, index) => (
                          <Cell key={`cell-${index}`} fill={pieColors[index % pieColors.length]} />
                        ))}
                      </Pie>
                      <Tooltip 
                        contentStyle={{ 
                          backgroundColor: 'hsl(var(--card))',
                          border: '1px solid hsl(var(--border))',
                          borderRadius: '8px',
                        }}
                        formatter={(value: number, name: string) => [`${value}명`, name]}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
                <div className="flex justify-center gap-6 mt-4">
                  {visitTypeData.map((item, index) => (
                    <div key={item.type} className="flex items-center gap-2">
                      <div 
                        className="h-3 w-3 rounded-full" 
                        style={{ backgroundColor: pieColors[index] }}
                      />
                      <span className="text-sm text-muted-foreground">
                        {item.type} ({item.percentage}%)
                      </span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* No-Show Rate Card */}
            <Card className="lg:col-span-2">
              <CardHeader>
                <CardTitle className="text-base">노쇼율 현황</CardTitle>
                <CardDescription>예약 후 미방문 비율</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-4xl font-bold">{stats.noShowRate}%</p>
                    <p className="text-sm text-muted-foreground mt-1">
                      전일 대비 2% 감소
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm text-muted-foreground">목표</p>
                    <p className="text-2xl font-semibold text-primary">5%</p>
                  </div>
                </div>
                
                {/* Progress Bar */}
                <div className="mt-6">
                  <div className="flex justify-between text-sm mb-2">
                    <span className="text-muted-foreground">진행 현황</span>
                    <span className="font-medium">
                      {Math.round((1 - stats.noShowRate / 15) * 100)}% 달성
                    </span>
                  </div>
                  <div className="h-3 rounded-full bg-muted overflow-hidden">
                    <div 
                      className="h-full rounded-full bg-primary transition-all"
                      style={{ width: `${Math.round((1 - stats.noShowRate / 15) * 100)}%` }}
                    />
                  </div>
                </div>
                
                <p className="text-xs text-muted-foreground mt-4">
                  SMS 리마인더와 취소 페널티 도입으로 노쇼율을 낮추고 있습니다.
                </p>
              </CardContent>
            </Card>
          </div>
        </>
      )}
    </div>
  );
}
