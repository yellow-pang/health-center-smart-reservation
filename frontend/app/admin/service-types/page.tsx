'use client';

import { useState, useEffect } from 'react';
import { Plus, Pencil, Trash2, Clock, Check, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
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
import { DataTable, type Column } from '@/src/components/common/data-table';
import { LoadingState } from '@/src/components/common/loading-state';
import { ErrorState } from '@/src/components/common/error-state';
import {
  createAdminServiceType,
  deactivateAdminServiceType,
  getAdminServiceTypes,
  updateAdminServiceType,
} from '@/src/lib/admin-master-data-api';
import type { ServiceType } from '@/src/lib/mock-data';
import { toast } from 'sonner';

type LoadState = 'loading' | 'success' | 'error';

export default function ServiceTypesPage() {
  const [serviceTypes, setServiceTypes] = useState<ServiceType[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<ServiceType | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Form state
  const [formName, setFormName] = useState('');
  const [formDescription, setFormDescription] = useState('');
  const [formMinutes, setFormMinutes] = useState('15');
  const [formIsActive, setFormIsActive] = useState(true);

  const loadData = async () => {
    setLoadState('loading');
    try {
      const data = await getAdminServiceTypes();
      setServiceTypes(data);
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
    setFormDescription('');
    setFormMinutes('15');
    setFormIsActive(true);
    setEditingItem(null);
  };

  const openCreateDialog = () => {
    resetForm();
    setIsDialogOpen(true);
  };

  const openEditDialog = (item: ServiceType) => {
    setEditingItem(item);
    setFormName(item.name);
    setFormDescription(item.description);
    setFormMinutes(String(item.defaultCapacity));
    setFormIsActive(item.active);
    setIsDialogOpen(true);
  };

  const handleSubmit = async () => {
    if (!formName.trim()) {
      toast.error('업무 유형명을 입력해주세요.');
      return;
    }

    setIsSubmitting(true);
    try {
      const payload = {
        code: editingItem?.code,
        name: formName.trim(),
        description: formDescription.trim(),
        defaultCapacity: parseInt(formMinutes) || 5,
        active: formIsActive,
      };

      if (editingItem) {
        const updatedServiceType = await updateAdminServiceType(editingItem.serviceTypeId, payload);
        setServiceTypes(prev =>
          prev.map(s =>
            s.serviceTypeId === editingItem.serviceTypeId
              ? updatedServiceType
              : s
          )
        );
        toast.success('업무 유형이 수정되었습니다.');
      } else {
        const createdServiceType = await createAdminServiceType(payload);
        setServiceTypes(prev => [...prev, createdServiceType]);
        toast.success('업무 유형이 추가되었습니다.');
      }
      setIsDialogOpen(false);
      resetForm();
    } catch {
      toast.error('오류가 발생했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deactivateAdminServiceType(id);
      setServiceTypes(prev => prev.filter(s => s.serviceTypeId !== id));
      toast.success('업무 유형이 비활성화되었습니다.');
    } catch {
      toast.error('비활성화 중 오류가 발생했습니다.');
    }
  };

  const columns: Column<ServiceType>[] = [
    {
      key: 'name',
      header: '업무명',
      cell: (item) => <span className="font-medium">{item.name}</span>,
    },
    {
      key: 'description',
      header: '설명',
      cell: (item) => (
        <span className="text-muted-foreground text-sm">{item.description}</span>
      ),
    },
    {
      key: 'defaultCapacity',
      header: '기본 정원',
      cell: (item) => (
        <div className="flex items-center gap-1 text-sm">
          <Clock className="h-3 w-3 text-muted-foreground" />
          {item.defaultCapacity}명
        </div>
      ),
      className: 'w-32',
    },
    {
      key: 'active',
      header: '상태',
      cell: (item) => (
        <span className={`text-xs px-2 py-1 rounded-full ${
          item.active
            ? 'bg-green-100 text-green-700' 
            : 'bg-gray-100 text-gray-500'
        }`}>
          {item.active ? '활성' : '비활성'}
        </span>
      ),
      className: 'w-20',
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
                <AlertDialogTitle>업무 유형 삭제</AlertDialogTitle>
                <AlertDialogDescription>
                  &quot;{item.name}&quot; 업무 유형을 비활성화하시겠습니까? 비활성화된 업무는 예약 선택 목록에서 제외됩니다.
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>취소</AlertDialogCancel>
                <AlertDialogAction
                  onClick={() => handleDelete(item.serviceTypeId)}
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
        title="업무 유형 관리"
        description="보건소 업무 유형을 관리합니다"
        actions={
          <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
            <DialogTrigger asChild>
              <Button onClick={openCreateDialog}>
                <Plus className="mr-2 h-4 w-4" />
                업무 유형 추가
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>
                  {editingItem ? '업무 유형 수정' : '새 업무 유형 추가'}
                </DialogTitle>
                <DialogDescription>
                  {editingItem ? '업무 유형 정보를 수정합니다.' : '새로운 업무 유형을 추가합니다.'}
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4 py-4">
                <div className="space-y-2">
                  <Label htmlFor="name">업무명</Label>
                  <Input
                    id="name"
                    value={formName}
                    onChange={(e) => setFormName(e.target.value)}
                    placeholder="예: 예방접종"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="description">설명</Label>
                  <Input
                    id="description"
                    value={formDescription}
                    onChange={(e) => setFormDescription(e.target.value)}
                    placeholder="예: 각종 예방접종 서비스"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="minutes">기본 정원</Label>
                  <Input
                    id="minutes"
                    type="number"
                    min="1"
                    value={formMinutes}
                    onChange={(e) => setFormMinutes(e.target.value)}
                  />
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
        }
      />

      <div className="mt-6">
        {loadState === 'loading' && <LoadingState />}
        {loadState === 'error' && <ErrorState onRetry={loadData} />}
        {loadState === 'success' && (
          <DataTable
            columns={columns}
            data={serviceTypes}
            keyExtractor={(item) => String(item.serviceTypeId)}
            emptyMessage="등록된 업무 유형이 없습니다."
          />
        )}
      </div>
    </div>
  );
}
