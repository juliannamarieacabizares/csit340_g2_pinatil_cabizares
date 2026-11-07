import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import SessionLoading from './SessionLoading';

// Use <ProtectedRoute /> for "must be logged in" and <ProtectedRoute roles={['ADMIN']} /> for role pages.
// This only hides pages. The real protection is the AuthInterceptor in Spring.
export default function ProtectedRoute({ roles }) {
  const { user, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) return <SessionLoading />;
  if (!user) return <Navigate to="/login" replace state={{ from: location }} />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/dashboard" replace />;
  return <Outlet />;
}