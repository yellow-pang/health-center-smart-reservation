'use client';

import { useState, useEffect } from 'react';
import { UserPlus, CheckCircle2, Loader2, RotateCcw, Clock } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { PageHeader } from '@/src/components/common/page-header';
import { LoadingState } from '@/src/components/common/loading-state';
import { registerWalkIn, getServiceTypes } from '@/src/lib/staff-api';
import { getServiceTypeName } from '@/src/lib/mock-data';
import type { QueueEntry, ServiceType } from '@/src/lib/mock-data';
import { toast } from 'sonner';
import { format } from 'date-fns';
import { ko } from 'date-fns/locale';

type State = 'idle' | 'loading' | 'success';

interface RecentEntry {
  queueEntry: QueueEntry;
  timestamp: Date;
}

export default function WalkInPage() {
  const [serviceTypes, setServiceTypes] = useState<ServiceType[]>([]);
  const [isLoadingServices, setIsLoadingServices] = useState(true);
  
  const [visitorName, setVisitorName] = useState('');
  const [visitorPhone, setVisitorPhone] = useState('');
  const [serviceTypeId, setServiceTypeId] = useState('');
  
  const [state, setState] = useState<State>('idle');
  const [queueEntry, setQueueEntry] = useState<QueueEntry | null>(null);
  const [recentEntries, setRecentEntries] = useState<RecentEntry[]>([]);

  useEffect(() => {
    const loadServices = async () => {
      try {
        const data = await getServiceTypes();
        setServiceTypes(data);
      } finally {
        setIsLoadingServices(false);
      }
    };
    loadServices();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!visitorName.trim() || !visitorPhone.trim() || !serviceTypeId) {
      toast.error('모든 항목을 입력해주세요.');
      return;
    }

    setState('loading');
    
    try {
      const selectedServiceType = serviceTypes.find(service => service.serviceTypeId === Number(serviceTypeId));
      const result = await registerWalkIn({
        visitorName: visitorName.trim(),
        visitorPhone: visitorPhone.trim(),
        serviceTypeId: Number(serviceTypeId),
        serviceTypeName: selectedServiceType?.name,
      });
      
      if (result.success && result.queueEntry) {
        setQueueEntry(result.queueEntry);
        setState('success');
        
        // Add to recent entries
        setRecentEntries(prev => [
          { queueEntry: result.queueEntry!, timestamp: new Date() },
          ...prev.slice(0, 4),
        ]);
        
        toast.success('현장 접수가 완료되었습니다!');
      } else {
        toast.error(result.error || '접수에 실패했습니다.');
        setState('idle');
      }
    } catch {
      toast.error('오류가 발생했습니다.');
      setState('idle');
    }
  };

  const handleReset = () => {
    setVisitorName('');
    setVisitorPhone('');
    setServiceTypeId('');
    setState('idle');
    setQueueEntry(null);
  };

  const getEntryServiceName = (entry: QueueEntry) => {
    return entry.serviceTypeName || getServiceTypeName(entry.serviceTypeId);
  };

  if (isLoadingServices) {
    return (
      <div className="max-w-2xl mx-auto">
        <PageHeader title="현장 접수" description="현장 방문 고객을 접수합니다" />
        <LoadingState />
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto">
      <PageHeader 
        title="현장 접수" 
        description="현장 방문 고객을 접수합니다"
      />

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        {/* Registration Form */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">방문자 정보</CardTitle>
            <CardDescription>
              현장 방문 고객 정보를 입력하세요
            </CardDescription>
          </CardHeader>
          <CardContent>
            {state === 'idle' || state === 'loading' ? (
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="name">방문자 이름</Label>
                  <Input
                    id="name"
                    placeholder="홍길동"
                    value={visitorName}
                    onChange={(e) => setVisitorName(e.target.value)}
                    disabled={state === 'loading'}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="phone">휴대폰 번호</Label>
                  <Input
                    id="phone"
                    type="tel"
                    placeholder="010-1234-5678"
                    value={visitorPhone}
                    onChange={(e) => setVisitorPhone(e.target.value)}
                    disabled={state === 'loading'}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="service">업무 유형</Label>
                  <Select
                    value={serviceTypeId}
                    onValueChange={setServiceTypeId}
                    disabled={state === 'loading'}
                  >
                    <SelectTrigger id="service">
                      <SelectValue placeholder="업무를 선택하세요" />
                    </SelectTrigger>
                    <SelectContent>
                      {serviceTypes.map((service) => (
                        <SelectItem key={service.serviceTypeId} value={String(service.serviceTypeId)}>
                          {service.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <Button 
                  type="submit" 
                  className="w-full" 
                  size="lg"
                  disabled={state === 'loading'}
                >
                  {state === 'loading' ? (
                    <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                  ) : (
                    <UserPlus className="mr-2 h-5 w-5" />
                  )}
                  접수하기
                </Button>
              </form>
            ) : state === 'success' && queueEntry ? (
              <div className="text-center py-4">
                <div className="flex h-16 w-16 items-center justify-center rounded-full bg-green-100 mx-auto mb-4">
                  <CheckCircle2 className="h-8 w-8 text-green-600" />
                </div>
                
                <h3 className="text-lg font-semibold mb-2">접수 완료!</h3>
                
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
                    <span>현장 방문</span>
                  </p>
                </div>

                <Button onClick={handleReset} className="w-full" size="lg">
                  <RotateCcw className="mr-2 h-4 w-4" />
                  다음 접수
                </Button>
              </div>
            ) : null}
          </CardContent>
        </Card>

        {/* Recent Entries */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">최근 접수 목록</CardTitle>
            <CardDescription>
              오늘 접수된 최근 5건
            </CardDescription>
          </CardHeader>
          <CardContent>
            {recentEntries.length === 0 ? (
              <div className="text-center py-8 text-muted-foreground">
                <Clock className="h-10 w-10 mx-auto mb-2 opacity-50" />
                <p className="text-sm">아직 접수 내역이 없습니다</p>
              </div>
            ) : (
              <div className="space-y-3">
                {recentEntries.map((entry, index) => (
                  <div 
                    key={`${entry.queueEntry.queueTicketId}-${index}`}
                    className="flex items-center justify-between p-3 rounded-lg bg-muted/50"
                  >
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <span className="font-mono font-semibold text-primary">
                          {entry.queueEntry.ticketNumber}
                        </span>
                        <span className="text-sm truncate">
                          {entry.queueEntry.visitorNameMasked}
                        </span>
                      </div>
                      <p className="text-xs text-muted-foreground">
                        {getEntryServiceName(entry.queueEntry)}
                      </p>
                    </div>
                    <span className="text-xs text-muted-foreground shrink-0 ml-2">
                      {format(entry.timestamp, 'HH:mm', { locale: ko })}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
