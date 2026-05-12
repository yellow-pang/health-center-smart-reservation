'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { CalendarDays, Clock, CheckCircle2, ArrowLeft, ArrowRight, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Calendar } from '@/components/ui/calendar';
import { PageHeader } from '@/src/components/common/page-header';
import { LoadingState } from '@/src/components/common/loading-state';
import { cn } from '@/lib/utils';
import { 
  getServiceTypes, 
  getReservationSlots, 
  createReservation 
} from '@/src/lib/mock-services';
import type { ServiceType, ReservationSlot, Reservation } from '@/src/lib/mock-data';
import { toast } from 'sonner';
import { format } from 'date-fns';
import { ko } from 'date-fns/locale';

type Step = 'service' | 'date' | 'time' | 'info' | 'complete';

export default function NewReservationPage() {
  const router = useRouter();
  const [step, setStep] = useState<Step>('service');
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Data states
  const [serviceTypes, setServiceTypes] = useState<ServiceType[]>([]);
  const [slots, setSlots] = useState<ReservationSlot[]>([]);
  
  // Selection states
  const [selectedService, setSelectedService] = useState<ServiceType | null>(null);
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(undefined);
  const [selectedSlot, setSelectedSlot] = useState<ReservationSlot | null>(null);
  const [visitorName, setVisitorName] = useState('');
  const [visitorPhone, setVisitorPhone] = useState('');
  const [completedReservation, setCompletedReservation] = useState<Reservation | null>(null);

  // Load service types on mount
  useEffect(() => {
    const loadServiceTypes = async () => {
      setIsLoading(true);
      try {
        const data = await getServiceTypes();
        setServiceTypes(data);
      } finally {
        setIsLoading(false);
      }
    };
    loadServiceTypes();
  }, []);

  // Load slots when service and date are selected
  useEffect(() => {
    if (selectedService && selectedDate) {
      const loadSlots = async () => {
        setIsLoading(true);
        try {
          const dateStr = format(selectedDate, 'yyyy-MM-dd');
          const data = await getReservationSlots(selectedService.id, dateStr);
          setSlots(data);
        } finally {
          setIsLoading(false);
        }
      };
      loadSlots();
    }
  }, [selectedService, selectedDate]);

  const handleServiceSelect = (service: ServiceType) => {
    setSelectedService(service);
    setStep('date');
  };

  const handleDateSelect = (date: Date | undefined) => {
    setSelectedDate(date);
    if (date) {
      setSelectedSlot(null);
      setStep('time');
    }
  };

  const handleSlotSelect = (slot: ReservationSlot) => {
    setSelectedSlot(slot);
    setStep('info');
  };

  const handleSubmit = async () => {
    if (!selectedService || !selectedDate || !selectedSlot) return;
    
    setIsSubmitting(true);
    try {
      const result = await createReservation({
        serviceTypeId: selectedService.id,
        date: format(selectedDate, 'yyyy-MM-dd'),
        time: selectedSlot.time,
        visitorName,
        visitorPhone,
      });
      
      if (result.success && result.reservation) {
        setCompletedReservation(result.reservation);
        setStep('complete');
        toast.success('예약이 완료되었습니다!');
      } else {
        toast.error('예약 중 오류가 발생했습니다.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const goBack = () => {
    const steps: Step[] = ['service', 'date', 'time', 'info'];
    const currentIndex = steps.indexOf(step);
    if (currentIndex > 0) {
      setStep(steps[currentIndex - 1]);
    }
  };

  const stepTitles: Record<Step, string> = {
    service: '업무 유형 선택',
    date: '날짜 선택',
    time: '시간 선택',
    info: '방문자 정보 입력',
    complete: '예약 완료',
  };

  return (
    <div className="max-w-2xl mx-auto">
      <PageHeader 
        title="예약 신청" 
        description="보건소 업무 예약을 신청합니다"
      />

      {/* Progress Steps */}
      <div className="flex items-center justify-between mt-6 mb-8">
        {(['service', 'date', 'time', 'info', 'complete'] as Step[]).map((s, index) => (
          <div key={s} className="flex items-center">
            <div className={cn(
              'flex h-8 w-8 items-center justify-center rounded-full text-sm font-medium',
              step === s ? 'bg-primary text-primary-foreground' :
              (['service', 'date', 'time', 'info', 'complete'].indexOf(step) > index) 
                ? 'bg-primary/20 text-primary' 
                : 'bg-muted text-muted-foreground'
            )}>
              {(['service', 'date', 'time', 'info', 'complete'].indexOf(step) > index) ? (
                <CheckCircle2 className="h-4 w-4" />
              ) : (
                index + 1
              )}
            </div>
            {index < 4 && (
              <div className={cn(
                'hidden sm:block w-12 h-0.5 ml-2',
                (['service', 'date', 'time', 'info', 'complete'].indexOf(step) > index) 
                  ? 'bg-primary/40' 
                  : 'bg-muted'
              )} />
            )}
          </div>
        ))}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">{stepTitles[step]}</CardTitle>
          {step !== 'complete' && (
            <CardDescription>
              {step === 'service' && '원하시는 업무를 선택하세요'}
              {step === 'date' && '방문 날짜를 선택하세요'}
              {step === 'time' && '예약 가능한 시간을 선택하세요'}
              {step === 'info' && '방문자 정보를 입력하세요'}
            </CardDescription>
          )}
        </CardHeader>
        <CardContent>
          {/* Step: Service Selection */}
          {step === 'service' && (
            isLoading ? <LoadingState /> : (
              <div className="grid gap-3">
                {serviceTypes.map((service) => (
                  <button
                    key={service.id}
                    onClick={() => handleServiceSelect(service)}
                    className={cn(
                      'flex items-start gap-4 p-4 rounded-lg border text-left transition-colors hover:bg-muted/50',
                      selectedService?.id === service.id && 'border-primary bg-primary/5'
                    )}
                  >
                    <CalendarDays className="h-5 w-5 text-primary shrink-0 mt-0.5" />
                    <div>
                      <p className="font-medium">{service.name}</p>
                      <p className="text-sm text-muted-foreground">{service.description}</p>
                      <p className="text-xs text-muted-foreground mt-1">
                        예상 소요시간: {service.estimatedMinutes}분
                      </p>
                    </div>
                  </button>
                ))}
              </div>
            )
          )}

          {/* Step: Date Selection */}
          {step === 'date' && (
            <div className="flex flex-col items-center">
              <Calendar
                mode="single"
                selected={selectedDate}
                onSelect={handleDateSelect}
                locale={ko}
                disabled={(date) => date < new Date() || date.getDay() === 0 || date.getDay() === 6}
                className="rounded-md border"
              />
              <Button variant="ghost" onClick={goBack} className="mt-4">
                <ArrowLeft className="mr-2 h-4 w-4" />
                이전으로
              </Button>
            </div>
          )}

          {/* Step: Time Selection */}
          {step === 'time' && (
            isLoading ? <LoadingState /> : (
              <div className="space-y-4">
                {slots.length === 0 ? (
                  <div className="text-center py-8 text-muted-foreground">
                    <Clock className="h-10 w-10 mx-auto mb-3 opacity-50" />
                    <p>선택하신 날짜에 예약 가능한 시간이 없습니다.</p>
                    <Button variant="outline" onClick={goBack} className="mt-4">
                      다른 날짜 선택
                    </Button>
                  </div>
                ) : (
                  <>
                    <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                      {slots.map((slot) => (
                        <button
                          key={slot.id}
                          onClick={() => handleSlotSelect(slot)}
                          className={cn(
                            'flex flex-col items-center justify-center p-3 rounded-lg border transition-colors hover:bg-muted/50',
                            selectedSlot?.id === slot.id && 'border-primary bg-primary/5'
                          )}
                        >
                          <span className="font-medium">{slot.time}</span>
                          <span className="text-xs text-muted-foreground">
                            {slot.capacity - slot.reserved}자리 남음
                          </span>
                        </button>
                      ))}
                    </div>
                    <Button variant="ghost" onClick={goBack} className="w-full mt-4">
                      <ArrowLeft className="mr-2 h-4 w-4" />
                      이전으로
                    </Button>
                  </>
                )}
              </div>
            )
          )}

          {/* Step: Visitor Info */}
          {step === 'info' && (
            <div className="space-y-4">
              <div className="rounded-lg bg-muted/50 p-4 text-sm">
                <p><span className="font-medium">업무:</span> {selectedService?.name}</p>
                <p><span className="font-medium">날짜:</span> {selectedDate && format(selectedDate, 'yyyy년 M월 d일 (E)', { locale: ko })}</p>
                <p><span className="font-medium">시간:</span> {selectedSlot?.time}</p>
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="name">방문자 이름</Label>
                <Input
                  id="name"
                  placeholder="홍길동"
                  value={visitorName}
                  onChange={(e) => setVisitorName(e.target.value)}
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
                />
              </div>

              <div className="flex gap-2 mt-6">
                <Button variant="outline" onClick={goBack} className="flex-1">
                  <ArrowLeft className="mr-2 h-4 w-4" />
                  이전
                </Button>
                <Button 
                  onClick={handleSubmit} 
                  className="flex-1"
                  disabled={!visitorName || !visitorPhone || isSubmitting}
                >
                  {isSubmitting ? (
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  ) : (
                    <ArrowRight className="mr-2 h-4 w-4" />
                  )}
                  예약 신청
                </Button>
              </div>
            </div>
          )}

          {/* Step: Complete */}
          {step === 'complete' && completedReservation && (
            <div className="text-center py-6">
              <div className="flex h-16 w-16 items-center justify-center rounded-full bg-green-100 mx-auto mb-4">
                <CheckCircle2 className="h-8 w-8 text-green-600" />
              </div>
              <h3 className="text-lg font-semibold mb-2">예약이 완료되었습니다!</h3>
              <p className="text-muted-foreground mb-6">아래 정보를 확인해 주세요.</p>
              
              <div className="rounded-lg bg-muted/50 p-4 text-left text-sm space-y-2 mb-6">
                <p className="flex justify-between">
                  <span className="text-muted-foreground">예약번호</span>
                  <span className="font-mono font-semibold">{completedReservation.reservationNumber}</span>
                </p>
                <p className="flex justify-between">
                  <span className="text-muted-foreground">업무</span>
                  <span>{selectedService?.name}</span>
                </p>
                <p className="flex justify-between">
                  <span className="text-muted-foreground">날짜</span>
                  <span>{completedReservation.date}</span>
                </p>
                <p className="flex justify-between">
                  <span className="text-muted-foreground">시간</span>
                  <span>{completedReservation.time}</span>
                </p>
              </div>

              <div className="flex gap-2">
                <Button variant="outline" className="flex-1" onClick={() => router.push('/citizen/reservations')}>
                  내 예약 보기
                </Button>
                <Button className="flex-1" onClick={() => {
                  setStep('service');
                  setSelectedService(null);
                  setSelectedDate(undefined);
                  setSelectedSlot(null);
                  setVisitorName('');
                  setVisitorPhone('');
                  setCompletedReservation(null);
                }}>
                  새 예약 신청
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
