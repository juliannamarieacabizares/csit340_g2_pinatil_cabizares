import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import './AppLayout.css';

export default function AppLayout() {
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState('');

  const handleSignOut = async () => {
    setError('');
    try {
      await signOut();
      navigate('/login', { replace: true });
    } catch (requestError) {
      setError(requestError.message);
    }
  };

  return (
    <div className="app-shell">
      <header className="app-navbar">
        <div className="app-navbar-brand">
          <span className="app-brand-mark">T</span> Test Account
        </div>
        <nav className="app-navbar-links" aria-label="Main">
          <NavLink to="/dashboard">Dashboard</NavLink>
          <NavLink to="/items">Items</NavLink>
          {user.role === 'ADMIN' && <NavLink to="/admin">Admin</NavLink>}
        </nav>
        <div className="app-navbar-user">
          <span className="app-user-name">{user.name}</span>
          <span className="app-role-badge">{user.role}</span>
          <button type="button" className="app-signout" onClick={handleSignOut}>Sign out</button>
        </div>
      </header>
      {error && <p className="app-error" role="alert">{error}</p>}
      <main className="app-content">
        <Outlet />
      </main>
    </div>
  );
}