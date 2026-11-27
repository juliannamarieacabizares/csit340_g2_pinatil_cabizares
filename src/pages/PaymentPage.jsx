import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getBooking, submitPayment } from '../playerApi';
import './PaymentPage.css';

// "11:00" -> "11:00 AM", "13:00" -> "01:00 PM"
function clock(t) {
  const [h, m] = t.split(':').map(Number);
  const ampm = h >= 12 ? 'PM' : 'AM';
  const hh = String(h % 12 === 0 ? 12 : h % 12).padStart(2, '0');
  return `${hh}:${String(m).padStart(2, '0')} ${ampm}`;
}

// "2026-12-05" -> "Sat, December 5, 2026"
function longDate(dateStr) {
  return new Date(`${dateStr}T00:00:00`).toLocaleDateString('en-US', {
    weekday: 'short', month: 'long', day: 'numeric', year: 'numeric',
  });
}

function peso(n) {
  return `₱${Number(n).toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function mmss(totalSeconds) {
  const m = Math.floor(totalSeconds / 60);
  const s = totalSeconds % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

// What the player sees for the booking and payment, from the server's answer.
function getState(details) {
  const payment = details.payment;
  if (details.status === 'CONFIRMED') return 'confirmed';
  if (details.status === 'CANCELLED') return 'cancelled';
  if (details.expired) return 'expired';
  if (payment && payment.status === 'SUBMITTED') return 'submitted';
  return 'awaiting'; // no payment yet, or the last one was rejected
}

const HERO_TEXT = {
  awaiting: 'Pay by GCash and send us the reference number.',
  submitted: 'We got your reference number. The admin will check it soon.',
  confirmed: 'You are all set. See you on the court!',
  expired: 'The 30-minute hold ran out, so the court was released.',
  cancelled: 'This booking was cancelled.',
};

const STATUS_CHIP = {
  submitted: 'Under review',
  confirmed: 'Confirmed',
  expired: 'Expired',
  cancelled: 'Cancelled',
};

export default function PaymentPage() {
  const { bookingId } = useParams();

  const [details, setDetails] = useState(null);
  const [loadError, setLoadError] = useState('');
  const [endsAt, setEndsAt] = useState(0); // when the hold ends (milliseconds)
  const [now, setNow] = useState(() => Date.now());
  const [reference, setReference] = useState('');
  const [submitError, setSubmitError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // The server sends the seconds left, so the countdown does not depend on this computer's clock.
  const applyDetails = useCallback((data) => {
    setDetails(data);
    setEndsAt(Date.now() + data.secondsLeft * 1000);
    setNow(Date.now());
  }, []);

  const load = useCallback(
    () => getBooking(bookingId).then(applyDetails).catch((e) => setLoadError(e.message)),
    [bookingId, applyDetails],
  );

  useEffect(() => {
    load();
  }, [load]);

  const state = details ? getState(details) : null;
  const remaining = Math.max(0, Math.ceil((endsAt - now) / 1000));

  // Tick every second while we wait for the payment. At 00:00 ask the server again.
  useEffect(() => {
    if (state !== 'awaiting' || endsAt === 0) return undefined;
    const timer = setInterval(() => {
      setNow(Date.now());
      if (Date.now() >= endsAt) {
        clearInterval(timer);
        load();
      }
    }, 1000);
    return () => clearInterval(timer);
  }, [state, endsAt, load]);

  if (loadError && !details) {
    return (
      <div className="pl-container">
        <div className="pp-message">
          <h2>We could not open this booking</h2>
          <p>{loadError}</p>
          <Link to="/schedule" className="pp-back">Back to Court Schedule</Link>
        </div>
      </div>
    );
  }

  if (!details) {
    return (
      <div className="pl-container">
        <p className="pp-loading">Loading your booking…</p>
      </div>
    );
  }

  const payment = details.payment;
  const hasLiveHold = state === 'awaiting' && details.secondsLeft > 0;
  const courtLabel = `${details.courtName}${details.courtDescription ? ` · ${details.courtDescription}` : ''}`;
  const gearText = details.gear.length === 0
    ? 'No rental gear'
    : details.gear.map((g) => `${g.quantity} × ${g.name}`).join(', ');

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitError('');

    const digits = reference.replace(/[\s-]/g, '');
    if (!/^\d{10,20}$/.test(digits)) {
      setSubmitError('Please enter the GCash reference number (10 to 20 digits).');
      return;
    }

    setSubmitting(true);
    try {
      const updated = await submitPayment(bookingId, reference);
      applyDetails(updated);
      setReference('');
    } catch (error) {
      setSubmitError(error.message);
      load(); // the hold may have ended, so refresh what the server knows
    } finally {
      setSubmitting(false);
    }
  }

  // Payment status chip (left card)
  let paymentChip = { text: 'Not yet submitted', cls: 'none' };
  if (payment) {
    if (payment.status === 'SUBMITTED') paymentChip = { text: 'Submitted · admin is checking', cls: 'submitted' };
    else if (payment.status === 'PAID') paymentChip = { text: 'Paid', cls: 'paid' };
    else if (payment.status === 'REJECTED') paymentChip = { text: 'Rejected', cls: 'rejected' };
    else paymentChip = { text: payment.status, cls: 'submitted' };
  }

  const badge = state === 'confirmed' ? { text: 'CONFIRMED', cls: 'confirmed' }
    : state === 'cancelled' ? { text: 'CANCELLED', cls: 'gray' }
      : state === 'expired' ? { text: 'EXPIRED', cls: 'gray' }
        : { text: 'PENDING', cls: 'pending' };

  return (
    <>
      {/* Hero */}
      <section className="pp-hero">
        <div className="pp-hero-inner">
          <div>
            <h1>ONE LAST STEP.</h1>
            <p>{HERO_TEXT[state]}</p>
          </div>

          <svg className="pp-hero-court" viewBox="0 0 400 96" aria-hidden="true">
            <g fill="none" stroke="#002752" strokeOpacity="0.2" strokeWidth="3">
              <rect x="1.5" y="1.5" width="397" height="93" />
              <line x1="120" y1="0" x2="120" y2="96" />
              <line x1="200" y1="0" x2="200" y2="96" />
              <line x1="280" y1="0" x2="280" y2="96" />
              <line x1="120" y1="48" x2="280" y2="48" />
            </g>
          </svg>

          <div className="pp-chip" role="timer" aria-live="off">
            {hasLiveHold ? (
              <>
                <small>HOLD EXPIRES IN</small>
                <strong>{mmss(remaining)}</strong>
              </>
            ) : (
              <>
                <small>BOOKING STATUS</small>
                <strong className="word">{STATUS_CHIP[state] ?? 'Waiting'}</strong>
              </>
            )}
          </div>
        </div>
      </section>

      <div className="pl-container pp-layout">
        {/* Left: booking summary */}
        <section className="pp-card">
          <h2 className="pp-title">Booking #{details.bookingReference}</h2>
          <span className={`pp-badge ${badge.cls}`}>{badge.text}</span>

          <dl className="pp-rows">
            <div><dt>COURT</dt><dd>{courtLabel}</dd></div>
            <div><dt>DATE</dt><dd>{longDate(details.date)}</dd></div>
            <div>
              <dt>TIME</dt>
              <dd>{clock(details.startTime)} – {clock(details.endTime)} ({details.hours} {details.hours === 1 ? 'hour' : 'hours'})</dd>
            </div>
            <div><dt>GEAR</dt><dd>{gearText}</dd></div>
          </dl>

          <div className="pp-fees">
            <div><span>Court fee</span><b>{peso(details.courtAmount)}</b></div>
            <div><span>Gear rental</span><b>{peso(details.equipmentAmount)}</b></div>
          </div>

          <div className="pp-total">
            <small>TOTAL TO PAY (FULL AMOUNT)</small>
            <strong>{peso(details.totalAmount)}</strong>
          </div>

          <div className="pp-status-row">
            <span>Payment status</span>
            <span className={`pp-pill ${paymentChip.cls}`}>{paymentChip.text}</span>
          </div>
          <p className="pp-note">Full amount only. Partial and split payments are not supported.</p>
        </section>

        {/* Right: pay with GCash */}
        <section className="pp-card">
          <h2 className="pp-title">Pay with GCash</h2>

          {state === 'awaiting' && (
            <>
              <div className="pp-gcash">
                <small className="send">SEND {peso(details.totalAmount)} TO</small>
                <div className="pp-gcash-grid">
                  <div>
                    <small>ACCOUNT NAME</small>
                    <strong>{details.gcash.accountName}</strong>
                  </div>
                  <div>
                    <small>GCASH NUMBER</small>
                    <strong className="number">{details.gcash.number}</strong>
                  </div>
                </div>
                <p>Tip: put the booking number in the GCash message.</p>
              </div>

              <form onSubmit={handleSubmit} noValidate>
                {payment && payment.status === 'REJECTED' && (
                  <p className="pp-alert">
                    The admin could not match your last reference number. Please check it and send it again.
                  </p>
                )}

                <label className="pp-label" htmlFor="gcash-ref">GCASH REFERENCE NUMBER</label>
                <input
                  id="gcash-ref"
                  className="pp-input"
                  type="text"
                  inputMode="numeric"
                  autoComplete="off"
                  maxLength={30}
                  placeholder="e.g. 1234 567 890 123"
                  value={reference}
                  onChange={(e) => setReference(e.target.value)}
                />

                {submitError && <p className="pp-error" role="alert">{submitError}</p>}

                <button type="submit" className="pp-submit" disabled={submitting}>
                  {submitting ? 'SENDING…' : 'SUBMIT PAYMENT REFERENCE'}
                </button>
              </form>
            </>
          )}

          {state === 'submitted' && (
            <div className="pp-result">
              <h3>Reference number sent</h3>
              <p className="ref">{payment.reference}</p>
              <p>The admin is checking your GCash payment. Your booking turns <b>Confirmed</b> after that.
                Your court stays held for you in the meantime.</p>
            </div>
          )}

          {state === 'confirmed' && (
            <div className="pp-result ok">
              <h3>Payment received. Your booking is confirmed.</h3>
              <p>Show your booking number at the counter: <b>{details.bookingReference}</b></p>
            </div>
          )}

          {(state === 'expired' || state === 'cancelled') && (
            <div className="pp-result">
              <h3>{state === 'expired' ? 'This hold has ended' : 'This booking is cancelled'}</h3>
              <p>You can pick another time on the Court Schedule.</p>
              <Link to="/schedule" className="pp-back">Pick another time</Link>
            </div>
          )}

          <div className="pp-how">
            <h4>HOW IT WORKS</h4>
            <p><span>1</span>Send the full amount by GCash.</p>
            <p><span>2</span>Enter the reference number here.</p>
            <p><span>3</span>The admin checks it and your booking turns Confirmed.</p>
            <p className="cash">Cash? Walk-in bookings are paid at the counter and recorded by staff.</p>
          </div>
        </section>
      </div>
    </>
  );
}
