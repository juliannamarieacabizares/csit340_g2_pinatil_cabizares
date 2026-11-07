import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import SessionLoading from './SessionLoading';

// Login and register pages: a signed-in user is sent on to the page they wanted
// (saved by ProtectedRoute in location.state.from), or to the dashboard.
export default function PublicOnlyRoute() {
  const { user, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) return <SessionLoading />;
  if (user) return <Navigate to={location.state?.from?.pathname || '/dashboard'} replace />;
  return <Outlet />;
}