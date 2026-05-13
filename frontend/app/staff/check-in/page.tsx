'use client';

import { useState } from 'react';
import { Search, CheckCircle2, Loader2, AlertCircle, RotateCcw } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { PageHeader } from '@/src/components/common/page-header';
import { checkInByReservationNumber } from '@/src/lib/staff-api';
import { getServiceTypeName } from '@/src/lib/mock-data';
import type { QueueEntry } from '@/src/lib/mock-data';
import { toast } from 'sonner';

type State = 'idle' | 'loading' | 'success' | 'error';

export default function CheckInPage() {
  const [reservationNumber, setReservationNumber] = useState('');
  const [state, setState] = useState<State>('idle');
  const [queueEntry, setQueueEntry] = useState<QueueEntry | null>(null);
  const [errorMessage, setErrorMessage] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!reservationNumber.trim()) {
      toast.error('예약번호를 입력해주세요.');
      return;
    }

    setState('loading');
    setErrorMessage('');
    
    try {
      const result = await checkInByReservationNumber(reservationNumber.trim());
      
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
        description="예약번호를 입력하여 체크인을 진행합니다"
      />

      <Card className="mt-6">
        <CardHeader>
          <CardTitle className="text-lg">예약번호 입력</CardTitle>
          <CardDescription>
            예약 확인 문자에서 받은 예약번호를 입력하세요
          </CardDescription>
        </CardHeader>
        <CardContent>
          {state === 'idle' || state === 'loading' || state === 'error' ? (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="reservationNumber">예약번호</Label>
                <Input
                  id="reservationNumber"
                  placeholder="R20260512001"
                  value={reservationNumber}
                  onChange={(e) => setReservationNumber(e.target.value.toUpperCase())}
                  disabled={state === 'loading'}
                  className="font-mono text-lg tracking-wider"
                  autoComplete="off"
                />
              </div>

              {state === 'error' && (
                <div className="flex items-center gap-2 rounded-md bg-destructive/10 p-3 text-sm text-destructive">
                  <AlertCircle className="h-4 w-4 shrink-0" />
                  <span>{errorMessage}</span>
                </div>
              )}

              <Button 
                type="submit" 
                className="w-full" 
                size="lg"
                disabled={state === 'loading'}
              >
                {state === 'loading' ? (
                  <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                ) : (
                  <Search className="mr-2 h-5 w-5" />
                )}
                체크인
              </Button>
            </form>
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

      {/* Quick Tips */}
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
