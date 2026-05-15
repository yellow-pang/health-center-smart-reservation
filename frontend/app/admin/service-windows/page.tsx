'use client';

import { useState, useEffect } from 'react';
import { Plus, Pencil, Building2, Trash2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Badge } from '@/components/ui/badge';
import { Checkbox } from '@/components/ui/checkbox';
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
} from '@/components/ui/dialog';
import { PageHeader } from '@/src/components/common/page-header';
import { LoadingState } from '@/src/components/common/loading-state';
import { ErrorState } from '@/src/components/common/error-state';
import {
  createAdminServiceWindow,
  deactivateAdminServiceWindow,
  getAdminServiceTypes,
  getAdminServiceWindows,
  getAdminStaff,
  updateAdminServiceWindow,
  type StaffMember,
} from '@/src/lib/admin-master-data-api';
import type { ServiceWindow, ServiceType } from '@/src/lib/mock-data';
import { toast } from 'sonner';
import { cn } from '@/lib/utils';

type LoadState = 'loading' | 'success' | 'error';
const UNASSIGNED_STAFF_VALUE = 'unassigned';

export default function ServiceWindowsPage() {
  const [windows, setWindows] = useState<ServiceWindow[]>([]);
  const [serviceTypes, setServiceTypes] = useState<ServiceType[]>([]);
  const [staff, setStaff] = useState<StaffMember[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<ServiceWindow | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Form state
  const [formWindowNumber, setFormWindowNumber] = useState('');
  const [formName, setFormName] = useState('');
  const [formStaffId, setFormStaffId] = useState(UNASSIGNED_STAFF_VALUE);
  const [formServiceTypeIds, setFormServiceTypeIds] = useState<number[]>([]);
  const [formIsActive, setFormIsActive] = useState(true);

  const loadData = async () => {
    setLoadState('loading');
    try {
      const [windowsData, servicesData, staffData] = await Promise.all([
        getAdminServiceWindows(),
        getAdminServiceTypes(),
        getAdminStaff(),
      ]);
      setWindows(windowsData);
      setServiceTypes(servicesData);
      setStaff(staffData);
      setLoadState('success');
    } catch {
      setLoadState('error');
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const resetForm = () => {
    setFormWindowNumber('');
    setFormName('');
    setFormStaffId(UNASSIGNED_STAFF_VALUE);
    setFormServiceTypeIds([]);
    setFormIsActive(true);
    setEditingItem(null);
  };

  const openCreateDialog = () => {
    resetForm();
    setIsDialogOpen(true);
  };

  const openEditDialog = (item: ServiceWindow) => {
    setEditingItem(item);
    setFormWindowNumber(String(item.windowNumber || ''));
    setFormName(item.name);
    setFormStaffId(item.staffId || UNASSIGNED_STAFF_VALUE);
    setFormServiceTypeIds(item.serviceTypeIds);
    setFormIsActive(item.active);
    setIsDialogOpen(true);
  };

  const handleServiceTypeToggle = (serviceTypeId: number) => {
    setFormServiceTypeIds(prev =>
      prev.includes(serviceTypeId)
        ? prev.filter(id => id !== serviceTypeId)
        : [...prev, serviceTypeId]
    );
  };

  const handleSubmit = async () => {
    if (!formWindowNumber || !formName.trim()) {
      toast.error('창구 번호와 창구명을 입력해주세요.');
      return;
    }
    if (formServiceTypeIds.length === 0) {
      toast.error('담당 업무를 하나 이상 선택해주세요.');
      return;
    }

    setIsSubmitting(true);
    try {
      const payload = {
        windowNumber: Number(formWindowNumber),
        name: formName.trim(),
        status: 'OPEN',
        active: formIsActive,
        staffId: formStaffId === UNASSIGNED_STAFF_VALUE ? null : Number(formStaffId),
        serviceTypeIds: formServiceTypeIds,
      };

      if (editingItem) {
        const updatedWindow = await updateAdminServiceWindow(editingItem.id, payload);
        setWindows(prev =>
          prev.map(window => window.id === editingItem.id ? updatedWindow : window)
        );
        toast.success('창구 정보가 수정되었습니다.');
      } else {
        const createdWindow = await createAdminServiceWindow(payload);
        setWindows(prev => [...prev, createdWindow]);
        toast.success('창구가 추가되었습니다.');
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

  const handleDeactivate = async (id: string) => {
    try {
      await deactivateAdminServiceWindow(id);
      setWindows(prev => prev.filter(window => window.id !== id));
      toast.success('창구가 비활성화되었습니다.');
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  };

  return (
    <div>
      <PageHeader
        title="창구 관리"
        description="보건소 창구와 담당 업무를 관리합니다"
        actions={
          <>
            <Button onClick={openCreateDialog}>
              <Plus className="mr-2 h-4 w-4" />
              창구 추가
            </Button>
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>
                  {editingItem ? '창구 정보 수정' : '새 창구 추가'}
                </DialogTitle>
                <DialogDescription>
                  {editingItem ? '창구 정보를 수정합니다.' : '새로운 창구를 추가합니다.'}
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4 py-4">
                <div className="space-y-2">
                  <Label htmlFor="windowNumber">창구 번호</Label>
                  <Input
                    id="windowNumber"
                    type="number"
                    min="1"
                    value={formWindowNumber}
                    onChange={(e) => setFormWindowNumber(e.target.value)}
                    placeholder="예: 4"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="name">창구명</Label>
                  <Input
                    id="name"
                    value={formName}
                    onChange={(e) => setFormName(e.target.value)}
                    placeholder="예: 1번 창구"
                  />
                </div>
                <div className="space-y-2">
                  <Label>담당자</Label>
                  <Select value={formStaffId} onValueChange={setFormStaffId}>
                    <SelectTrigger>
                      <SelectValue placeholder="담당자 선택" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value={UNASSIGNED_STAFF_VALUE}>미배정</SelectItem>
                      {staff.filter(item => item.active).map((item) => (
                        <SelectItem key={item.id} value={String(item.id)}>{item.name}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label>담당 업무</Label>
                  <div className="rounded-md border p-4 space-y-3">
                    {serviceTypes.filter((st) => st.active).map((st) => (
                      <div key={st.serviceTypeId} className="flex items-center space-x-2">
                        <Checkbox
                          id={`st-${st.serviceTypeId}`}
                          checked={formServiceTypeIds.includes(st.serviceTypeId)}
                          onCheckedChange={() => handleServiceTypeToggle(st.serviceTypeId)}
                        />
                        <label
                          htmlFor={`st-${st.serviceTypeId}`}
                          className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                        >
                          {st.name}
                        </label>
                      </div>
                    ))}
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <Label htmlFor="active">활성화</Label>
                  <Switch
                    id="active"
                    checked={formIsActive}
                    onCheckedChange={setFormIsActive}
                  />
                </div>
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setIsDialogOpen(false)}>
                  취소
                </Button>
                <Button onClick={handleSubmit} disabled={isSubmitting}>
                  {editingItem ? '수정' : '추가'}
                </Button>
              </DialogFooter>
            </DialogContent>
            </Dialog>
          </>
        }
      />

      <div className="mt-6">
        {loadState === 'loading' && <LoadingState />}
        {loadState === 'error' && <ErrorState onRetry={loadData} />}
        {loadState === 'success' && (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {windows.map((window) => (
              <Card 
                key={window.id}
                className={cn(!window.active && 'opacity-60')}
              >
                <CardContent className="p-4">
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex items-center gap-2">
                      <div className={cn(
                        'flex h-10 w-10 items-center justify-center rounded-lg',
                        window.active ? 'bg-primary/10' : 'bg-muted'
                      )}>
                        <Building2 className={cn(
                          'h-5 w-5',
                          window.active ? 'text-primary' : 'text-muted-foreground'
                        )} />
                      </div>
                      <div>
                        <h3 className="font-medium">{window.name}</h3>
                        <span className={cn(
                          'text-xs px-2 py-0.5 rounded-full',
                          window.active
                            ? 'bg-green-100 text-green-700' 
                            : 'bg-gray-100 text-gray-500'
                        )}>
                          {window.active ? '운영 중' : '미운영'}
                        </span>
                      </div>
                    </div>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-8 w-8"
                      onClick={() => openEditDialog(window)}
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
                          <AlertDialogTitle>창구 비활성화</AlertDialogTitle>
                          <AlertDialogDescription>
                            &quot;{window.name}&quot; 창구를 비활성화하시겠습니까?
                          </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                          <AlertDialogCancel>취소</AlertDialogCancel>
                          <AlertDialogAction
                            onClick={() => handleDeactivate(window.id)}
                            className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                          >
                            비활성화
                          </AlertDialogAction>
                        </AlertDialogFooter>
                      </AlertDialogContent>
                    </AlertDialog>
                  </div>
                  
                  <div className="space-y-2">
                    <p className="text-xs text-muted-foreground">담당 업무</p>
                    <div className="flex flex-wrap gap-1">
                      {window.serviceTypeIds.length > 0 ? (
                        window.serviceTypeIds.map((stId) => (
                          <Badge key={stId} variant="secondary" className="text-xs">
                            {getServiceTypeName(stId)}
                          </Badge>
                        ))
                      ) : (
                        <span className="text-xs text-muted-foreground">미지정</span>
                      )}
                    </div>
                  </div>

                  <div className="mt-3 border-t pt-3">
                    <p className="text-xs text-muted-foreground">담당자</p>
                    <p className="text-sm font-medium">{window.staffName || '미배정'}</p>
                  </div>

                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

function getErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }

  return '요청 처리 중 오류가 발생했습니다.';
}
