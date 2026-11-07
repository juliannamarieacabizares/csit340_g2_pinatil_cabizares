import { useAuth } from '../hooks/useAuth';

export default function DashboardPage() {
  const { user } = useAuth();

  return (
    <>
      <h1>Welcome, {user.name}</h1>
      <p>You are signed in as {user.email}.</p>
      <div className="app-card-grid">
        <section className="app-card">
          <h2>Your account</h2>
          <p>Role: {user.role}</p>
        </section>
        <section className="app-card">
          <h2>Coming soon</h2>
          <p>The main modules of your system will go here.</p>
        </section>
      </div>
    </>
  );
}