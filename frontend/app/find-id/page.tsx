'use client';

import { useState } from 'react';
import Link from 'next/link';
import { ArrowLeft, Building2, CheckCircle2, IdCard, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

export default function FindIdPage() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isFound, setIsFound] = useState(false);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setIsSubmitting(true);

    await new Promise(resolve => setTimeout(resolve, 500));

    setIsSubmitting(false);
    setIsFound(true);
    toast.success('아이디 찾기 결과가 준비되었습니다');
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
          <p className="mt-1 text-sm text-muted-foreground">아이디 찾기</p>
        </div>

        <Card>
          <CardHeader className="space-y-1">
            <CardTitle className="flex items-center gap-2 text-lg">
              <IdCard className="h-5 w-5" />
              아이디 찾기
            </CardTitle>
            <CardDescription>가입 시 입력한 이름과 휴대폰 번호를 입력하세요</CardDescription>
          </CardHeader>
          <CardContent>
            {isFound ? (
              <div className="space-y-4">
                <Alert>
                  <CheckCircle2 className="h-4 w-4" />
                  <AlertTitle>가입 이메일 예시</AlertTitle>
                  <AlertDescription>
                    citizen@example.com. 실제 아이디 조회 API는 후속 작업에서 연결합니다.
                  </AlertDescription>
                </Alert>
                <Button asChild className="w-full">
                  <Link href="/login">로그인 화면으로 이동</Link>
                </Button>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="name">이름</Label>
                  <Input id="name" name="name" placeholder="홍길동" required disabled={isSubmitting} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="phone">휴대폰 번호</Label>
                  <Input id="phone" name="phone" type="tel" placeholder="010-1234-5678" required disabled={isSubmitting} />
                </div>
                <Button type="submit" className="w-full" disabled={isSubmitting}>
                  {isSubmitting && <Loader2 className="h-4 w-4 animate-spin" />}
                  아이디 찾기
                </Button>
              </form>
            )}
          </CardContent>
        </Card>
      </div>
    </main>
  );
}
