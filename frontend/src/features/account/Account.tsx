import { Heading, VStack } from '@chakra-ui/react';
import { ProfileSection } from './components/ProfileSection';
import { PasswordSection } from './components/PasswordSection';
import { DangerZone } from './components/DangerZone';
import { Spinner } from '../../components/Spinner';
import { ErrorMessage } from '../../components/ErrorMessage';
import { useAuth } from '../../context/auth/useAuth';
import { useUser } from '../../hooks/useUser';

export const Account = () => {
  const { user: authUser, patchUser } = useAuth();
  const { user, loading, error, updateUser, deleteUser } = useUser(authUser?.id);

  return (
    <VStack align="stretch" spacing={6} maxW="640px">
      <Heading size="lg">Account</Heading>

      {error ? <ErrorMessage error={error} /> : null}

      {loading && !error ? (
        <Spinner label="Loading account…" />
      ) : user ? (
        <>
          <ProfileSection
            user={user}
            onUpdate={updateUser}
            onUsernameChanged={username => patchUser({ username })}
          />
          <PasswordSection onUpdate={updateUser} />
          <DangerZone onDelete={deleteUser} />
        </>
      ) : null}
    </VStack>
  );
};
