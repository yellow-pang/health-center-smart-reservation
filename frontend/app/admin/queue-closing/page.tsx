'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { AlertTriangle, CalendarDays, ClipboardCheck, RefreshCw, UserX } from 'lucide-react';
import { toast } from 'sonner';

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
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { cn } from '@/lib/utils';
import { DataTable, type Column } from '@/src/components/common/data-table';
import { ErrorState } from '@/src/components/common/error-state';
import { LoadingState } from '@/src/components/common/loading-state';
import { MetricCard } from '@/src/components/common/metric-card';
import { PageHeader } from '@/src/components/common/page-header';
import { StatusBadge } from '@/src/components/common/status-badge';
import { closePendingQueueTickets, getQueueEntries } from '@/src/lib/staff-api';
import type { QueueEntry } from '@/src/lib/mock-data';

type LoadState = 'loading' | 'success' | 'error';

export default function QueueClosingPage() {
  const [selectedDate, setSelectedDate] = useState(() => getTodayDate());
  const [entries, setEntries] = useState<QueueEntry[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isClosing, setIsClosing] = useState(false);
  const [dialogOpen, setDialogOpen] = useState(false);

  const loadData = useCallback(async (showRefreshing = false) => {
    if (showRefreshing) {
      setIsRefreshing(true);
    } else {
      setLoadState('loading');
    }

    try {
      const queueData = await getQueueEntries({ date: selectedDate });
      setEntries(queueData);
      setLoadState('success');
    } catch {
      setLoadState('error');
    } finally {
      setIsRefreshing(false);
    }
  }, [selectedDate]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const summary = useMemo(() => ({
    total: entries.length,
    waiting: entries.filter((entry) => entry.status === 'WAITING').length,
    called: entries.filter((entry) => entry.status === 'CALLED').length,
    inProgress: entries.filter((entry) => entry.status === 'IN_PROGRESS').length,
    hold: entries.filter((entry) => entry.status === 'HOLD').length,
  }), [entries]);

  const handleClosePending = async () => {
    setIsClosing(true);
    try {
      const result = await closePendingQueueTickets(selectedDate);
      toast.success(`${result.date} 미처리 대기표 ${result.closedCount}건을 미응답 처리했습니다.`);
      setDialogOpen(false);
      await loadData(true);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : '마감 처리에 실패했습니다.');
    } finally {
      setIsClosing(false);
    }
  };

  const columns: Column<QueueEntry>[] = [
    {
      key: 'ticketNumber',
      header: '대기번호',
      cell: (entry) => <span className="font-mono font-semibold">{entry.ticketNumber}</span>,
      className: 'w-24',
    },
    {
      key: 'visitor',
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
      cell: (entry) => entry.serviceTypeName || '-',
    },
    {
      key: 'visitType',
      header: '유형',
      cell: (entry) => (
        <span className={cn(
          'text-xs px-2 py-0.5 rounded',
          entry.visitType === 'RESERVED'
            ? 'bg-blue-100 text-blue-700'
            : 'bg-gray-100 text-gray-700',
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
      key: 'issuedAt',
      header: '접수 시각',
      cell: (entry) => formatTime(entry.issuedAt || entry.createdAt),
      className: 'w-28',
    },
  ];

  return (
    <div>
      <PageHeader
        title="대기 마감 관리"
        description={`${selectedDate} 미처리 대기표를 확인하고 마감합니다`}
        actions={
          <div className="flex w-full flex-col gap-2 sm:w-auto sm:flex-row">
            <div className="relative">
              <CalendarDays className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                aria-label="마감 대상 날짜"
                type="date"
                value={selectedDate}
                onChange={(event) => setSelectedDate(event.target.value)}
                className="h-10 pl-9 sm:w-40"
                disabled={isClosing}
              />
            </div>
            <Button
              variant="outline"
              onClick={() => loadData(true)}
              disabled={isRefreshing || isClosing}
            >
              <RefreshCw className={cn('h-4 w-4 mr-2', isRefreshing && 'animate-spin')} />
              새로고침
            </Button>
          </div>
        }
      />

      {loadState === 'loading' && <LoadingState />}

      {loadState === 'error' && <ErrorState onRetry={() => loadData()} />}

      {loadState === 'success' && (
        <>
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
            <MetricCard title="마감 대상" value={summary.total} subtitle="건" icon={ClipboardCheck} />
            <MetricCard title="호출 전" value={summary.waiting} subtitle="건" icon={AlertTriangle} />
            <MetricCard title="호출/보류" value={summary.called + summary.hold} subtitle="건" icon={RefreshCw} />
            <MetricCard title="처리 중" value={summary.inProgress} subtitle="건" icon={UserX} />
          </div>

          <Card className="mt-6">
            <CardHeader>
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <CardTitle className="text-base">마감 전 확인</CardTitle>
                  <CardDescription>
                    목록의 대기표는 마감 실행 시 모두 미응답으로 변경됩니다.
                  </CardDescription>
                </div>
                <AlertDialog open={dialogOpen} onOpenChange={setDialogOpen}>
                  <AlertDialogTrigger asChild>
                    <Button disabled={summary.total === 0 || isClosing}>
                      <ClipboardCheck className="h-4 w-4 mr-2" />
                      마감 처리
                    </Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>{selectedDate} 대기표를 마감할까요?</AlertDialogTitle>
                      <AlertDialogDescription>
                        미처리 대기표 {summary.total}건이 미응답 상태로 변경됩니다. 처리 후에는 대기열 진행 목록에서 제외됩니다.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel disabled={isClosing}>취소</AlertDialogCancel>
                      <AlertDialogAction onClick={handleClosePending} disabled={isClosing}>
                        {isClosing ? '처리 중' : '마감 실행'}
                      </AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
              </div>
            </CardHeader>
            <CardContent>
              <DataTable
                columns={columns}
                data={entries}
                keyExtractor={(entry) => String(entry.queueTicketId)}
                emptyMessage="마감할 미처리 대기표가 없습니다."
              />
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
}

function getTodayDate(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function formatTime(value?: string): string {
  if (!value) {
    return '-';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '-';
  }

  return date.toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
  });
}
