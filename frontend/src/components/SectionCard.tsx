import type { ReactNode } from 'react';
import { Box, Heading, Text, VStack } from '@chakra-ui/react';

interface SectionCardProps {
  title: string;
  description?: string;
  children: ReactNode;
  /** Use a red border to mark a destructive section. */
  danger?: boolean;
}

export const SectionCard = ({
  title,
  description,
  children,
  danger = false,
}: SectionCardProps) => (
  <Box
    bg="gray.800"
    borderWidth="1px"
    borderColor={danger ? 'red.700' : 'gray.700'}
    borderRadius="lg"
    p={6}
  >
    <VStack align="stretch" spacing={1} mb={4}>
      <Heading size="sm" color={danger ? 'red.300' : 'textColor.light'}>
        {title}
      </Heading>
      {description ? (
        <Text fontSize="sm" color="textColor.medium">
          {description}
        </Text>
      ) : null}
    </VStack>
    {children}
  </Box>
);
