'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Building2, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Checkbox } from '@/components/ui/checkbox';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { useAuth } from '@/src/contexts/auth-context';
import type { UserRole } from '@/src/lib/mock-data';
import { toast } from 'sonner';

const REMEMBERED_LOGIN_EMAIL_KEY = 'healthcenter.rememberedLoginEmail';
const AUTO_LOGIN_CHECKED_KEY = 'healthcenter.autoLogin';

const roleRedirects: Record<UserRole, string> = {
  CITIZEN: '/citizen/reservations/new',
  STAFF: '/staff/check-in',
  ADMIN: '/admin/dashboard',
};

export default function LoginPage() {
  const router = useRouter();
  const { user, login, isLoading } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberEmail, setRememberEmail] = useState(false);
  const [autoLogin, setAutoLogin] = useState(false);

  useEffect(() => {
    const rememberedEmail = window.localStorage.getItem(REMEMBERED_LOGIN_EMAIL_KEY);
    if (rememberedEmail) {
      setEmail(rememberedEmail);
      setRememberEmail(true);
    }

    setAutoLogin(window.localStorage.getItem(AUTO_LOGIN_CHECKED_KEY) === 'true');
  }, []);

  useEffect(() => {
    if (!isLoading && user) {
      router.replace(roleRedirects[user.role]);
    }
  }, [isLoading, router, user]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const normalizedEmail = email.trim();
    const result = await login(normalizedEmail, password, autoLogin);
    if (result.success && result.user) {
      if (rememberEmail) {
        window.localStorage.setItem(REMEMBERED_LOGIN_EMAIL_KEY, normalizedEmail);
      } else {
        window.localStorage.removeItem(REMEMBERED_LOGIN_EMAIL_KEY);
      }
      if (autoLogin) {
        window.localStorage.setItem(AUTO_LOGIN_CHECKED_KEY, 'true');
      } else {
        window.localStorage.removeItem(AUTO_LOGIN_CHECKED_KEY);
      }
      toast.success('로그인 성공');
      router.push(roleRedirects[result.user.role]);
    } else {
      toast.error(result.error || '로그인 실패');
    }
  };

  const handleSocialLogin = (providerName: string) => {
    toast.info(`${providerName} 로그인은 준비 중입니다`);
  };

  const isCheckingSession = isLoading && !user;

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="flex flex-col items-center mb-8">
          <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-primary mb-4">
            <Building2 className="h-8 w-8 text-primary-foreground" />
          </div>
          <h1 className="text-xl font-bold text-foreground">보건소 스마트 예약 시스템</h1>
          <p className="text-sm text-muted-foreground mt-1">예약, 대기, 혼잡도 분석 통합 시스템</p>
        </div>

        {isCheckingSession ? (
          <Card>
            <CardContent className="flex flex-col items-center justify-center gap-4 py-10 text-center">
              <Loader2 className="h-7 w-7 animate-spin text-primary" />
              <div className="space-y-1">
                <p className="text-sm font-medium">로그인 상태 확인 중</p>
                <p className="text-xs text-muted-foreground">
                  저장된 로그인 정보를 확인하고 있습니다.
                </p>
              </div>
            </CardContent>
          </Card>
        ) : (
          <Card>
            <CardHeader className="space-y-1">
              <CardTitle className="text-lg">로그인</CardTitle>
              <CardDescription>
                이메일과 비밀번호를 입력하세요
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="email">이메일</Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="staff@test.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    disabled={isLoading}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="password">비밀번호</Label>
                  <Input
                    id="password"
                    type="password"
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    disabled={isLoading}
                  />
                </div>
                <div className="grid gap-3 sm:grid-cols-2">
                  <div className="flex items-center gap-2">
                    <Checkbox
                      id="remember-email"
                      checked={rememberEmail}
                      onCheckedChange={(checked) => setRememberEmail(checked === true)}
                      disabled={isLoading}
                    />
                    <Label htmlFor="remember-email" className="cursor-pointer text-sm font-normal">
                      아이디 기억
                    </Label>
                  </div>
                  <div className="flex items-center gap-2">
                    <Checkbox
                      id="auto-login"
                      checked={autoLogin}
                      onCheckedChange={(checked) => setAutoLogin(checked === true)}
                      disabled={isLoading}
                    />
                    <Label htmlFor="auto-login" className="cursor-pointer text-sm font-normal">
                      자동 로그인
                    </Label>
                  </div>
                </div>
                <Button type="submit" className="w-full" disabled={isLoading}>
                  {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  로그인
                </Button>
              </form>

              <div className="mt-4 flex flex-wrap items-center justify-center gap-x-4 gap-y-2 text-sm">
                <Link href="/register" className="font-medium text-primary hover:underline">
                  회원가입
                </Link>
                <Link href="/find-id" className="text-muted-foreground hover:text-foreground hover:underline">
                  아이디 찾기
                </Link>
                <Link href="/reset-password" className="text-muted-foreground hover:text-foreground hover:underline">
                  비밀번호 찾기
                </Link>
              </div>

              <div className="relative my-6">
                <Separator />
                <span className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 bg-card px-2 text-xs text-muted-foreground">
                  소셜 로그인
                </span>
              </div>

              <div className="space-y-2">
                <Button
                  type="button"
                  variant="outline"
                  className="h-11 w-full border-[#FEE500] bg-[#FEE500] text-[#191919] transition-all hover:border-[#191919]/20 hover:bg-[#FEE500] hover:text-[#191919] hover:shadow-md active:scale-[0.98] active:bg-[#FEE500]"
                  onClick={() => handleSocialLogin('카카오')}
                  disabled={isLoading}
                >
                  <svg viewBox="0 0 24 24" className="mr-2 h-5 w-5" fill="currentColor" aria-hidden="true">
                    <path d="M12 3c5.799 0 10.5 3.664 10.5 8.185 0 4.52-4.701 8.184-10.5 8.184-.58 0-1.158-.037-1.727-.11l-4.408 2.883c-.501.265-.678.236-.472-.413l.892-3.678C3.405 16.591 1.5 14.061 1.5 11.185 1.5 6.665 6.201 3 12 3z" />
                  </svg>
                  카카오로 시작하기
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  className="h-11 w-full border-[#03C75A] bg-[#03C75A] text-white transition-all hover:border-white/30 hover:bg-[#03C75A] hover:text-white hover:shadow-md active:scale-[0.98] active:bg-[#03C75A]"
                  onClick={() => handleSocialLogin('네이버')}
                  disabled={isLoading}
                >
                  <svg viewBox="0 0 24 24" className="mr-2 h-5 w-5" fill="currentColor" aria-hidden="true">
                    <path d="M16.273 12.845 7.376 0H0v24h7.727V11.155L16.624 24H24V0h-7.727v12.845z" />
                  </svg>
                  네이버로 시작하기
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  className="h-11 w-full border-gray-300 bg-white text-[#3c4043] transition-all hover:border-[#4285F4] hover:bg-white hover:text-[#3c4043] hover:shadow-md active:scale-[0.98] active:bg-white"
                  onClick={() => handleSocialLogin('구글')}
                  disabled={isLoading}
                >
                  <svg viewBox="0 0 24 24" className="mr-2 h-5 w-5" aria-hidden="true">
                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
                  </svg>
                  구글로 시작하기
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        <p className="text-center text-xs text-muted-foreground mt-6">
          테스트 계정 비밀번호는 password1234입니다.
        </p>
      </div>
    </div>
  );
}
