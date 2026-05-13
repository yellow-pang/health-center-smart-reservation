'use client';

import { AppSidebar } from './app-sidebar';
import { useAuth } from '@/src/contexts/auth-context';
import { redirect } from 'next/navigation';
import { useEffect } from 'react';
import type { ReactNode } from 'react';

interface AppLayoutProps {
  children: ReactNode;
}

export function AppLayout({ children }: AppLayoutProps) {
  const { user, isLoading } = useAuth();

  useEffect(() => {
    if (!isLoading && !user) {
      redirect('/login');
    }
  }, [isLoading, user]);

  if (isLoading || !user) {
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
