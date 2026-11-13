import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { homeFor, useAuth } from '../context/AuthContext.jsx';

const ACCOUNTS = [
  { role: 'Customer', email: 'customer@autocare.demo', password: 'Customer123!', note: 'Book services and track your car' },
  { role: 'Mechanic', email: 'mechanic@autocare.demo', password: 'Mechanic123!', note: 'Run the workshop job board' },
];

/** One-click demo logins for people trying the project. */
export default function DemoAccounts() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [busy, setBusy] = useState('');
  const [error, setError] = useState('');

  const tryAs = async (account) => {
    setBusy(account.role);
    setError('');
    try {
      const user = await login(account.email, account.password);
      navigate(homeFor(user));
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy('');
    }
  };

  return (
    <div className="demo-accounts">
      <h3>Try it without signing up</h3>
      <div className="demo-grid">
        {ACCOUNTS.map((account) => (
          <button key={account.role} type="button" className="demo-button" onClick={() => tryAs(account)} disabled={!!busy}>
            <strong>{busy === account.role ? 'Logging in…' : `Log in as ${account.role.toLowerCase()}`}</strong>
            <span>{account.note}</span>
          </button>
        ))}
      </div>
      {error && <p className="form-error" role="alert">{error}</p>}
    </div>
  );
}
