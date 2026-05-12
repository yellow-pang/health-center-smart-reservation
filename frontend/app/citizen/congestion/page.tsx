'use client';

import { useState, useEffect } from 'react';
import { RefreshCw, Users, Clock } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { PageHeader } from '@/src/components/common/page-header';
import { StatusBadge } from '@/src/components/common/status-badge';
import { LoadingState } from '@/src/components/common/loading-state';
import { ErrorState } from '@/src/components/common/error-state';
import { getCongestionInfo } from '@/src/lib/mock-services';
import type { CongestionInfo } from '@/src/lib/mock-data';
import { cn } from '@/lib/utils';

type LoadState = 'loading' | 'success' | 'error';

export default function CongestionPage() {
  const [congestionData, setCongestionData] = useState<CongestionInfo[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [lastUpdated, setLastUpdated] = useState<Date>(new Date());

  const loadCongestion = async (showRefreshing = false) => {
    if (showRefreshing) {
      setIsRefreshing(true);
    } else {
      setLoadState('loading');
    }
    
    try {
      const data = await getCongestionInfo();
      setCongestionData(data);
      setLastUpdated(new Date());
      setLoadState('success');
    } catch {
      setLoadState('error');
    } finally {
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    loadCongestion();
    
    // Auto refresh every 30 seconds
    const interval = setInterval(() => {
      loadCongestion(true);
    }, 30000);
    
    return () => clearInterval(interval);
  }, []);

  const getLevelColor = (level: CongestionInfo['level']) => {
    switch (level) {
      case 'LOW':
        return 'bg-green-50 border-green-200';
      case 'MEDIUM':
        return 'bg-amber-50 border-amber-200';
      case 'HIGH':
        return 'bg-red-50 border-red-200';
    }
  };

  const getLevelIcon = (level: CongestionInfo['level']) => {
    switch (level) {
      case 'LOW':
        return 'text-green-600';
      case 'MEDIUM':
        return 'text-amber-600';
      case 'HIGH':
        return 'text-red-600';
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <PageHeader 
        title="현재 혼잡도" 
        description="업무별 대기 현황을 실시간으로 확인하세요"
        actions={
          <Button 
            variant="outline" 
            size="sm"
            onClick={() => loadCongestion(true)}
            disabled={isRefreshing}
          >
            <RefreshCw className={cn('h-4 w-4 mr-2', isRefreshing && 'animate-spin')} />
            새로고침
          </Button>
        }
      />

      <p className="text-xs text-muted-foreground mt-2">
        마지막 업데이트: {lastUpdated.toLocaleTimeString('ko-KR')}
      </p>

      <div className="mt-6">
        {loadState === 'loading' && <LoadingState message="혼잡도 정보를 불러오는 중..." />}
        
        {loadState === 'error' && (
          <ErrorState onRetry={() => loadCongestion()} />
        )}

        {loadState === 'success' && (
          <div className="space-y-3">
            {congestionData.map((info) => (
              <Card 
                key={info.serviceTypeId} 
                className={cn('transition-colors', getLevelColor(info.level))}
              >
                <CardContent className="p-4">
                  <div className="flex items-center justify-between">
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2 mb-2">
                        <h3 className="font-medium">{info.serviceTypeName}</h3>
                        <StatusBadge status={info.level} />
                      </div>
                      <div className="flex items-center gap-4 text-sm">
                        <div className="flex items-center gap-1.5">
                          <Users className={cn('h-4 w-4', getLevelIcon(info.level))} />
                          <span>
                            대기 <span className="font-semibold">{info.waitingCount}</span>명
                          </span>
                        </div>
                        <div className="flex items-center gap-1.5">
                          <Clock className={cn('h-4 w-4', getLevelIcon(info.level))} />
                          <span>
                            예상 <span className="font-semibold">{info.estimatedWaitMinutes}</span>분
                          </span>
                        </div>
                      </div>
                    </div>
                    <div className={cn(
                      'flex h-12 w-12 items-center justify-center rounded-full shrink-0',
                      info.level === 'LOW' && 'bg-green-100',
                      info.level === 'MEDIUM' && 'bg-amber-100',
                      info.level === 'HIGH' && 'bg-red-100',
                    )}>
                      <span className={cn(
                        'text-lg font-bold',
                        getLevelIcon(info.level)
                      )}>
                        {info.waitingCount}
                      </span>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}

        {/* Legend */}
        <div className="mt-6 flex items-center justify-center gap-6 text-sm">
          <div className="flex items-center gap-2">
            <div className="h-3 w-3 rounded-full bg-green-500" />
            <span className="text-muted-foreground">여유</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="h-3 w-3 rounded-full bg-amber-500" />
            <span className="text-muted-foreground">보통</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="h-3 w-3 rounded-full bg-red-500" />
            <span className="text-muted-foreground">혼잡</span>
          </div>
        </div>
      </div>
    </div>
  );
}
