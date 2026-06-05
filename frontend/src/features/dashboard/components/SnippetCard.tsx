import { useState } from 'react';
import {
  Badge,
  Box,
  Heading,
  HStack,
  IconButton,
  Spacer,
  Text,
  useDisclosure,
  useToast,
} from '@chakra-ui/react';
import { FiTrash2, FiArrowRight } from 'react-icons/fi';
import { useNavigate } from 'react-router-dom';
import { ConfirmDialog } from '../../../components/ConfirmDialog';
import { snippetsApi } from '../../../api/snippetsApi';
import { errorToast, successToast } from '../../../components/toast';
import { isAppError } from '../../../lib/normalizeError';
import type { Snippet } from '../../../types/snippet';

interface SnippetCardProps {
  snippet: Snippet;
  onDeleted: () => void;
}

export const SnippetCard = ({ snippet, onDeleted }: SnippetCardProps) => {
  const navigate = useNavigate();
  const toast = useToast();
  const { isOpen, onOpen, onClose } = useDisclosure();
  const [deleting, setDeleting] = useState(false);

  const openEditor = () => navigate(`/editor/${snippet.id}`);

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await snippetsApi.deleteSnippet(snippet.id);
      toast(successToast('Snippet deleted.'));
      onClose();
      onDeleted();
    } catch (error) {
      if (isAppError(error)) {
        toast(errorToast(error));
      }
    } finally {
      setDeleting(false);
    }
  };

  return (
    <>
      <Box
        bg="gray.800"
        borderWidth="1px"
        borderColor="gray.700"
        borderRadius="lg"
        p={4}
        transition="border-color 0.15s"
        _hover={{ borderColor: 'gray.600' }}
        cursor="pointer"
        onClick={openEditor}
        role="group"
      >
        <HStack align="start" mb={3}>
          <Heading size="sm" noOfLines={1} flex="1" title={snippet.title}>
            {snippet.title || 'Untitled'}
          </Heading>
          <IconButton
            aria-label="Delete snippet"
            icon={<FiTrash2 />}
            size="xs"
            variant="ghost"
            colorScheme="red"
            opacity={0}
            _groupHover={{ opacity: 1 }}
            onClick={event => {
              event.stopPropagation();
              onOpen();
            }}
          />
        </HStack>

        <HStack>
          <Badge
            colorScheme="blue"
            variant="subtle"
            textTransform="none"
            fontWeight="normal"
          >
            {snippet.language?.name ?? 'Unknown'}
          </Badge>
          <Spacer />
          <HStack
            spacing={1}
            color="textColor.medium"
            fontSize="xs"
            _groupHover={{ color: 'brand.500' }}
          >
            <Text>Open</Text>
            <FiArrowRight />
          </HStack>
        </HStack>
      </Box>

      <ConfirmDialog
        isOpen={isOpen}
        onClose={onClose}
        onConfirm={handleDelete}
        isLoading={deleting}
        title="Delete snippet"
        body={
          <>
            Are you sure you want to delete{' '}
            <Text as="span" color="textColor.light" fontWeight="semibold">
              {snippet.title || 'this snippet'}
            </Text>
            ? This action cannot be undone.
          </>
        }
        confirmLabel="Delete"
      />
    </>
  );
};
