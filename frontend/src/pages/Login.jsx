import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { homeFor, useAuth } from '../context/AuthContext.jsx';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      const user = await login(email, password);
      const next = params.get('next');
      // Only follow internal links, never a full URL to another site
      navigate(next && next.startsWith('/') && !next.startsWith('//') ? next : homeFor(user));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="auth-page">
      <h1>Log in</h1>
      <form className="form" onSubmit={submit}>
        <label>
          Email
          <input type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label>
          Password
          <input type="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </label>
        {error && <p className="form-error" role="alert">{error}</p>}
        <button type="submit" className="button" disabled={busy}>{busy ? 'Logging in…' : 'Log in'}</button>
      </form>
      <p className="muted">
        New here? <Link to={`/register${params.get('next') ? `?next=${encodeURIComponent(params.get('next'))}` : ''}`}>Create an account</Link>
      </p>
      <p className="muted small">
        Demo customer: customer@autocare.demo / Customer123!<br />
        Demo mechanic: mechanic@autocare.demo / Mechanic123!
      </p>
    </section>
  );
}
