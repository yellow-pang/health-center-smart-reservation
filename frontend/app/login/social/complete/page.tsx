'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  AUTO_LOGIN_STORAGE_KEY,
} from '@/src/lib/api-client';
import { completeSocialSignup } from '@/src/lib/social-auth-api';
import type { UserRole } from '@/src/lib/mock-data';
import { toast } from 'sonner';

const SOCIAL_COMPLETION_TOKEN_KEY = 'healthcenter.socialCompletionToken';
const SOCIAL_COMPLETION_PROVIDER_KEY = 'healthcenter.socialCompletionProvider';
const SOCIAL_COMPLETION_NAME_KEY = 'healthcenter.socialCompletionName';

const roleRedirects: Record<UserRole, string> = {
  CITIZEN: '/citizen/reservations/new',
  STAFF: '/staff/check-in',
  ADMIN: '/admin/dashboard',
};

const providerLabels: Record<string, string> = {
  kakao: '카카오',
  naver: '네이버',
  google: '구글',
};

export default function SocialSignupCompletePage() {
  const router = useRouter();
  const [completionToken, setCompletionToken] = useState('');
  const [provider, setProvider] = useState('');
  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const storedToken = window.sessionStorage.getItem(SOCIAL_COMPLETION_TOKEN_KEY);
    if (!storedToken) {
      toast.error('소셜 로그인 추가 정보 입력 시간이 만료되었습니다.');
      router.replace('/login');
      return;
    }

    setCompletionToken(storedToken);
    setProvider(window.sessionStorage.getItem(SOCIAL_COMPLETION_PROVIDER_KEY) || '');
    setName(window.sessionStorage.getItem(SOCIAL_COMPLETION_NAME_KEY) || '');
    setIsLoading(false);
  }, [router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const normalizedEmail = email.trim();
    const normalizedName = name.trim();
    if (!normalizedEmail || !normalizedName) {
      toast.error('이메일과 이름을 입력해 주세요.');
      return;
    }

    setIsSubmitting(true);
    const rememberLogin = window.localStorage.getItem(AUTO_LOGIN_STORAGE_KEY) === 'true';
    const result = await completeSocialSignup({
      completionToken,
      email: normalizedEmail,
      name: normalizedName,
      rememberLogin,
    });
    setIsSubmitting(false);

    if (result.success && result.user) {
      window.sessionStorage.removeItem(SOCIAL_COMPLETION_TOKEN_KEY);
      window.sessionStorage.removeItem(SOCIAL_COMPLETION_PROVIDER_KEY);
      window.sessionStorage.removeItem(SOCIAL_COMPLETION_NAME_KEY);
      toast.success('소셜 회원가입 완료');
      router.replace(roleRedirects[result.user.role]);
      return;
    }

    toast.error(result.error || '소셜 회원가입을 완료할 수 없습니다.');
  };

  const providerLabel = providerLabels[provider] || '소셜';

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background p-4">
        <Card className="w-full max-w-md">
          <CardContent className="flex flex-col items-center justify-center gap-4 py-10 text-center">
            <Loader2 className="h-7 w-7 animate-spin text-primary" />
            <p className="text-sm font-medium">추가 정보 입력 준비 중</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle className="text-lg">추가 정보 입력</CardTitle>
          <CardDescription>
            {providerLabel} 로그인을 완료하려면 이메일과 이름이 필요합니다.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="social-email">이메일</Label>
              <Input
                id="social-email"
                type="email"
                placeholder="email@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                disabled={isSubmitting}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="social-name">이름</Label>
              <Input
                id="social-name"
                type="text"
                placeholder="이름"
                value={name}
                onChange={(e) => setName(e.target.value)}
                disabled={isSubmitting}
                required
              />
            </div>
            <Button type="submit" className="w-full" disabled={isSubmitting}>
              {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              가입 완료
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
