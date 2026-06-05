import { Heading, VStack } from '@chakra-ui/react';
import { ProfileSection } from './components/ProfileSection';
import { PasswordSection } from './components/PasswordSection';
import { DangerZone } from './components/DangerZone';
import { Spinner } from '../../components/Spinner';
import { ErrorMessage } from '../../components/ErrorMessage';
import { useAuth } from '../../context/auth/useAuth';
import { useUser } from '../../hooks/useUser';

export const Account = () => {
  const { user: authUser } = useAuth();
  const { user, loading, error, updateUser, deleteUser } = useUser(authUser?.id);

  return (
    <VStack align="stretch" spacing={6} maxW="640px">
      <Heading size="lg">Account</Heading>

      {error ? <ErrorMessage error={error} /> : null}

      {loading || !user ? (
        <Spinner label="Loading account…" />
      ) : (
        <>
          <ProfileSection
            user={user}
            onUpdate={updateUser}
            onUsernameChanged={() => undefined}
          />
          <PasswordSection onUpdate={updateUser} />
          <DangerZone onDelete={deleteUser} />
        </>
      )}
    </VStack>
  );
};
