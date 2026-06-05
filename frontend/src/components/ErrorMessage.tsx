import { Alert, AlertIcon, AlertDescription } from '@chakra-ui/react';
import type { AppError } from '../types/api';

interface ErrorMessageProps {
  error: AppError | string | null | undefined;
}

/**
 * Inline error display. Used for 403 and form-level errors (errors that must
 * appear near the relevant content rather than as a global toast).
 */
export const ErrorMessage = ({ error }: ErrorMessageProps) => {
  if (!error) {
    return null;
  }
  const message = typeof error === 'string' ? error : error.message;
  return (
    <Alert
      status="error"
      variant="subtle"
      borderRadius="md"
      bg="rgba(239, 68, 68, 0.12)"
      color="textColor.light"
      fontSize="sm"
    >
      <AlertIcon color="statusColor.failed" />
      <AlertDescription>{message}</AlertDescription>
    </Alert>
  );
};
