import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [form, setForm] = useState({ fullName: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const update = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      await register(form.fullName, form.email, form.password);
      const next = params.get('next');
      navigate(next && next.startsWith('/') && !next.startsWith('//') ? next : '/my-car');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="auth-page">
      <h1>Create your account</h1>
      <form className="form" onSubmit={submit}>
        <label>
          Full name
          <input autoComplete="name" value={form.fullName} onChange={update('fullName')} required maxLength={80} />
        </label>
        <label>
          Email
          <input type="email" autoComplete="email" value={form.email} onChange={update('email')} required />
        </label>
        <label>
          Password
          <input type="password" autoComplete="new-password" value={form.password} onChange={update('password')} required minLength={8} maxLength={72} />
          <span className="hint">At least 8 characters</span>
        </label>
        {error && <p className="form-error" role="alert">{error}</p>}
        <button type="submit" className="button" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</button>
      </form>
      <p className="muted">Already have an account? <Link to="/login">Log in</Link></p>
    </section>
  );
}
