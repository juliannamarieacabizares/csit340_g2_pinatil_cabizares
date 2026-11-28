import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getScheduleAll } from '../playerApi';
import './CourtSchedulePage.css';

const MAX_HOURS = 3;

function toDateString(d) {
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${m}-${day}`;
}

function addDays(dateStr, n) {
  const d = new Date(`${dateStr}T00:00:00`);
  d.setDate(d.getDate() + n);
  return toDateString(d);
}

function label(t) {
  const [h, m] = t.split(':').map(Number);
  const ampm = h >= 12 ? 'PM' : 'AM';
  return `${h % 12 === 0 ? 12 : h % 12}:${String(m).padStart(2, '0')} ${ampm}`;
}

function fullDate(dateStr) {
  return new Date(`${dateStr}T00:00:00`).toLocaleDateString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric',
  });
}

function shortDate(dateStr) {
  return new Date(`${dateStr}T00:00:00`).toLocaleDateString('en-US', {
    weekday: 'short', month: 'short', day: 'numeric',
  });
}

function peso(n) {
  return `₱${Number(n).toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

const STATUS_TEXT = {
  AVAILABLE: 'Available',
  PENDING_HOLD: 'Pending',
  RESERVED: 'Reserved',
  PAST: 'Unavailable',
  CLOSED: 'Unavailable',
};

export default function CourtSchedulePage() {
  const navigate = useNavigate();
  const dateInputRef = useRef(null);
  const today = toDateString(new Date());

  const [date, setDate] = useState(today);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [pick, setPick] = useState(null); // { courtIndex, slotIndex }
  const [hours, setHours] = useState(1);
  const [error, setError] = useState('');

  // Load the grid for the chosen date.
  useEffect(() => {
    let active = true;
    getScheduleAll(date)
      .then((result) => { if (active) setData(result); })
      .catch((e) => { if (active) setError(e.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [date]);

  // Changing the date clears the old selection and shows the loading text.
  function changeDate(newDate) {
    setPick(null);
    setHours(1);
    setError('');
    setLoading(true);
    setDate(newDate);
  }

  const courts = data?.courts ?? [];
  const slots = data?.slots ?? [];
  const allClosed =
    slots.length > 0 && slots.every((s) => s.statuses.every((st) => st === 'CLOSED'));

  // How many hours in a row are free from the picked slot (max 3)
  let maxHours = 0;
  if (pick) {
    for (let i = 0; i < MAX_HOURS; i += 1) {
      const s = slots[pick.slotIndex + i];
      if (s && s.statuses[pick.courtIndex] === 'AVAILABLE') maxHours += 1;
      else break;
    }
  }
  const effectiveHours = Math.min(hours, maxHours || 1);

  function isPicked(courtIndex, slotIndex) {
    return Boolean(
      pick &&
        pick.courtIndex === courtIndex &&
        slotIndex >= pick.slotIndex &&
        slotIndex < pick.slotIndex + effectiveHours,
    );
  }

  function clearPick() {
    setPick(null);
    setHours(1);
  }

  function handleCellClick(courtIndex, slotIndex) {
    if (isPicked(courtIndex, slotIndex)) {
      clearPick(); // click a selected slot again to clear it
      return;
    }
    setPick({ courtIndex, slotIndex });
    setHours(1);
  }

  function openDatePicker() {
    const input = dateInputRef.current;
    if (!input) return;
    try {
      input.showPicker();
    } catch {
      input.focus();
    }
  }

  // The booking page reads everything from the URL, so a refresh keeps working.
  function handleBook() {
    const court = courts[pick.courtIndex];
    const start = slots[pick.slotIndex];
    const params = new URLSearchParams({
      courtId: court.courtId,
      date,
      startTime: start.startTime,
      hours: effectiveHours,
    });
    navigate(`/booking?${params}`);
  }

  const pickedCourt = pick ? courts[pick.courtIndex] : null;
  const startSlot = pick ? slots[pick.slotIndex] : null;
  const endSlot = pick ? slots[pick.slotIndex + effectiveHours - 1] : null;
  const fee = pickedCourt ? effectiveHours * Number(pickedCourt.hourlyRate) : 0;

  return (
    <>
      <div className={`pl-container cs-page${pick ? ' has-bar' : ''}`}>
        {/* Heading + date selector */}
        <header className="cs-header">
          <div>
            <h1>Book a court</h1>
            <p>Select an available time to get started.</p>
          </div>

          <div className="cs-date">
            <button
              type="button"
              className="cs-date-arrow"
              aria-label="Previous day"
              disabled={date <= today}
              onClick={() => changeDate(addDays(date, -1))}
            >
              ‹
            </button>
            <button type="button" className="cs-date-label" onClick={openDatePicker}>
              {fullDate(date)}
            </button>
            <input
              ref={dateInputRef}
              className="cs-date-input"
              type="date"
              tabIndex={-1}
              aria-hidden="true"
              value={date}
              min={today}
              onChange={(e) => e.target.value && changeDate(e.target.value)}
            />
            <button
              type="button"
              className="cs-date-arrow"
              aria-label="Next day"
              onClick={() => changeDate(addDays(date, 1))}
            >
              ›
            </button>
          </div>
        </header>

        {/* Legend */}
        <ul className="cs-legend" aria-label="Legend">
          <li><span className="sw available" />Available</li>
          <li><span className="sw pending" />Pending</li>
          <li><span className="sw reserved" />Reserved</li>
          <li><span className="sw unavailable" />Unavailable</li>
          <li><span className="sw selected" />Selected</li>
        </ul>

        {error && <p className="cs-error" role="alert">{error}</p>}
        {allClosed && <p className="cs-notice">The facility is closed on this day.</p>}

        {/* Schedule */}
        <section className="cs-board" aria-label="Court schedule">
          {loading && <p className="cs-empty">Loading schedule…</p>}
          {!loading && courts.length === 0 && !error && (
            <p className="cs-empty">No courts are available right now.</p>
          )}

          {!loading && courts.length > 0 && (
            <div className="cs-grid" style={{ '--courts': courts.length }}>
              <div className="cs-row cs-head">
                <div className="cs-timehead">Time</div>
                {courts.map((c) => (
                  <div key={c.courtId} className="cs-courthead">
                    <strong>{c.courtName}</strong>
                    {c.description && <span className="desc">{c.description}</span>}
                    <span className="rate">{peso(c.hourlyRate).replace('.00', '')}/hr</span>
                  </div>
                ))}
              </div>

              {slots.map((s, si) => (
                <div key={s.slotId} className="cs-row">
                  <div className="cs-time">{label(s.startTime)}</div>

                  {courts.map((c, ci) => {
                    const st = s.statuses[ci];
                    const picked = isPicked(ci, si);
                    const joinUp = picked && isPicked(ci, si - 1);
                    const joinDown = picked && isPicked(ci, si + 1);
                    const wrapClass = `cs-cell-wrap${joinUp ? ' join-up' : ''}${joinDown ? ' join-down' : ''}`;

                    if (picked) {
                      return (
                        <div key={c.courtId} className={wrapClass}>
                          <button
                            type="button"
                            className="cs-cell selected"
                            aria-pressed="true"
                            onClick={() => handleCellClick(ci, si)}
                          >
                            Selected
                          </button>
                        </div>
                      );
                    }

                    if (st === 'AVAILABLE') {
                      return (
                        <div key={c.courtId} className={wrapClass}>
                          <button
                            type="button"
                            className="cs-cell available"
                            aria-pressed="false"
                            onClick={() => handleCellClick(ci, si)}
                          >
                            Available
                          </button>
                        </div>
                      );
                    }

                    const cls = st === 'PENDING_HOLD' ? 'pending' : st === 'RESERVED' ? 'reserved' : 'unavailable';
                    const tip = st === 'PENDING_HOLD' ? 'Held while payment is pending' : undefined;
                    return (
                      <div key={c.courtId} className={wrapClass}>
                        <div className={`cs-cell ${cls}`} title={tip}>
                          {STATUS_TEXT[st] ?? 'Unavailable'}
                        </div>
                      </div>
                    );
                  })}
                </div>
              ))}
            </div>
          )}
        </section>
      </div>

      {/* Compact booking bar: only after a slot is picked. The page keeps
          extra space at the bottom (has-bar), so no schedule row is covered. */}
      {pick && (
        <section className="cs-bar" aria-label="Your selection">
          <div className="cs-bar-inner">
            <div className="cs-bar-info">
              <strong>{pickedCourt.courtName}</strong>
              <span>
                {shortDate(date)} · {label(startSlot.startTime)} – {label(endSlot.endTime)}
              </span>
            </div>

            <div className="cs-bar-dur" role="group" aria-label="Duration">
              {[1, 2, 3].map((h) => (
                <button
                  key={h}
                  type="button"
                  disabled={h > maxHours}
                  className={effectiveHours === h ? 'on' : ''}
                  title={h > maxHours ? 'The next hour is not available' : undefined}
                  onClick={() => setHours(h)}
                >
                  {h} {h === 1 ? 'hr' : 'hrs'}
                </button>
              ))}
            </div>

            <div className="cs-bar-fee">
              <small>Court fee</small>
              <strong>{peso(fee)}</strong>
            </div>

            <div className="cs-bar-actions">
              <button type="button" className="cs-clear" onClick={clearPick}>Clear</button>
              <button type="button" className="cs-continue" onClick={handleBook}>
                Continue to booking →
              </button>
            </div>
          </div>
        </section>
      )}
    </>
  );
}
