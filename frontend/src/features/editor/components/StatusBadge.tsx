import { Badge, HStack, Spinner } from '@chakra-ui/react';
import { ExecutionStatus, type ExecutionStatus as Status } from '../../../types/execution';

const STATUS_COLOR: Record<Status, string> = {
  [ExecutionStatus.PENDING]: 'gray',
  [ExecutionStatus.RUNNING]: 'blue',
  [ExecutionStatus.COMPLETED]: 'green',
  [ExecutionStatus.FAILED]: 'red',
};

const isActive = (status: Status): boolean =>
  status === ExecutionStatus.PENDING || status === ExecutionStatus.RUNNING;

interface StatusBadgeProps {
  status: Status;
}

export const StatusBadge = ({ status }: StatusBadgeProps) => (
  <Badge
    colorScheme={STATUS_COLOR[status]}
    variant="subtle"
    textTransform="none"
    px={2}
    py={0.5}
    borderRadius="md"
  >
    <HStack spacing={1.5}>
      {isActive(status) ? <Spinner size="xs" speed="0.7s" /> : null}
      <span>{status}</span>
    </HStack>
  </Badge>
);
