import { useState } from 'react';
import type { FormEvent } from 'react';
import {
  Box,
  Button,
  Divider,
  HStack,
  IconButton,
  Input,
  Select,
  Tag,
  Text,
  useDisclosure,
  useToast,
  VStack,
} from '@chakra-ui/react';
import { FiTrash2, FiUserPlus } from 'react-icons/fi';
import { LoadingButton } from '../../../components/LoadingButton';
import { ConfirmDialog } from '../../../components/ConfirmDialog';
import { ErrorMessage } from '../../../components/ErrorMessage';
import { Spinner } from '../../../components/Spinner';
import { useShareUsers } from '../../../hooks/useShareUsers';
import { useSubmit } from '../../../hooks/useSubmit';
import { errorToast, successToast } from '../../../components/toast';
import { isAppError } from '../../../lib/normalizeError';
import { Permission, permissionLabel } from '../../../types/share';

interface ShareUsersPanelProps {
  shareId: string;
}

export const ShareUsersPanel = ({ shareId }: ShareUsersPanelProps) => {
  const toast = useToast();
  const { users, loading, error, addUser, removeUser, removeAll } =
    useShareUsers(shareId);

  const removeAllDialog = useDisclosure();
  const addSubmit = useSubmit();
  const removeAllSubmit = useSubmit();
  const [username, setUsername] = useState('');
  const [permission, setPermission] = useState<Permission>(
    Permission.READ_ONLY
  );
  const [removingId, setRemovingId] = useState<string | null>(null);

  const handleAdd = async (event: FormEvent) => {
    event.preventDefault();
    await addSubmit.run(
      () => addUser({ username: username.trim(), permission }),
      {
        guard: () => username.trim() !== '',
        successMessage: 'User added.',
        onSuccess: () => setUsername(''),
      }
    );
  };

  const handleRemove = async (rowId: string, removeUsername: string) => {
    setRemovingId(rowId);
    try {
      await removeUser(removeUsername);
      toast(successToast('User removed.'));
    } catch (err) {
      if (isAppError(err)) {
        toast(errorToast(err));
      }
    } finally {
      setRemovingId(null);
    }
  };

  const handleRemoveAll = async () => {
    await removeAllSubmit.run(() => removeAll(), {
      successMessage: 'All users removed.',
      onSuccess: () => removeAllDialog.onClose(),
    });
  };

  return (
    <Box>
      <Divider borderColor="gray.700" my={4} />
      <Text fontSize="sm" fontWeight="semibold" mb={3}>
        Users with access
      </Text>

      <form onSubmit={handleAdd}>
        <HStack spacing={2} mb={4} align="stretch">
          <Input
            placeholder="Username"
            value={username}
            onChange={event => setUsername(event.target.value)}
            flex="1"
          />
          <Select
            value={permission}
            onChange={event => setPermission(event.target.value as Permission)}
            w="130px"
          >
            <option value={Permission.READ_ONLY}>
              {permissionLabel(Permission.READ_ONLY)}
            </option>
            <option value={Permission.EDIT}>
              {permissionLabel(Permission.EDIT)}
            </option>
          </Select>
          <LoadingButton
            type="submit"
            colorScheme="blue"
            leftIcon={<FiUserPlus />}
            isLoading={addSubmit.submitting}
            isDisabled={username.trim() === ''}
          >
            Add
          </LoadingButton>
        </HStack>
      </form>

      {error ? <ErrorMessage error={error} /> : null}

      {loading ? (
        <Spinner label="Loading users…" />
      ) : users.length === 0 ? (
        <Text fontSize="sm" color="textColor.medium">
          No users added yet.
        </Text>
      ) : (
        <VStack align="stretch" spacing={2}>
          {users.map(shareUser => (
            <HStack
              key={shareUser.id}
              justify="space-between"
              bg="gray.900"
              borderWidth="1px"
              borderColor="gray.700"
              borderRadius="md"
              px={3}
              py={2}
            >
              <Text
                fontSize="sm"
                noOfLines={1}
                title={shareUser.username ?? shareUser.userId}
              >
                {shareUser.username ?? shareUser.userId}
              </Text>
              <HStack spacing={2}>
                <Tag size="sm" colorScheme="blue" variant="subtle">
                  {permissionLabel(shareUser.permission)}
                </Tag>
                <IconButton
                  aria-label="Remove user"
                  icon={<FiTrash2 />}
                  size="xs"
                  variant="ghost"
                  colorScheme="red"
                  isLoading={removingId === shareUser.id}
                  isDisabled={!shareUser.username}
                  onClick={() =>
                    shareUser.username &&
                    handleRemove(shareUser.id, shareUser.username)
                  }
                />
              </HStack>
            </HStack>
          ))}

          <Button
            size="xs"
            variant="ghost"
            colorScheme="red"
            alignSelf="flex-start"
            onClick={removeAllDialog.onOpen}
          >
            Remove all users
          </Button>
        </VStack>
      )}

      <ConfirmDialog
        isOpen={removeAllDialog.isOpen}
        onClose={removeAllDialog.onClose}
        onConfirm={handleRemoveAll}
        isLoading={removeAllSubmit.submitting}
        title="Remove all users"
        body="This removes every user from this share. Are you sure?"
        confirmLabel="Remove all"
      />
    </Box>
  );
};
