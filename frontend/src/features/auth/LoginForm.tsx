import { useState } from 'react';
import type { FormEvent } from 'react';
import {
  FormControl,
  FormErrorMessage,
  FormLabel,
  Input,
  Link,
  Text,
  VStack,
} from '@chakra-ui/react';
import { Link as RouterLink, useLocation, useNavigate } from 'react-router-dom';
import { AuthCard } from './components/AuthCard';
import { LoadingButton } from '../../components/LoadingButton';
import { useAuth } from '../../context/auth/useAuth';
import { useSubmit } from '../../hooks/useSubmit';
import { validateRequired, type FieldErrors } from '../../lib/validation';

type LoginField = 'username' | 'password';

interface LocationState {
  from?: { pathname?: string };
}

export const LoginForm = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { submitting, run } = useSubmit();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<FieldErrors<LoginField>>({});

  const redirectTo =
    (location.state as LocationState | null)?.from?.pathname ?? '/dashboard';

  const validate = (): boolean => {
    const next: FieldErrors<LoginField> = {
      username: validateRequired(username, 'Username'),
      password: validateRequired(password, 'Password'),
    };
    setErrors(next);
    return !next.username && !next.password;
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    await run(() => login({ username, password }), {
      guard: validate,
      onSuccess: () => navigate(redirectTo, { replace: true }),
    });
  };

  return (
    <AuthCard
      title="Sign in"
      subtitle="Welcome back. Sign in to continue."
      footer={
        <Text>
          Don&apos;t have an account?{' '}
          <Link as={RouterLink} to="/register" color="brand.500">
            Create one
          </Link>
        </Text>
      }
    >
      <form onSubmit={handleSubmit} noValidate>
        <VStack spacing={4} align="stretch">
          <FormControl isInvalid={!!errors.username}>
            <FormLabel fontSize="sm">Username</FormLabel>
            <Input
              value={username}
              onChange={event => setUsername(event.target.value)}
              autoComplete="username"
              autoFocus
            />
            <FormErrorMessage>{errors.username}</FormErrorMessage>
          </FormControl>

          <FormControl isInvalid={!!errors.password}>
            <FormLabel fontSize="sm">Password</FormLabel>
            <Input
              type="password"
              value={password}
              onChange={event => setPassword(event.target.value)}
              autoComplete="current-password"
            />
            <FormErrorMessage>{errors.password}</FormErrorMessage>
          </FormControl>

          <LoadingButton
            type="submit"
            colorScheme="blue"
            isLoading={submitting}
            w="100%"
            mt={2}
          >
            Sign in
          </LoadingButton>
        </VStack>
      </form>
    </AuthCard>
  );
};
