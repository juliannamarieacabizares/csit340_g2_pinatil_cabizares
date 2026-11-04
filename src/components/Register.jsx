import { useState } from 'react';

export default function Register({ onSwitchToLogin }) {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: ''
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch('http://localhost:8080/api/users/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        alert('User registered successfully!');
      } else {
        alert('Registration failed!');
      }
    } catch (error) {
      console.error('Error connecting to backend:', error);
      alert('Cannot connect to Spring Boot server.');
    }
  };

  return (
    <div style={{ maxWidth: '350px', margin: '2rem auto', textAlign: 'center' }}>
      <h2>Register Account</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <input 
          type="text" 
          name="name" 
          placeholder="Full Name" 
          value={formData.name} 
          onChange={handleChange} 
          required 
        />
        <input 
          type="email" 
          name="email" 
          placeholder="Email Address" 
          value={formData.email} 
          onChange={handleChange} 
          required 
        />
        <input 
          type="password" 
          name="password" 
          placeholder="Password" 
          value={formData.password} 
          onChange={handleChange} 
          required 
        />
        <button type="submit">Sign Up</button>
      </form>
      <p style={{ marginTop: '1rem' }}>
        Already have an account?{' '}
        <button type="button" onClick={onSwitchToLogin} style={{ background: 'none', border: 'none', color: '#646cff', cursor: 'pointer', textDecoration: 'underline' }}>
          Login here
        </button>
      </p>
    </div>
  );
}