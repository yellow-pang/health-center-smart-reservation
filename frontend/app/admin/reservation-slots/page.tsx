'use client';

import { useState, useEffect } from 'react';
import { Pencil, Plus, Filter, Trash2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
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
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { PageHeader } from '@/src/components/common/page-header';
import { DataTable, type Column } from '@/src/components/common/data-table';
import { LoadingState } from '@/src/components/common/loading-state';
import { ErrorState } from '@/src/components/common/error-state';
import {
  createAdminReservationSlot,
  deactivateAdminReservationSlot,
  getAdminReservationSlots,
  getAdminServiceTypes,
  updateAdminReservationSlot,
} from '@/src/lib/admin-master-data-api';
import type { ReservationSlot, ServiceType } from '@/src/lib/mock-data';
import { toast } from 'sonner';
import { format, parseISO } from 'date-fns';
import { ko } from 'date-fns/locale';
import { cn } from '@/lib/utils';

type LoadState = 'loading' | 'success' | 'error';

export default function ReservationSlotsPage() {
  const [slots, setSlots] = useState<ReservationSlot[]>([]);
  const [serviceTypes, setServiceTypes] = useState<ServiceType[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [filterServiceType, setFilterServiceType] = useState<string>('all');
  const [filterDate, setFilterDate] = useState<string>(() => getTodayDate());
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingSlot, setEditingSlot] = useState<ReservationSlot | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Form state
  const [formServiceType, setFormServiceType] = useState('');
  const [formDate, setFormDate] = useState('');
  const [formTime, setFormTime] = useState('');
  const [formCapacity, setFormCapacity] = useState('5');

  const loadData = async () => {
    setLoadState('loading');
    try {
      const serviceTypeId = filterServiceType === 'all' ? undefined : Number(filterServiceType);
      const [servicesData, slotsData] = await Promise.all([
        getAdminServiceTypes(),
        getAdminReservationSlots({ serviceTypeId, date: filterDate }),
      ]);
      setSlots(slotsData);
      setServiceTypes(servicesData);
      setLoadState('success');
    } catch {
      setLoadState('error');
    }
  };

  useEffect(() => {
    loadData();
  }, [filterDate, filterServiceType]);

  const resetForm = () => {
    setEditingSlot(null);
    setFormServiceType('');
    setFormDate('');
    setFormTime('');
    setFormCapacity('5');
  };

  const openCreateDialog = () => {
    resetForm();
    setIsDialogOpen(true);
  };

  const openEditDialog = (slot: ReservationSlot) => {
    setEditingSlot(slot);
    setFormServiceType(String(slot.serviceTypeId));
    setFormDate(slot.date);
    setFormTime(slot.startTime);
    setFormCapacity(String(slot.capacity));
    setIsDialogOpen(true);
  };

  const handleSubmit = async () => {
    if (!formServiceType || !formDate || !formTime) {
      toast.error('모든 항목을 입력해주세요.');
      return;
    }

    setIsSubmitting(true);
    try {
      const payload = {
        serviceTypeId: Number(formServiceType),
        date: formDate,
        startTime: formTime,
        endTime: getEndTime(formTime),
        capacity: parseInt(formCapacity) || 5,
      };

      if (editingSlot) {
        const updatedSlot = await updateAdminReservationSlot(editingSlot.slotId, {
          ...payload,
          active: true,
        });

        setSlots(prev =>
          prev.map(slot => slot.slotId === editingSlot.slotId ? updatedSlot : slot)
        );
        toast.success('예약 슬롯이 수정되었습니다.');
      } else {
        const createdSlot = await createAdminReservationSlot(payload);
        setSlots(prev => [...prev, createdSlot]);
        toast.success('예약 슬롯이 추가되었습니다.');
      }

      setIsDialogOpen(false);
      resetForm();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  const getServiceTypeName = (serviceTypeId: number) =>
    serviceTypes.find(serviceType => serviceType.serviceTypeId === serviceTypeId)?.name || '미확인 업무';

  const handleDeactivate = async (slotId: number) => {
    try {
      await deactivateAdminReservationSlot(slotId);
      setSlots(prev => prev.filter(slot => slot.slotId !== slotId));
      toast.success('예약 슬롯이 비활성화되었습니다.');
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  };

  const columns: Column<ReservationSlot>[] = [
    {
      key: 'date',
      header: '날짜',
      cell: (item) => format(parseISO(item.date), 'yyyy년 M월 d일 (E)', { locale: ko }),
    },
    {
      key: 'startTime',
      header: '시간',
      cell: (item) => <span className="font-mono">{item.startTime}</span>,
      className: 'w-24',
    },
    {
      key: 'serviceType',
      header: '업무 유형',
      cell: (item) => getServiceTypeName(item.serviceTypeId),
    },
    {
      key: 'capacity',
      header: '정원',
      cell: (item) => `${item.capacity}명`,
      className: 'w-20',
    },
    {
      key: 'reservedCount',
      header: '예약됨',
      cell: (item) => {
        const percentage = (item.reservedCount / item.capacity) * 100;
        return (
          <div className="flex items-center gap-2">
            <div className="w-16 h-2 rounded-full bg-muted overflow-hidden">
              <div 
                className={cn(
                  'h-full rounded-full transition-all',
                  percentage >= 100 ? 'bg-red-500' :
                  percentage >= 70 ? 'bg-amber-500' :
                  'bg-green-500'
                )}
                style={{ width: `${Math.min(percentage, 100)}%` }}
              />
            </div>
            <span className="text-sm">
              {item.reservedCount}/{item.capacity}
            </span>
          </div>
        );
      },
      className: 'w-36',
    },
    {
      key: 'status',
      header: '상태',
      cell: (item) => {
        const remaining = item.availableCount;
        if (remaining <= 0) {
          return <span className="text-xs px-2 py-1 rounded-full bg-red-100 text-red-700">마감</span>;
        }
        if (remaining <= 2) {
          return <span className="text-xs px-2 py-1 rounded-full bg-amber-100 text-amber-700">마감 임박</span>;
        }
        return <span className="text-xs px-2 py-1 rounded-full bg-green-100 text-green-700">예약 가능</span>;
      },
      className: 'w-24',
    },
    {
      key: 'actions',
      header: '',
      cell: (item) => (
        <div className="flex items-center justify-end gap-1">
          <Button
            variant="ghost"
            size="icon"
            className="h-8 w-8"
            onClick={() => openEditDialog(item)}
          >
            <Pencil className="h-4 w-4" />
          </Button>
          <AlertDialog>
            <AlertDialogTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className="h-8 w-8 text-destructive hover:text-destructive"
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>예약 슬롯 비활성화</AlertDialogTitle>
                <AlertDialogDescription>
                  {format(parseISO(item.date), 'yyyy년 M월 d일', { locale: ko })} {item.startTime} 슬롯을 비활성화하시겠습니까?
                  기존 예약은 유지되고 신규 예약 선택 목록에서 제외됩니다.
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>취소</AlertDialogCancel>
                <AlertDialogAction
                  onClick={() => handleDeactivate(item.slotId)}
                  className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                >
                  비활성화
                </AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>
        </div>
      ),
      className: 'w-24',
    },
  ];

  return (
    <div>
      <PageHeader
        title="예약 슬롯 관리"
        description="업무별 예약 가능 시간을 관리합니다"
        actions={
          <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
            <DialogTrigger asChild>
              <Button onClick={openCreateDialog}>
                <Plus className="mr-2 h-4 w-4" />
                슬롯 추가
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>{editingSlot ? '예약 슬롯 수정' : '새 예약 슬롯 추가'}</DialogTitle>
                <DialogDescription>
                  {editingSlot ? '예약 가능 시간과 정원을 수정합니다.' : '새로운 예약 가능 시간대를 추가합니다.'}
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4 py-4">
                <div className="space-y-2">
                  <Label>업무 유형</Label>
                  <Select value={formServiceType} onValueChange={setFormServiceType}>
                    <SelectTrigger>
                      <SelectValue placeholder="업무 선택" />
                    </SelectTrigger>
                    <SelectContent>
                      {serviceTypes.map((st) => (
                        st.active && (
                          <SelectItem key={st.serviceTypeId} value={String(st.serviceTypeId)}>{st.name}</SelectItem>
                        )
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="date">날짜</Label>
                  <Input
                    id="date"
                    type="date"
                    value={formDate}
                    onChange={(e) => setFormDate(e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="time">시작 시간</Label>
                  <Input
                    id="time"
                    type="time"
                    value={formTime}
                    onChange={(e) => setFormTime(e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="capacity">정원</Label>
                  <Input
                    id="capacity"
                    type="number"
                    min="1"
                    value={formCapacity}
                    onChange={(e) => setFormCapacity(e.target.value)}
                  />
                </div>
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setIsDialogOpen(false)}>
                  취소
                </Button>
                <Button onClick={handleSubmit} disabled={isSubmitting}>
                  {editingSlot ? '수정' : '추가'}
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        }
      />

      {/* Filters */}
      <Card className="mt-6">
        <CardHeader className="pb-3">
          <CardTitle className="text-base flex items-center gap-2">
            <Filter className="h-4 w-4" />
            필터
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap gap-4">
            <div className="w-full sm:w-48">
              <Select value={filterServiceType} onValueChange={setFilterServiceType}>
                <SelectTrigger>
                  <SelectValue placeholder="업무 유형" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">전체 업무</SelectItem>
                  {serviceTypes.map(st => (
                    st.active && (
                      <SelectItem key={st.serviceTypeId} value={String(st.serviceTypeId)}>{st.name}</SelectItem>
                    )
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="w-full sm:w-48">
              <Input
                type="date"
                value={filterDate}
                onChange={(e) => setFilterDate(e.target.value || getTodayDate())}
                placeholder="날짜 선택"
              />
            </div>
            {(filterServiceType !== 'all' || filterDate !== getTodayDate()) && (
              <Button 
                variant="ghost" 
                onClick={() => {
                  setFilterServiceType('all');
                  setFilterDate(getTodayDate());
                }}
              >
                필터 초기화
              </Button>
            )}
          </div>
        </CardContent>
      </Card>

      <div className="mt-6">
        {loadState === 'loading' && <LoadingState />}
        {loadState === 'error' && <ErrorState onRetry={loadData} />}
        {loadState === 'success' && (
          <DataTable
            columns={columns}
            data={slots}
            keyExtractor={(item) => String(item.slotId)}
            emptyMessage="등록된 예약 슬롯이 없습니다."
          />
        )}
      </div>
    </div>
  );
}

function getTodayDate(): string {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
}

function getEndTime(startTime: string): string {
  const [hour, minute] = startTime.split(':').map(Number);
  const date = new Date(2000, 0, 1, hour, minute);
  date.setMinutes(date.getMinutes() + 30);

  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

function getErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }

  return '요청 처리 중 오류가 발생했습니다.';
}
