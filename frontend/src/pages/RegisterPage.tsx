import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { errorMessage } from '../api/client';

export default function RegisterPage() {
  const { user, register } = useAuth();
  const navigate = useNavigate();
  const [name, setName] = useState('');
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
      await register(name, email, password);
      navigate('/');
    } catch (err) {
      setError(errorMessage(err));
      setBusy(false);
    }
  };

  return (
    <div className="auth-wrap">
      <div className="auth-side">
        <h1>Create your workspace.</h1>
        <p>Projects, a board, reminders and a dashboard. It takes less than a minute to start.</p>
      </div>
      <div className="auth-form">
        <form onSubmit={submit}>
          <h2>Create account</h2>
          {error && <div className="error">{error}</div>}
          <label className="field">
            Full name
            <input value={name} onChange={(e) => setName(e.target.value)} required maxLength={100} autoFocus />
          </label>
          <label className="field">
            Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          <label className="field">
            Password (at least 8 characters)
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={8} maxLength={72} />
          </label>
          <button className="btn primary" disabled={busy} style={{ width: '100%' }}>Create account</button>
          <p style={{ marginTop: '1rem' }}>Already registered? <Link to="/login">Sign in</Link></p>
        </form>
      </div>
    </div>
  );
}
