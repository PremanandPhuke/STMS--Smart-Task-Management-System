import { useEffect, useState } from 'react';
import { usersApi } from '../api/services';
import { errorMessage } from '../api/client';
import type { User } from '../api/types';

/** Only reachable by ADMIN users (see ProtectedRoute adminOnly and the backend /api/admin/** rule). */
export default function AdminPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    usersApi.adminList().then(setUsers).catch((e) => setError(errorMessage(e)));
  }, []);

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Users</h1>
          <p>All registered accounts. Visible to admins only.</p>
        </div>
      </div>
      {error && <div className="error">{error}</div>}
      <div className="table-wrap">
        <table>
          <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th></tr></thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}><td>{u.id}</td><td>{u.name}</td><td>{u.email}</td><td>{u.role}</td></tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
