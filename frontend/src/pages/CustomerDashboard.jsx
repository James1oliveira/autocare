import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api, formatPrice } from '../api.js';
import StatusSteps from '../components/StatusSteps.jsx';
import { useAuth } from '../context/AuthContext.jsx';

// Today's date in the visitor's own timezone, as YYYY-MM-DD
const today = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

export default function CustomerDashboard() {
  const { token, user } = useAuth();
  const [params] = useSearchParams();
  const [catalog, setCatalog] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [form, setForm] = useState({
    vehicle: '',
    serviceCode: params.get('service') || '',
    preferredDate: today(),
    description: '',
  });
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);

  const loadBookings = useCallback(() => {
    api('/api/bookings/mine', { token }).then(setBookings).catch((e) => setError(e.message));
  }, [token]);

  useEffect(() => {
    api('/api/catalog').then(setCatalog).catch((e) => setError(e.message));
    loadBookings();
  }, [loadBookings]);

  const update = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));
  const selected = catalog.find((c) => c.code === form.serviceCode);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setBusy(true);
    try {
      const booking = await api('/api/bookings', { method: 'POST', token, body: form });
      setMessage(`Booked: ${booking.serviceName} for your ${booking.vehicle}.`);
      setForm((f) => ({ ...f, description: '' }));
      loadBookings();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const cancel = async (id) => {
    setError('');
    try {
      await api(`/api/bookings/${id}/cancel`, { method: 'POST', token });
      loadBookings();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="dashboard">
      <section className="panel">
        <h1>Book a service</h1>
        <p className="muted">Hi {user.fullName.split(' ')[0]}, pick a service and a day that suits you.</p>
        <form className="form" onSubmit={submit}>
          <label>
            Your car
            <input value={form.vehicle} onChange={update('vehicle')} placeholder="e.g. 2019 VW Polo 1.0 TSI" required maxLength={80} />
          </label>
          <label>
            Service
            <select value={form.serviceCode} onChange={update('serviceCode')} required>
              <option value="" disabled>Choose a service</option>
              {catalog.map((item) => (
                <option key={item.code} value={item.code}>{item.name}</option>
              ))}
            </select>
            {selected && <span className="hint">{formatPrice(selected.priceFrom, selected.priceTo)} · about {selected.estimatedTime}</span>}
          </label>
          <label>
            Preferred day
            <input type="date" min={today()} value={form.preferredDate} onChange={update('preferredDate')} required />
          </label>
          <label>
            <span className="label-text">What&rsquo;s happening? <span className="optional">(optional)</span></span>
            <textarea rows={3} value={form.description} onChange={update('description')} maxLength={1000} />
          </label>
          {error && <p className="form-error" role="alert">{error}</p>}
          {message && <p className="form-success" role="status">{message}</p>}
          <button type="submit" className="button" disabled={busy}>{busy ? 'Booking…' : 'Book service'}</button>
        </form>
      </section>

      <section className="panel panel-wide">
        <h2>Your bookings</h2>
        {bookings.length === 0 ? (
          <p className="muted">No bookings yet. Book your first service and you&rsquo;ll be able to follow it here.</p>
        ) : (
          <ul className="booking-list">
            {bookings.map((b) => (
              <li key={b.id} className="booking">
                <div className="booking-head">
                  <div>
                    <h3>{b.serviceName}</h3>
                    <p className="muted">{b.vehicle} · {new Date(`${b.preferredDate}T00:00:00`).toLocaleDateString('en-ZA', { weekday: 'short', day: 'numeric', month: 'short' })}</p>
                  </div>
                  {b.status === 'BOOKED' && (
                    <button type="button" className="link-button danger" onClick={() => cancel(b.id)}>Cancel booking</button>
                  )}
                </div>
                <StatusSteps status={b.status} />
                {b.mechanicNotes && (
                  <p className="mechanic-note"><strong>{b.mechanicName || 'Mechanic'}:</strong> {b.mechanicNotes}</p>
                )}
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
