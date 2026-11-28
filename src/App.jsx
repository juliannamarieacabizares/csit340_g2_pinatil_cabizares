import { Navigate, Route, Routes, useSearchParams } from 'react-router-dom';
import AppLayout from './components/AppLayout';
import PlayerLayout from './components/PlayerLayout';
import ProtectedRoute from './components/ProtectedRoute';
import PublicLayout from './components/PublicLayout';
import PublicOnlyRoute from './components/PublicOnlyRoute';
import AdminPage from './pages/AdminPage';
import BookingDetailsPage from './pages/BookingDetailsPage';
import CourtSchedulePage from './pages/CourtSchedulePage';
import DashboardPage from './pages/DashboardPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ItemsPage from './pages/ItemsPage';
import LoginPage from './pages/LoginPage';
import MyBookings from './pages/MyBookings';
import NotFoundPage from './pages/NotFoundPage';
import PaymentPage from './pages/PaymentPage';
import RegisterPage from './pages/RegisterPage';
import './App.css';

// The reset email links to /?resetToken=..., so forward that to the reset page.
function RootRedirect() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('resetToken');
  if (token) {
    return <Navigate to={`/reset-password?resetToken=${encodeURIComponent(token)}`} replace />;
  }
  return <Navigate to="/dashboard" replace />;
}

function App() {
  return (
    <Routes>
      <Route path="/" element={<RootRedirect />} />

      {/* Only for logged-out users */}
      <Route element={<PublicOnlyRoute />}>
        <Route element={<PublicLayout />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>
      </Route>

      {/* Open to everyone */}
      <Route element={<PublicLayout />}>
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ForgotPasswordPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>

      {/* Only for logged-in users */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/items" element={<ItemsPage />} />

          {/* The old single-slot booking page is retired: Court Schedule (below)
              replaces it with multi-hour, multi-court, gear-aware booking.
              Anyone with the old link is sent to the new flow instead. */}
          <Route path="/book-court" element={<Navigate to="/schedule" replace />} />

          {/* Only for admins */}
          <Route element={<ProtectedRoute roles={['ADMIN']} />}>
            <Route path="/admin" element={<AdminPage />} />
          </Route>
        </Route>

        {/* Player pages (shared navbar) */}
        <Route element={<PlayerLayout />}>
          <Route path="/schedule" element={<CourtSchedulePage />} />
          <Route path="/booking" element={<BookingDetailsPage />} />
          <Route path="/payment/:bookingId" element={<PaymentPage />} />
          <Route path="/my-bookings" element={<MyBookings />} />
        </Route>
      </Route>
    </Routes>
  );
}

export default App;
