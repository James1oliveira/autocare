import { Link, NavLink, useNavigate } from 'react-router-dom';
import { homeFor, useAuth } from '../context/AuthContext.jsx';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="navbar">
      <Link to="/" className="brand" aria-label="AutoCare AI home">
        <svg viewBox="0 0 32 32" width="28" height="28" aria-hidden="true">
          <circle cx="16" cy="16" r="11" fill="none" stroke="currentColor" strokeWidth="3.5" />
          <circle cx="16" cy="16" r="3.5" fill="var(--amber)" />
        </svg>
        <span>AutoCare <b>AI</b></span>
      </Link>
      <nav className="nav-links">
        {user ? (
          <>
            <NavLink to={homeFor(user)}>{user.role === 'CUSTOMER' ? 'My bookings' : 'Job board'}</NavLink>
            <span className="nav-user">{user.fullName}</span>
            <button type="button" className="link-button" onClick={handleLogout}>Log out</button>
          </>
        ) : (
          <>
            <NavLink to="/login">Log in</NavLink>
            <Link to="/register" className="button button-small">Create account</Link>
          </>
        )}
      </nav>
    </header>
  );
}
