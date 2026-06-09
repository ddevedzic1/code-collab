import { useEffect, useState } from 'react';
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
import { validateUsername } from '../../../lib/validation';
import type { User, UserUpdateRequest } from '../../../types/user';

interface ProfileSectionProps {
  user: User;
  onUpdate: (body: UserUpdateRequest) => Promise<User>;
  onUsernameChanged: (username: string) => void;
}

export const ProfileSection = ({
  user,
  onUpdate,
  onUsernameChanged,
}: ProfileSectionProps) => {
  const { submitting, run } = useSubmit();
  const [username, setUsername] = useState(user.username);
  const [error, setError] = useState<string | undefined>();

  useEffect(() => {
    setUsername(user.username);
  }, [user.username]);

  const isUnchanged = username.trim() === user.username;

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    await run(() => onUpdate({ username: username.trim() }), {
      guard: () => {
        const validationError = validateUsername(username);
        setError(validationError);
        return !validationError && !isUnchanged;
      },
      successMessage: 'Profile updated.',
      onSuccess: updated => onUsernameChanged(updated.username),
    });
  };

  return (
    <SectionCard title="Profile" description="Update your account details.">
      <form onSubmit={handleSubmit} noValidate>
        <VStack spacing={4} align="stretch">
          <FormControl>
            <FormLabel fontSize="sm">Email</FormLabel>
            <Input value={user.email} isDisabled isReadOnly />
          </FormControl>

          <FormControl isInvalid={!!error}>
            <FormLabel fontSize="sm">Username</FormLabel>
            <Input
              value={username}
              onChange={event => setUsername(event.target.value)}
              autoComplete="username"
            />
            <FormErrorMessage>{error}</FormErrorMessage>
          </FormControl>

          <LoadingButton
            type="submit"
            colorScheme="blue"
            alignSelf="flex-start"
            isLoading={submitting}
            isDisabled={isUnchanged}
          >
            Save changes
          </LoadingButton>
        </VStack>
      </form>
    </SectionCard>
  );
};
