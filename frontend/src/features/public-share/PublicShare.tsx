import { useEffect, useState } from 'react';
import {
  Badge,
  Box,
  Button,
  Center,
  Flex,
  Heading,
  HStack,
  Tag,
  Text,
} from '@chakra-ui/react';
import { FiEdit2, FiEye, FiHome, FiLock } from 'react-icons/fi';
import { useNavigate } from 'react-router-dom';
import { CodeEditor } from '../../components/CodeEditor';
import { LoadingButton } from '../../components/LoadingButton';
import { Spinner } from '../../components/Spinner';
import { useSharedSnippet } from '../../hooks/useSharedSnippet';
import { useSubmit } from '../../hooks/useSubmit';
import { useAuth } from '../../context/auth/useAuth';
import { snippetsApi } from '../../api/snippetsApi';
import { isAppError } from '../../lib/normalizeError';
import { Permission, permissionLabel } from '../../types/share';

interface PublicShareProps {
  token: string;
}

export const PublicShare = ({ token }: PublicShareProps) => {
  const { shared, loading, error } = useSharedSnippet(token);
  const { submitting, run } = useSubmit();
  const { status } = useAuth();
  const navigate = useNavigate();

  const homePath = status === 'authed' ? '/dashboard' : '/login';
  const goHome = () => navigate(homePath);

  const [content, setContent] = useState('');

  useEffect(() => {
    if (shared) {
      setContent(shared.content);
    }
  }, [shared]);

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 's') {
        event.preventDefault();
        if (shared?.permission !== Permission.EDIT) {
          return;
        }
        run(
          () => snippetsApi.updateSnippet(shared.snippetId, { content }),
          { successMessage: 'Changes saved.' }
        );
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [shared, content, run]);

  if (loading) {
    return <Spinner full label="Loading shared snippet…" />;
  }

  if (error || !shared) {
    const isForbidden = isAppError(error) && error.status === 403;
    const isNotFound = isAppError(error) && error.status === 404;
    return (
      <Center minH="100vh" px={4}>
        <Box
          textAlign="center"
          maxW="420px"
          bg="gray.800"
          borderWidth="1px"
          borderColor="gray.700"
          borderRadius="lg"
          p={8}
        >
          <Center mb={3} color="textColor.medium">
            <FiLock size={28} />
          </Center>
          <Heading size="md" mb={2}>
            {isForbidden
              ? 'No access'
              : isNotFound
                ? 'Snippet not found'
                : 'Unable to load snippet'}
          </Heading>
          <Text fontSize="sm" color="textColor.medium" mb={5}>
            {isForbidden
              ? 'You do not have permission to view this shared snippet.'
              : isNotFound
                ? 'This share link is invalid or has been removed.'
                : (error?.message ?? 'Please try again later.')}
          </Text>
          <Button
            colorScheme="blue"
            leftIcon={<FiHome />}
            onClick={goHome}
          >
            {status === 'authed' ? 'Go to dashboard' : 'Go to sign in'}
          </Button>
        </Box>
      </Center>
    );
  }

  const canEdit = shared.permission === Permission.EDIT;

  const handleSave = async () => {
    await run(() => snippetsApi.updateSnippet(shared.snippetId, { content }), {
      successMessage: 'Changes saved.',
    });
  };

  return (
    <Flex direction="column" h="100vh">
      <Flex
        align="center"
        px={6}
        h="56px"
        borderBottomWidth="1px"
        borderColor="gray.700"
        bg="gray.800"
        flexShrink={0}
        gap={3}
      >
        <HStack
          spacing={2}
          cursor="pointer"
          onClick={goHome}
          title="CodeCollab"
        >
          <Box color="brand.500" fontWeight="bold">
            {'</>'}
          </Box>
          <Heading size="sm" noOfLines={1}>
            {shared.title || 'Shared snippet'}
          </Heading>
        </HStack>

        <Badge
          colorScheme="blue"
          variant="subtle"
          textTransform="none"
          fontWeight="normal"
        >
          {shared.language?.name ?? 'Unknown'}
        </Badge>

        <Tag
          size="sm"
          colorScheme={canEdit ? 'green' : 'gray'}
          variant="subtle"
          gap={1}
        >
          {canEdit ? <FiEdit2 /> : <FiEye />}
          {permissionLabel(shared.permission)}
        </Tag>

        <Box flex="1" />

        <Button
          variant="ghost"
          leftIcon={<FiHome />}
          onClick={goHome}
        >
          {status === 'authed' ? 'Dashboard' : 'Sign in'}
        </Button>

        {canEdit ? (
          <LoadingButton colorScheme="blue" onClick={handleSave} isLoading={submitting}>
            Save
          </LoadingButton>
        ) : null}
      </Flex>

      <Box flex="1" p={4} overflow="hidden">
        <CodeEditor
          value={content}
          onChange={canEdit ? setContent : undefined}
          languageCode={shared.language?.code}
          readOnly={!canEdit}
          height="100%"
        />
      </Box>
    </Flex>
  );
};
