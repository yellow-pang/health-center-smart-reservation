'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Building2, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { useAuth } from '@/src/contexts/auth-context';
import type { UserRole } from '@/src/lib/mock-data';
import { toast } from 'sonner';

const testAccounts: { role: UserRole; label: string; description: string }[] = [
  { role: 'CITIZEN', label: '시민으로 로그인', description: '예약 신청, 조회, 취소' },
  { role: 'STAFF', label: '직원으로 로그인', description: '체크인, 현장접수, 대기열 관리' },
  { role: 'ADMIN', label: '관리자로 로그인', description: '대시보드, 설정 관리' },
];

const roleRedirects: Record<UserRole, string> = {
  CITIZEN: '/citizen/reservations/new',
  STAFF: '/staff/check-in',
  ADMIN: '/admin/dashboard',
};

export default function LoginPage() {
  const router = useRouter();
  const { login, loginWithRole, isLoading } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const result = await login(email, password);
    if (result.success) {
      toast.success('로그인 성공');
      // Determine redirect based on email domain or role
      const user = testAccounts.find(a => email.includes(a.role.toLowerCase()));
      router.push(user ? roleRedirects[user.role] : '/citizen/reservations/new');
    } else {
      toast.error(result.error || '로그인 실패');
    }
  };

  const handleTestLogin = async (role: UserRole) => {
    const result = await loginWithRole(role);
    if (result.success) {
      toast.success('로그인 성공');
      router.push(roleRedirects[role]);
    } else {
      toast.error(result.error || '로그인 실패');
    }
  };

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

        <Card>
          <CardHeader className="space-y-1">
            <CardTitle className="text-lg">로그인</CardTitle>
            <CardDescription>
              이메일과 비밀번호를 입력하거나 테스트 계정을 선택하세요
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email">이메일</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="email@example.com"
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
                또는 테스트 계정 선택
              </span>
            </div>

            <div className="space-y-2">
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
              {testAccounts.map((account) => (
                <Button
                  key={account.role}
                  variant="outline"
                  className="w-full justify-start h-auto py-3"
                  onClick={() => handleTestLogin(account.role)}
                  disabled={isLoading}
                >
                  <div className="text-left">
                    <p className="font-medium">{account.label}</p>
                    <p className="text-xs text-muted-foreground">{account.description}</p>
                  </div>
                </Button>
              ))}
            </div>
          </CardContent>
        </Card>

        <p className="text-center text-xs text-muted-foreground mt-6">
          이 시스템은 MVP 데모 버전입니다. 실제 인증은 구현되어 있지 않습니다.
        </p>
      </div>
    </div>
  );
}
