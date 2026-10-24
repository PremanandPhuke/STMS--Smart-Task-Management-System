import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';

/** Wrap routes with this to require a logged-in user (and optionally the ADMIN role). */
export default function ProtectedRoute({ adminOnly = false }: { adminOnly?: boolean }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="center-screen">Loading…</div>;
  if (!user) return <Navigate to="/login" replace />;
  if (adminOnly && user.role !== 'ADMIN') return <Navigate to="/" replace />;
  return <Outlet />;
}
