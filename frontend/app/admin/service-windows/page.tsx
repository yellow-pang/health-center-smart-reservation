'use client';

import { useState, useEffect } from 'react';
import { Plus, Pencil, Building2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Badge } from '@/components/ui/badge';
import { Checkbox } from '@/components/ui/checkbox';
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
import { LoadingState } from '@/src/components/common/loading-state';
import { ErrorState } from '@/src/components/common/error-state';
import { getServiceWindows, getServiceTypes } from '@/src/lib/mock-services';
import { getServiceTypeName } from '@/src/lib/mock-data';
import type { ServiceWindow, ServiceType } from '@/src/lib/mock-data';
import { toast } from 'sonner';
import { cn } from '@/lib/utils';

type LoadState = 'loading' | 'success' | 'error';

export default function ServiceWindowsPage() {
  const [windows, setWindows] = useState<ServiceWindow[]>([]);
  const [serviceTypes, setServiceTypes] = useState<ServiceType[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<ServiceWindow | null>(null);

  // Form state
  const [formName, setFormName] = useState('');
  const [formServiceTypeIds, setFormServiceTypeIds] = useState<number[]>([]);
  const [formIsActive, setFormIsActive] = useState(true);

  const loadData = async () => {
    setLoadState('loading');
    try {
      const [windowsData, servicesData] = await Promise.all([
        getServiceWindows(),
        getServiceTypes(),
      ]);
      setWindows(windowsData);
      setServiceTypes(servicesData);
      setLoadState('success');
    } catch {
      setLoadState('error');
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const resetForm = () => {
    setFormName('');
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
    setFormName(item.name);
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

  const handleSubmit = () => {
    if (!formName.trim()) {
      toast.error('창구명을 입력해주세요.');
      return;
    }

    if (editingItem) {
      setWindows(prev =>
        prev.map(w =>
          w.id === editingItem.id
            ? { ...w, name: formName, serviceTypeIds: formServiceTypeIds, active: formIsActive }
            : w
        )
      );
      toast.success('창구 정보가 수정되었습니다.');
    } else {
      const newWindow: ServiceWindow = {
        id: `win-${Date.now()}`,
        name: formName,
        serviceTypeIds: formServiceTypeIds,
        active: formIsActive,
      };
      setWindows(prev => [...prev, newWindow]);
      toast.success('창구가 추가되었습니다.');
    }
    
    setIsDialogOpen(false);
    resetForm();
  };

  return (
    <div>
      <PageHeader
        title="창구 관리"
        description="보건소 창구와 담당 업무를 관리합니다"
        actions={
          <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
            <DialogTrigger asChild>
              <Button onClick={openCreateDialog}>
                <Plus className="mr-2 h-4 w-4" />
                창구 추가
              </Button>
            </DialogTrigger>
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
                  <Label htmlFor="name">창구명</Label>
                  <Input
                    id="name"
                    value={formName}
                    onChange={(e) => setFormName(e.target.value)}
                    placeholder="예: 1번 창구"
                  />
                </div>
                <div className="space-y-2">
                  <Label>담당 업무</Label>
                  <div className="rounded-md border p-4 space-y-3">
                    {serviceTypes.map((st) => (
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
                <Button onClick={handleSubmit}>
                  {editingItem ? '수정' : '추가'}
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
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

                  {window.staffId && (
                    <div className="mt-3 pt-3 border-t">
                      <p className="text-xs text-muted-foreground">담당자</p>
                      <p className="text-sm font-medium">
                        {window.staffId === 'staff-1' ? '김직원' : 
                         window.staffId === 'staff-2' ? '이직원' : 
                         window.staffId === 'staff-3' ? '박직원' : '미배정'}
                      </p>
                    </div>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
