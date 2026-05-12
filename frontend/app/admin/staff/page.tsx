'use client';

import { useState, useEffect } from 'react';
import { Plus, Pencil, Trash2, UserCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
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
import { getServiceWindows } from '@/src/lib/mock-services';
import type { ServiceWindow } from '@/src/lib/mock-data';
import { toast } from 'sonner';

type LoadState = 'loading' | 'success' | 'error';

interface StaffMember {
  id: string;
  name: string;
  email: string;
  phone: string;
  windowId?: string;
  isActive: boolean;
}

// Mock staff data
const initialStaff: StaffMember[] = [
  { id: 'staff-1', name: '김직원', email: 'kim@health.go.kr', phone: '010-2345-6789', windowId: 'win-1', isActive: true },
  { id: 'staff-2', name: '이직원', email: 'lee@health.go.kr', phone: '010-3456-7890', windowId: 'win-2', isActive: true },
  { id: 'staff-3', name: '박직원', email: 'park@health.go.kr', phone: '010-4567-8901', windowId: 'win-3', isActive: true },
  { id: 'staff-4', name: '최직원', email: 'choi@health.go.kr', phone: '010-5678-9012', isActive: false },
];

export default function StaffManagementPage() {
  const [staff, setStaff] = useState<StaffMember[]>([]);
  const [windows, setWindows] = useState<ServiceWindow[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<StaffMember | null>(null);

  // Form state
  const [formName, setFormName] = useState('');
  const [formEmail, setFormEmail] = useState('');
  const [formPhone, setFormPhone] = useState('');
  const [formWindowId, setFormWindowId] = useState<string>('');
  const [formIsActive, setFormIsActive] = useState(true);

  const loadData = async () => {
    setLoadState('loading');
    try {
      const windowsData = await getServiceWindows();
      setWindows(windowsData);
      setStaff(initialStaff);
      setLoadState('success');
    } catch {
      setLoadState('error');
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const getWindowName = (windowId?: string) => {
    if (!windowId) return '-';
    return windows.find(w => w.id === windowId)?.name || '-';
  };

  const resetForm = () => {
    setFormName('');
    setFormEmail('');
    setFormPhone('');
    setFormWindowId('');
    setFormIsActive(true);
    setEditingItem(null);
  };

  const openCreateDialog = () => {
    resetForm();
    setIsDialogOpen(true);
  };

  const openEditDialog = (item: StaffMember) => {
    setEditingItem(item);
    setFormName(item.name);
    setFormEmail(item.email);
    setFormPhone(item.phone);
    setFormWindowId(item.windowId || '');
    setFormIsActive(item.isActive);
    setIsDialogOpen(true);
  };

  const handleSubmit = () => {
    if (!formName.trim() || !formEmail.trim()) {
      toast.error('이름과 이메일을 입력해주세요.');
      return;
    }

    if (editingItem) {
      setStaff(prev =>
        prev.map(s =>
          s.id === editingItem.id
            ? { ...s, name: formName, email: formEmail, phone: formPhone, windowId: formWindowId || undefined, isActive: formIsActive }
            : s
        )
      );
      toast.success('직원 정보가 수정되었습니다.');
    } else {
      const newStaff: StaffMember = {
        id: `staff-${Date.now()}`,
        name: formName,
        email: formEmail,
        phone: formPhone,
        windowId: formWindowId || undefined,
        isActive: formIsActive,
      };
      setStaff(prev => [...prev, newStaff]);
      toast.success('직원이 추가되었습니다.');
    }
    
    setIsDialogOpen(false);
    resetForm();
  };

  const handleDelete = (id: string) => {
    setStaff(prev => prev.filter(s => s.id !== id));
    toast.success('직원이 삭제되었습니다.');
  };

  const columns: Column<StaffMember>[] = [
    {
      key: 'name',
      header: '이름',
      cell: (item) => (
        <div className="flex items-center gap-2">
          <UserCircle className="h-8 w-8 text-muted-foreground" />
          <div>
            <p className="font-medium">{item.name}</p>
            <p className="text-xs text-muted-foreground">{item.email}</p>
          </div>
        </div>
      ),
    },
    {
      key: 'phone',
      header: '연락처',
      cell: (item) => item.phone,
    },
    {
      key: 'window',
      header: '담당 창구',
      cell: (item) => getWindowName(item.windowId),
    },
    {
      key: 'isActive',
      header: '상태',
      cell: (item) => (
        <span className={`text-xs px-2 py-1 rounded-full ${
          item.isActive 
            ? 'bg-green-100 text-green-700' 
            : 'bg-gray-100 text-gray-500'
        }`}>
          {item.isActive ? '근무 중' : '비활성'}
        </span>
      ),
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
                <AlertDialogTitle>직원 삭제</AlertDialogTitle>
                <AlertDialogDescription>
                  &quot;{item.name}&quot; 직원을 삭제하시겠습니까?
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>취소</AlertDialogCancel>
                <AlertDialogAction
                  onClick={() => handleDelete(item.id)}
                  className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                >
                  삭제
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
        title="직원 관리"
        description="보건소 직원을 관리합니다"
        actions={
          <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
            <DialogTrigger asChild>
              <Button onClick={openCreateDialog}>
                <Plus className="mr-2 h-4 w-4" />
                직원 추가
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>
                  {editingItem ? '직원 정보 수정' : '새 직원 추가'}
                </DialogTitle>
                <DialogDescription>
                  {editingItem ? '직원 정보를 수정합니다.' : '새로운 직원을 추가합니다.'}
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4 py-4">
                <div className="space-y-2">
                  <Label htmlFor="name">이름</Label>
                  <Input
                    id="name"
                    value={formName}
                    onChange={(e) => setFormName(e.target.value)}
                    placeholder="홍길동"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email">이메일</Label>
                  <Input
                    id="email"
                    type="email"
                    value={formEmail}
                    onChange={(e) => setFormEmail(e.target.value)}
                    placeholder="hong@health.go.kr"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="phone">연락처</Label>
                  <Input
                    id="phone"
                    type="tel"
                    value={formPhone}
                    onChange={(e) => setFormPhone(e.target.value)}
                    placeholder="010-1234-5678"
                  />
                </div>
                <div className="space-y-2">
                  <Label>담당 창구</Label>
                  <Select value={formWindowId} onValueChange={setFormWindowId}>
                    <SelectTrigger>
                      <SelectValue placeholder="창구 선택 (선택사항)" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="">미배정</SelectItem>
                      {windows.filter(w => w.isActive).map((w) => (
                        <SelectItem key={w.id} value={w.id}>{w.name}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
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
          <DataTable
            columns={columns}
            data={staff}
            keyExtractor={(item) => item.id}
            emptyMessage="등록된 직원이 없습니다."
          />
        )}
      </div>
    </div>
  );
}
