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
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { AuthCard } from './components/AuthCard';
import { LoadingButton } from '../../components/LoadingButton';
import { useAuth } from '../../context/auth/useAuth';
import { useSubmit } from '../../hooks/useSubmit';
import {
  validateEmail,
  validatePassword,
  validateUsername,
  type FieldErrors,
} from '../../lib/validation';

type RegisterField = 'username' | 'email' | 'password';

export const RegisterForm = () => {
  const { register } = useAuth();
  const navigate = useNavigate();
  const { submitting, run } = useSubmit();

  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<FieldErrors<RegisterField>>({});

  const validate = (): boolean => {
    const next: FieldErrors<RegisterField> = {
      username: validateUsername(username),
      email: validateEmail(email),
      password: validatePassword(password),
    };
    setErrors(next);
    return !next.username && !next.email && !next.password;
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    await run(() => register({ username, email, password }), {
      guard: validate,
      successMessage: 'Account created. Welcome to CodeCollab!',
      onSuccess: () => navigate('/dashboard', { replace: true }),
    });
  };

  return (
    <AuthCard
      title="Create account"
      subtitle="Start writing, running and sharing code."
      footer={
        <Text>
          Already have an account?{' '}
          <Link as={RouterLink} to="/login" color="brand.500">
            Sign in
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

          <FormControl isInvalid={!!errors.email}>
            <FormLabel fontSize="sm">Email</FormLabel>
            <Input
              type="email"
              value={email}
              onChange={event => setEmail(event.target.value)}
              autoComplete="email"
            />
            <FormErrorMessage>{errors.email}</FormErrorMessage>
          </FormControl>

          <FormControl isInvalid={!!errors.password}>
            <FormLabel fontSize="sm">Password</FormLabel>
            <Input
              type="password"
              value={password}
              onChange={event => setPassword(event.target.value)}
              autoComplete="new-password"
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
            Create account
          </LoadingButton>
        </VStack>
      </form>
    </AuthCard>
  );
};
