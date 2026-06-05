import { Navigate } from 'react-router-dom';
import { LoginForm } from '../features/auth/LoginForm';
import { Spinner } from '../components/Spinner';
import { useAuth } from '../context/auth/useAuth';

export const LoginPage = () => {
  const { status } = useAuth();

  if (status === 'bootstrapping') {
    return <Spinner full label="Loading…" />;
  }
  if (status === 'authed') {
    return <Navigate to="/dashboard" replace />;
  }
  return <LoginForm />;
};
