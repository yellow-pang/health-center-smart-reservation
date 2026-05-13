'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { CalendarPlus, Calendar, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
import { PageHeader } from '@/src/components/common/page-header';
import { StatusBadge } from '@/src/components/common/status-badge';
import { LoadingState } from '@/src/components/common/loading-state';
import { EmptyState } from '@/src/components/common/empty-state';
import { ErrorState } from '@/src/components/common/error-state';
import { getUserReservations, cancelReservation } from '@/src/lib/reservation-api';
import { getServiceTypeName } from '@/src/lib/mock-data';
import type { Reservation } from '@/src/lib/mock-data';
import { toast } from 'sonner';
import { format, parseISO } from 'date-fns';
import { ko } from 'date-fns/locale';

type LoadState = 'loading' | 'success' | 'error';

export default function MyReservationsPage() {
  const router = useRouter();
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [cancelingId, setCancelingId] = useState<number | null>(null);

  const loadReservations = async () => {
    setLoadState('loading');
    try {
      const data = await getUserReservations();
      setReservations(data);
      setLoadState('success');
    } catch {
      setLoadState('error');
    }
  };

  useEffect(() => {
    loadReservations();
  }, []);

  const handleCancel = async (reservationId: number) => {
    setCancelingId(reservationId);
    try {
      const result = await cancelReservation(reservationId);
      if (result.success) {
        toast.success('예약이 취소되었습니다.');
        // Update local state
        setReservations(prev => 
          prev.map(r => r.reservationId === reservationId ? { ...r, status: 'CANCELED' as const } : r)
        );
      } else {
        toast.error(result.error || '취소 중 오류가 발생했습니다.');
      }
    } finally {
      setCancelingId(null);
    }
  };

  const canCancel = (status: Reservation['status']) => {
    return status === 'RESERVED';
  };

  const getReservationServiceName = (reservation: Reservation) => {
    return reservation.serviceTypeName || getServiceTypeName(reservation.serviceTypeId);
  };

  // Sort reservations: upcoming first, then by date
  const sortedReservations = [...reservations].sort((a, b) => {
    const dateA = parseISO(`${a.date}T${a.startTime}`);
    const dateB = parseISO(`${b.date}T${b.startTime}`);
    return dateB.getTime() - dateA.getTime();
  });

  const upcomingReservations = sortedReservations.filter(r => 
    r.status === 'RESERVED' && parseISO(r.date) >= new Date(new Date().setHours(0,0,0,0))
  );
  const pastReservations = sortedReservations.filter(r => 
    r.status === 'COMPLETED' || r.status === 'CANCELED' || r.status === 'NO_SHOW' || parseISO(r.date) < new Date(new Date().setHours(0,0,0,0))
  );

  return (
    <div className="max-w-2xl mx-auto">
      <PageHeader 
        title="내 예약" 
        description="예약 현황을 확인하고 관리할 수 있습니다"
        actions={
          <Button onClick={() => router.push('/citizen/reservations/new')}>
            <CalendarPlus className="mr-2 h-4 w-4" />
            새 예약
          </Button>
        }
      />

      <div className="mt-6 space-y-6">
        {loadState === 'loading' && <LoadingState />}
        
        {loadState === 'error' && (
          <ErrorState onRetry={loadReservations} />
        )}

        {loadState === 'success' && reservations.length === 0 && (
          <EmptyState
            icon={Calendar}
            title="예약 내역이 없습니다"
            description="새로운 예약을 신청해 보세요."
            action={
              <Button onClick={() => router.push('/citizen/reservations/new')}>
                <CalendarPlus className="mr-2 h-4 w-4" />
                예약 신청하기
              </Button>
            }
          />
        )}

        {loadState === 'success' && reservations.length > 0 && (
          <>
            {/* Upcoming Reservations */}
            {upcomingReservations.length > 0 && (
              <section>
                <h2 className="text-sm font-medium text-muted-foreground mb-3">예정된 예약</h2>
                <div className="space-y-3">
                  {upcomingReservations.map((reservation) => (
                    <Card key={reservation.reservationId}>
                      <CardContent className="p-4">
                        <div className="flex items-start justify-between gap-4">
                          <div className="min-w-0 flex-1">
                            <div className="flex items-center gap-2 mb-1">
                              <h3 className="font-medium truncate">
                                {getReservationServiceName(reservation)}
                              </h3>
                              <StatusBadge status={reservation.status} />
                            </div>
                            <p className="text-sm text-muted-foreground">
                              {format(parseISO(reservation.date), 'yyyy년 M월 d일 (E)', { locale: ko })} {reservation.startTime}
                            </p>
                            <p className="text-xs text-muted-foreground mt-1">
                              예약번호: <span className="font-mono">{reservation.reservationNo}</span>
                            </p>
                          </div>
                          {canCancel(reservation.status) && (
                            <AlertDialog>
                              <AlertDialogTrigger asChild>
                                <Button 
                                  variant="ghost" 
                                  size="sm" 
                                  className="text-destructive hover:text-destructive shrink-0"
                                  disabled={cancelingId === reservation.reservationId}
                                >
                                  <X className="h-4 w-4 mr-1" />
                                  취소
                                </Button>
                              </AlertDialogTrigger>
                              <AlertDialogContent>
                                <AlertDialogHeader>
                                  <AlertDialogTitle>예약을 취소하시겠습니까?</AlertDialogTitle>
                                  <AlertDialogDescription>
                                    {getReservationServiceName(reservation)} - {format(parseISO(reservation.date), 'yyyy년 M월 d일', { locale: ko })} {reservation.startTime}
                                    <br />
                                    취소 후에는 되돌릴 수 없습니다.
                                  </AlertDialogDescription>
                                </AlertDialogHeader>
                                <AlertDialogFooter>
                                  <AlertDialogCancel>아니요</AlertDialogCancel>
                                  <AlertDialogAction 
                                    onClick={() => handleCancel(reservation.reservationId)}
                                    className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                                  >
                                    예약 취소
                                  </AlertDialogAction>
                                </AlertDialogFooter>
                              </AlertDialogContent>
                            </AlertDialog>
                          )}
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              </section>
            )}

            {/* Past Reservations */}
            {pastReservations.length > 0 && (
              <section>
                <h2 className="text-sm font-medium text-muted-foreground mb-3">지난 예약</h2>
                <div className="space-y-3">
                  {pastReservations.map((reservation) => (
                    <Card key={reservation.reservationId} className="opacity-70">
                      <CardContent className="p-4">
                        <div className="flex items-start justify-between gap-4">
                          <div className="min-w-0 flex-1">
                            <div className="flex items-center gap-2 mb-1">
                              <h3 className="font-medium truncate">
                                {getReservationServiceName(reservation)}
                              </h3>
                              <StatusBadge status={reservation.status} />
                            </div>
                            <p className="text-sm text-muted-foreground">
                              {format(parseISO(reservation.date), 'yyyy년 M월 d일 (E)', { locale: ko })} {reservation.startTime}
                            </p>
                            <p className="text-xs text-muted-foreground mt-1">
                              예약번호: <span className="font-mono">{reservation.reservationNo}</span>
                            </p>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              </section>
            )}
          </>
        )}
      </div>
    </div>
  );
}
