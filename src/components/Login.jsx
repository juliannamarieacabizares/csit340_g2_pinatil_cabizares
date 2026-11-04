import { useState } from 'react';

export default function Login({ onSwitchToRegister }) {
  const [credentials, setCredentials] = useState({ email: '', password: '' });

  const handleChange = (e) => {
    setCredentials({ ...credentials, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    alert(`Logging in with ${credentials.email}`);
  };

  return (
    <div style={{ maxWidth: '350px', margin: '2rem auto', textAlign: 'center' }}>
      <h2>Login</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <input 
          type="email" 
          name="email" 
          placeholder="Email Address" 
          value={credentials.email} 
          onChange={handleChange} 
          required 
        />
        <input 
          type="password" 
          name="password" 
          placeholder="Password" 
          value={credentials.password} 
          onChange={handleChange} 
          required 
        />
        <button type="submit">Sign In</button>
      </form>
      <p style={{ marginTop: '1rem' }}>
        Don't have an account?{' '}
        <button type="button" onClick={onSwitchToRegister} style={{ background: 'none', border: 'none', color: '#646cff', cursor: 'pointer', textDecoration: 'underline' }}>
          Register here
        </button>
      </p>
    </div>
  );
}