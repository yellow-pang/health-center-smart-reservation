import { AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

interface ErrorStateProps {
  title?: string;
  description?: string;
  onRetry?: () => void;
  className?: string;
}

export function ErrorState({ 
  title = '오류가 발생했습니다',
  description = '잠시 후 다시 시도해주세요.',
  onRetry,
  className 
}: ErrorStateProps) {
  return (
    <div className={cn('flex flex-col items-center justify-center py-12 text-center', className)}>
      <div className="flex h-14 w-14 items-center justify-center rounded-full bg-destructive/10 mb-4">
        <AlertCircle className="h-7 w-7 text-destructive" />
      </div>
      <h3 className="text-base font-medium text-foreground mb-1">{title}</h3>
      <p className="text-sm text-muted-foreground max-w-sm mb-4">{description}</p>
      {onRetry && (
        <Button variant="outline" onClick={onRetry}>
          다시 시도
        </Button>
      )}
    </div>
  );
}
