import { Navigate, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import Home from './pages/Home.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import CustomerDashboard from './pages/CustomerDashboard.jsx';
import MechanicDashboard from './pages/MechanicDashboard.jsx';

export default function App() {
  return (
    <>
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/my-car"
            element={(
              <ProtectedRoute roles={['CUSTOMER', 'ADMIN']}>
                <CustomerDashboard />
              </ProtectedRoute>
            )}
          />
          <Route
            path="/workshop"
            element={(
              <ProtectedRoute roles={['MECHANIC', 'ADMIN']}>
                <MechanicDashboard />
              </ProtectedRoute>
            )}
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <footer className="site-footer">
        <p>AutoCare AI, built by James Oliveira with Spring Boot microservices, Spring AI and React.</p>
      </footer>
    </>
  );
}
