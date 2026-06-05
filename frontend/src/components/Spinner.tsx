import { Center, Spinner as ChakraSpinner, Text, VStack } from '@chakra-ui/react';

interface SpinnerProps {
  /** Fill the viewport height (use for full-page loading states). */
  full?: boolean;
  label?: string;
}

export const Spinner = ({ full = false, label }: SpinnerProps) => (
  <Center h={full ? '100vh' : '100%'} w="100%" py={full ? 0 : 8}>
    <VStack spacing={3}>
      <ChakraSpinner
        thickness="3px"
        speed="0.65s"
        color="brand.500"
        size="lg"
      />
      {label ? (
        <Text fontSize="sm" color="textColor.medium">
          {label}
        </Text>
      ) : null}
    </VStack>
  </Center>
);
