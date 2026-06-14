import type { ReactNode } from 'react';
import {
  Box,
  Flex,
  HStack,
  Heading,
  Spacer,
  Button,
} from '@chakra-ui/react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { FiGrid } from 'react-icons/fi';
import { UserMenu } from './UserMenu';

interface AppLayoutProps {
  children: ReactNode;
  /** Render children full-bleed (no max width / padding) — used by the editor. */
  fluid?: boolean;
}

export const AppLayout = ({ children, fluid = false }: AppLayoutProps) => {
  const navigate = useNavigate();

  return (
    <Flex direction="column" h="100vh">
      <Flex
        as="header"
        align="center"
        px={6}
        h="56px"
        borderBottomWidth="1px"
        borderColor="gray.700"
        bg="gray.800"
        flexShrink={0}
      >
        <HStack
          spacing={2}
          cursor="pointer"
          onClick={() => navigate('/dashboard')}
        >
          <Box color="brand.500" fontWeight="bold" fontSize="lg">
            {'</>'}
          </Box>
          <Heading size="sm" letterSpacing="tight">
            CodeCollab
          </Heading>
        </HStack>

        <HStack spacing={1} ml={8}>
          <Button
            as={RouterLink}
            to="/dashboard"
            variant="ghost"
            size="sm"
            leftIcon={<FiGrid />}
          >
            Dashboard
          </Button>
        </HStack>

        <Spacer />

        <UserMenu />
      </Flex>

      <Box as="main" flex="1" overflow={fluid ? 'hidden' : 'auto'}>
        {fluid ? (
          children
        ) : (
          <Box maxW="1200px" mx="auto" px={6} py={6}>
            {children}
          </Box>
        )}
      </Box>
    </Flex>
  );
};
