'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Loader2 } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import {
  AUTO_LOGIN_STORAGE_KEY,
  setAuthTokens,
} from '@/src/lib/api-client';
import type { UserRole } from '@/src/lib/mock-data';
import { toast } from 'sonner';

const roleRedirects: Record<UserRole, string> = {
  CITIZEN: '/citizen/reservations/new',
  STAFF: '/staff/check-in',
  ADMIN: '/admin/dashboard',
};

export default function SocialLoginCallbackPage() {
  const router = useRouter();
  const [message, setMessage] = useState('소셜 로그인 결과를 확인하고 있습니다.');

  useEffect(() => {
    const params = new URLSearchParams(window.location.hash.replace(/^#/, ''));
    const error = params.get('error');
    if (error) {
      toast.error(error);
      setMessage(error);
      router.replace('/login');
      return;
    }

    const accessToken = params.get('accessToken');
    const refreshToken = params.get('refreshToken');
    const role = params.get('role') as UserRole | null;
    if (!accessToken || !refreshToken || !role || !roleRedirects[role]) {
      toast.error('소셜 로그인 응답을 확인할 수 없습니다.');
      setMessage('소셜 로그인 응답을 확인할 수 없습니다.');
      router.replace('/login');
      return;
    }

    const rememberLogin = window.localStorage.getItem(AUTO_LOGIN_STORAGE_KEY) === 'true';
    setAuthTokens(accessToken, refreshToken, rememberLogin);
    toast.success('로그인 성공');
    router.replace(roleRedirects[role]);
  }, [router]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md">
        <CardContent className="flex flex-col items-center justify-center gap-4 py-10 text-center">
          <Loader2 className="h-7 w-7 animate-spin text-primary" />
          <div className="space-y-1">
            <p className="text-sm font-medium">소셜 로그인 처리 중</p>
            <p className="text-xs text-muted-foreground">{message}</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
