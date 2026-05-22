'use client';

import Link from 'next/link';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { AlertTriangle, RefreshCw, X } from 'lucide-react';
import { toast } from 'sonner';

import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { getQueueEntries } from '@/src/lib/staff-api';
import type { QueueEntry, QueueStatus } from '@/src/lib/mock-data';

const CLOSING_ALERT_HOUR = 17;
const CLOSING_ALERT_MINUTE = 30;
const REFRESH_INTERVAL_MS = 5 * 60 * 1000;
const ACTIVE_STATUSES: QueueStatus[] = ['WAITING', 'CALLED', 'HOLD', 'IN_PROGRESS'];

type LoadState = 'idle' | 'loading' | 'success' | 'error';

export function AdminQueueClosingAlert() {
  const [now, setNow] = useState(() => new Date());
  const [isPageVisible, setIsPageVisible] = useState(() => {
    if (typeof document === 'undefined') {
      return true;
    }

    return document.visibilityState === 'visible';
  });
  const today = useMemo(() => getTodayDate(now), [now]);
  const dismissedKey = `queue-closing-alert-dismissed:${today}`;
  const [entries, setEntries] = useState<QueueEntry[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('idle');
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isDismissed, setIsDismissed] = useState(() => {
    if (typeof window === 'undefined') {
      return false;
    }

    return window.localStorage.getItem(dismissedKey) === 'true';
  });
  const [hasShownToast, setHasShownToast] = useState(false);

  const shouldCheck = useMemo(() => isAfterClosingAlertTime(now), [now]);

  useEffect(() => {
    setIsDismissed(window.localStorage.getItem(dismissedKey) === 'true');
  }, [dismissedKey]);

  useEffect(() => {
    const handleVisibilityChange = () => {
      const visible = document.visibilityState === 'visible';
      setIsPageVisible(visible);

      if (visible) {
        setNow(new Date());
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => document.removeEventListener('visibilitychange', handleVisibilityChange);
  }, []);

  useEffect(() => {
    const handleStorageChange = (event: StorageEvent) => {
      if (event.key === dismissedKey) {
        setIsDismissed(event.newValue === 'true');
      }
    };

    window.addEventListener('storage', handleStorageChange);

    return () => window.removeEventListener('storage', handleStorageChange);
  }, [dismissedKey]);

  useEffect(() => {
    if (shouldCheck) {
      return undefined;
    }

    const delayMs = Math.max(getClosingAlertTime(now).getTime() - now.getTime(), 0);
    const timer = window.setTimeout(() => {
      setNow(new Date());
    }, delayMs);

    return () => window.clearTimeout(timer);
  }, [now, shouldCheck]);

  const loadPendingEntries = useCallback(async (showLoading = false) => {
    if (!shouldCheck || isDismissed || !isPageVisible) {
      return;
    }

    if (showLoading) {
      setIsRefreshing(true);
      setLoadState((currentLoadState) => {
        if (currentLoadState === 'idle') {
          return 'loading';
        }

        return currentLoadState;
      });
    }

    try {
      const queueEntries = await getQueueEntries({ date: today });
      setEntries(queueEntries.filter((entry) => ACTIVE_STATUSES.includes(entry.status)));
      setLoadState('success');
    } catch {
      setLoadState('error');
    } finally {
      setIsRefreshing(false);
    }
  }, [isDismissed, isPageVisible, shouldCheck, today]);

  useEffect(() => {
    loadPendingEntries(true);

    if (!shouldCheck || isDismissed || !isPageVisible) {
      return undefined;
    }

    const timer = window.setInterval(() => {
      loadPendingEntries();
    }, REFRESH_INTERVAL_MS);

    return () => window.clearInterval(timer);
  }, [isDismissed, isPageVisible, loadPendingEntries, shouldCheck]);

  useEffect(() => {
    if (hasShownToast || isDismissed || loadState !== 'success' || entries.length === 0) {
      return;
    }

    toast.warning(`오늘 미처리 대기표 ${entries.length}건이 남아 있습니다.`);
    setHasShownToast(true);
  }, [entries.length, hasShownToast, isDismissed, loadState]);

  const handleDismiss = () => {
    window.localStorage.setItem(dismissedKey, 'true');
    setIsDismissed(true);
  };

  if (!shouldCheck || isDismissed || loadState !== 'success' || entries.length === 0) {
    return null;
  }

  return (
    <Alert className="mb-4 border-amber-300 bg-amber-50 text-amber-950">
      <AlertTriangle className="h-4 w-4" />
      <AlertTitle className="flex min-w-0 flex-wrap items-center gap-2 pr-8">
        <span>마감 전 미처리 대기표가 남아 있습니다</span>
        <span className="rounded bg-amber-100 px-2 py-0.5 text-xs font-semibold text-amber-900">
          {entries.length}건
        </span>
      </AlertTitle>
      <AlertDescription>
        <div className="flex w-full flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <p>
            오늘 {formatClosingTime()} 이후에도 진행 중인 대기표가 있습니다. 대기 마감 관리에서 대상 목록을 확인해 주세요.
          </p>
          <div className="flex shrink-0 items-center gap-2">
            <Button
              asChild
              size="sm"
              className="bg-amber-900 text-amber-50 hover:bg-amber-800"
            >
              <Link href="/admin/queue-closing">마감 관리</Link>
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className="h-8 w-8 text-amber-900 hover:bg-amber-100"
              onClick={() => loadPendingEntries(true)}
              aria-label="마감 알림 새로고침"
              disabled={isRefreshing}
            >
              <RefreshCw className={cn('h-4 w-4', isRefreshing && 'animate-spin')} />
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className="h-8 w-8 text-amber-900 hover:bg-amber-100"
              onClick={handleDismiss}
              aria-label="오늘 마감 알림 숨기기"
            >
              <X className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </AlertDescription>
    </Alert>
  );
}

function getTodayDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function isAfterClosingAlertTime(now: Date): boolean {
  return now >= getClosingAlertTime(now);
}

function getClosingAlertTime(now: Date): Date {
  const closingTime = new Date(now);
  closingTime.setHours(CLOSING_ALERT_HOUR, CLOSING_ALERT_MINUTE, 0, 0);
  return closingTime;
}

function formatClosingTime(): string {
  return `${String(CLOSING_ALERT_HOUR).padStart(2, '0')}:${String(CLOSING_ALERT_MINUTE).padStart(2, '0')}`;
}
