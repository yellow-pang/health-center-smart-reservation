'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { 
  Calendar, 
  ClipboardList, 
  Activity, 
  UserCheck, 
  UserPlus, 
  Users, 
  LayoutDashboard,
  Settings,
  FileText,
  Clock,
  Building2,
  LogOut,
  Menu,
  X,
  Home,
  ClipboardCheck,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/src/contexts/auth-context';
import type { UserRole } from '@/src/lib/mock-data';
import { useState } from 'react';

interface NavItem {
  href: string;
  label: string;
  icon: typeof Calendar;
}

const navItems: Record<UserRole, NavItem[]> = {
  CITIZEN: [
    { href: '/citizen/reservations/new', label: '예약 신청', icon: Calendar },
    { href: '/citizen/reservations', label: '내 예약', icon: ClipboardList },
    { href: '/citizen/congestion', label: '현재 혼잡도', icon: Activity },
  ],
  STAFF: [
    { href: '/staff/check-in', label: '체크인', icon: UserCheck },
    { href: '/staff/walk-in', label: '현장 접수', icon: UserPlus },
    { href: '/staff/queues', label: '대기열 관리', icon: Users },
  ],
  ADMIN: [
    { href: '/admin/dashboard', label: '대시보드', icon: LayoutDashboard },
    { href: '/admin/service-types', label: '업무 유형 관리', icon: FileText },
    { href: '/admin/reservation-slots', label: '예약 슬롯 관리', icon: Clock },
    { href: '/admin/queue-closing', label: '대기 마감 관리', icon: ClipboardCheck },
    { href: '/admin/staff', label: '직원 관리', icon: Users },
    { href: '/admin/service-windows', label: '창구 관리', icon: Building2 },
  ],
};

const roleLabels: Record<UserRole, string> = {
  CITIZEN: '시민',
  STAFF: '직원',
  ADMIN: '관리자',
};

export function AppSidebar() {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const [isMobileOpen, setIsMobileOpen] = useState(false);

  if (!user) return null;

  const items = navItems[user.role];

  return (
    <>
      {/* Mobile Header */}
      <div className="fixed top-0 left-0 right-0 z-50 flex h-14 items-center justify-between border-b bg-sidebar px-4 lg:hidden">
        <Link href="/" className="flex items-center gap-2">
          <Building2 className="h-6 w-6 text-sidebar-primary" />
          <span className="font-semibold text-sidebar-foreground">보건소</span>
        </Link>
        <Button
          variant="ghost"
          size="icon"
          onClick={() => setIsMobileOpen(!isMobileOpen)}
          className="text-sidebar-foreground hover:bg-sidebar-accent"
        >
          {isMobileOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
        </Button>
      </div>

      {/* Mobile Overlay */}
      {isMobileOpen && (
        <div 
          className="fixed inset-0 z-40 bg-black/50 lg:hidden"
          onClick={() => setIsMobileOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside className={cn(
        'fixed top-0 left-0 z-40 h-full w-64 bg-sidebar border-r border-sidebar-border transition-transform lg:translate-x-0',
        isMobileOpen ? 'translate-x-0' : '-translate-x-full'
      )}>
        <div className="flex h-full flex-col">
          {/* Logo */}
          <div className="flex h-14 items-center gap-2 border-b border-sidebar-border px-4">
            <Building2 className="h-6 w-6 text-sidebar-primary" />
            <span className="font-semibold text-sidebar-foreground">보건소 예약시스템</span>
          </div>

          {/* User Info */}
          <div className="border-b border-sidebar-border p-4">
            <div className="flex items-center gap-3">
              <div className="flex h-9 w-9 items-center justify-center rounded-full bg-sidebar-primary text-sidebar-primary-foreground font-medium text-sm">
                {user.name.charAt(0)}
              </div>
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium text-sidebar-foreground">{user.name}</p>
                <p className="text-xs text-sidebar-foreground/70">{roleLabels[user.role]}</p>
              </div>
            </div>
          </div>

          {/* Navigation */}
          <nav className="flex-1 overflow-y-auto p-3">
            <ul className="space-y-1">
              {items.map((item) => {
                const isActive = pathname === item.href;
                return (
                  <li key={item.href}>
                    <Link
                      href={item.href}
                      onClick={() => setIsMobileOpen(false)}
                      className={cn(
                        'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                        isActive
                          ? 'bg-sidebar-primary text-sidebar-primary-foreground'
                          : 'text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground'
                      )}
                    >
                      <item.icon className="h-4 w-4 shrink-0" />
                      <span className="truncate">{item.label}</span>
                    </Link>
                  </li>
                );
              })}
            </ul>
          </nav>

          {/* Footer */}
          <div className="border-t border-sidebar-border p-3">
            <Button
              variant="ghost"
              className="w-full justify-start gap-3 text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
              onClick={async () => {
                await logout();
                window.location.href = '/login';
              }}
            >
              <LogOut className="h-4 w-4" />
              <span>로그아웃</span>
            </Button>
          </div>
        </div>
      </aside>
    </>
  );
}
