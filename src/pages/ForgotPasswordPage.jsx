import { useNavigate, useSearchParams } from 'react-router-dom';
import ForgotPassword from '../components/ForgotPassword';

// Used for /forgot-password (ask for a link) and /reset-password?resetToken=... (set a new password).
export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('resetToken') || '';

  return (
    <ForgotPassword
      token={token}
      onSwitchToLogin={() => navigate('/login')}
      onComplete={() =>
        navigate('/login', {
          replace: true,
          state: { notice: 'Your password has been updated. Sign in with your new password.' },
        })
      }
    />
  );
}