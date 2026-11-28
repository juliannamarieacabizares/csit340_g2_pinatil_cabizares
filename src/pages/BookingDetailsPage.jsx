import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { createBooking, getBookingOptions } from '../playerApi';
import './BookingDetailsPage.css';

const MAX_HOURS = 3;

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

// ₱600 or ₱12.50 (no decimals when the number is whole)
function peso(n) {
  const value = Number(n);
  const whole = Number.isInteger(value);
  return `₱${value.toLocaleString('en-PH', {
    minimumFractionDigits: whole ? 0 : 2,
    maximumFractionDigits: 2,
  })}`;
}

// Letter shown in the gear icon: "Pickleball Paddle" -> P, "Pickleball Set" -> S, "Ball Machine" -> B
function gearLetter(name) {
  const short = name.replace(/^pickleball\s+/i, '').trim() || name;
  return short.charAt(0).toUpperCase();
}

function pesoFull(n) {
  return `₱${Number(n).toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

export default function BookingDetailsPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const courtId = searchParams.get('courtId');
  const date = searchParams.get('date');
  const startTime = searchParams.get('startTime');
  const startHours = Math.min(Math.max(Number(searchParams.get('hours')) || 1, 1), MAX_HOURS);

  const [hours, setHours] = useState(startHours);
  const [options, setOptions] = useState(null);
  const [qty, setQty] = useState({}); // { equipmentId: quantity }
  const [loadError, setLoadError] = useState('');
  const [submitError, setSubmitError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const requestId = useRef(0);

  const missingDetails = !courtId || !date || !startTime;

  // Ask the server for the court, end time, max hours and equipment stock.
  // It runs again every time the number of hours changes.
  function loadOptions(forHours) {
    const current = requestId.current + 1;
    requestId.current = current;
    return getBookingOptions({ courtId, date, startTime, hours: forHours })
      .then((data) => {
        if (current !== requestId.current) return; // an older answer, ignore it
        setLoadError('');
        setOptions(data);
        if (data.hours !== forHours) setHours(data.hours);
        // keep the chosen quantities, but never above the stock left for these hours
        setQty((previous) => {
          const next = {};
          data.equipment.forEach((e) => {
            next[e.equipmentId] = Math.min(previous[e.equipmentId] ?? 0, e.available);
          });
          return next;
        });
      })
      .catch((error) => {
        if (current !== requestId.current) return;
        setLoadError(error.message);
      });
  }

  useEffect(() => {
    if (missingDetails) return;
    loadOptions(hours);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [courtId, date, startTime, hours]);

  if (missingDetails) {
    return (
      <div className="pl-container">
        <div className="bd-message">
          <h2>We need your court and time first</h2>
          <p>Please pick an hour on the Court Schedule.</p>
          <Link to="/schedule" className="bd-back">Go to Court Schedule</Link>
        </div>
      </div>
    );
  }

  if (loadError && !options) {
    return (
      <div className="pl-container">
        <div className="bd-message">
          <h2>This time is not available</h2>
          <p>{loadError}</p>
          <Link to="/schedule" className="bd-back">Back to Court Schedule</Link>
        </div>
      </div>
    );
  }

  if (!options) {
    return (
      <div className="pl-container">
        <p className="bd-loading">Loading your booking…</p>
      </div>
    );
  }

  // Prices shown here use the rates sent by the server.
  // The server calculates them again when you confirm.
  const rate = Number(options.court.hourlyRate);
  const gearLines = options.equipment.map((e) => {
    const quantity = qty[e.equipmentId] ?? 0;
    return { ...e, quantity, subtotal: quantity * Number(e.rentalFee) };
  });
  const gearSubtotal = gearLines.reduce((sum, line) => sum + line.subtotal, 0);
  const total = Number(options.courtFee) + gearSubtotal;

  function changeQty(equipmentId, delta, available) {
    setQty((previous) => {
      const next = (previous[equipmentId] ?? 0) + delta;
      return { ...previous, [equipmentId]: Math.max(0, Math.min(next, available)) };
    });
  }

  async function handleConfirm() {
    setSubmitError('');
    setSubmitting(true);
    try {
      const created = await createBooking({
        courtId: Number(courtId),
        date: options.date,
        startTime: options.startTime,
        hours: options.hours,
        equipment: gearLines
          .filter((line) => line.quantity > 0)
          .map((line) => ({ equipmentId: line.equipmentId, quantity: line.quantity })),
      });
      navigate(`/payment/${created.bookingId}`, { state: { booking: created } });
    } catch (error) {
      setSubmitError(error.message);
      loadOptions(hours); // refresh the stock in case something changed
    } finally {
      setSubmitting(false);
    }
  }

  const courtLabel = `${options.court.courtName}${options.court.description ? ` · ${options.court.description}` : ''}`;

  return (
    <>
      {/* Hero */}
      <section className="bd-hero">
        <div className="bd-hero-inner">
          <div>
            <h1>ALMOST GAME TIME.</h1>
            <p>Check your hour, add your gear, and lock it in.</p>
          </div>
          <svg className="bd-hero-court" viewBox="0 0 400 96" aria-hidden="true">
            <g fill="none" stroke="#002752" strokeOpacity="0.2" strokeWidth="3">
              <rect x="1.5" y="1.5" width="397" height="93" />
              <line x1="120" y1="0" x2="120" y2="96" />
              <line x1="200" y1="0" x2="200" y2="96" />
              <line x1="280" y1="0" x2="280" y2="96" />
              <line x1="120" y1="48" x2="280" y2="48" />
            </g>
          </svg>
        </div>
      </section>

      <div className="pl-container bd-layout">
        {/* Left card */}
        <section className="bd-card">
          <div className="bd-step">
            <span className="bd-num">1</span>
            <h2>Your court &amp; time</h2>
          </div>

          <div className="bd-fields">
            <div>
              <span className="bd-label">COURT</span>
              <div className="bd-field">{courtLabel}</div>
            </div>
            <div>
              <span className="bd-label">DATE</span>
              <div className="bd-field">{longDate(options.date)}</div>
            </div>
            <div>
              <span className="bd-label">START</span>
              <div className="bd-field">{clock(options.startTime)}</div>
            </div>
            <div>
              <span className="bd-label">END</span>
              <div className="bd-field">{clock(options.endTime)}</div>
            </div>
          </div>

          <span className="bd-label bd-label-long">HOW LONG?</span>
          <div className="bd-long">
            {[1, 2, 3].map((h) => (
              <button
                key={h}
                type="button"
                className={`bd-pill${options.hours === h ? ' on' : ''}`}
                disabled={h > options.maxHours}
                title={h > options.maxHours ? 'The next hour is not available' : undefined}
                onClick={() => setHours(h)}
              >
                {h} {h === 1 ? 'hour' : 'hours'}
              </button>
            ))}
            <span className="bd-hint">1 to 3 whole hours</span>
          </div>

          <hr className="bd-divider" />

          <div className="bd-step">
            <span className="bd-num">2</span>
            <h2>Add your gear (optional)</h2>
          </div>
          <p className="bd-sub">Flat fee per item for the whole booking. Stock is checked for your hours.</p>

          <div className="bd-gear">
            {gearLines.length === 0 && <p className="bd-sub">No rental gear is available right now.</p>}
            {gearLines.map((line, index) => (
              <div key={line.equipmentId} className={`bd-item${line.quantity > 0 ? ' has-qty' : ''}`}>
                <span className={`bd-icon c${index % 3}`}>{gearLetter(line.name)}</span>
                <div className="bd-item-info">
                  <strong>{line.name}</strong>
                  <span>{peso(line.rentalFee)} / booking</span>
                </div>
                <span className="bd-left">{line.available > 0 ? `${line.available} left` : 'Sold out'}</span>
                <div className="bd-counter">
                  <button
                    type="button"
                    className="minus"
                    aria-label={`Remove one ${line.name}`}
                    disabled={line.quantity === 0}
                    onClick={() => changeQty(line.equipmentId, -1, line.available)}
                  >
                    −
                  </button>
                  <span className="bd-qty" aria-live="polite">{line.quantity}</span>
                  <button
                    type="button"
                    className="plus"
                    aria-label={`Add one ${line.name}`}
                    disabled={line.quantity >= line.available}
                    onClick={() => changeQty(line.equipmentId, 1, line.available)}
                  >
                    +
                  </button>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Right column */}
        <aside className="bd-side">
          <section className="bd-total-card">
            <h3>YOUR TOTAL</h3>

            <ul className="bd-lines">
              <li>
                <span>Court fee ({peso(rate)} × {options.hours} {options.hours === 1 ? 'hr' : 'hrs'})</span>
                <b>{peso(options.courtFee)}</b>
              </li>
              {gearLines.map((line) => (
                <li key={line.equipmentId}>
                  <span>{line.name} ({peso(line.rentalFee)} × {line.quantity})</span>
                  <b>{peso(line.subtotal)}</b>
                </li>
              ))}
            </ul>

            <div className="bd-gear-sub">
              <span>Gear subtotal</span>
              <b>{peso(gearSubtotal)}</b>
            </div>

            <div className="bd-due">
              <small>TOTAL AMOUNT DUE</small>
              <strong>{pesoFull(total)}</strong>
            </div>

            {submitError && <p className="bd-error" role="alert">{submitError}</p>}

            <button type="button" className="bd-confirm" disabled={submitting} onClick={handleConfirm}>
              {submitting ? 'LOCKING IT IN…' : 'CONFIRM & PROCEED TO PAYMENT'}
            </button>
            <p className="bd-note">The price is checked again by the server.</p>
          </section>

          <section className="bd-hold">
            <h3>30-MINUTE HOLD</h3>
            <p>We keep your court for 30 minutes after you confirm.</p>
            <p>Pay by GCash in time, or the slot goes back to the pool.</p>
          </section>
        </aside>
      </div>
    </>
  );
}
