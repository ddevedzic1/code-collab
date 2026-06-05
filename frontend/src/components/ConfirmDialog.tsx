import { useRef } from 'react';
import type { ReactNode } from 'react';
import {
  AlertDialog,
  AlertDialogBody,
  AlertDialogContent,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogOverlay,
  Button,
} from '@chakra-ui/react';
import { LoadingButton } from './LoadingButton';

interface ConfirmDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  body: ReactNode;
  confirmLabel?: string;
  cancelLabel?: string;
  isLoading?: boolean;
  /** Use a red confirm button for destructive actions (the default). */
  isDestructive?: boolean;
}

export const ConfirmDialog = ({
  isOpen,
  onClose,
  onConfirm,
  title,
  body,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  isLoading = false,
  isDestructive = true,
}: ConfirmDialogProps) => {
  const cancelRef = useRef<HTMLButtonElement>(null);

  return (
    <AlertDialog
      isOpen={isOpen}
      leastDestructiveRef={cancelRef}
      onClose={onClose}
      isCentered
    >
      <AlertDialogOverlay>
        <AlertDialogContent bg="gray.800" color="textColor.light">
          <AlertDialogHeader fontSize="lg" fontWeight="semibold">
            {title}
          </AlertDialogHeader>

          <AlertDialogBody fontSize="sm" color="textColor.medium">
            {body}
          </AlertDialogBody>

          <AlertDialogFooter>
            <Button
              ref={cancelRef}
              onClick={onClose}
              variant="ghost"
              isDisabled={isLoading}
            >
              {cancelLabel}
            </Button>
            <LoadingButton
              colorScheme={isDestructive ? 'red' : 'blue'}
              onClick={onConfirm}
              isLoading={isLoading}
              ml={3}
            >
              {confirmLabel}
            </LoadingButton>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialogOverlay>
    </AlertDialog>
  );
};
