'use client';

import { useState, useEffect } from 'react';
import { 
  Users, Clock, PlayCircle, PauseCircle, CheckCircle, XCircle, 
  Bell, RefreshCw, Filter 
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { PageHeader } from '@/src/components/common/page-header';
import { MetricCard } from '@/src/components/common/metric-card';
import { StatusBadge } from '@/src/components/common/status-badge';
import { DataTable, type Column } from '@/src/components/common/data-table';
import { LoadingState } from '@/src/components/common/loading-state';
import { ErrorState } from '@/src/components/common/error-state';
import { 
  getQueueEntries, 
  getServiceTypes, 
  updateQueueStatus 
} from '@/src/lib/staff-api';
import { getServiceTypeName } from '@/src/lib/mock-data';
import type { QueueEntry, QueueStatus, ServiceType } from '@/src/lib/mock-data';
import { toast } from 'sonner';
import { cn } from '@/lib/utils';

type LoadState = 'loading' | 'success' | 'error';
type QueueView = 'active' | 'completed' | 'canceled' | 'no-show' | 'all';

const ALL_QUEUE_STATUSES: QueueStatus[] = [
  'WAITING',
  'CALLED',
  'IN_PROGRESS',
  'HOLD',
  'COMPLETED',
  'NO_SHOW',
  'CANCELED',
];

const ACTIVE_QUEUE_STATUSES: QueueStatus[] = ['WAITING', 'CALLED', 'IN_PROGRESS', 'HOLD'];

export default function QueuesPage() {
  const [entries, setEntries] = useState<QueueEntry[]>([]);
  const [serviceTypes, setServiceTypes] = useState<ServiceType[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [isRefreshing, setIsRefreshing] = useState(false);
  
  // Filters
  const [filterServiceType, setFilterServiceType] = useState<string>('all');
  const [queueView, setQueueView] = useState<QueueView>('active');
  
  // Action loading states
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  const loadData = async (showRefreshing = false, view: QueueView = queueView) => {
    if (showRefreshing) {
      setIsRefreshing(true);
    } else {
      setLoadState('loading');
    }

    try {
      const [queueData, serviceData] = await Promise.all([
        loadQueueEntries(view),
        getServiceTypes(),
      ]);
      setEntries(queueData);
      setServiceTypes(serviceData);
      setLoadState('success');
    } catch {
      setLoadState('error');
    } finally {
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [queueView]);

  const handleStatusChange = async (entryId: number, newStatus: QueueStatus) => {
    setActionLoading(String(entryId));
    try {
      const result = await updateQueueStatus(entryId, newStatus);
      if (result.success) {
        setEntries(prev => {
          const nextEntry = result.queueEntry;
          if (nextEntry && !isEntryVisibleInView(nextEntry, queueView)) {
            return prev.filter(e => e.queueTicketId !== entryId);
          }

          return prev.map(e => e.queueTicketId === entryId ? (nextEntry || { ...e, status: newStatus }) : e);
        });
        toast.success('상태가 변경되었습니다.');
      } else {
        toast.error(result.error || '상태 변경에 실패했습니다.');
      }
    } catch {
      toast.error('오류가 발생했습니다.');
    } finally {
      setActionLoading(null);
    }
  };

  // Filter entries
  const filteredEntries = entries.filter(entry => {
    if (filterServiceType !== 'all' && String(entry.serviceTypeId) !== filterServiceType) return false;
    return true;
  });

  // Summary counts
  const summary = {
    waiting: entries.filter(entry => entry.status === 'WAITING').length,
    called: entries.filter(entry => entry.status === 'CALLED').length,
    inProgress: entries.filter(entry => entry.status === 'IN_PROGRESS').length,
    hold: entries.filter(entry => entry.status === 'HOLD').length,
  };

  const getEntryServiceName = (entry: QueueEntry) => {
    return entry.serviceTypeName || getServiceTypeName(entry.serviceTypeId);
  };

  // Get available actions for each status
  const getAvailableActions = (entry: QueueEntry) => {
    const actions: { status: QueueStatus; label: string; icon: typeof Bell; variant?: 'default' | 'destructive' | 'outline' }[] = [];
    
    switch (entry.status) {
      case 'WAITING':
        actions.push({ status: 'CALLED', label: '호출', icon: Bell, variant: 'default' });
        actions.push({ status: 'CANCELED', label: '취소', icon: XCircle, variant: 'destructive' });
        break;
      case 'CALLED':
        actions.push({ status: 'IN_PROGRESS', label: '시작', icon: PlayCircle, variant: 'default' });
        actions.push({ status: 'HOLD', label: '보류', icon: PauseCircle, variant: 'outline' });
        break;
      case 'IN_PROGRESS':
        actions.push({ status: 'COMPLETED', label: '완료', icon: CheckCircle, variant: 'default' });
        break;
      case 'HOLD':
        actions.push({ status: 'CALLED', label: '재호출', icon: Bell, variant: 'default' });
        actions.push({ status: 'NO_SHOW', label: '미응답', icon: XCircle, variant: 'destructive' });
        actions.push({ status: 'CANCELED', label: '취소', icon: XCircle, variant: 'destructive' });
        break;
    }
    
    return actions;
  };

  const columns: Column<QueueEntry>[] = [
    {
      key: 'ticketNumber',
      header: '대기번호',
      cell: (entry) => (
        <span className={cn(
          'font-mono font-semibold',
          entry.status === 'CALLED' && 'text-primary animate-pulse'
        )}>
          {entry.ticketNumber}
        </span>
      ),
      className: 'w-24',
    },
    {
      key: 'visitorName',
      header: '방문자',
      cell: (entry) => (
        <div>
          <p className="font-medium">{entry.visitorNameMasked}</p>
          <p className="text-xs text-muted-foreground">{entry.visitorPhoneMasked}</p>
        </div>
      ),
    },
    {
      key: 'serviceType',
      header: '업무',
      cell: (entry) => getEntryServiceName(entry),
    },
    {
      key: 'visitType',
      header: '유형',
      cell: (entry) => (
        <span className={cn(
          'text-xs px-2 py-0.5 rounded',
          entry.visitType === 'RESERVED' 
            ? 'bg-blue-100 text-blue-700' 
            : 'bg-gray-100 text-gray-700'
        )}>
          {entry.visitType === 'RESERVED' ? '예약' : '현장'}
        </span>
      ),
      className: 'w-20',
    },
    {
      key: 'status',
      header: '상태',
      cell: (entry) => <StatusBadge status={entry.status} />,
      className: 'w-24',
    },
    {
      key: 'actions',
      header: '액션',
      cell: (entry) => {
        const actions = getAvailableActions(entry);
        if (actions.length === 0) return <span className="text-muted-foreground text-sm">-</span>;
        
        return (
          <div className="flex items-center gap-1">
            {actions.map(action => (
              <Button
                key={action.status}
                size="sm"
                variant={action.variant || 'outline'}
                onClick={() => handleStatusChange(entry.queueTicketId, action.status)}
                disabled={actionLoading === String(entry.queueTicketId)}
                className="h-7 text-xs"
              >
                <action.icon className="h-3 w-3 mr-1" />
                {action.label}
              </Button>
            ))}
          </div>
        );
      },
      className: 'w-48',
    },
  ];

  return (
    <div>
      <PageHeader 
        title="대기열 관리" 
        description="현재 대기 중인 방문자를 관리합니다"
        actions={
          <Button 
            variant="outline" 
            onClick={() => loadData(true)}
            disabled={isRefreshing}
          >
            <RefreshCw className={cn('h-4 w-4 mr-2', isRefreshing && 'animate-spin')} />
            새로고침
          </Button>
        }
      />

      {loadState === 'loading' && <LoadingState />}
      
      {loadState === 'error' && <ErrorState onRetry={() => loadData()} />}

      {loadState === 'success' && (
        <>
          {/* Summary Cards */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
            <MetricCard
              title="대기 중"
              value={summary.waiting}
              icon={Users}
            />
            <MetricCard
              title="호출 중"
              value={summary.called}
              icon={Bell}
            />
            <MetricCard
              title="처리 중"
              value={summary.inProgress}
              icon={PlayCircle}
            />
            <MetricCard
              title="보류"
              value={summary.hold}
              icon={PauseCircle}
            />
          </div>

          {/* Filters */}
          <Card className="mt-6">
            <CardHeader className="pb-3">
              <CardTitle className="text-base flex items-center gap-2">
                <Filter className="h-4 w-4" />
                조회 범위
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-col gap-4">
                <Tabs value={queueView} onValueChange={(value) => setQueueView(value as QueueView)}>
                  <TabsList className="grid w-full grid-cols-2 sm:grid-cols-5">
                    <TabsTrigger value="active">진행 중</TabsTrigger>
                    <TabsTrigger value="completed">완료</TabsTrigger>
                    <TabsTrigger value="canceled">취소</TabsTrigger>
                    <TabsTrigger value="no-show">미응답</TabsTrigger>
                    <TabsTrigger value="all">전체</TabsTrigger>
                  </TabsList>
                </Tabs>
                <div className="w-full sm:w-48">
                  <Select value={filterServiceType} onValueChange={setFilterServiceType}>
                    <SelectTrigger>
                      <SelectValue placeholder="업무 유형" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">전체 업무</SelectItem>
                      {serviceTypes.map(st => (
                        <SelectItem key={st.serviceTypeId} value={String(st.serviceTypeId)}>{st.name}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Queue Table */}
          <div className="mt-6">
            <DataTable
              columns={columns}
              data={filteredEntries}
              keyExtractor={(entry) => String(entry.queueTicketId)}
              emptyMessage={getEmptyMessage(queueView)}
            />
          </div>
        </>
      )}
    </div>
  );
}

async function loadQueueEntries(view: QueueView): Promise<QueueEntry[]> {
  switch (view) {
    case 'active':
      return getQueueEntries();
    case 'completed':
      return getQueueEntries({ status: 'COMPLETED' });
    case 'canceled':
      return getQueueEntries({ status: 'CANCELED' });
    case 'no-show':
      return getQueueEntries({ status: 'NO_SHOW' });
    case 'all': {
      const results = await Promise.all(
        ALL_QUEUE_STATUSES.map(status => getQueueEntries({ status })),
      );
      return dedupeQueueEntries(results.flat());
    }
  }
}

function dedupeQueueEntries(entries: QueueEntry[]): QueueEntry[] {
  return Array.from(
    new Map(entries.map(entry => [entry.queueTicketId, entry])).values(),
  );
}

function isEntryVisibleInView(entry: QueueEntry, view: QueueView): boolean {
  switch (view) {
    case 'active':
      return ACTIVE_QUEUE_STATUSES.includes(entry.status);
    case 'completed':
      return entry.status === 'COMPLETED';
    case 'canceled':
      return entry.status === 'CANCELED';
    case 'no-show':
      return entry.status === 'NO_SHOW';
    case 'all':
      return true;
  }
}

function getEmptyMessage(view: QueueView): string {
  switch (view) {
    case 'active':
      return '진행 중인 방문자가 없습니다.';
    case 'completed':
      return '완료된 방문자가 없습니다.';
    case 'canceled':
      return '취소된 방문자가 없습니다.';
    case 'no-show':
      return '미응답 방문자가 없습니다.';
    case 'all':
      return '대기열 내역이 없습니다.';
  }
}
