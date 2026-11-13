import { Navigate, useLocation } from 'react-router-dom';
import { homeFor, useAuth } from '../context/AuthContext.jsx';

export default function ProtectedRoute({ roles, children }) {
  const { user } = useAuth();
  const location = useLocation();
  if (!user) {
    return <Navigate to={`/login?next=${encodeURIComponent(location.pathname + location.search)}`} replace />;
  }
  if (roles && !roles.includes(user.role)) {
    return <Navigate to={homeFor(user)} replace />;
  }
  return children;
}
