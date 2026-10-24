import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { labelsApi } from '../api/services';
import { errorMessage } from '../api/client';
import type { Label } from '../api/types';

export default function LabelsPage() {
  const [labels, setLabels] = useState<Label[]>([]);
  const [name, setName] = useState('');
  const [color, setColor] = useState('#0f766e');
  const [error, setError] = useState('');

  const load = () => labelsApi.list().then(setLabels).catch((e) => setError(errorMessage(e)));

  useEffect(() => {
    load();
  }, []);

  const create = async (e: FormEvent) => {
    e.preventDefault();
    try {
      await labelsApi.create(name, color);
      setName('');
      setError('');
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const remove = async (id: number) => {
    try {
      await labelsApi.remove(id);
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Labels</h1>
          <p>Use labels to group tasks by type, like bug or feature.</p>
        </div>
      </div>

      <form className="panel row" onSubmit={create} style={{ marginBottom: '1rem' }}>
        <input placeholder="Label name" value={name} onChange={(e) => setName(e.target.value)} required maxLength={50} style={{ maxWidth: 260 }} />
        <input type="color" value={color} onChange={(e) => setColor(e.target.value)} aria-label="Label color" />
        <button className="btn primary">Add label</button>
      </form>

      {error && <div className="error">{error}</div>}
      {labels.length === 0 && <div className="empty">No labels yet.</div>}

      <div className="panel">
        {labels.map((l) => (
          <div key={l.id} className="task-line" style={{ cursor: 'default' }}>
            <span className="title"><span className="chip" style={{ background: l.color }}>{l.name}</span></span>
            <button className="btn small danger" onClick={() => remove(l.id)}>Delete</button>
          </div>
        ))}
      </div>
    </>
  );
}
