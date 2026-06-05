import { Navigate } from 'react-router-dom';
import { RegisterForm } from '../features/auth/RegisterForm';
import { Spinner } from '../components/Spinner';
import { useAuth } from '../context/auth/useAuth';

export const RegisterPage = () => {
  const { status } = useAuth();

  if (status === 'bootstrapping') {
    return <Spinner full label="Loading…" />;
  }
  if (status === 'authed') {
    return <Navigate to="/dashboard" replace />;
  }
  return <RegisterForm />;
};
