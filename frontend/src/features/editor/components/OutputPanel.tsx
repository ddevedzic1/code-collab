import {
  Box,
  Center,
  HStack,
  Text,
  VStack,
} from '@chakra-ui/react';
import { FiTerminal } from 'react-icons/fi';
import { StatusBadge } from './StatusBadge';
import type { Execution } from '../../../types/execution';

interface OutputPanelProps {
  execution: Execution | null;
}

interface OutputStreamProps {
  label: string;
  content: string;
  tone?: 'normal' | 'error';
}

const OutputStream = ({ label, content, tone = 'normal' }: OutputStreamProps) => (
  <Box>
    <Text
      fontSize="xs"
      fontWeight="semibold"
      color="textColor.medium"
      textTransform="uppercase"
      mb={1}
    >
      {label}
    </Text>
    <Box
      as="pre"
      fontFamily="mono"
      fontSize="13px"
      whiteSpace="pre-wrap"
      wordBreak="break-word"
      bg="gray.900"
      borderWidth="1px"
      borderColor="gray.700"
      borderRadius="md"
      p={3}
      color={tone === 'error' ? 'red.300' : 'textColor.light'}
      maxH="320px"
      overflowY="auto"
    >
      {content}
    </Box>
  </Box>
);

export const OutputPanel = ({ execution }: OutputPanelProps) => {
  if (!execution) {
    return (
      <Center h="100%" flexDirection="column" color="textColor.medium" px={4}>
        <FiTerminal size={28} />
        <Text mt={3} fontSize="sm" textAlign="center">
          Run the snippet to see its output here.
        </Text>
      </Center>
    );
  }

  const hasStdout = execution.stdout != null && execution.stdout !== '';
  const hasStderr = execution.stderr != null && execution.stderr !== '';

  return (
    <VStack align="stretch" spacing={4} p={4} overflowY="auto" h="100%">
      <HStack justify="space-between" wrap="wrap" gap={2}>
        <StatusBadge status={execution.status} />
        <HStack spacing={4} fontSize="xs" color="textColor.medium">
          {execution.exitCode != null ? (
            <Text>
              exit code:{' '}
              <Text
                as="span"
                color={execution.exitCode === 0 ? 'green.300' : 'red.300'}
                fontWeight="semibold"
              >
                {execution.exitCode}
              </Text>
            </Text>
          ) : null}
          {execution.durationMs != null ? (
            <Text>duration: {execution.durationMs} ms</Text>
          ) : null}
        </HStack>
      </HStack>

      {hasStdout ? (
        <OutputStream label="stdout" content={execution.stdout as string} />
      ) : null}

      {hasStderr ? (
        <OutputStream
          label="stderr"
          content={execution.stderr as string}
          tone="error"
        />
      ) : null}

      {!hasStdout && !hasStderr ? (
        <Text fontSize="sm" color="textColor.medium">
          No output was produced.
        </Text>
      ) : null}
    </VStack>
  );
};
