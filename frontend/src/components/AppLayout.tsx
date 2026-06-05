import type { ReactNode } from 'react';
import {
  Box,
  Flex,
  HStack,
  Heading,
  Spacer,
  Text,
  Menu,
  MenuButton,
  MenuList,
  MenuItem,
  MenuDivider,
  Button,
} from '@chakra-ui/react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { FiChevronDown, FiLogOut, FiUser, FiGrid } from 'react-icons/fi';
import { useAuth } from '../context/auth/useAuth';

interface AppLayoutProps {
  children: ReactNode;
  /** Render children full-bleed (no max width / padding) — used by the editor. */
  fluid?: boolean;
}

export const AppLayout = ({ children, fluid = false }: AppLayoutProps) => {
  const { user, logout } = useAuth();
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

        <Menu>
          <MenuButton
            as={Button}
            variant="ghost"
            size="sm"
            rightIcon={<FiChevronDown />}
          >
            <Text fontSize="sm">{user?.username ?? 'Account'}</Text>
          </MenuButton>
          <MenuList bg="gray.800" borderColor="gray.700">
            <MenuItem
              icon={<FiUser />}
              bg="gray.800"
              _hover={{ bg: 'gray.700' }}
              onClick={() => navigate('/account')}
            >
              Account
            </MenuItem>
            <MenuDivider borderColor="gray.700" />
            <MenuItem
              icon={<FiLogOut />}
              bg="gray.800"
              _hover={{ bg: 'gray.700' }}
              onClick={() => logout()}
            >
              Sign out
            </MenuItem>
          </MenuList>
        </Menu>
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
