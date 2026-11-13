import { useCallback, useEffect, useState } from 'react';
import { ACTION_LABELS, STATUS_LABELS, api } from '../api.js';
import { useAuth } from '../context/AuthContext.jsx';

const COLUMNS = ['BOOKED', 'IN_PROGRESS', 'READY'];

function JobCard({ job, onMove }) {
  const [notes, setNotes] = useState('');
  const [busy, setBusy] = useState(false);

  const move = async (status) => {
    setBusy(true);
    await onMove(job.id, status, notes);
    setBusy(false);
    setNotes('');
  };

  const actions = job.nextStatuses.filter((s) => s !== 'CANCELLED');

  return (
    <article className="job">
      <h3>{job.vehicle}</h3>
      <p className="job-service">{job.serviceName}</p>
      <p className="muted small">
        {job.customerName} · {new Date(`${job.preferredDate}T00:00:00`).toLocaleDateString('en-ZA', { day: 'numeric', month: 'short' })}
      </p>
      {job.description && <p className="job-description">&ldquo;{job.description}&rdquo;</p>}
      {job.mechanicNotes && <p className="mechanic-note">{job.mechanicNotes}</p>}
      {actions.length > 0 && (
        <div className="job-actions">
          <label className="visually-hidden" htmlFor={`notes-${job.id}`}>Notes for the customer</label>
          <input
            id={`notes-${job.id}`}
            placeholder="Note for the customer (optional)"
            value={notes}
            maxLength={1000}
            onChange={(e) => setNotes(e.target.value)}
          />
          {actions.map((status) => (
            <button key={status} type="button" className="button button-small" disabled={busy} onClick={() => move(status)}>
              {ACTION_LABELS[status]}
            </button>
          ))}
        </div>
      )}
    </article>
  );
}

export default function MechanicDashboard() {
  const { token } = useAuth();
  const [jobs, setJobs] = useState([]);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    api('/api/bookings', { token }).then(setJobs).catch((e) => setError(e.message));
  }, [token]);

  useEffect(() => {
    load();
  }, [load]);

  const move = async (id, status, mechanicNotes) => {
    setError('');
    try {
      await api(`/api/bookings/${id}/status`, { method: 'PATCH', token, body: { status, mechanicNotes } });
      load();
    } catch (e) {
      setError(e.message);
    }
  };

  const done = jobs.filter((j) => j.status === 'COLLECTED' || j.status === 'CANCELLED');

  return (
    <div className="workshop">
      <div className="section-head">
        <h1>Job board</h1>
        <p className="muted">Move each car along as you work on it. Customers see each update on their bookings page.</p>
      </div>
      {error && <p className="form-error" role="alert">{error}</p>}

      <div className="board">
        {COLUMNS.map((column) => {
          const columnJobs = jobs.filter((j) => j.status === column);
          return (
            <section key={column} className={`board-column column-${column.toLowerCase()}`}>
              <h2>
                {STATUS_LABELS[column]} <span className="count">{columnJobs.length}</span>
              </h2>
              {columnJobs.length === 0 ? (
                <p className="muted small">Nothing here.</p>
              ) : (
                columnJobs.map((job) => <JobCard key={job.id} job={job} onMove={move} />)
              )}
            </section>
          );
        })}
      </div>

      {done.length > 0 && (
        <details className="done-jobs">
          <summary>Finished and cancelled ({done.length})</summary>
          <ul>
            {done.map((j) => (
              <li key={j.id}>
                {j.vehicle}: {j.serviceName} <span className="muted">({STATUS_LABELS[j.status]})</span>
              </li>
            ))}
          </ul>
        </details>
      )}
    </div>
  );
}
