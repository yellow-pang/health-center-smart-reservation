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

export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'CANCELED' | 'COMPLETED' | 'NO_SHOW';

export interface User {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  phone?: string;
}

export interface ServiceType {
  id: string;
  name: string;
  description: string;
  estimatedMinutes: number;
  isActive: boolean;
}

export interface ReservationSlot {
  id: string;
  serviceTypeId: string;
  date: string;
  time: string;
  capacity: number;
  reserved: number;
}

export interface Reservation {
  id: string;
  reservationNumber: string;
  userId: string;
  serviceTypeId: string;
  visitorName: string;
  visitorPhone: string;
  date: string;
  time: string;
  status: ReservationStatus;
  createdAt: string;
}

export interface QueueEntry {
  id: string;
  queueNumber: string;
  visitorName: string;
  visitorPhone: string;
  serviceTypeId: string;
  visitType: VisitType;
  status: QueueStatus;
  reservationId?: string;
  windowId?: string;
  calledAt?: string;
  startedAt?: string;
  completedAt?: string;
  createdAt: string;
}

export interface ServiceWindow {
  id: string;
  name: string;
  serviceTypeIds: string[];
  staffId?: string;
  isActive: boolean;
}

export interface Staff {
  id: string;
  userId: string;
  windowId?: string;
  isActive: boolean;
}

export interface CongestionInfo {
  serviceTypeId: string;
  serviceTypeName: string;
  waitingCount: number;
  estimatedWaitMinutes: number;
  level: 'LOW' | 'MEDIUM' | 'HIGH';
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
  { id: 'st-1', name: '예방접종', description: '각종 예방접종 서비스', estimatedMinutes: 15, isActive: true },
  { id: 'st-2', name: '건강검진', description: '건강검진 상담 및 예약', estimatedMinutes: 30, isActive: true },
  { id: 'st-3', name: '증명서 발급', description: '각종 건강 관련 증명서 발급', estimatedMinutes: 10, isActive: true },
  { id: 'st-4', name: '모자보건', description: '임산부 및 영유아 건강관리', estimatedMinutes: 20, isActive: true },
  { id: 'st-5', name: '정신건강 상담', description: '정신건강 관련 상담 서비스', estimatedMinutes: 45, isActive: true },
];

export const mockReservationSlots: ReservationSlot[] = [
  { id: 'slot-1', serviceTypeId: 'st-1', date: '2026-05-13', time: '09:00', capacity: 5, reserved: 2 },
  { id: 'slot-2', serviceTypeId: 'st-1', date: '2026-05-13', time: '09:30', capacity: 5, reserved: 5 },
  { id: 'slot-3', serviceTypeId: 'st-1', date: '2026-05-13', time: '10:00', capacity: 5, reserved: 1 },
  { id: 'slot-4', serviceTypeId: 'st-1', date: '2026-05-13', time: '10:30', capacity: 5, reserved: 3 },
  { id: 'slot-5', serviceTypeId: 'st-1', date: '2026-05-13', time: '11:00', capacity: 5, reserved: 0 },
  { id: 'slot-6', serviceTypeId: 'st-2', date: '2026-05-13', time: '09:00', capacity: 3, reserved: 1 },
  { id: 'slot-7', serviceTypeId: 'st-2', date: '2026-05-13', time: '10:00', capacity: 3, reserved: 2 },
  { id: 'slot-8', serviceTypeId: 'st-2', date: '2026-05-13', time: '11:00', capacity: 3, reserved: 0 },
  { id: 'slot-9', serviceTypeId: 'st-3', date: '2026-05-13', time: '09:00', capacity: 10, reserved: 4 },
  { id: 'slot-10', serviceTypeId: 'st-3', date: '2026-05-13', time: '10:00', capacity: 10, reserved: 6 },
];

export const mockReservations: Reservation[] = [
  {
    id: 'res-1',
    reservationNumber: 'R20260512001',
    userId: 'user-1',
    serviceTypeId: 'st-1',
    visitorName: '홍길동',
    visitorPhone: '010-1234-5678',
    date: '2026-05-13',
    time: '09:00',
    status: 'CONFIRMED',
    createdAt: '2026-05-12T10:00:00Z',
  },
  {
    id: 'res-2',
    reservationNumber: 'R20260512002',
    userId: 'user-1',
    serviceTypeId: 'st-3',
    visitorName: '홍길동',
    visitorPhone: '010-1234-5678',
    date: '2026-05-14',
    time: '10:00',
    status: 'PENDING',
    createdAt: '2026-05-12T11:30:00Z',
  },
  {
    id: 'res-3',
    reservationNumber: 'R20260510001',
    userId: 'user-1',
    serviceTypeId: 'st-2',
    visitorName: '홍길동',
    visitorPhone: '010-1234-5678',
    date: '2026-05-10',
    time: '14:00',
    status: 'COMPLETED',
    createdAt: '2026-05-08T09:00:00Z',
  },
];

export const mockQueueEntries: QueueEntry[] = [
  {
    id: 'q-1',
    queueNumber: 'A001',
    visitorName: '김철수',
    visitorPhone: '010-1111-2222',
    serviceTypeId: 'st-1',
    visitType: 'RESERVED',
    status: 'IN_PROGRESS',
    windowId: 'win-1',
    calledAt: '2026-05-12T09:05:00Z',
    startedAt: '2026-05-12T09:07:00Z',
    createdAt: '2026-05-12T08:55:00Z',
  },
  {
    id: 'q-2',
    queueNumber: 'A002',
    visitorName: '이영희',
    visitorPhone: '010-2222-3333',
    serviceTypeId: 'st-1',
    visitType: 'WALK_IN',
    status: 'CALLED',
    windowId: 'win-2',
    calledAt: '2026-05-12T09:10:00Z',
    createdAt: '2026-05-12T09:00:00Z',
  },
  {
    id: 'q-3',
    queueNumber: 'A003',
    visitorName: '박민수',
    visitorPhone: '010-3333-4444',
    serviceTypeId: 'st-1',
    visitType: 'RESERVED',
    status: 'WAITING',
    createdAt: '2026-05-12T09:02:00Z',
  },
  {
    id: 'q-4',
    queueNumber: 'B001',
    visitorName: '최지영',
    visitorPhone: '010-4444-5555',
    serviceTypeId: 'st-2',
    visitType: 'RESERVED',
    status: 'WAITING',
    createdAt: '2026-05-12T08:50:00Z',
  },
  {
    id: 'q-5',
    queueNumber: 'B002',
    visitorName: '정수진',
    visitorPhone: '010-5555-6666',
    serviceTypeId: 'st-2',
    visitType: 'WALK_IN',
    status: 'HOLD',
    windowId: 'win-3',
    calledAt: '2026-05-12T09:00:00Z',
    createdAt: '2026-05-12T08:45:00Z',
  },
  {
    id: 'q-6',
    queueNumber: 'C001',
    visitorName: '강호준',
    visitorPhone: '010-6666-7777',
    serviceTypeId: 'st-3',
    visitType: 'WALK_IN',
    status: 'WAITING',
    createdAt: '2026-05-12T09:08:00Z',
  },
  {
    id: 'q-7',
    queueNumber: 'C002',
    visitorName: '윤서연',
    visitorPhone: '010-7777-8888',
    serviceTypeId: 'st-3',
    visitType: 'RESERVED',
    status: 'WAITING',
    createdAt: '2026-05-12T09:12:00Z',
  },
];

export const mockServiceWindows: ServiceWindow[] = [
  { id: 'win-1', name: '1번 창구', serviceTypeIds: ['st-1', 'st-4'], staffId: 'staff-1', isActive: true },
  { id: 'win-2', name: '2번 창구', serviceTypeIds: ['st-1', 'st-4'], staffId: 'staff-2', isActive: true },
  { id: 'win-3', name: '3번 창구', serviceTypeIds: ['st-2', 'st-5'], staffId: 'staff-3', isActive: true },
  { id: 'win-4', name: '4번 창구', serviceTypeIds: ['st-3'], isActive: true },
  { id: 'win-5', name: '5번 창구', serviceTypeIds: ['st-3'], isActive: false },
];

export const mockStaff: Staff[] = [
  { id: 'staff-1', userId: 'user-2', windowId: 'win-1', isActive: true },
  { id: 'staff-2', userId: 'staff-user-2', windowId: 'win-2', isActive: true },
  { id: 'staff-3', userId: 'staff-user-3', windowId: 'win-3', isActive: true },
];

export const mockCongestionInfo: CongestionInfo[] = [
  { serviceTypeId: 'st-1', serviceTypeName: '예방접종', waitingCount: 3, estimatedWaitMinutes: 15, level: 'MEDIUM' },
  { serviceTypeId: 'st-2', serviceTypeName: '건강검진', waitingCount: 5, estimatedWaitMinutes: 45, level: 'HIGH' },
  { serviceTypeId: 'st-3', serviceTypeName: '증명서 발급', waitingCount: 2, estimatedWaitMinutes: 8, level: 'LOW' },
  { serviceTypeId: 'st-4', serviceTypeName: '모자보건', waitingCount: 0, estimatedWaitMinutes: 0, level: 'LOW' },
  { serviceTypeId: 'st-5', serviceTypeName: '정신건강 상담', waitingCount: 1, estimatedWaitMinutes: 20, level: 'LOW' },
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
  { serviceType: '건강검진', avgMinutes: 28 },
  { serviceType: '증명서 발급', avgMinutes: 8 },
  { serviceType: '모자보건', avgMinutes: 15 },
  { serviceType: '정신건강 상담', avgMinutes: 22 },
];

export const mockVisitTypeRatio: VisitTypeRatio[] = [
  { type: '예약 방문', count: 85, percentage: 67 },
  { type: '현장 접수', count: 42, percentage: 33 },
];

// ============================================
// Helper functions to get data
// ============================================

export function getServiceTypeName(id: string): string {
  return mockServiceTypes.find(s => s.id === id)?.name || '알 수 없음';
}

export function getServiceTypeById(id: string): ServiceType | undefined {
  return mockServiceTypes.find(s => s.id === id);
}

export function getWindowName(id: string): string {
  return mockServiceWindows.find(w => w.id === id)?.name || '알 수 없음';
}

export function getAvailableSlots(serviceTypeId: string, date: string): ReservationSlot[] {
  return mockReservationSlots.filter(
    slot => slot.serviceTypeId === serviceTypeId && 
            slot.date === date && 
            slot.reserved < slot.capacity
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
