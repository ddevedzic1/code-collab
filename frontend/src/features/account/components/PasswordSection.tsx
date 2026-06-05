import { useState } from 'react';
import type { FormEvent } from 'react';
import {
  FormControl,
  FormErrorMessage,
  FormLabel,
  Input,
  VStack,
  useToast,
} from '@chakra-ui/react';
import { SectionCard } from '../../../components/SectionCard';
import { LoadingButton } from '../../../components/LoadingButton';
import { errorToast, successToast } from '../../../components/toast';
import {
  validatePassword,
  validatePasswordConfirm,
} from '../../../lib/validation';
import { isAppError } from '../../../lib/normalizeError';
import type { User, UserUpdateRequest } from '../../../types/user';

interface PasswordSectionProps {
  onUpdate: (body: UserUpdateRequest) => Promise<User>;
}

export const PasswordSection = ({ onUpdate }: PasswordSectionProps) => {
  const toast = useToast();
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [passwordError, setPasswordError] = useState<string | undefined>();
  const [confirmError, setConfirmError] = useState<string | undefined>();
  const [submitting, setSubmitting] = useState(false);

  const validate = (): boolean => {
    const pError = validatePassword(password);
    const cError = validatePasswordConfirm(password, confirm);
    setPasswordError(pError);
    setConfirmError(cError);
    return !pError && !cError;
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (submitting || !validate()) {
      return;
    }
    setSubmitting(true);
    try {
      await onUpdate({ password });
      setPassword('');
      setConfirm('');
      toast(successToast('Password changed.'));
    } catch (err) {
      if (isAppError(err)) {
        toast(errorToast(err));
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <SectionCard
      title="Password"
      description="Choose a strong password you don't use elsewhere."
    >
      <form onSubmit={handleSubmit} noValidate>
        <VStack spacing={4} align="stretch">
          <FormControl isInvalid={!!passwordError}>
            <FormLabel fontSize="sm">New password</FormLabel>
            <Input
              type="password"
              value={password}
              onChange={event => setPassword(event.target.value)}
              autoComplete="new-password"
            />
            <FormErrorMessage>{passwordError}</FormErrorMessage>
          </FormControl>

          <FormControl isInvalid={!!confirmError}>
            <FormLabel fontSize="sm">Confirm new password</FormLabel>
            <Input
              type="password"
              value={confirm}
              onChange={event => setConfirm(event.target.value)}
              autoComplete="new-password"
            />
            <FormErrorMessage>{confirmError}</FormErrorMessage>
          </FormControl>

          <LoadingButton
            type="submit"
            colorScheme="blue"
            alignSelf="flex-start"
            isLoading={submitting}
          >
            Change password
          </LoadingButton>
        </VStack>
      </form>
    </SectionCard>
  );
};
