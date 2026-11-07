import { useLocation, useNavigate } from 'react-router-dom';
import Login from '../components/Login';
import { useAuth } from '../hooks/useAuth';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { setUser } = useAuth();

  // After setUser, PublicOnlyRoute sends the user to the right page by itself.
  return (
    <Login
      notice={location.state?.notice || ''}
      onLogin={setUser}
      onSwitchToRegister={() => navigate('/register')}
      onForgotPassword={() => navigate('/forgot-password')}
    />
  );
}