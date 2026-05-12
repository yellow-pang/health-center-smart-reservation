// ============================================
// Mock Service Functions
// These functions simulate API calls and will be replaced with real API calls by Codex
// ============================================

import {
  type User,
  type UserRole,
  type Reservation,
  type ReservationSlot,
  type QueueEntry,
  type QueueStatus,
  type ServiceType,
  type ServiceWindow,
  type CongestionInfo,
  type DashboardStats,
  type HourlyVisitors,
  type ServiceWaitTime,
  type VisitTypeRatio,
  mockUsers,
  mockReservations,
  mockReservationSlots,
  mockQueueEntries,
  mockServiceTypes,
  mockServiceWindows,
  mockCongestionInfo,
  mockDashboardStats,
  mockHourlyVisitors,
  mockServiceWaitTimes,
  mockVisitTypeRatio,
  getAvailableSlots,
} from './mock-data';

// Simulate network delay
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

// ============================================
// Auth Services
// ============================================

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  user?: User;
  error?: string;
}

export async function login(credentials: LoginCredentials): Promise<LoginResponse> {
  await delay(500);
  
  const user = mockUsers.find(u => u.email === credentials.email);
  if (user) {
    return { success: true, user };
  }
  return { success: false, error: '이메일 또는 비밀번호가 올바르지 않습니다.' };
}

export async function loginWithRole(role: UserRole): Promise<LoginResponse> {
  await delay(300);
  
  const user = mockUsers.find(u => u.role === role);
  if (user) {
    return { success: true, user };
  }
  return { success: false, error: '사용자를 찾을 수 없습니다.' };
}

export async function logout(): Promise<void> {
  await delay(200);
  // Clear session - will be implemented with real auth
}

// ============================================
// Reservation Services
// ============================================

export interface CreateReservationInput {
  serviceTypeId: number;
  date: string;
  startTime: string;
  visitorName: string;
  visitorPhone: string;
}

export interface CreateReservationResponse {
  success: boolean;
  reservation?: Reservation;
  error?: string;
}

export async function getServiceTypes(): Promise<ServiceType[]> {
  await delay(300);
  return mockServiceTypes.filter(s => s.active);
}

export async function getReservationSlots(serviceTypeId: number, date: string): Promise<ReservationSlot[]> {
  await delay(300);
  return getAvailableSlots(serviceTypeId, date);
}

export async function createReservation(input: CreateReservationInput): Promise<CreateReservationResponse> {
  await delay(500);
  
  const newReservation: Reservation = {
    reservationId: Date.now(),
    reservationNo: `RSV-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-${String(Math.floor(Math.random() * 1000)).padStart(4, '0')}`,
    userId: 'user-1', // Will be from auth context
    serviceTypeId: input.serviceTypeId,
    visitorName: input.visitorName,
    visitorPhone: input.visitorPhone,
    date: input.date,
    startTime: input.startTime,
    status: 'RESERVED',
    createdAt: new Date().toISOString(),
  };
  
  return { success: true, reservation: newReservation };
}

export async function getUserReservations(): Promise<Reservation[]> {
  await delay(400);
  return mockReservations;
}

export async function cancelReservation(reservationId: number): Promise<{ success: boolean; error?: string }> {
  await delay(400);
  
  const reservation = mockReservations.find(r => r.reservationId === reservationId);
  if (!reservation) {
    return { success: false, error: '예약을 찾을 수 없습니다.' };
  }
  if (reservation.status !== 'RESERVED') {
    return { success: false, error: '취소할 수 없는 예약입니다.' };
  }
  
  return { success: true };
}

// ============================================
// Congestion Services
// ============================================

export async function getCongestionInfo(): Promise<CongestionInfo[]> {
  await delay(300);
  return mockCongestionInfo;
}

// ============================================
// Queue Services (Staff)
// ============================================

export interface CheckInResponse {
  success: boolean;
  queueEntry?: QueueEntry;
  error?: string;
}

export async function checkInByReservationNumber(reservationNo: string): Promise<CheckInResponse> {
  await delay(400);
  
  const reservation = mockReservations.find(r => r.reservationNo === reservationNo);
  if (!reservation) {
    return { success: false, error: '예약을 찾을 수 없습니다.' };
  }
  
  const queueEntry: QueueEntry = {
    queueTicketId: Date.now(),
    ticketNumber: `A${String(Math.floor(Math.random() * 100)).padStart(3, '0')}`,
    visitorNameMasked: `${reservation.visitorName.charAt(0)}*${reservation.visitorName.slice(-1)}`,
    visitorPhoneMasked: reservation.visitorPhone.replace(/(\d{3})-\d{4}-(\d{4})/, '$1-****-$2'),
    serviceTypeId: reservation.serviceTypeId,
    visitType: 'RESERVED',
    status: 'WAITING',
    reservationId: reservation.reservationId,
    createdAt: new Date().toISOString(),
  };
  
  return { success: true, queueEntry };
}

export interface WalkInInput {
  visitorName: string;
  visitorPhone: string;
  serviceTypeId: number;
}

export async function registerWalkIn(input: WalkInInput): Promise<CheckInResponse> {
  await delay(400);
  
  const serviceType = mockServiceTypes.find(s => s.serviceTypeId === input.serviceTypeId);
  const prefix = serviceType?.name.charAt(0) || 'X';
  
  const queueEntry: QueueEntry = {
    queueTicketId: Date.now(),
    ticketNumber: `${prefix}${String(Math.floor(Math.random() * 100)).padStart(3, '0')}`,
    visitorNameMasked: `${input.visitorName.charAt(0)}*${input.visitorName.slice(-1)}`,
    visitorPhoneMasked: input.visitorPhone.replace(/(\d{3})-\d{4}-(\d{4})/, '$1-****-$2'),
    serviceTypeId: input.serviceTypeId,
    visitType: 'WALK_IN',
    status: 'WAITING',
    createdAt: new Date().toISOString(),
  };
  
  return { success: true, queueEntry };
}

export async function getQueueEntries(filters?: {
  serviceTypeId?: number;
  status?: QueueStatus;
}): Promise<QueueEntry[]> {
  await delay(300);
  
  let entries = [...mockQueueEntries];
  
  if (filters?.serviceTypeId) {
    entries = entries.filter(e => e.serviceTypeId === filters.serviceTypeId);
  }
  if (filters?.status) {
    entries = entries.filter(e => e.status === filters.status);
  }
  
  return entries;
}

export async function updateQueueStatus(
  queueTicketId: number,
  newStatus: QueueStatus
): Promise<{ success: boolean; error?: string }> {
  await delay(300);
  
  const entry = mockQueueEntries.find(e => e.queueTicketId === queueTicketId);
  if (!entry) {
    return { success: false, error: '대기자를 찾을 수 없습니다.' };
  }
  
  return { success: true };
}

// ============================================
// Admin Dashboard Services
// ============================================

export async function getDashboardStats(): Promise<DashboardStats> {
  await delay(300);
  return mockDashboardStats;
}

export async function getHourlyVisitors(): Promise<HourlyVisitors[]> {
  await delay(300);
  return mockHourlyVisitors;
}

export async function getServiceWaitTimes(): Promise<ServiceWaitTime[]> {
  await delay(300);
  return mockServiceWaitTimes;
}

export async function getVisitTypeRatio(): Promise<VisitTypeRatio[]> {
  await delay(300);
  return mockVisitTypeRatio;
}

// ============================================
// Admin CRUD Services
// ============================================

export async function getAllServiceTypes(): Promise<ServiceType[]> {
  await delay(300);
  return mockServiceTypes;
}

export async function createServiceType(input: Omit<ServiceType, 'serviceTypeId'>): Promise<{ success: boolean; serviceType?: ServiceType }> {
  await delay(400);
  const newServiceType: ServiceType = {
    ...input,
    serviceTypeId: Date.now(),
  };
  return { success: true, serviceType: newServiceType };
}

export async function updateServiceType(id: number, input: Partial<ServiceType>): Promise<{ success: boolean }> {
  await delay(400);
  return { success: true };
}

export async function deleteServiceType(id: number): Promise<{ success: boolean }> {
  await delay(400);
  return { success: true };
}

export async function getAllReservationSlots(): Promise<ReservationSlot[]> {
  await delay(300);
  return mockReservationSlots;
}

export async function getServiceWindows(): Promise<ServiceWindow[]> {
  await delay(300);
  return mockServiceWindows;
}

export async function updateServiceWindow(id: string, input: Partial<ServiceWindow>): Promise<{ success: boolean }> {
  await delay(400);
  return { success: true };
}
