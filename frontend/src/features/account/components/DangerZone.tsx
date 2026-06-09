import { Button, Text, useDisclosure } from '@chakra-ui/react';
import { SectionCard } from '../../../components/SectionCard';
import { ConfirmDialog } from '../../../components/ConfirmDialog';
import { useAuth } from '../../../context/auth/useAuth';
import { useSubmit } from '../../../hooks/useSubmit';

interface DangerZoneProps {
  onDelete: () => Promise<void>;
}

export const DangerZone = ({ onDelete }: DangerZoneProps) => {
  const { logout } = useAuth();
  const { isOpen, onOpen, onClose } = useDisclosure();
  const { submitting, run } = useSubmit();

  const handleDelete = async () => {
    await run(() => onDelete(), {
      successMessage: 'Your account has been deleted.',
      keepSubmittingOnSuccess: true,
      onSuccess: () => logout(),
    });
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
        isLoading={submitting}
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
