import {
  Box,
  Modal,
  ModalBody,
  ModalCloseButton,
  ModalContent,
  ModalHeader,
  ModalOverlay,
} from '@chakra-ui/react';
import { ShareCreateForm } from './ShareCreateForm';
import { ShareDetails } from './ShareDetails';
import { Spinner } from '../../../components/Spinner';
import { ErrorMessage } from '../../../components/ErrorMessage';
import { useShare } from '../../../hooks/useShare';

interface ShareModalProps {
  snippetId: string;
  isOpen: boolean;
  onClose: () => void;
}

export const ShareModal = ({ snippetId, isOpen, onClose }: ShareModalProps) => {
  const { status, share, error, createShare, updateShare, deleteShare } =
    useShare(snippetId, isOpen);

  return (
    <Modal isOpen={isOpen} onClose={onClose} size="lg" isCentered scrollBehavior="inside">
      <ModalOverlay />
      <ModalContent bg="gray.800">
        <ModalHeader>Share snippet</ModalHeader>
        <ModalCloseButton />
        <ModalBody pb={6}>
          {status === 'loading' || status === 'idle' ? (
            <Box py={8}>
              <Spinner label="Loading share…" />
            </Box>
          ) : status === 'error' ? (
            <ErrorMessage error={error} />
          ) : status === 'none' ? (
            <ShareCreateForm onCreate={createShare} />
          ) : share ? (
            <ShareDetails
              share={share}
              onUpdate={updateShare}
              onDelete={deleteShare}
            />
          ) : null}
        </ModalBody>
      </ModalContent>
    </Modal>
  );
};
