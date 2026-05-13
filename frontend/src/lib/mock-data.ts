// ============================================
// Types
// ============================================

export type UserRole = 'CITIZEN' | 'STAFF' | 'ADMIN';

export type QueueStatus = 
  | 'WAITING' 
  | 'CALLED' 
  | 'IN_PROGRESS' 
  | 'HOLD' 
  | 'COMPLETED' 
  | 'NO_SHOW' 
  | 'CANCELED';

export type VisitType = 'RESERVED' | 'WALK_IN';

export type ReservationStatus = 'RESERVED' | 'CANCELED' | 'CHECKED_IN' | 'NO_SHOW' | 'COMPLETED';

export type CongestionLevel = 'LOW' | 'NORMAL' | 'HIGH';

export interface User {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  phone?: string;
}

export interface ServiceType {
  serviceTypeId: number;
  healthCenterId?: number | null;
  code: string;
  name: string;
  description: string;
  defaultCapacity: number;
  active: boolean;
}

export interface ReservationSlot {
  slotId: number;
  serviceTypeId: number;
  date: string;
  startTime: string;
  endTime: string;
  capacity: number;
  reservedCount: number;
  availableCount: number;
  available: boolean;
}

export interface Reservation {
  reservationId: number;
  reservationNo: string;
  userId?: string;
  serviceTypeId: number;
  serviceTypeName?: string;
  reservationSlotId?: number;
  visitorName: string;
  visitorPhone: string;
  date: string;
  startTime: string;
  endTime?: string;
  status: ReservationStatus;
  createdAt?: string;
  reservedAt?: string;
}

export interface QueueEntry {
  queueTicketId: number;
  ticketNumber: string;
  visitorNameMasked: string;
  visitorPhoneMasked: string;
  serviceTypeId: number;
  visitType: VisitType;
  status: QueueStatus;
  reservationId?: number;
  windowId?: string;
  calledAt?: string;
  startedAt?: string;
  completedAt?: string;
  createdAt: string;
}

export interface ServiceWindow {
  id: string;
  name: string;
  serviceTypeIds: number[];
  staffId?: string;
  active: boolean;
}

export interface Staff {
  id: string;
  userId: string;
  windowId?: string;
  isActive: boolean;
}

export interface CongestionInfo {
  serviceTypeId: number;
  serviceTypeName: string;
  waitingCount: number;
  estimatedWaitMinutes: number;
  level: CongestionLevel;
}

export interface DashboardStats {
  todayVisitors: number;
  currentWaiting: number;
  avgWaitMinutes: number;
  noShowRate: number;
}

export interface HourlyVisitors {
  hour: string;
  count: number;
}

export interface ServiceWaitTime {
  serviceType: string;
  avgMinutes: number;
}

export interface VisitTypeRatio {
  type: string;
  count: number;
  percentage: number;
}

// ============================================
// Mock Data
// ============================================

export const mockUsers: User[] = [
  { id: 'user-1', email: 'citizen@test.com', name: '홍길동', role: 'CITIZEN', phone: '010-1234-5678' },
  { id: 'user-2', email: 'staff@test.com', name: '김직원', role: 'STAFF', phone: '010-2345-6789' },
  { id: 'user-3', email: 'admin@test.com', name: '박관리', role: 'ADMIN', phone: '010-3456-7890' },
];

export const mockServiceTypes: ServiceType[] = [
  { serviceTypeId: 1, code: 'VACCINATION', name: '예방접종', description: '예방접종 예약 및 접수', defaultCapacity: 5, active: true },
  { serviceTypeId: 2, code: 'HEALTH_CHECK', name: '건강검진/검사', description: '건강검진과 기본 검사 접수', defaultCapacity: 5, active: true },
  { serviceTypeId: 3, code: 'HEALTH_CONSULT', name: '건강상담', description: '보건 상담과 생활 건강 안내', defaultCapacity: 5, active: true },
];

export const mockReservationSlots: ReservationSlot[] = [
  { slotId: 1, serviceTypeId: 1, date: '2026-05-13', startTime: '09:00', endTime: '09:30', capacity: 5, reservedCount: 2, availableCount: 3, available: true },
  { slotId: 2, serviceTypeId: 1, date: '2026-05-13', startTime: '09:30', endTime: '10:00', capacity: 5, reservedCount: 5, availableCount: 0, available: false },
  { slotId: 3, serviceTypeId: 1, date: '2026-05-13', startTime: '10:00', endTime: '10:30', capacity: 5, reservedCount: 1, availableCount: 4, available: true },
  { slotId: 4, serviceTypeId: 2, date: '2026-05-13', startTime: '09:00', endTime: '09:30', capacity: 5, reservedCount: 1, availableCount: 4, available: true },
  { slotId: 5, serviceTypeId: 2, date: '2026-05-13', startTime: '10:00', endTime: '10:30', capacity: 5, reservedCount: 2, availableCount: 3, available: true },
  { slotId: 6, serviceTypeId: 3, date: '2026-05-13', startTime: '09:00', endTime: '09:30', capacity: 5, reservedCount: 4, availableCount: 1, available: true },
];

export const mockReservations: Reservation[] = [
  {
    reservationId: 1,
    reservationNo: 'RSV-20260513-0001',
    userId: 'user-1',
    serviceTypeId: 1,
    visitorName: '홍길동',
    visitorPhone: '010-1234-5678',
    date: '2026-05-13',
    startTime: '09:00',
    status: 'RESERVED',
    createdAt: '2026-05-12T10:00:00Z',
  },
  {
    reservationId: 2,
    reservationNo: 'RSV-20260514-0002',
    userId: 'user-1',
    serviceTypeId: 3,
    visitorName: '홍길동',
    visitorPhone: '010-1234-5678',
    date: '2026-05-14',
    startTime: '10:00',
    status: 'RESERVED',
    createdAt: '2026-05-12T11:30:00Z',
  },
  {
    reservationId: 3,
    reservationNo: 'RSV-20260510-0001',
    userId: 'user-1',
    serviceTypeId: 2,
    visitorName: '홍길동',
    visitorPhone: '010-1234-5678',
    date: '2026-05-10',
    startTime: '14:00',
    status: 'COMPLETED',
    createdAt: '2026-05-08T09:00:00Z',
  },
];

export const mockQueueEntries: QueueEntry[] = [
  {
    queueTicketId: 1,
    ticketNumber: 'A001',
    visitorNameMasked: '김*수',
    visitorPhoneMasked: '010-****-2222',
    serviceTypeId: 1,
    visitType: 'RESERVED',
    status: 'IN_PROGRESS',
    windowId: 'win-1',
    calledAt: '2026-05-12T09:05:00Z',
    startedAt: '2026-05-12T09:07:00Z',
    createdAt: '2026-05-12T08:55:00Z',
  },
  {
    queueTicketId: 2,
    ticketNumber: 'A002',
    visitorNameMasked: '이*희',
    visitorPhoneMasked: '010-****-3333',
    serviceTypeId: 1,
    visitType: 'WALK_IN',
    status: 'CALLED',
    windowId: 'win-2',
    calledAt: '2026-05-12T09:10:00Z',
    createdAt: '2026-05-12T09:00:00Z',
  },
  {
    queueTicketId: 3,
    ticketNumber: 'A003',
    visitorNameMasked: '박*수',
    visitorPhoneMasked: '010-****-4444',
    serviceTypeId: 1,
    visitType: 'RESERVED',
    status: 'WAITING',
    createdAt: '2026-05-12T09:02:00Z',
  },
  {
    queueTicketId: 4,
    ticketNumber: 'B001',
    visitorNameMasked: '최*영',
    visitorPhoneMasked: '010-****-5555',
    serviceTypeId: 2,
    visitType: 'RESERVED',
    status: 'WAITING',
    createdAt: '2026-05-12T08:50:00Z',
  },
  {
    queueTicketId: 5,
    ticketNumber: 'B002',
    visitorNameMasked: '정*진',
    visitorPhoneMasked: '010-****-6666',
    serviceTypeId: 2,
    visitType: 'WALK_IN',
    status: 'HOLD',
    windowId: 'win-3',
    calledAt: '2026-05-12T09:00:00Z',
    createdAt: '2026-05-12T08:45:00Z',
  },
  {
    queueTicketId: 6,
    ticketNumber: 'C001',
    visitorNameMasked: '강*준',
    visitorPhoneMasked: '010-****-7777',
    serviceTypeId: 3,
    visitType: 'WALK_IN',
    status: 'WAITING',
    createdAt: '2026-05-12T09:08:00Z',
  },
  {
    queueTicketId: 7,
    ticketNumber: 'C002',
    visitorNameMasked: '윤*연',
    visitorPhoneMasked: '010-****-8888',
    serviceTypeId: 3,
    visitType: 'RESERVED',
    status: 'WAITING',
    createdAt: '2026-05-12T09:12:00Z',
  },
];

export const mockServiceWindows: ServiceWindow[] = [
  { id: 'win-1', name: '1번 창구', serviceTypeIds: [1], staffId: 'staff-1', active: true },
  { id: 'win-2', name: '2번 창구', serviceTypeIds: [2], staffId: 'staff-2', active: true },
  { id: 'win-3', name: '3번 창구', serviceTypeIds: [3], staffId: 'staff-3', active: true },
];

export const mockStaff: Staff[] = [
  { id: 'staff-1', userId: 'user-2', windowId: 'win-1', isActive: true },
  { id: 'staff-2', userId: 'staff-user-2', windowId: 'win-2', isActive: true },
  { id: 'staff-3', userId: 'staff-user-3', windowId: 'win-3', isActive: true },
];

export const mockCongestionInfo: CongestionInfo[] = [
  { serviceTypeId: 1, serviceTypeName: '예방접종', waitingCount: 3, estimatedWaitMinutes: 15, level: 'NORMAL' },
  { serviceTypeId: 2, serviceTypeName: '건강검진/검사', waitingCount: 5, estimatedWaitMinutes: 45, level: 'HIGH' },
  { serviceTypeId: 3, serviceTypeName: '건강상담', waitingCount: 2, estimatedWaitMinutes: 8, level: 'LOW' },
];

export const mockDashboardStats: DashboardStats = {
  todayVisitors: 127,
  currentWaiting: 12,
  avgWaitMinutes: 18,
  noShowRate: 8.5,
};

export const mockHourlyVisitors: HourlyVisitors[] = [
  { hour: '09:00', count: 15 },
  { hour: '10:00', count: 28 },
  { hour: '11:00', count: 35 },
  { hour: '12:00', count: 12 },
  { hour: '13:00', count: 8 },
  { hour: '14:00', count: 22 },
  { hour: '15:00', count: 25 },
  { hour: '16:00', count: 18 },
  { hour: '17:00', count: 10 },
];

export const mockServiceWaitTimes: ServiceWaitTime[] = [
  { serviceType: '예방접종', avgMinutes: 12 },
  { serviceType: '건강검진/검사', avgMinutes: 28 },
  { serviceType: '건강상담', avgMinutes: 8 },
];

export const mockVisitTypeRatio: VisitTypeRatio[] = [
  { type: '예약 방문', count: 85, percentage: 67 },
  { type: '현장 접수', count: 42, percentage: 33 },
];

// ============================================
// Helper functions to get data
// ============================================

export function getServiceTypeName(serviceTypeId: number): string {
  return mockServiceTypes.find(s => s.serviceTypeId === serviceTypeId)?.name || '알 수 없음';
}

export function getServiceTypeById(serviceTypeId: number): ServiceType | undefined {
  return mockServiceTypes.find(s => s.serviceTypeId === serviceTypeId);
}

export function getWindowName(id: string): string {
  return mockServiceWindows.find(w => w.id === id)?.name || '알 수 없음';
}

export function getAvailableSlots(serviceTypeId: number, date: string): ReservationSlot[] {
  return mockReservationSlots.filter(
    slot => slot.serviceTypeId === serviceTypeId && 
            slot.date === date && 
            slot.available
  );
}

export function getUserReservations(userId: string): Reservation[] {
  return mockReservations.filter(r => r.userId === userId);
}

export function getQueueSummary() {
  const waiting = mockQueueEntries.filter(q => q.status === 'WAITING').length;
  const called = mockQueueEntries.filter(q => q.status === 'CALLED').length;
  const inProgress = mockQueueEntries.filter(q => q.status === 'IN_PROGRESS').length;
  const hold = mockQueueEntries.filter(q => q.status === 'HOLD').length;
  return { waiting, called, inProgress, hold };
}
