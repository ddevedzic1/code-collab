import { useState } from 'react';
import { Button, Text, useDisclosure, useToast } from '@chakra-ui/react';
import { SectionCard } from '../../../components/SectionCard';
import { ConfirmDialog } from '../../../components/ConfirmDialog';
import { errorToast, successToast } from '../../../components/toast';
import { useAuth } from '../../../context/auth/useAuth';
import { isAppError } from '../../../lib/normalizeError';

interface DangerZoneProps {
  onDelete: () => Promise<void>;
}

export const DangerZone = ({ onDelete }: DangerZoneProps) => {
  const toast = useToast();
  const { logout } = useAuth();
  const { isOpen, onOpen, onClose } = useDisclosure();
  const [deleting, setDeleting] = useState(false);

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await onDelete();
      toast(successToast('Your account has been deleted.'));
      await logout();
    } catch (error) {
      if (isAppError(error)) {
        toast(errorToast(error));
      }
      setDeleting(false);
    }
  };

  return (
    <SectionCard
      danger
      title="Delete account"
      description="Permanently delete your account. This cannot be undone."
    >
      <Button colorScheme="red" variant="outline" onClick={onOpen}>
        Delete my account
      </Button>

      <ConfirmDialog
        isOpen={isOpen}
        onClose={onClose}
        onConfirm={handleDelete}
        isLoading={deleting}
        title="Delete account"
        body={
          <Text>
            This will permanently delete your account and sign you out. Are you
            sure you want to continue?
          </Text>
        }
        confirmLabel="Delete account"
      />
    </SectionCard>
  );
};
