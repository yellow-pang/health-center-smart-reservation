'use client';

import { useState } from 'react';
import { Search, CheckCircle2, Loader2, AlertCircle, RotateCcw, UserCheck } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { PageHeader } from '@/src/components/common/page-header';
import { checkInByReservationNumber, searchReservationsForCheckIn } from '@/src/lib/staff-api';
import { getServiceTypeName } from '@/src/lib/mock-data';
import type { QueueEntry, Reservation } from '@/src/lib/mock-data';
import { toast } from 'sonner';

type State = 'idle' | 'loading' | 'success' | 'error';
type SearchState = 'idle' | 'loading' | 'done' | 'error';

const today = new Date().toISOString().slice(0, 10);

export default function CheckInPage() {
  const [reservationNumber, setReservationNumber] = useState('');
  const [keyword, setKeyword] = useState('');
  const [date, setDate] = useState(today);
  const [state, setState] = useState<State>('idle');
  const [searchState, setSearchState] = useState<SearchState>('idle');
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [queueEntry, setQueueEntry] = useState<QueueEntry | null>(null);
  const [errorMessage, setErrorMessage] = useState('');
  const [searchErrorMessage, setSearchErrorMessage] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!reservationNumber.trim()) {
      toast.error('예약번호를 입력해주세요.');
      return;
    }

    await handleCheckIn(reservationNumber.trim());
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();

    setSearchState('loading');
    setSearchErrorMessage('');

    try {
      const result = await searchReservationsForCheckIn({
        keyword: keyword.trim() || undefined,
        date: date || undefined,
        status: 'RESERVED',
      });
      setReservations(result);
      setSearchState('done');
      if (result.length === 0) {
        toast.info('조건에 맞는 예약이 없습니다.');
      }
    } catch (error) {
      setSearchErrorMessage(error instanceof Error ? error.message : '예약 검색에 실패했습니다.');
      setSearchState('error');
    }
  };

  const handleCheckIn = async (reservationNo: string, reservation?: Reservation) => {
    setState('loading');
    setErrorMessage('');

    try {
      const result = await checkInByReservationNumber(reservationNo, reservation);

      if (result.success && result.queueEntry) {
        setQueueEntry(result.queueEntry);
        setState('success');
        toast.success('체크인이 완료되었습니다!');
      } else {
        setErrorMessage(result.error || '체크인에 실패했습니다.');
        setState('error');
      }
    } catch {
      setErrorMessage('오류가 발생했습니다. 다시 시도해주세요.');
      setState('error');
    }
  };

  const handleReset = () => {
    setReservationNumber('');
    setState('idle');
    setQueueEntry(null);
    setErrorMessage('');
  };

  const getEntryServiceName = (entry: QueueEntry) => {
    return entry.serviceTypeName || getServiceTypeName(entry.serviceTypeId);
  };

  return (
    <div className="max-w-lg mx-auto">
      <PageHeader 
        title="예약 체크인" 
        description="예약자를 검색하거나 예약번호로 체크인을 진행합니다"
      />

      <Card className="mt-6">
        <CardHeader>
          <CardTitle className="text-lg">예약자 검색</CardTitle>
          <CardDescription>
            이름, 전화번호, 예약번호 일부로 오늘 예약자를 찾습니다
          </CardDescription>
        </CardHeader>
        <CardContent>
          {state === 'idle' || state === 'loading' || state === 'error' ? (
            <div className="space-y-6">
              <form onSubmit={handleSearch} className="space-y-4">
                <div className="grid gap-3 sm:grid-cols-[1fr_9rem]">
                  <div className="space-y-2">
                    <Label htmlFor="keyword">검색어</Label>
                    <Input
                      id="keyword"
                      placeholder="홍길동, 01012345678, RSV-"
                      value={keyword}
                      onChange={(e) => setKeyword(e.target.value)}
                      disabled={searchState === 'loading' || state === 'loading'}
                      autoComplete="off"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="date">예약일</Label>
                    <Input
                      id="date"
                      type="date"
                      value={date}
                      onChange={(e) => setDate(e.target.value)}
                      disabled={searchState === 'loading' || state === 'loading'}
                    />
                  </div>
                </div>

                <Button
                  type="submit"
                  className="w-full"
                  disabled={searchState === 'loading' || state === 'loading'}
                >
                  {searchState === 'loading' ? (
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  ) : (
                    <Search className="mr-2 h-4 w-4" />
                  )}
                  예약 검색
                </Button>
              </form>

              {searchState === 'error' && (
                <div className="flex items-center gap-2 rounded-md bg-destructive/10 p-3 text-sm text-destructive">
                  <AlertCircle className="h-4 w-4 shrink-0" />
                  <span>{searchErrorMessage}</span>
                </div>
              )}

              {searchState === 'done' && (
                <div className="space-y-3">
                  {reservations.length === 0 ? (
                    <div className="rounded-md border border-dashed p-4 text-center text-sm text-muted-foreground">
                      검색 결과가 없습니다.
                    </div>
                  ) : (
                    reservations.map((reservation) => (
                      <div
                        key={reservation.reservationId}
                        className="rounded-md border p-4"
                      >
                        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                          <div className="min-w-0 space-y-1">
                            <div className="flex flex-wrap items-center gap-2">
                              <span className="font-semibold">{reservation.visitorName}</span>
                              <span className="text-sm text-muted-foreground">{maskPhone(reservation.visitorPhone)}</span>
                            </div>
                            <p className="text-sm text-muted-foreground">
                              {reservation.serviceTypeName || getServiceTypeName(reservation.serviceTypeId)}
                              {' · '}
                              {reservation.date} {formatTime(reservation.startTime)}
                            </p>
                            <p className="font-mono text-xs text-muted-foreground">{reservation.reservationNo}</p>
                          </div>
                          <Button
                            type="button"
                            onClick={() => handleCheckIn(reservation.reservationNo, reservation)}
                            disabled={state === 'loading'}
                            className="sm:w-28"
                          >
                            {state === 'loading' ? (
                              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            ) : (
                              <UserCheck className="mr-2 h-4 w-4" />
                            )}
                            접수
                          </Button>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              )}

              {state === 'error' && (
                <div className="flex items-center gap-2 rounded-md bg-destructive/10 p-3 text-sm text-destructive">
                  <AlertCircle className="h-4 w-4 shrink-0" />
                  <span>{errorMessage}</span>
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-3 border-t pt-4">
                <div className="space-y-2">
                  <Label htmlFor="reservationNumber">예약번호 직접 입력</Label>
                  <Input
                    id="reservationNumber"
                    placeholder="RSV-SWAGGER-CHECKIN-001"
                    value={reservationNumber}
                    onChange={(e) => setReservationNumber(e.target.value.toUpperCase())}
                    disabled={state === 'loading'}
                    className="font-mono tracking-wider"
                    autoComplete="off"
                  />
                </div>
                <Button
                  type="submit"
                  variant="outline"
                  className="w-full"
                  disabled={state === 'loading'}
                >
                  {state === 'loading' ? (
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  ) : (
                    <Search className="mr-2 h-4 w-4" />
                  )}
                  예약번호로 접수
                </Button>
              </form>
            </div>
          ) : state === 'success' && queueEntry ? (
            <div className="text-center py-4">
              <div className="flex h-16 w-16 items-center justify-center rounded-full bg-green-100 mx-auto mb-4">
                <CheckCircle2 className="h-8 w-8 text-green-600" />
              </div>
              
              <h3 className="text-lg font-semibold mb-2">체크인 완료!</h3>
              
              {/* Queue Number Display */}
              <div className="my-6 p-6 rounded-xl bg-primary text-primary-foreground">
                <p className="text-sm opacity-80 mb-1">대기번호</p>
                <p className="text-5xl font-bold tracking-wider">{queueEntry.ticketNumber}</p>
              </div>

              <div className="text-left rounded-lg bg-muted/50 p-4 text-sm space-y-2 mb-6">
                <p className="flex justify-between">
                  <span className="text-muted-foreground">방문자</span>
                  <span className="font-medium">{queueEntry.visitorNameMasked}</span>
                </p>
                <p className="flex justify-between">
                  <span className="text-muted-foreground">업무</span>
                  <span>{getEntryServiceName(queueEntry)}</span>
                </p>
                <p className="flex justify-between">
                  <span className="text-muted-foreground">접수 유형</span>
                  <span>예약 방문</span>
                </p>
              </div>

              <Button onClick={handleReset} className="w-full" size="lg">
                <RotateCcw className="mr-2 h-4 w-4" />
                다음 체크인
              </Button>
            </div>
          ) : null}
        </CardContent>
      </Card>

      <Card className="mt-4">
        <CardContent className="p-4">
          <p className="text-sm text-muted-foreground">
            <span className="font-medium text-foreground">테스트 예약번호:</span>{' '}
            RSV-SWAGGER-CHECKIN-001
          </p>
        </CardContent>
      </Card>
    </div>
  );
}

function formatTime(time: string): string {
  return time.slice(0, 5);
}

function maskPhone(phone: string): string {
  return phone.replace(/(\d{3})-?\d{4}-?(\d{4})/, '$1-****-$2');
}
