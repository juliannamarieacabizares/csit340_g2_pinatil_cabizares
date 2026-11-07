import { useCallback, useEffect, useMemo, useState } from 'react';
import { getCurrentUser, logout } from '../authApi';
import { AuthContext } from './AuthContext';

export default function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  // Ask the backend who is logged in when the app starts (keeps you signed in after a refresh).
  useEffect(() => {
    let active = true;
    getCurrentUser()
      .then((currentUser) => {
        if (active) setUser(currentUser);
      })
      .catch(() => {})
      .finally(() => {
        if (active) setIsLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const signOut = useCallback(async () => {
    await logout();
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, isLoading, setUser, signOut }),
    [user, isLoading, signOut],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}