import { useEffect, useState } from 'react';
import Login from './components/Login';
import Register from './components/Register';
import ForgotPassword from './components/ForgotPassword';
import { getCurrentUser, logout } from './authApi';
import './App.css';

function App() {
  const [resetToken, setResetToken] = useState(() => new URLSearchParams(window.location.search).get('resetToken') || '');
  const [currentView, setCurrentView] = useState(resetToken ? 'reset' : 'register');
  const [user, setUser] = useState(null);
  const [isCheckingSession, setIsCheckingSession] = useState(!resetToken);
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    if (resetToken) return undefined;

    let active = true;
    getCurrentUser()
      .then((currentUser) => {
        if (active) setUser(currentUser);
      })
      .catch(() => {})
      .finally(() => {
        if (active) setIsCheckingSession(false);
      });

    return () => {
      active = false;
    };
  }, [resetToken]);

  const handleLogout = async () => {
    setError('');
    try {
      await logout();
      setUser(null);
      setCurrentView('login');
    } catch (requestError) {
      setError(requestError.message);
    }
  };

  const handlePasswordResetComplete = () => {
    window.history.replaceState({}, '', window.location.pathname);
    setResetToken('');
    setNotice('Your password has been updated. Sign in with your new password.');
    setCurrentView('login');
  };

  if (isCheckingSession) {
    return <div className="App"><main className="auth-panel auth-loading">Checking your session...</main></div>;
  }

  if (user) {
    return (
      <div className="App">
        <main className="auth-panel">
          <div className="brand"><span className="brand-mark">T</span> Test Account</div>
          <h1 className="auth-heading">You’re signed in</h1>
          <p className="auth-description">Signed in as {user.email}</p>
          {error && <p className="form-message form-message-error" role="alert">{error}</p>}
          <button className="auth-submit" type="button" onClick={handleLogout}>Sign out</button>
        </main>
      </div>
    );
  }

  return (
    <div className="App">
      {currentView === 'register' && (
        <Register
          onSwitchToLogin={(message = '') => {
            setNotice(message);
            setCurrentView('login');
          }}
        />
      )}
      {currentView === 'login' && (
        <Login
          notice={notice}
          onLogin={setUser}
          onSwitchToRegister={() => {
            setNotice('');
            setCurrentView('register');
          }}
          onForgotPassword={() => {
            setNotice('');
            setCurrentView('forgot');
          }}
        />
      )}
      {(currentView === 'forgot' || currentView === 'reset') && (
        <ForgotPassword
          token={currentView === 'reset' ? resetToken : ''}
          onSwitchToLogin={() => {
            if (resetToken) {
              window.history.replaceState({}, '', window.location.pathname);
              setResetToken('');
            }
            setCurrentView('login');
          }}
          onComplete={handlePasswordResetComplete}
        />
      )}
    </div>
  );
}

export default App;