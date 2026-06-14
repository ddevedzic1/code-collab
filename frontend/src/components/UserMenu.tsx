import {
  Button,
  Menu,
  MenuButton,
  MenuDivider,
  MenuItem,
  MenuList,
  Text,
} from '@chakra-ui/react';
import { FiChevronDown, FiLogOut, FiUser } from 'react-icons/fi';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/auth/useAuth';

/** Username dropdown with account and sign-out actions, used in app headers. */
export const UserMenu = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
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
  );
};
