import { useState } from 'react';
import type { FormEvent } from 'react';
import {
  FormControl,
  FormErrorMessage,
  FormLabel,
  Input,
  VStack,
} from '@chakra-ui/react';
import { SectionCard } from '../../../components/SectionCard';
import { LoadingButton } from '../../../components/LoadingButton';
import { useSubmit } from '../../../hooks/useSubmit';
import {
  validatePassword,
  validatePasswordConfirm,
} from '../../../lib/validation';
import type { User, UserUpdateRequest } from '../../../types/user';

interface PasswordSectionProps {
  onUpdate: (body: UserUpdateRequest) => Promise<User>;
}

export const PasswordSection = ({ onUpdate }: PasswordSectionProps) => {
  const { submitting, run } = useSubmit();
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [passwordError, setPasswordError] = useState<string | undefined>();
  const [confirmError, setConfirmError] = useState<string | undefined>();

  const validate = (): boolean => {
    const pError = validatePassword(password);
    const cError = validatePasswordConfirm(password, confirm);
    setPasswordError(pError);
    setConfirmError(cError);
    return !pError && !cError;
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    await run(() => onUpdate({ password }), {
      guard: validate,
      successMessage: 'Password changed.',
      onSuccess: () => {
        setPassword('');
        setConfirm('');
      },
    });
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
