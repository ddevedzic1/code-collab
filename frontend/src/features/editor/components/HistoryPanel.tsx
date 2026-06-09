import { useEffect } from 'react';
import {
  Box,
  Center,
  HStack,
  IconButton,
  Text,
  Tooltip,
  VStack,
} from '@chakra-ui/react';
import { FiClock, FiRefreshCw } from 'react-icons/fi';
import { StatusBadge } from './StatusBadge';
import { Spinner } from '../../../components/Spinner';
import { ErrorMessage } from '../../../components/ErrorMessage';
import { useExecutions } from '../../../hooks/useExecutions';
import type { Execution } from '../../../types/execution';

interface HistoryPanelProps {
  snippetId: string;
  /** Bumping this value forces a refetch (e.g. after a new run completes). */
  refreshToken: number;
  activeExecutionId?: string;
  onSelect: (execution: Execution) => void;
}

const PAGE_SIZE = 20;

export const HistoryPanel = ({
  snippetId,
  refreshToken,
  activeExecutionId,
  onSelect,
}: HistoryPanelProps) => {
  const { page, loading, error, refetch } = useExecutions({
    snippetId,
    page: 0,
    size: PAGE_SIZE,
    sort: 'createdAt,desc',
  });

  useEffect(() => {
    if (refreshToken > 0) {
      refetch();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [refreshToken]);

  const executions = page?.result ?? [];

  if (loading && !page) {
    return <Spinner label="Loading history…" />;
  }

  if (error) {
    return (
      <Box p={4}>
        <ErrorMessage error={error} />
      </Box>
    );
  }

  if (executions.length === 0) {
    return (
      <Center h="100%" flexDirection="column" color="textColor.medium" px={4}>
        <FiClock size={28} />
        <Text mt={3} fontSize="sm" textAlign="center">
          No runs yet. Past executions will appear here.
        </Text>
      </Center>
    );
  }

  return (
    <VStack align="stretch" spacing={0} h="100%" overflowY="auto">
      <HStack justify="space-between" px={4} py={2} flexShrink={0}>
        <Text fontSize="xs" color="textColor.medium">
          {page?.totalElements ?? executions.length} run
          {(page?.totalElements ?? 0) === 1 ? '' : 's'}
        </Text>
        <Tooltip label="Refresh">
          <IconButton
            aria-label="Refresh history"
            icon={<FiRefreshCw />}
            size="xs"
            variant="ghost"
            onClick={refetch}
          />
        </Tooltip>
      </HStack>

      {executions.map(execution => {
        const isActive = execution.id === activeExecutionId;
        return (
          <HStack
            key={execution.id}
            px={4}
            py={3}
            spacing={3}
            cursor="pointer"
            borderTopWidth="1px"
            borderColor="gray.700"
            bg={isActive ? 'gray.700' : 'transparent'}
            _hover={{ bg: 'gray.700' }}
            onClick={() => onSelect(execution)}
          >
            <StatusBadge status={execution.status} />
            <Box flex="1" />
            <Text fontSize="xs" color="textColor.medium">
              {execution.durationMs != null
                ? `${execution.durationMs} ms`
                : '—'}
            </Text>
            <Text
              fontSize="xs"
              color={
                execution.exitCode === 0
                  ? 'green.300'
                  : execution.exitCode != null
                    ? 'red.300'
                    : 'textColor.medium'
              }
            >
              exit {execution.exitCode ?? '—'}
            </Text>
          </HStack>
        );
      })}
    </VStack>
  );
};
