'use client';

import { AppSidebar } from './app-sidebar';
import { useAuth } from '@/src/contexts/auth-context';
import { canAccessRole } from '@/src/lib/route-access';
import type { UserRole } from '@/src/lib/mock-data';
import { usePathname, useRouter } from 'next/navigation';
import { useEffect } from 'react';
import type { ReactNode } from 'react';

interface AppLayoutProps {
  children: ReactNode;
  allowedRoles?: UserRole[];
}

export function AppLayout({ children, allowedRoles }: AppLayoutProps) {
  const { user, isLoading } = useAuth();
  const pathname = usePathname();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !user) {
      router.replace('/login');
      return;
    }

    if (!isLoading && user && !canAccessRole(user.role, allowedRoles)) {
      const from = encodeURIComponent(pathname);
      router.replace(`/access-denied?from=${from}`);
    }
  }, [allowedRoles, isLoading, pathname, router, user]);

  if (isLoading || !user || !canAccessRole(user.role, allowedRoles)) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background">
      <AppSidebar />
      <main className="lg:pl-64 pt-14 lg:pt-0">
        <div className="p-4 sm:p-6 lg:p-8">
          {children}
        </div>
      </main>
    </div>
  );
}
