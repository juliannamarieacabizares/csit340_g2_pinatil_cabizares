import { useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import './PlayerLayout.css';

function initials(name) {
  return (name || '')
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word[0])
    .join('')
    .toUpperCase();
}

export default function PlayerLayout() {
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  const { pathname } = useLocation();
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

  // The booking page is part of the "Court Schedule" flow, and the payment page
  // belongs to "My Bookings", so keep those pills active there.
  const scheduleClass = ({ isActive }) =>
    (isActive || pathname.startsWith('/booking') ? 'active' : '');
  const bookingsClass = ({ isActive }) =>
    (isActive || pathname.startsWith('/payment') ? 'active' : '');

  return (
    <div className="pl-shell">
      <header className="pl-nav">
        <div className="pl-nav-inner">
          <div className="pl-brand">PICKLEBALL COURT RESERVATION &amp; RENTAL</div>

          <nav className="pl-links" aria-label="Main">
            <NavLink to="/schedule" className={scheduleClass}>Court Schedule</NavLink>
            <NavLink to="/my-bookings" className={bookingsClass}>My Bookings</NavLink>
            {/* Only shown to admins. The real protection is the server (AuthInterceptor). */}
            {user.role === 'ADMIN' && <NavLink to="/admin">Admin</NavLink>}
          </nav>

          <div className="pl-user">
            <span className="pl-name">{user.name}</span>
            <button type="button" className="pl-logout" onClick={handleSignOut}>Log out</button>
            <div className="pl-avatar" aria-hidden="true">{initials(user.name)}</div>
          </div>
        </div>
      </header>

      {error && <p className="pl-error" role="alert">{error}</p>}

      {/* Each player page puts its own content in a .pl-container */}
      <main>
        <Outlet />
      </main>
    </div>
  );
}
