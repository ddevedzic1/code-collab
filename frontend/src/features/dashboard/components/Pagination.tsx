import { HStack, IconButton, Text } from '@chakra-ui/react';
import { FiChevronLeft, FiChevronRight } from 'react-icons/fi';

interface PaginationProps {
  /** Zero-indexed current page. */
  currentPage: number;
  totalPages: number;
  totalElements: number;
  onPageChange: (page: number) => void;
}

export const Pagination = ({
  currentPage,
  totalPages,
  totalElements,
  onPageChange,
}: PaginationProps) => {
  if (totalPages <= 1) {
    return null;
  }

  const canPrev = currentPage > 0;
  const canNext = currentPage + 1 < totalPages;

  return (
    <HStack justify="space-between" w="100%" pt={2}>
      <Text fontSize="sm" color="textColor.medium">
        Page {currentPage + 1} of {totalPages} · {totalElements} total
      </Text>
      <HStack spacing={2}>
        <IconButton
          aria-label="Previous page"
          icon={<FiChevronLeft />}
          size="sm"
          variant="outline"
          borderColor="gray.700"
          isDisabled={!canPrev}
          onClick={() => onPageChange(currentPage - 1)}
        />
        <IconButton
          aria-label="Next page"
          icon={<FiChevronRight />}
          size="sm"
          variant="outline"
          borderColor="gray.700"
          isDisabled={!canNext}
          onClick={() => onPageChange(currentPage + 1)}
        />
      </HStack>
    </HStack>
  );
};
