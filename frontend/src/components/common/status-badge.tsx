import { cn } from '@/lib/utils';
import type { CongestionLevel, QueueStatus, ReservationStatus } from '@/src/lib/mock-data';

interface StatusBadgeProps {
  status: QueueStatus | ReservationStatus | CongestionLevel;
  className?: string;
}

const statusConfig: Record<string, { label: string; className: string }> = {
  // Queue statuses
  WAITING: { label: '대기 중', className: 'bg-blue-100 text-blue-800' },
  CALLED: { label: '호출 중', className: 'bg-amber-100 text-amber-800 animate-pulse' },
  IN_PROGRESS: { label: '처리 중', className: 'bg-green-100 text-green-800' },
  HOLD: { label: '보류', className: 'bg-orange-100 text-orange-800' },
  COMPLETED: { label: '완료', className: 'bg-gray-100 text-gray-600' },
  NO_SHOW: { label: '미응답', className: 'bg-red-100 text-red-800' },
  CANCELED: { label: '취소', className: 'bg-gray-200 text-gray-500' },
  
  // Reservation statuses
  RESERVED: { label: '예약됨', className: 'bg-green-100 text-green-800' },
  CHECKED_IN: { label: '체크인', className: 'bg-blue-100 text-blue-800' },
  
  // Congestion levels
  LOW: { label: '여유', className: 'bg-green-100 text-green-800' },
  NORMAL: { label: '보통', className: 'bg-amber-100 text-amber-800' },
  HIGH: { label: '혼잡', className: 'bg-red-100 text-red-800' },
};

export function StatusBadge({ status, className }: StatusBadgeProps) {
  const config = statusConfig[status] || { label: status, className: 'bg-gray-100 text-gray-600' };
  
  return (
    <span 
      className={cn(
        'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium',
        config.className,
        className
      )}
    >
      {config.label}
    </span>
  );
}
