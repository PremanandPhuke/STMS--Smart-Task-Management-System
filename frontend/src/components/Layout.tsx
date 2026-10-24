import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const signOut = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">Smart Task Manager</div>
        <nav className="nav">
          <NavLink to="/" end>Dashboard</NavLink>
          <NavLink to="/projects">Projects</NavLink>
          <NavLink to="/tasks">All tasks</NavLink>
          <NavLink to="/labels">Labels</NavLink>
          {user?.role === 'ADMIN' && <NavLink to="/admin">Users (admin)</NavLink>}
        </nav>
        <div className="sidebar-foot">
          <div className="who">{user?.name}</div>
          <div>{user?.email}</div>
          <button className="btn small" style={{ marginTop: '.6rem' }} onClick={signOut}>
            Sign out
          </button>
        </div>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
