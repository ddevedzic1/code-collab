import type { UseToastOptions } from '@chakra-ui/react';
import type { AppError } from '../types/api';

const BASE_OPTIONS: UseToastOptions = {
  position: 'top-right',
  duration: 4000,
  isClosable: true,
  variant: 'solid',
};

export const successToast = (description: string): UseToastOptions => ({
  ...BASE_OPTIONS,
  status: 'success',
  description,
});

/**
 * Build error toast options from a normalized AppError. A stable `id` derived
 * from the code prevents identical errors from stacking when several requests
 * fail at once.
 */
export const errorToast = (error: AppError): UseToastOptions => ({
  ...BASE_OPTIONS,
  status: 'error',
  id: `error-${error.code}`,
  description: error.message,
});
