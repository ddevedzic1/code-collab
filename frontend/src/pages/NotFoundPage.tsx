import { Box, Button, Center, Heading, Text, VStack } from '@chakra-ui/react';
import { Link as RouterLink } from 'react-router-dom';

export const NotFoundPage = () => (
  <Center minH="100vh" px={4}>
    <VStack spacing={4} textAlign="center">
      <Box color="brand.500" fontWeight="bold" fontSize="4xl">
        404
      </Box>
      <Heading size="md">Page not found</Heading>
      <Text fontSize="sm" color="textColor.medium">
        The page you&apos;re looking for doesn&apos;t exist.
      </Text>
      <Button as={RouterLink} to="/dashboard" colorScheme="blue">
        Go to dashboard
      </Button>
    </VStack>
  </Center>
);
