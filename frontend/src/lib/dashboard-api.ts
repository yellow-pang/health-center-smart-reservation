import { apiRequest } from './api-client';
import type {
  DashboardStats,
  HourlyVisitors,
  ServiceWaitTime,
  VisitTypeRatio,
} from './mock-data';

interface DashboardSummaryApiResponse {
  todayVisitCount: number;
  currentWaitingCount: number;
  averageWaitMinutes: number;
  noShowRate: number;
}

interface HourlyVisitorsApiResponse {
  hour: number | string;
  visitCount: number;
}

interface ServiceWaitTimeApiResponse {
  serviceTypeId: number;
  serviceTypeName: string;
  averageWaitMinutes: number;
  calledCount: number;
}

interface VisitTypeRatioApiResponse {
  totalVisitCount: number;
  reservedVisitCount: number;
  walkInVisitCount: number;
  reservedVisitRatio: number;
  walkInVisitRatio: number;
}

interface NoShowRateApiResponse {
  targetReservationCount: number;
  noShowReservationCount: number;
  noShowRate: number;
}

export interface DashboardDateFilter {
  date?: string;
}

export async function getDashboardStats(
  filter: DashboardDateFilter = {},
): Promise<DashboardStats> {
  const summary = await apiRequest<DashboardSummaryApiResponse>('/api/dashboard/summary', {
    query: { date: filter.date },
  });

  return {
    todayVisitors: summary.todayVisitCount,
    currentWaiting: summary.currentWaitingCount,
    avgWaitMinutes: summary.averageWaitMinutes,
    noShowRate: summary.noShowRate,
  };
}

export async function getHourlyVisitors(
  filter: DashboardDateFilter = {},
): Promise<HourlyVisitors[]> {
  const visitors = await apiRequest<HourlyVisitorsApiResponse[]>(
    '/api/dashboard/hourly-visits',
    {
      query: { date: filter.date },
    },
  );

  return visitors.map((item) => ({
    hour: formatHourLabel(item.hour),
    count: item.visitCount,
  }));
}

function formatHourLabel(hour: number | string): string {
  const normalizedHour = normalizeHour(hour);

  return `${String(normalizedHour).padStart(2, '0')}시`;
}

function normalizeHour(hour: number | string): number {
  if (typeof hour === 'number' && Number.isFinite(hour)) {
    return clampHour(Math.trunc(hour));
  }

  const parsedHour = Number.parseInt(String(hour), 10);
  if (Number.isFinite(parsedHour)) {
    return clampHour(parsedHour);
  }

  return 0;
}

function clampHour(hour: number): number {
  return Math.max(0, Math.min(23, hour));
}

export async function getServiceWaitTimes(
  filter: DashboardDateFilter = {},
): Promise<ServiceWaitTime[]> {
  const waitTimes = await apiRequest<ServiceWaitTimeApiResponse[]>(
    '/api/dashboard/service-wait-times',
    {
      query: { date: filter.date },
    },
  );

  return waitTimes.map((item) => ({
    serviceType: item.serviceTypeName,
    avgMinutes: item.averageWaitMinutes,
  }));
}

export async function getVisitTypeRatio(
  filter: DashboardDateFilter = {},
): Promise<VisitTypeRatio[]> {
  const ratio = await apiRequest<VisitTypeRatioApiResponse>(
    '/api/dashboard/visit-type-ratio',
    {
      query: { date: filter.date },
    },
  );

  return [
    {
      type: '예약 방문',
      count: ratio.reservedVisitCount,
      percentage: ratio.reservedVisitRatio,
    },
    {
      type: '현장 접수',
      count: ratio.walkInVisitCount,
      percentage: ratio.walkInVisitRatio,
    },
  ];
}

export async function getNoShowRate(
  filter: DashboardDateFilter = {},
): Promise<NoShowRateApiResponse> {
  return apiRequest<NoShowRateApiResponse>('/api/dashboard/no-show-rate', {
    query: { date: filter.date },
  });
}
