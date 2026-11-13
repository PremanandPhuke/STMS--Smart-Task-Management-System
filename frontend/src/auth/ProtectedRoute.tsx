import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { Spinner } from '../components/Loaders';

/** Wrap routes with this to require a logged-in user (and optionally the ADMIN role). */
export default function ProtectedRoute({ adminOnly = false }: { adminOnly?: boolean }) {
  const { user, loading } = useAuth();
//   if (loading) return <div className="center-screen">Loading…</div>;
if (loading) return <div className="center-screen"><Spinner size={38} /></div>;
  if (!user) return <Navigate to="/login" replace />;
  if (adminOnly && user.role !== 'ADMIN') return <Navigate to="/" replace />;
  return <Outlet />;
}
