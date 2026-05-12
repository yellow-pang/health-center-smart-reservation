import type { Metadata } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';
import { Analytics } from '@vercel/analytics/next';
import { Toaster } from '@/components/ui/sonner';
import { AuthProvider } from '@/src/contexts/auth-context';
import './globals.css';

const _geist = Geist({ subsets: ['latin'], variable: '--font-geist-sans' });
const _geistMono = Geist_Mono({ subsets: ['latin'], variable: '--font-geist-mono' });

export const metadata: Metadata = {
  title: '보건소 스마트 예약 시스템',
  description: '보건소 예약, 대기, 혼잡도 분석을 위한 통합 시스템',
  generator: 'v0.app',
  icons: {
    icon: [
      {
        url: '/icon-light-32x32.png',
        media: '(prefers-color-scheme: light)',
      },
      {
        url: '/icon-dark-32x32.png',
        media: '(prefers-color-scheme: dark)',
      },
      {
        url: '/icon.svg',
        type: 'image/svg+xml',
      },
    ],
    apple: '/apple-icon.png',
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
