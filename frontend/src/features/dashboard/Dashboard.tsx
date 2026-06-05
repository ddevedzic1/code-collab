import { useEffect, useMemo, useState } from 'react';
import {
  Box,
  Button,
  Center,
  Heading,
  HStack,
  SimpleGrid,
  Text,
  useDisclosure,
  VStack,
} from '@chakra-ui/react';
import { FiPlus, FiFileText } from 'react-icons/fi';
import { SnippetFilters } from './components/SnippetFilters';
import { SnippetCard } from './components/SnippetCard';
import { Pagination } from './components/Pagination';
import { NewSnippetModal } from './components/NewSnippetModal';
import { Spinner } from '../../components/Spinner';
import { ErrorMessage } from '../../components/ErrorMessage';
import { useSnippets } from '../../hooks/useSnippets';
import { useDebouncedValue } from '../../hooks/useDebouncedValue';
import type { SnippetListParams } from '../../types/snippet';

const PAGE_SIZE = 12;

export const Dashboard = () => {
  const { isOpen, onOpen, onClose } = useDisclosure();

  const [title, setTitle] = useState('');
  const [languageId, setLanguageId] = useState('');
  const [page, setPage] = useState(0);

  const debouncedTitle = useDebouncedValue(title);

  // Reset to the first page whenever a filter changes.
  useEffect(() => {
    setPage(0);
  }, [debouncedTitle, languageId]);

  const params = useMemo<SnippetListParams>(
    () => ({
      title: debouncedTitle || undefined,
      languageId: languageId || undefined,
      page,
      size: PAGE_SIZE,
      sort: 'title,asc',
    }),
    [debouncedTitle, languageId, page]
  );

  const { page: result, loading, error, refetch } = useSnippets(params);

  const snippets = result?.result ?? [];
  const isEmpty = !loading && !error && snippets.length === 0;
  const hasFilters = debouncedTitle !== '' || languageId !== '';

  return (
    <VStack align="stretch" spacing={5}>
      <HStack>
        <Heading size="lg">Your snippets</Heading>
        <Box flex="1" />
        <Button colorScheme="blue" leftIcon={<FiPlus />} onClick={onOpen}>
          New snippet
        </Button>
      </HStack>

      <SnippetFilters
        title={title}
        onTitleChange={setTitle}
        languageId={languageId}
        onLanguageChange={setLanguageId}
      />

      {error ? <ErrorMessage error={error} /> : null}

      {loading ? (
        <Box py={16}>
          <Spinner label="Loading snippets…" />
        </Box>
      ) : isEmpty ? (
        <Center
          py={16}
          flexDirection="column"
          color="textColor.medium"
          borderWidth="1px"
          borderColor="gray.700"
          borderStyle="dashed"
          borderRadius="lg"
        >
          <FiFileText size={32} />
          <Text mt={3} fontWeight="medium" color="textColor.light">
            {hasFilters ? 'No snippets match your filters' : 'No snippets yet'}
          </Text>
          <Text fontSize="sm">
            {hasFilters
              ? 'Try adjusting your search or language filter.'
              : 'Create your first snippet to get started.'}
          </Text>
          {!hasFilters ? (
            <Button mt={4} size="sm" colorScheme="blue" leftIcon={<FiPlus />} onClick={onOpen}>
              New snippet
            </Button>
          ) : null}
        </Center>
      ) : (
        <>
          <SimpleGrid columns={{ base: 1, md: 2, lg: 3 }} spacing={4}>
            {snippets.map(snippet => (
              <SnippetCard
                key={snippet.id}
                snippet={snippet}
                onDeleted={refetch}
              />
            ))}
          </SimpleGrid>

          {result ? (
            <Pagination
              currentPage={result.currentPage}
              totalPages={result.totalPages}
              totalElements={result.totalElements}
              onPageChange={setPage}
            />
          ) : null}
        </>
      )}

      <NewSnippetModal isOpen={isOpen} onClose={onClose} />
    </VStack>
  );
};
