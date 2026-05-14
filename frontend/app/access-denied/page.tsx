'use client';

import Link from 'next/link';
import { useSearchParams } from 'next/navigation';
import { Suspense } from 'react';
import { ArrowLeft, LogIn, ShieldAlert } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { useAuth } from '@/src/contexts/auth-context';
import { LoadingState } from '@/src/components/common/loading-state';
import { getRoleHomePath, ROLE_LABELS } from '@/src/lib/route-access';

const routeLabels: Array<[string, string]> = [
  ['/admin', '관리자'],
  ['/staff', '직원'],
  ['/citizen', '시민'],
];

export default function AccessDeniedPage() {
  return (
    <Suspense fallback={<LoadingState message="접근 권한을 확인하는 중입니다." className="min-h-screen" />}>
      <AccessDeniedContent />
    </Suspense>
  );
}

function AccessDeniedContent() {
  const { user, isLoading } = useAuth();
  const searchParams = useSearchParams();
  const from = searchParams.get('from') || '';
  const requiredRoleLabel = routeLabels.find(([prefix]) => from.startsWith(prefix))?.[1];
  const homePath = getRoleHomePath(user?.role);

  if (isLoading) {
    return <LoadingState message="접근 권한을 확인하는 중입니다." className="min-h-screen" />;
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-background px-4 py-10">
      <Card className="w-full max-w-md border-border">
        <CardContent className="flex flex-col items-center gap-6 p-8 text-center">
          <div className="flex h-14 w-14 items-center justify-center rounded-full bg-destructive/10 text-destructive">
            <ShieldAlert className="h-7 w-7" aria-hidden="true" />
          </div>

          <div className="space-y-2">
            <h1 className="text-2xl font-semibold tracking-normal text-foreground">
              접근 권한이 없습니다
            </h1>
            <p className="text-sm leading-6 text-muted-foreground">
              {user
                ? `${ROLE_LABELS[user.role]} 계정으로는${requiredRoleLabel ? ` ${requiredRoleLabel} 화면에` : ' 이 화면에'} 접근할 수 없습니다.`
                : '로그인이 필요한 화면입니다.'}
            </p>
          </div>

          <div className="flex w-full flex-col gap-2 sm:flex-row">
            {user ? (
              <Button asChild className="flex-1 gap-2">
                <Link href={homePath}>
                  <ArrowLeft className="h-4 w-4" />
                  내 화면으로 이동
                </Link>
              </Button>
            ) : (
              <Button asChild className="flex-1 gap-2">
                <Link href="/login">
                  <LogIn className="h-4 w-4" />
                  로그인
                </Link>
              </Button>
            )}
          </div>
        </CardContent>
      </Card>
    </main>
  );
}
