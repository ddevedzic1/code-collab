import type { ReactElement, ReactNode } from 'react';
import { render } from '@testing-library/react';
import { ChakraProvider } from '@chakra-ui/react';
import { MemoryRouter } from 'react-router-dom';
import { AuthContext, type AuthContextValue } from '../context/auth/AuthContext';
import theme from '../theme';

const noop = () => undefined;

const defaultAuth: AuthContextValue = {
  status: 'authed',
  user: { id: 'u1', username: 'alice' },
  login: async () => ({ id: 'u1', username: 'alice' }),
  register: async () => undefined,
  logout: async () => undefined,
  patchUser: noop,
};

interface Options {
  auth?: Partial<AuthContextValue>;
  route?: string;
}

/** Render a component inside the providers it needs (Chakra, Router, Auth). */
export const renderWithProviders = (
  ui: ReactElement,
  { auth, route = '/' }: Options = {}
) => {
  const authValue: AuthContextValue = { ...defaultAuth, ...auth };
  const wrapper = ({ children }: { children: ReactNode }) => (
    <ChakraProvider theme={theme}>
      <MemoryRouter initialEntries={[route]}>
        <AuthContext.Provider value={authValue}>
          {children}
        </AuthContext.Provider>
      </MemoryRouter>
    </ChakraProvider>
  );
  return render(ui, { wrapper });
};
