import { Button, type ButtonProps } from '@chakra-ui/react';

/**
 * Thin wrapper over Chakra's Button that ties loading to disabled, so an
 * in-flight request both shows a spinner and blocks double-submits.
 */
export const LoadingButton = ({
  isLoading,
  isDisabled,
  children,
  ...props
}: ButtonProps) => (
  <Button
    isLoading={isLoading}
    isDisabled={isLoading || isDisabled}
    {...props}
  >
    {children}
  </Button>
);
