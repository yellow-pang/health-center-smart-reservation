import type { UserRole } from './mock-data';

export const ROLE_HOME_PATHS: Record<UserRole, string> = {
  CITIZEN: '/citizen/reservations/new',
  STAFF: '/staff/check-in',
  ADMIN: '/admin/dashboard',
};

export const ROLE_LABELS: Record<UserRole, string> = {
  CITIZEN: '시민',
  STAFF: '직원',
  ADMIN: '관리자',
};

export function canAccessRole(userRole: UserRole, allowedRoles?: UserRole[]): boolean {
  return !allowedRoles || allowedRoles.includes(userRole);
}

export function getRoleHomePath(role?: UserRole): string {
  return role ? ROLE_HOME_PATHS[role] : '/login';
}
