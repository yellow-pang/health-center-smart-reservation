import type { Metadata } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';
import { Analytics } from '@vercel/analytics/next';
import { Toaster } from '@/components/ui/sonner';
import { AuthProvider } from '@/src/contexts/auth-context';
import './globals.css';

const _geist = Geist({ subsets: ['latin'], variable: '--font-geist-sans' });
const _geistMono = Geist_Mono({ subsets: ['latin'], variable: '--font-geist-mono' });
const appUrl = process.env.NEXT_PUBLIC_APP_URL || 'http://localhost:3000';
const ogImage = '/og-image.png';

export const metadata: Metadata = {
  metadataBase: new URL(appUrl),
  title: '보건소 스마트 예약 시스템',
  description: '가까운 보건소 예약을 미리 잡고 대기 없이 방문하세요',
  manifest: '/manifest.json',
  icons: {
    icon: '/health_reservation_icon_128.png',
    apple: '/apple-touch-icon.png',
  },
  openGraph: {
    title: '보건소 스마트 예약 시스템',
    description: '가까운 보건소 예약을 미리 잡고 대기 없이 방문하세요',
    url: appUrl,
    siteName: '보건소 스마트 예약 시스템',
    images: [{ url: ogImage, width: 1080, height: 1080, alt: '보건소 스마트 예약 시스템' }],
    locale: 'ko_KR',
    type: 'website',
  },
  twitter: {
    card: 'summary_large_image',
    title: '보건소 스마트 예약 시스템',
    description: '가까운 보건소 예약을 미리 잡고 대기 없이 방문하세요',
    images: [ogImage],
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" className={`${_geist.variable} ${_geistMono.variable} bg-background`}>
      <body className="font-sans antialiased">
        <AuthProvider>
          {children}
          <Toaster position="top-center" />
        </AuthProvider>
        {process.env.NODE_ENV === 'production' && <Analytics />}
      </body>
    </html>
  );
}
