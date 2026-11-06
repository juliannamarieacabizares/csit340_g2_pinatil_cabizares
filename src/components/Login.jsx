import { useState } from 'react';
import { login } from '../authApi';

export default function Login({ onSwitchToRegister, onForgotPassword, onLogin, notice }) {
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (e) => {
    setCredentials({ ...credentials, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsSubmitting(true);
    try {
      onLogin(await login(credentials));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="auth-panel">
      <div className="brand"><span className="brand-mark">T</span> Test Account</div>
      <h1 className="auth-heading">Welcome back</h1>
      <p className="auth-description">Sign in to continue to your account.</p>
      {notice && <p className="form-message form-message-success" role="status">{notice}</p>}
      {error && <p className="form-message form-message-error" role="alert">{error}</p>}
      <form className="auth-form" onSubmit={handleSubmit}>
        <div className="form-field">
          <label htmlFor="login-email">Email address</label>
          <input
            id="login-email"
            type="email"
            name="email"
            autoComplete="email"
            placeholder="name@example.com"
            value={credentials.email}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-field">
          <label htmlFor="login-password">Password</label>
          <input
            id="login-password"
            type="password"
            name="password"
            autoComplete="current-password"
            placeholder="Enter your password"
            value={credentials.password}
            onChange={handleChange}
            required
          />
        </div>
        <button className="auth-inline-action" type="button" onClick={onForgotPassword}>Forgot password?</button>
        <button className="auth-submit" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Signing in...' : 'Sign in'}
        </button>
      </form>
      <p className="auth-switch-row">
        Don't have an account?{' '}
        <button className="auth-switch" type="button" onClick={onSwitchToRegister}>
          Create one
        </button>
      </p>
    </main>
  );
}