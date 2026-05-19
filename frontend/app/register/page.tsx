'use client';

import { useState } from 'react';
import Link from 'next/link';
import { ArrowLeft, Building2, CheckCircle2, Loader2, UserPlus } from 'lucide-react';
import { toast } from 'sonner';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { normalizePhoneNumberInput } from '@/src/lib/phone-number';

export default function RegisterPage() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isComplete, setIsComplete] = useState(false);
  const [phone, setPhone] = useState('');

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setIsSubmitting(true);

    await new Promise(resolve => setTimeout(resolve, 500));

    setIsSubmitting(false);
    setIsComplete(true);
    toast.success('회원가입 요청이 접수되었습니다');
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
          <p className="mt-1 text-sm text-muted-foreground">시민 회원가입</p>
        </div>

        <Card>
          <CardHeader className="space-y-1">
            <CardTitle className="flex items-center gap-2 text-lg">
              <UserPlus className="h-5 w-5" />
              회원가입
            </CardTitle>
            <CardDescription>예약 신청에 사용할 기본 정보를 입력하세요</CardDescription>
          </CardHeader>
          <CardContent>
            {isComplete ? (
              <div className="space-y-4">
                <Alert>
                  <CheckCircle2 className="h-4 w-4" />
                  <AlertTitle>가입 요청 접수</AlertTitle>
                  <AlertDescription>
                    현재 화면은 v0 MVP 보강용 mock 흐름입니다. 실제 가입 API 연동은 후속 작업에서 진행합니다.
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
                  <Input
                    id="phone"
                    name="phone"
                    type="tel"
                    inputMode="numeric"
                    placeholder="01012345678"
                    value={phone}
                    onChange={(event) => setPhone(normalizePhoneNumberInput(event.target.value))}
                    required
                    disabled={isSubmitting}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email">이메일</Label>
                  <Input id="email" name="email" type="email" placeholder="citizen@example.com" required disabled={isSubmitting} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="password">비밀번호</Label>
                  <Input id="password" name="password" type="password" placeholder="8자 이상" required disabled={isSubmitting} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="passwordConfirm">비밀번호 확인</Label>
                  <Input id="passwordConfirm" name="passwordConfirm" type="password" placeholder="비밀번호 재입력" required disabled={isSubmitting} />
                </div>
                <Button type="submit" className="w-full" disabled={isSubmitting}>
                  {isSubmitting && <Loader2 className="h-4 w-4 animate-spin" />}
                  가입 요청
                </Button>
              </form>
            )}
          </CardContent>
        </Card>
      </div>
    </main>
  );
}
