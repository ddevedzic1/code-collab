import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/auth/useAuth';
import { Spinner } from './Spinner';

/**
 * Gates protected routes. While the auth bootstrap is in progress it shows a
 * spinner (so we never flash /login during the validate call); once resolved
 * it either renders the route or redirects anonymous users to /login.
 */
export const ProtectedRoute = () => {
  const { status } = useAuth();
  const location = useLocation();

  if (status === 'bootstrapping') {
    return <Spinner full label="Loading…" />;
  }

  if (status === 'anon') {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return <Outlet />;
};
