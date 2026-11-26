import { useCallback, useEffect, useState } from 'react';
import {
  adminPing,
  getPendingCancellations,
  approveCancellation,
  rejectCancellation,
} from '../authApi';
import './AdminPage.css';

export default function AdminPage() {
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [cancellations, setCancellations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [processingId, setProcessingId] = useState(null);
  const [success, setSuccess] = useState('');

  const loadCancellations = useCallback(async () => {
    const data = await getPendingCancellations();
    setCancellations(data);
  }, []);

  useEffect(() => {
    let active = true;

    Promise.all([
      adminPing(),
      getPendingCancellations(),
    ])
      .then(([adminData, cancellationData]) => {
        if (active) {
          setMessage(adminData.message);
          setCancellations(cancellationData);
        }
      })
      .catch((requestError) => {
        if (active) setError(requestError.message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  const formatDate = (date) => {
    return new Date(date).toLocaleString('en-PH', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  };

  const formatCurrency = (amount) => {
    return `₱${Number(amount).toFixed(2)}`;
  };

  const handleDecision = async (cancellationId, action) => {
    const actionLabel = action === 'approve' ? 'approve' : 'reject';

    const confirmed = window.confirm(
      `Are you sure you want to ${actionLabel} this cancellation request?`
    );

    if (!confirmed) return;

    setProcessingId(cancellationId);
    setError('');
    setSuccess('');

    try {
      if (action === 'approve') {
        await approveCancellation(cancellationId);
        setSuccess('Cancellation request approved successfully.');
      } else {
        await rejectCancellation(cancellationId);
        setSuccess('Cancellation request rejected successfully.');
      }

      await loadCancellations();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setProcessingId(null);
    }
  };

  return (
    <div className="admin-page">
      <h1>Admin Dashboard</h1>
      <p>Manage pickleball booking cancellation requests.</p>

      {message && <p>Server says: {message}</p>}

      {error && (
        <p className="app-error" role="alert">
          {error}
        </p>
      )}

      {success && (
        <p className="admin-success" role="status">
          {success}
        </p>
      )}

      <section className="admin-cancellations">
        <div className="admin-section-heading">
          <div>
            <h2>Cancellation Requests</h2>
            <p>Review and process customer cancellation requests.</p>
          </div>

          <span className="admin-pending-count">
            {cancellations.length} Pending
          </span>
        </div>

        {loading ? (
          <p>Loading cancellation requests...</p>
        ) : cancellations.length === 0 ? (
          <div className="admin-empty">
            <h3>No pending requests</h3>
            <p>There are currently no cancellation requests to review.</p>
          </div>
        ) : (
          <div className="admin-cancellation-list">
            {cancellations.map((cancellation) => (
              <article
                className="admin-cancellation-card"
                key={cancellation.cancellationId}
              >
                <div className="admin-cancellation-header">
                  <div>
                    <span>BOOKING REFERENCE</span>
                    <h3>{cancellation.bookingReference}</h3>
                  </div>

                  <span className="admin-pending-status">
                    {cancellation.cancellationStatus}
                  </span>
                </div>

                <div className="admin-cancellation-details">
                  <div>
                    <span>Customer</span>
                    <strong>{cancellation.requestedBy}</strong>
                  </div>

                  <div>
                    <span>Request date</span>
                    <strong>
                      {formatDate(cancellation.requestedAt)}
                    </strong>
                  </div>

                  <div>
                    <span>Estimated refund</span>
                    <strong>
                      {formatCurrency(cancellation.refundAmount)}
                    </strong>
                  </div>
                </div>

                <div className="admin-cancellation-reason">
                  <span>Reason for cancellation</span>
                  <p>{cancellation.reason}</p>
                </div>

                <div className="admin-cancellation-actions">
                  <button
                    type="button"
                    className="admin-reject-button"
                    onClick={() =>
                      handleDecision(
                        cancellation.cancellationId,
                        'reject'
                      )
                    }
                    disabled={processingId !== null}
                  >
                    {processingId === cancellation.cancellationId
                      ? 'Processing...'
                      : 'Reject'}
                  </button>

                  <button
                    type="button"
                    className="admin-approve-button"
                    onClick={() =>
                      handleDecision(
                        cancellation.cancellationId,
                        'approve'
                      )
                    }
                    disabled={processingId !== null}
                  >
                    {processingId === cancellation.cancellationId
                      ? 'Processing...'
                      : 'Approve'}
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
