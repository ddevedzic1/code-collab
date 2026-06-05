import type { ReactNode } from 'react';
import { Box, Center, Heading, HStack, Text, VStack } from '@chakra-ui/react';

interface AuthCardProps {
  title: string;
  subtitle?: string;
  children: ReactNode;
  footer?: ReactNode;
}

export const AuthCard = ({
  title,
  subtitle,
  children,
  footer,
}: AuthCardProps) => (
  <Center minH="100vh" px={4}>
    <VStack spacing={6} w="100%" maxW="400px">
      <HStack spacing={2}>
        <Box color="brand.500" fontWeight="bold" fontSize="2xl">
          {'</>'}
        </Box>
        <Heading size="md">CodeCollab</Heading>
      </HStack>

      <Box
        w="100%"
        bg="gray.800"
        borderWidth="1px"
        borderColor="gray.700"
        borderRadius="lg"
        p={8}
      >
        <VStack spacing={1} align="stretch" mb={6}>
          <Heading size="md">{title}</Heading>
          {subtitle ? (
            <Text fontSize="sm" color="textColor.medium">
              {subtitle}
            </Text>
          ) : null}
        </VStack>
        {children}
      </Box>

      {footer ? (
        <Text fontSize="sm" color="textColor.medium">
          {footer}
        </Text>
      ) : null}
    </VStack>
  </Center>
);
