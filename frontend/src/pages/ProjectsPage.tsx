import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { projectsApi } from '../api/services';
import { errorMessage } from '../api/client';
import Modal from '../components/Modal';
import type { Project } from '../api/types';

export default function ProjectsPage() {
  const [projects, setProjects] = useState<Project[] | null>(null);
  const [error, setError] = useState('');
  const [editing, setEditing] = useState<Project | 'new' | null>(null);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  const load = () => projectsApi.list().then(setProjects).catch((e) => setError(errorMessage(e)));

  useEffect(() => {
    load();
  }, []);

  const openForm = (p: Project | 'new') => {
    setEditing(p);
    setName(p === 'new' ? '' : p.name);
    setDescription(p === 'new' ? '' : p.description ?? '');
    setError('');
  };

  const save = async (e: FormEvent) => {
    e.preventDefault();
    try {
      if (editing === 'new') await projectsApi.create(name, description);
      else if (editing) await projectsApi.update(editing.id, name, description);
      setEditing(null);
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const remove = async (p: Project) => {
    if (!window.confirm(`Delete "${p.name}" and all of its tasks?`)) return;
    try {
      await projectsApi.remove(p.id);
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Projects</h1>
          <p>Each project has its own board.</p>
        </div>
        <button className="btn primary" onClick={() => openForm('new')}>New project</button>
      </div>

      {error && !editing && <div className="error">{error}</div>}
      {projects === null && <p className="muted">Loading…</p>}
      {projects?.length === 0 && <div className="empty">No projects yet. Create one to start adding tasks.</div>}

      <div className="cards">
        {projects?.map((p) => (
          <div className="panel" key={p.id}>
            <h2>{p.name}</h2>
            <p className="muted">{p.description || 'No description'}</p>
            <p className="muted" style={{ fontSize: '.82rem' }}>Owner: {p.ownerName}</p>
            <div className="row">
              <Link className="btn primary small" to={`/projects/${p.id}`}>Open board</Link>
              <button className="btn small" onClick={() => openForm(p)}>Edit</button>
              <button className="btn small danger" onClick={() => remove(p)}>Delete</button>
            </div>
          </div>
        ))}
      </div>

      {editing && (
        <Modal title={editing === 'new' ? 'New project' : 'Edit project'} onClose={() => setEditing(null)}>
          <form onSubmit={save}>
            {error && <div className="error">{error}</div>}
            <label className="field">
              Name
              <input value={name} onChange={(e) => setName(e.target.value)} required maxLength={150} autoFocus />
            </label>
            <label className="field">
              Description
              <textarea value={description} onChange={(e) => setDescription(e.target.value)} />
            </label>
            <div className="row">
              <span className="spacer" />
              <button type="button" className="btn" onClick={() => setEditing(null)}>Cancel</button>
              <button className="btn primary">Save project</button>
            </div>
          </form>
        </Modal>
      )}
    </>
  );
}
