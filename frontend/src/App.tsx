import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './components/ProtectedRoute';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { DashboardPage } from './pages/DashboardPage';
import { EditorPage } from './pages/EditorPage';
import { AccountPage } from './pages/AccountPage';
import { PublicSharePage } from './pages/PublicSharePage';
import { NotFoundPage } from './pages/NotFoundPage';

const App = () => (
  <Routes>
    {/* Public routes */}
    <Route path="/login" element={<LoginPage />} />
    <Route path="/register" element={<RegisterPage />} />

    <Route element={<ProtectedRoute />}>
      <Route path="/dashboard" element={<DashboardPage />} />
      <Route path="/editor/:snippetId" element={<EditorPage />} />
      <Route path="/account" element={<AccountPage />} />
      <Route path="/s/:token" element={<PublicSharePage />} />
    </Route>

    {/* Defaults */}
    <Route path="/" element={<Navigate to="/dashboard" replace />} />
    <Route path="*" element={<NotFoundPage />} />
  </Routes>
);

export default App;
