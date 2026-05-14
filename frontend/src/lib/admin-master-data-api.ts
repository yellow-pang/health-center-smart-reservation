import { apiRequest, type ApiRequestBody } from './api-client';
import type { ReservationSlot, ServiceType, ServiceWindow, UserRole } from './mock-data';

interface ServiceTypeApiResponse {
  id: number;
  healthCenterId: number | null;
  code: string;
  name: string;
  description: string;
  defaultCapacity: number;
  active: boolean;
}

interface StaffApiResponse {
  id: number;
  healthCenterId: number;
  email: string;
  name: string;
  phone: string;
  role: UserRole;
  active: boolean;
}

interface ServiceWindowApiResponse {
  id: number;
  healthCenterId: number;
  windowNumber: number;
  name: string;
  status: string;
  active: boolean;
  serviceTypes: ServiceTypeApiResponse[];
}

export interface StaffMember {
  id: number;
  healthCenterId: number;
  name: string;
  email: string;
  phone: string;
  role: UserRole;
  active: boolean;
}

export interface ServiceTypeFormInput {
  code?: string;
  name: string;
  description: string;
  defaultCapacity: number;
  active: boolean;
}

export interface ReservationSlotCreateInput {
  serviceTypeId: number;
  date: string;
  startTime: string;
  endTime: string;
  capacity: number;
}

export async function getAdminServiceTypes(): Promise<ServiceType[]> {
  const serviceTypes = await apiRequest<ServiceTypeApiResponse[]>('/api/admin/service-types');

  return serviceTypes.map(toServiceType);
}

export async function createAdminServiceType(input: ServiceTypeFormInput): Promise<ServiceType> {
  const serviceType = await apiRequest<ServiceTypeApiResponse>('/api/admin/service-types', {
    method: 'POST',
    body: {
      code: input.code || toServiceTypeCode(input.name),
      name: input.name,
      description: input.description,
      defaultCapacity: input.defaultCapacity,
    } satisfies ApiRequestBody,
  });

  return toServiceType(serviceType);
}

export async function updateAdminServiceType(
  serviceTypeId: number,
  input: ServiceTypeFormInput,
): Promise<ServiceType> {
  const serviceType = await apiRequest<ServiceTypeApiResponse>(`/api/admin/service-types/${serviceTypeId}`, {
    method: 'PUT',
    body: {
      name: input.name,
      description: input.description,
      defaultCapacity: input.defaultCapacity,
      active: input.active,
    } satisfies ApiRequestBody,
  });

  return toServiceType(serviceType);
}

export async function deactivateAdminServiceType(serviceTypeId: number): Promise<ServiceType> {
  const serviceType = await apiRequest<ServiceTypeApiResponse>(
    `/api/admin/service-types/${serviceTypeId}/deactivate`,
    { method: 'PATCH' },
  );

  return toServiceType(serviceType);
}

export async function activateAdminServiceType(serviceTypeId: number): Promise<ServiceType> {
  const serviceType = await apiRequest<ServiceTypeApiResponse>(
    `/api/admin/service-types/${serviceTypeId}/activate`,
    { method: 'PATCH' },
  );

  return toServiceType(serviceType);
}

export async function getAdminReservationSlots(filters: {
  serviceTypeId?: number;
  date: string;
}): Promise<ReservationSlot[]> {
  const serviceTypeIds = filters.serviceTypeId
    ? [filters.serviceTypeId]
    : (await getAdminServiceTypes()).map((serviceType) => serviceType.serviceTypeId);

  const slotGroups = await Promise.all(
    serviceTypeIds.map((serviceTypeId) =>
      apiRequest<ReservationSlot[]>('/api/reservation-slots', {
        query: { serviceTypeId, date: filters.date },
      }),
    ),
  );

  return slotGroups.flat().sort((a, b) => {
    const dateCompare = a.date.localeCompare(b.date);
    if (dateCompare !== 0) {
      return dateCompare;
    }

    return a.startTime.localeCompare(b.startTime);
  });
}

export async function createAdminReservationSlot(
  input: ReservationSlotCreateInput,
): Promise<ReservationSlot> {
  return apiRequest<ReservationSlot>('/api/admin/reservation-slots', {
    method: 'POST',
    body: {
      serviceTypeId: input.serviceTypeId,
      date: input.date,
      startTime: input.startTime,
      endTime: input.endTime,
      capacity: input.capacity,
    } satisfies ApiRequestBody,
  });
}

export async function getAdminStaff(): Promise<StaffMember[]> {
  const staff = await apiRequest<StaffApiResponse[]>('/api/admin/staff');
  return staff.map(toStaffMember);
}

export async function getAdminServiceWindows(): Promise<ServiceWindow[]> {
  const windows = await apiRequest<ServiceWindowApiResponse[]>('/api/admin/service-windows');
  return windows.map(toServiceWindow);
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

function toStaffMember(staff: StaffApiResponse): StaffMember {
  return {
    id: staff.id,
    healthCenterId: staff.healthCenterId,
    name: staff.name,
    email: staff.email,
    phone: staff.phone,
    role: staff.role,
    active: staff.active,
  };
}

function toServiceWindow(window: ServiceWindowApiResponse): ServiceWindow {
  return {
    id: String(window.id),
    name: window.name,
    serviceTypeIds: window.serviceTypes.map((serviceType) => serviceType.id),
    active: window.active && window.status === 'OPEN',
  };
}

function toServiceTypeCode(name: string): string {
  return name
    .trim()
    .replace(/\s+/g, '_')
    .replace(/[^\dA-Za-z_]/g, '')
    .toUpperCase() || `SERVICE_${Date.now()}`;
}
