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

              <div className="mt-6">
                <Button
                  asChild
                  variant="secondary"
                  className="w-full justify-start h-auto py-3"
                  disabled={isLoading}
                >
                  <Link href="/social-login">
                    <div className="text-left">
                      <p className="font-medium">소셜 로그인</p>
                      <p className="text-xs text-muted-foreground">간편 인증 진입 화면</p>
                    </div>
                  </Link>
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
