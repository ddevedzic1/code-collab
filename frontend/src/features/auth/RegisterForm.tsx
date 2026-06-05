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
  useToast,
} from '@chakra-ui/react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { AuthCard } from './components/AuthCard';
import { LoadingButton } from '../../components/LoadingButton';
import { useAuth } from '../../context/auth/useAuth';
import { errorToast, successToast } from '../../components/toast';
import {
  validateEmail,
  validatePassword,
  validateUsername,
  type FieldErrors,
} from '../../lib/validation';
import { isAppError } from '../../lib/normalizeError';

type RegisterField = 'username' | 'email' | 'password';

export const RegisterForm = () => {
  const { register } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();

  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<FieldErrors<RegisterField>>({});
  const [submitting, setSubmitting] = useState(false);

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
    if (submitting || !validate()) {
      return;
    }
    setSubmitting(true);
    try {
      await register({ username, email, password });
      toast(successToast('Account created. Welcome to CodeCollab!'));
      navigate('/dashboard', { replace: true });
    } catch (error) {
      if (isAppError(error)) {
        toast(errorToast(error));
      }
    } finally {
      setSubmitting(false);
    }
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
