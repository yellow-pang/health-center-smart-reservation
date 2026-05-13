import { apiRequest, type ApiRequestBody } from './api-client';
import type { Reservation, ReservationSlot, ServiceType } from './mock-data';

interface ServiceTypeApiResponse {
  id: number;
  healthCenterId: number | null;
  code: string;
  name: string;
  description: string;
  defaultCapacity: number;
  active: boolean;
}

interface ReservationCreateApiResponse {
  reservationId: number;
  reservationNo: string;
  status: Reservation['status'];
}

export interface CreateReservationInput {
  serviceTypeId: number;
  reservationSlotId: number;
  visitorName: string;
  visitorPhone: string;
  date: string;
  startTime: string;
  endTime?: string;
}

export interface CreateReservationResponse {
  success: boolean;
  reservation?: Reservation;
  error?: string;
}

export async function getServiceTypes(): Promise<ServiceType[]> {
  const serviceTypes = await apiRequest<ServiceTypeApiResponse[]>('/api/service-types');
  return serviceTypes.map(toServiceType);
}

export async function getReservationSlots(
  serviceTypeId: number,
  date: string,
): Promise<ReservationSlot[]> {
  return apiRequest<ReservationSlot[]>('/api/reservation-slots', {
    query: { serviceTypeId, date },
  });
}

export async function createReservation(
  input: CreateReservationInput,
): Promise<CreateReservationResponse> {
  try {
    const response = await apiRequest<ReservationCreateApiResponse>('/api/reservations', {
      method: 'POST',
      body: {
        serviceTypeId: input.serviceTypeId,
        reservationSlotId: input.reservationSlotId,
        visitorName: input.visitorName,
        visitorPhone: input.visitorPhone,
      } satisfies ApiRequestBody,
    });

    return {
      success: true,
      reservation: {
        reservationId: response.reservationId,
        reservationNo: response.reservationNo,
        serviceTypeId: input.serviceTypeId,
        reservationSlotId: input.reservationSlotId,
        visitorName: input.visitorName,
        visitorPhone: input.visitorPhone,
        date: input.date,
        startTime: input.startTime,
        endTime: input.endTime,
        status: response.status,
        createdAt: new Date().toISOString(),
      },
    };
  } catch (error) {
    return {
      success: false,
      error: getErrorMessage(error),
    };
  }
}

export async function getUserReservations(): Promise<Reservation[]> {
  return apiRequest<Reservation[]>('/api/reservations/me');
}

export async function cancelReservation(
  reservationId: number,
): Promise<{ success: boolean; error?: string }> {
  try {
    await apiRequest<null>(`/api/reservations/${reservationId}`, {
      method: 'DELETE',
    });

    return { success: true };
  } catch (error) {
    return {
      success: false,
      error: getErrorMessage(error),
    };
  }
}

function toServiceType(serviceType: ServiceTypeApiResponse): ServiceType {
  return {
    serviceTypeId: serviceType.id,
    healthCenterId: serviceType.healthCenterId,
    code: serviceType.code,
    name: serviceType.name,
    description: serviceType.description,
    defaultCapacity: serviceType.defaultCapacity,
    active: serviceType.active,
  };
}

function getErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }

  return '요청 처리 중 오류가 발생했습니다.';
}
