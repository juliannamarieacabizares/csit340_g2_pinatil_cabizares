import { useEffect, useState } from 'react';
import { adminPing } from '../authApi';

export default function AdminPage() {
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    adminPing()
      .then((data) => {
        if (active) setMessage(data.message);
      })
      .catch((requestError) => {
        if (active) setError(requestError.message);
      });
    return () => {
      active = false;
    };
  }, []);

  return (
    <>
      <h1>Admin area</h1>
      <p>Only users with the ADMIN role can see this page.</p>
      {message && <p>Server says: {message}</p>}
      {error && <p className="app-error" role="alert">{error}</p>}
    </>
  );
}