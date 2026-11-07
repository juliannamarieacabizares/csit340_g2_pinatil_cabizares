import { useNavigate } from 'react-router-dom';
import Register from '../components/Register';

export default function RegisterPage() {
  const navigate = useNavigate();

  return (
    <Register
      onSwitchToLogin={(message = '') => navigate('/login', { state: { notice: message } })}
    />
  );
}