import { useState } from 'react';
import type { FormEvent } from 'react';
import {
  FormControl,
  FormErrorMessage,
  FormLabel,
  Input,
  Modal,
  ModalBody,
  ModalCloseButton,
  ModalContent,
  ModalFooter,
  ModalHeader,
  ModalOverlay,
  VStack,
  Button,
  useToast,
} from '@chakra-ui/react';
import { useNavigate } from 'react-router-dom';
import { LanguageSelect } from '../../../components/LanguageSelect';
import { LoadingButton } from '../../../components/LoadingButton';
import { snippetsApi } from '../../../api/snippetsApi';
import { errorToast } from '../../../components/toast';
import { validateRequired } from '../../../lib/validation';
import { isAppError } from '../../../lib/normalizeError';

interface NewSnippetModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const NewSnippetModal = ({ isOpen, onClose }: NewSnippetModalProps) => {
  const navigate = useNavigate();
  const toast = useToast();

  const [title, setTitle] = useState('');
  const [languageId, setLanguageId] = useState('');
  const [titleError, setTitleError] = useState<string | undefined>();
  const [languageError, setLanguageError] = useState<string | undefined>();
  const [submitting, setSubmitting] = useState(false);

  const reset = () => {
    setTitle('');
    setLanguageId('');
    setTitleError(undefined);
    setLanguageError(undefined);
  };

  const handleClose = () => {
    if (submitting) {
      return;
    }
    reset();
    onClose();
  };

  const validate = (): boolean => {
    const tError = validateRequired(title, 'Title');
    const lError = languageId ? undefined : 'Please select a language.';
    setTitleError(tError);
    setLanguageError(lError);
    return !tError && !lError;
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (submitting || !validate()) {
      return;
    }
    setSubmitting(true);
    try {
      const snippet = await snippetsApi.createSnippet({
        languageId,
        title: title.trim(),
        content: '',
      });
      reset();
      onClose();
      navigate(`/editor/${snippet.id}`);
    } catch (error) {
      if (isAppError(error)) {
        toast(errorToast(error));
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} isCentered>
      <ModalOverlay />
      <ModalContent bg="gray.800">
        <form onSubmit={handleSubmit} noValidate>
          <ModalHeader>New snippet</ModalHeader>
          <ModalCloseButton isDisabled={submitting} />
          <ModalBody>
            <VStack spacing={4} align="stretch">
              <FormControl isInvalid={!!titleError}>
                <FormLabel fontSize="sm">Title</FormLabel>
                <Input
                  value={title}
                  onChange={event => setTitle(event.target.value)}
                  placeholder="My snippet"
                  autoFocus
                />
                <FormErrorMessage>{titleError}</FormErrorMessage>
              </FormControl>

              <FormControl isInvalid={!!languageError}>
                <FormLabel fontSize="sm">Language</FormLabel>
                <LanguageSelect
                  value={languageId}
                  onChange={event => setLanguageId(event.target.value)}
                />
                <FormErrorMessage>{languageError}</FormErrorMessage>
              </FormControl>
            </VStack>
          </ModalBody>
          <ModalFooter>
            <Button variant="ghost" mr={3} onClick={handleClose} isDisabled={submitting}>
              Cancel
            </Button>
            <LoadingButton type="submit" colorScheme="blue" isLoading={submitting}>
              Create
            </LoadingButton>
          </ModalFooter>
        </form>
      </ModalContent>
    </Modal>
  );
};
