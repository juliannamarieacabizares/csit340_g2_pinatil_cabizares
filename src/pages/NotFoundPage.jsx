import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <main className="auth-panel">
      <h1 className="auth-heading">Page not found</h1>
      <p className="auth-description">The page you are looking for does not exist.</p>
      <p className="auth-switch-row">
        <Link to="/">Go to the home page</Link>
      </p>
    </main>
  );
}