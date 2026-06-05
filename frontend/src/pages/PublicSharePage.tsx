import { Navigate, useParams } from 'react-router-dom';
import { PublicShare } from '../features/public-share/PublicShare';

export const PublicSharePage = () => {
  const { token } = useParams<{ token: string }>();

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return <PublicShare token={token} />;
};
