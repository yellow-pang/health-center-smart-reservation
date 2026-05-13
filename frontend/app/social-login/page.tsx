'use client';

import Link from 'next/link';
import { ArrowLeft, Building2, KeyRound, ShieldCheck } from 'lucide-react';
import { toast } from 'sonner';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

const providers = [
  { id: 'kakao', label: '카카오로 계속하기', description: '휴대폰 기반 간편 인증' },
  { id: 'naver', label: '네이버로 계속하기', description: '이메일 기반 간편 인증' },
  { id: 'google', label: 'Google로 계속하기', description: '외부 계정 기반 인증' },
];

export default function SocialLoginPage() {
  const handleProviderClick = (label: string) => {
    toast.info(`${label} 연동은 후속 API 작업에서 연결합니다`);
  };

  return (
    <main className="min-h-screen bg-background p-4">
      <div className="mx-auto flex min-h-[calc(100vh-2rem)] w-full max-w-md flex-col justify-center">
        <Button asChild variant="ghost" className="mb-4 w-fit px-2">
          <Link href="/login">
            <ArrowLeft className="h-4 w-4" />
            로그인으로 돌아가기
          </Link>
        </Button>

        <div className="mb-8 flex flex-col items-center">
          <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-xl bg-primary">
            <Building2 className="h-8 w-8 text-primary-foreground" />
          </div>
          <h1 className="text-xl font-bold text-foreground">보건소 스마트 예약 시스템</h1>
          <p className="mt-1 text-sm text-muted-foreground">간편 로그인</p>
        </div>

        <Card>
          <CardHeader className="space-y-1">
            <CardTitle className="flex items-center gap-2 text-lg">
              <KeyRound className="h-5 w-5" />
              소셜 로그인
            </CardTitle>
            <CardDescription>사용할 인증 방식을 선택하세요</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <Alert>
              <ShieldCheck className="h-4 w-4" />
              <AlertTitle>Mock 인증 화면</AlertTitle>
              <AlertDescription>
                실제 소셜 인증 공급자 연동은 아직 연결하지 않았습니다.
              </AlertDescription>
            </Alert>

            <div className="space-y-2">
              {providers.map(provider => (
                <Button
                  key={provider.id}
                  type="button"
                  variant="outline"
                  className="h-auto w-full justify-start py-3"
                  onClick={() => handleProviderClick(provider.label)}
                >
                  <div className="text-left">
                    <p className="font-medium">{provider.label}</p>
                    <p className="text-xs text-muted-foreground">{provider.description}</p>
                  </div>
                </Button>
              ))}
            </div>

            <Button asChild className="w-full">
              <Link href="/login">이메일로 로그인</Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    </main>
  );
}
