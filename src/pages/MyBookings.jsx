
import { useCallback, useEffect, useState } from 'react';
import {
  getMyBookings,
  getMyCancellations,
  requestBookingCancellation,
} from '../authApi';
import './MyBookings.css';

export default function MyBookings() {
  const [bookings, setBookings] = useState([]);
  const [cancellations, setCancellations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadBookings = useCallback(async () => {
    const [bookingData, cancellationData] = await Promise.all([
      getMyBookings(),
      getMyCancellations(),
    ]);

    setBookings(bookingData);
    setCancellations(cancellationData);
  }, []);

  useEffect(() => {
    loadBookings()
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [loadBookings]);

  const formatDate = (date) => {
    return new Date(`${date}T00:00:00`).toLocaleDateString(
      'en-PH',
      {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      }
    );
  };

  const formatTime = (time) => {
    return new Date(`2000-01-01T${time}`).toLocaleTimeString(
      'en-PH',
      {
        hour: 'numeric',
        minute: '2-digit',
        hour12: true,
      }
    );
  };

  const formatCurrency = (amount) => {
    return `₱${Number(amount).toFixed(2)}`;
  };

  const getCancellation = (bookingId) => {
    return cancellations.find(
      (cancellation) => cancellation.bookingId === bookingId
    );
  };

  const handleCancellation = async (event) => {
    event.preventDefault();

    if (!selectedBooking || !reason.trim()) {
      setError('Please provide a cancellation reason.');
      return;
    }

    setSubmitting(true);
    setError('');
    setSuccess('');

    try {
      await requestBookingCancellation(
        selectedBooking.bookingId,
        reason.trim()
      );

      await loadBookings();

      setSelectedBooking(null);
      setReason('');
      setSuccess('Your cancellation request has been submitted.');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="my-bookings-page">
      <div className="my-bookings-heading">
        <h1>My Bookings</h1>
        <p>View and track your pickleball court reservations.</p>
      </div>

      {error && (
        <div className="my-bookings-message error">
          {error}
        </div>
      )}

      {success && (
        <div className="my-bookings-message success">
          {success}
        </div>
      )}

      {loading ? (
        <p className="my-bookings-loading">
          Loading your bookings...
        </p>
      ) : bookings.length === 0 ? (
        <div className="my-bookings-empty">
          <h2>No bookings yet</h2>
          <p>Your court reservations will appear here.</p>
        </div>
      ) : (
        <div className="my-bookings-list">
          {bookings.map((booking) => {
            const cancellation = getCancellation(booking.bookingId);

            return (
              <article
                className="my-booking-card"
                key={booking.bookingId}
              >
                <div className="my-booking-header">
                  <div>
                    <span className="my-booking-label">
                      BOOKING REFERENCE
                    </span>
                    <h2>{booking.bookingReference}</h2>
                  </div>

                  <span
                    className={`my-booking-status ${booking.bookingStatus.toLowerCase()}`}
                  >
                    {booking.bookingStatus}
                  </span>
                </div>

                <div className="my-booking-details">
                  <div>
                    <span>Court</span>
                    <strong>{booking.courtName}</strong>
                  </div>

                  <div>
                    <span>Date</span>
                    <strong>{formatDate(booking.bookingDate)}</strong>
                  </div>

                  <div>
                    <span>Time</span>
                    <strong>
                      {formatTime(booking.startTime)} -{' '}
                      {formatTime(booking.endTime)}
                    </strong>
                  </div>

                  <div>
                    <span>Time slot</span>
                    <strong>{booking.slotName}</strong>
                  </div>
                </div>

                <div className="my-booking-total">
                  <span>Total amount</span>
                  <strong>
                    {formatCurrency(booking.totalAmount)}
                  </strong>
                </div>

                {cancellation && (
                  <div className="my-cancellation-info">
                    <div className="my-cancellation-header">
                      <strong>Cancellation Request</strong>
                      <span
                        className={`my-cancellation-status ${cancellation.cancellationStatus.toLowerCase()}`}
                      >
                        {cancellation.cancellationStatus}
                      </span>
                    </div>

                    <p>
                      <strong>Reason:</strong> {cancellation.reason}
                    </p>

                    <p>
                      <strong>Estimated refund:</strong>{' '}
                      {formatCurrency(cancellation.refundAmount)}
                    </p>
                  </div>
                )}

                {!cancellation &&
                  ['PENDING', 'CONFIRMED'].includes(
                    booking.bookingStatus
                  ) && (
                    <div className="my-booking-actions">
                      <button
                        type="button"
                        className="my-cancel-button"
                        onClick={() => {
                          setSelectedBooking(booking);
                          setReason('');
                          setError('');
                          setSuccess('');
                        }}
                      >
                        Request Cancellation
                      </button>
                    </div>
                  )}
              </article>
            );
          })}
        </div>
      )}

      {selectedBooking && (
        <div className="my-cancellation-modal">
          <form
            className="my-cancellation-form"
            onSubmit={handleCancellation}
          >
            <h2>Request Cancellation</h2>

            <p>
              Booking: <strong>{selectedBooking.bookingReference}</strong>
            </p>

            <label htmlFor="cancellationReason">
              Reason for cancellation
            </label>

            <textarea
              id="cancellationReason"
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              placeholder="Please explain why you want to cancel this booking."
              rows={4}
              required
            />

            <div className="my-cancellation-form-actions">
              <button
                type="button"
                className="my-cancel-close-button"
                onClick={() => {
                  setSelectedBooking(null);
                  setReason('');
                }}
                disabled={submitting}
              >
                Back
              </button>

              <button
                type="submit"
                className="my-cancel-submit-button"
                disabled={submitting || !reason.trim()}
              >
                {submitting ? 'Submitting...' : 'Submit Request'}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
