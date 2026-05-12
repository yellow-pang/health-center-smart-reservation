'use client';

import { AppLayout } from '@/src/components/layout/app-layout';
import type { ReactNode } from 'react';

export default function AdminLayout({ children }: { children: ReactNode }) {
  return <AppLayout>{children}</AppLayout>;
}
