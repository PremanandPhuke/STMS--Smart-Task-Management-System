import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { errorMessage } from '../api/client';

export default function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      await login(email, password);
      navigate('/');
    } catch (err) {
      setError(errorMessage(err));
      setBusy(false);
    }
  };

  return (
    <div className="auth-wrap">
      <div className="auth-side">
        <h1>Know what to do next.</h1>
        <p>Every task gets a smart priority score from its urgency and due date, so the work that matters rises to the top.</p>
      </div>
      <div className="auth-form">
        <form onSubmit={submit}>
          <h2>Sign in</h2>
          {error && <div className="error">{error}</div>}
          <label className="field">
            Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus />
          </label>
          <label className="field">
            Password
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </label>
          <button className="btn primary" disabled={busy} style={{ width: '100%' }}>Sign in</button>
          <p style={{ marginTop: '1rem' }}>New here? <Link to="/register">Create an account</Link></p>
          <div className="hint">
            Demo login: demo@stms.com / Demo@1234<br />
            Admin login: admin@stms.com / Admin@123
          </div>
        </form>
      </div>
    </div>
  );
}
