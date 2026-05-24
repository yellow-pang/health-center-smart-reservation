import { apiRequest } from './api-client';
import type { QueueEntry, QueueStatus, Reservation, ServiceType, VisitType } from './mock-data';
import { getServiceTypes } from './reservation-api';

interface VisitQueueResponse {
  visitId: number;
  queueTicketId: number;
  ticketNumber: number;
  status: QueueStatus;
}

interface QueueTicketApiResponse {
  queueTicketId: number;
  visitId: number;
  serviceTypeId: number;
  serviceTypeName: string;
  ticketNumber: number;
  status: QueueStatus;
  visitType: VisitType;
  visitorName: string;
  visitorPhone: string;
  issuedAt: string;
  calledAt: string | null;
  startedAt: string | null;
  completedAt: string | null;
  holdAt: string | null;
}

interface ClosePendingQueueTicketsApiResponse {
  date: string;
  closedCount: number;
}

export interface CheckInResponse {
  success: boolean;
  queueEntry?: QueueEntry;
  error?: string;
}

export interface WalkInInput {
  visitorName: string;
  visitorPhone: string;
  serviceTypeId: number;
  serviceTypeName?: string;
}

export interface StaffReservationSearchFilters {
  keyword?: string;
  date?: string;
  status?: Reservation['status'];
}

export async function searchReservationsForCheckIn(
  filters: StaffReservationSearchFilters,
): Promise<Reservation[]> {
  return apiRequest<Reservation[]>('/api/reservations/staff/search', {
    query: {
      keyword: filters.keyword,
      date: filters.date,
      status: filters.status || 'RESERVED',
    },
  });
}

export async function checkInByReservationNumber(
  reservationNo: string,
  reservation?: Reservation,
): Promise<CheckInResponse> {
  try {
    const response = await apiRequest<VisitQueueResponse>('/api/visits/check-in', {
      method: 'POST',
      body: { reservationNo },
    });

    return {
      success: true,
      queueEntry: {
        queueTicketId: response.queueTicketId,
        visitId: response.visitId,
        ticketNumber: String(response.ticketNumber),
        visitorNameMasked: reservation ? maskName(reservation.visitorName) : '-',
        visitorPhoneMasked: reservation ? maskPhone(reservation.visitorPhone) : '-',
        serviceTypeId: reservation?.serviceTypeId || 0,
        serviceTypeName: reservation?.serviceTypeName || '예약 방문',
        visitType: 'RESERVED',
        status: response.status,
        reservationId: reservation?.reservationId,
        createdAt: new Date().toISOString(),
      },
    };
  } catch (error) {
    return { success: false, error: getErrorMessage(error) };
  }
}

export async function registerWalkIn(input: WalkInInput): Promise<CheckInResponse> {
  try {
    const response = await apiRequest<VisitQueueResponse>('/api/visits/walk-in', {
      method: 'POST',
      body: {
        serviceTypeId: input.serviceTypeId,
        visitorName: input.visitorName,
        visitorPhone: input.visitorPhone,
      },
    });

    return {
      success: true,
      queueEntry: {
        queueTicketId: response.queueTicketId,
        visitId: response.visitId,
        ticketNumber: String(response.ticketNumber),
        visitorNameMasked: maskName(input.visitorName),
        visitorPhoneMasked: maskPhone(input.visitorPhone),
        serviceTypeId: input.serviceTypeId,
        serviceTypeName: input.serviceTypeName,
        visitType: 'WALK_IN',
        status: response.status,
        createdAt: new Date().toISOString(),
      },
    };
  } catch (error) {
    return { success: false, error: getErrorMessage(error) };
  }
}

export async function getQueueEntries(filters?: {
  serviceTypeId?: number;
  status?: QueueStatus;
  date?: string;
  limit?: number;
}): Promise<QueueEntry[]> {
  const entries = await apiRequest<QueueTicketApiResponse[]>('/api/queues', {
    query: {
      serviceTypeId: filters?.serviceTypeId,
      status: filters?.status,
      date: filters?.date,
      limit: filters?.limit,
    },
  });

  return entries.map(toQueueEntry);
}

export async function closePendingQueueTickets(
  date: string,
): Promise<ClosePendingQueueTicketsApiResponse> {
  return apiRequest<ClosePendingQueueTicketsApiResponse>('/api/queues/admin/close-pending', {
    method: 'POST',
    query: { date },
  });
}

export async function updateQueueStatus(
  queueTicketId: number,
  newStatus: QueueStatus,
): Promise<{ success: boolean; queueEntry?: QueueEntry; error?: string }> {
  const action = toQueueAction(newStatus);
  if (!action) {
    return { success: false, error: '지원하지 않는 대기 상태 변경입니다.' };
  }

  try {
    const entry = await apiRequest<QueueTicketApiResponse>(`/api/queues/${queueTicketId}/${action}`, {
      method: 'POST',
    });

    return { success: true, queueEntry: toQueueEntry(entry) };
  } catch (error) {
    return { success: false, error: getErrorMessage(error) };
  }
}

export { getServiceTypes };

function toQueueEntry(entry: QueueTicketApiResponse): QueueEntry {
  return {
    queueTicketId: entry.queueTicketId,
    visitId: entry.visitId,
    ticketNumber: String(entry.ticketNumber),
    visitorNameMasked: maskName(entry.visitorName),
    visitorPhoneMasked: maskPhone(entry.visitorPhone),
    serviceTypeId: entry.serviceTypeId,
    serviceTypeName: entry.serviceTypeName,
    visitType: entry.visitType,
    status: entry.status,
    issuedAt: entry.issuedAt,
    calledAt: entry.calledAt || undefined,
    startedAt: entry.startedAt || undefined,
    completedAt: entry.completedAt || undefined,
    holdAt: entry.holdAt || undefined,
    createdAt: entry.issuedAt,
  };
}

function toQueueAction(status: QueueStatus): string | null {
  const actionByStatus: Partial<Record<QueueStatus, string>> = {
    CALLED: 'call',
    IN_PROGRESS: 'start',
    COMPLETED: 'complete',
    HOLD: 'hold',
    NO_SHOW: 'no-show',
    CANCELED: 'cancel',
  };

  return actionByStatus[status] || null;
}

function maskName(name: string): string {
  if (!name) {
    return '-';
  }

  if (name.length <= 2) {
    return `${name.charAt(0)}*`;
  }

  return `${name.charAt(0)}*${name.slice(-1)}`;
}

function maskPhone(phone: string): string {
  return phone.replace(/(\d{3})-?\d{4}-?(\d{4})/, '$1-****-$2');
}

function getErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }

  return '요청 처리 중 오류가 발생했습니다.';
}
