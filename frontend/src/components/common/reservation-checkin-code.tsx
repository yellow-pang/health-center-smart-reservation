'use client';

import { useEffect, useRef, useState } from 'react';
import { QrCode } from 'lucide-react';
import JsBarcode from 'jsbarcode';
import QRCode from 'qrcode';

import { cn } from '@/lib/utils';

interface ReservationCheckInCodeProps {
  reservationNo: string;
  title?: string;
  description?: string;
  compact?: boolean;
  className?: string;
}

export function ReservationCheckInCode({
  reservationNo,
  title = '체크인 코드',
  description = '직원에게 QR 또는 바코드를 보여주세요.',
  compact = false,
  className,
}: ReservationCheckInCodeProps) {
  const normalizedReservationNo = reservationNo.trim().toUpperCase();
  const barcodeRef = useRef<SVGSVGElement | null>(null);
  const [qrDataUrl, setQrDataUrl] = useState('');

  useEffect(() => {
    let active = true;

    QRCode.toDataURL(normalizedReservationNo, {
      errorCorrectionLevel: 'M',
      margin: 1,
      width: compact ? 120 : 168,
      color: {
        dark: '#111827',
        light: '#ffffff',
      },
    })
      .then((dataUrl) => {
        if (active) {
          setQrDataUrl(dataUrl);
        }
      })
      .catch(() => {
        if (active) {
          setQrDataUrl('');
        }
      });

    return () => {
      active = false;
    };
  }, [compact, normalizedReservationNo]);

  useEffect(() => {
    if (!barcodeRef.current || !normalizedReservationNo) {
      return;
    }

    JsBarcode(barcodeRef.current, normalizedReservationNo, {
      format: 'CODE128',
      displayValue: true,
      font: 'monospace',
      fontSize: compact ? 12 : 14,
      height: compact ? 42 : 56,
      margin: 0,
      width: compact ? 1.2 : 1.6,
      lineColor: '#111827',
    });
  }, [compact, normalizedReservationNo]);

  return (
    <div className={cn('rounded-lg border bg-background p-4', className)}>
      <div className="mb-3 flex items-start gap-2">
        <QrCode className="mt-0.5 h-4 w-4 shrink-0 text-primary" />
        <div className="min-w-0">
          <p className="text-sm font-semibold">{title}</p>
          <p className="text-xs text-muted-foreground">{description}</p>
        </div>
      </div>

      <div className={cn(
        'grid gap-4',
        compact ? 'grid-cols-1' : 'sm:grid-cols-[auto_1fr]',
      )}>
        <div className="flex justify-center">
          {qrDataUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={qrDataUrl}
              alt={`${normalizedReservationNo} QR 코드`}
              className={cn(
                'rounded border bg-white p-2',
                compact ? 'h-28 w-28' : 'h-40 w-40',
              )}
            />
          ) : (
            <div className={cn(
              'flex items-center justify-center rounded border bg-muted text-xs text-muted-foreground',
              compact ? 'h-28 w-28' : 'h-40 w-40',
            )}>
              QR 생성 중
            </div>
          )}
        </div>

        <div className="min-w-0 space-y-3">
          <div className="overflow-x-auto rounded border bg-white p-3">
            <svg
              ref={barcodeRef}
              role="img"
              aria-label={`${normalizedReservationNo} 바코드`}
              className="mx-auto block max-w-full text-black"
            />
          </div>

          <div className="rounded bg-muted/60 px-3 py-2">
            <p className="text-xs text-muted-foreground">예약번호</p>
            <p className="break-all font-mono text-sm font-semibold tracking-wide">
              {normalizedReservationNo}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
