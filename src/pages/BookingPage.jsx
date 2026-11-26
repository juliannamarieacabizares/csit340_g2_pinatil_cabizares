import { useEffect, useState } from 'react';
import {
  getActiveCourt,
  getBookingAvailability,
  createBooking,
} from '../authApi';
import './BookingPage.css';

export default function BookingPage() {
  const [court, setCourt] = useState(null);
  const [date, setDate] = useState(() => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const year = tomorrow.getFullYear();
    const month = String(tomorrow.getMonth() + 1).padStart(2, '0');
    const day = String(tomorrow.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  });

  const [slots, setSlots] = useState([]);
  const [selectedSlot, setSelectedSlot] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    getActiveCourt()
      .then(setCourt)
      .catch((err) => setError(err.message));
  }, []);

  useEffect(() => {
    if (!date) return;

    setLoading(true);
    setError('');
    setSuccess('');
    setSelectedSlot('');

    getBookingAvailability(date)
      .then(setSlots)
      .catch((err) => {
        setSlots([]);
        setError(err.message);
      })
      .finally(() => setLoading(false));
  }, [date]);

  const handleBooking = async () => {
    if (!selectedSlot) {
      setError('Please select an available time slot.');
      return;
    }

    setSubmitting(true);
    setError('');
    setSuccess('');

    try {
      const booking = await createBooking({
        bookingDate: date,
        slotId: Number(selectedSlot),
      });

      setSuccess(
        `Booking created successfully! Your reference number is ${booking.bookingReference}.`
      );

      setSelectedSlot('');

      const updatedSlots = await getBookingAvailability(date);
      setSlots(updatedSlots);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const selectedTimeSlot = slots.find(
    (slot) => String(slot.slotId) === selectedSlot
  );

  return (
    <div className="booking-page">
      <div className="booking-heading">
        <h1>Book a Court</h1>
        <p>Choose your preferred date and available playing time.</p>
      </div>

      {error && <div className="booking-message booking-error">{error}</div>}
      {success && (
        <div className="booking-message booking-success">{success}</div>
      )}

      {court && (
        <section className="booking-court-card">
          <div>
            <span className="booking-label">AVAILABLE COURT</span>
            <h2>{court.courtName}</h2>
            <p>{court.description}</p>
          </div>

          <div className="booking-price">
            <strong>₱{Number(court.hourlyRate).toFixed(2)}</strong>
            <span>per hour</span>
          </div>
        </section>
      )}

      <section className="booking-selection">
        <h2>Select your booking date</h2>

        <input
          type="date"
          value={date}
          min={new Date().toLocaleDateString('en-CA')}
          onChange={(event) => setDate(event.target.value)}
        />
      </section>

      <section className="booking-slots-section">
        <div className="booking-section-heading">
          <h2>Available time slots</h2>
          <span>{slots.filter((slot) => slot.available).length} available</span>
        </div>

        {loading ? (
          <p>Loading available time slots...</p>
        ) : slots.length === 0 ? (
          <p>No time slots available for this date.</p>
        ) : (
          <div className="booking-slots-grid">
            {slots.map((slot) => (
              <button
                key={slot.slotId}
                type="button"
                disabled={!slot.available}
                className={`booking-slot ${
                  selectedSlot === String(slot.slotId) ? 'selected' : ''
                } ${!slot.available ? 'unavailable' : ''}`}
                onClick={() => setSelectedSlot(String(slot.slotId))}
              >
                <strong>{slot.slotName}</strong>
                <span>
                  {slot.startTime.slice(0, 5)} - {slot.endTime.slice(0, 5)}
                </span>
                <small>
                  {slot.available ? 'Available' : 'Booked'}
                </small>
              </button>
            ))}
          </div>
        )}
      </section>

      {selectedTimeSlot && court && (
        <section className="booking-summary">
          <h2>Booking Summary</h2>

          <div>
            <span>Court</span>
            <strong>{court.courtName}</strong>
          </div>

          <div>
            <span>Date</span>
            <strong>{date}</strong>
          </div>

          <div>
            <span>Time</span>
            <strong>
              {selectedTimeSlot.startTime.slice(0, 5)} -{' '}
              {selectedTimeSlot.endTime.slice(0, 5)}
            </strong>
          </div>

          <div>
            <span>Court fee</span>
            <strong>₱{Number(court.hourlyRate).toFixed(2)}</strong>
          </div>

          <button
            type="button"
            className="booking-confirm-button"
            onClick={handleBooking}
            disabled={submitting}
          >
            {submitting ? 'Processing...' : 'Confirm Booking'}
          </button>
        </section>
      )}
    </div>
  );
}