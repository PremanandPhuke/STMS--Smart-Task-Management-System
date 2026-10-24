import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import Modal from './Modal';
import { labelsApi, tasksApi, usersApi } from '../api/services';
import { errorMessage } from '../api/client';
import {
  PRIORITIES, PRIORITY_LABEL, RECURRENCES, RECURRENCE_LABEL, STATUSES, STATUS_LABEL,
} from '../api/types';
import type { Label, Priority, Recurrence, Task, TaskStatus, User } from '../api/types';

interface Props {
  projectId: number;
  task?: Task;            // present = edit mode
  parentTaskId?: number;  // present = create a subtask
  onClose: () => void;
  onSaved: () => void;
}

export default function TaskFormModal({ projectId, task, parentTaskId, onClose, onSaved }: Props) {
  const [title, setTitle] = useState(task?.title ?? '');
  const [description, setDescription] = useState(task?.description ?? '');
  const [status, setStatus] = useState<TaskStatus>(task?.status ?? 'TODO');
  const [priority, setPriority] = useState<Priority>(task?.priority ?? 'MEDIUM');
  const [recurrence, setRecurrence] = useState<Recurrence>(task?.recurrence ?? 'NONE');
  const [dueDate, setDueDate] = useState(task?.dueDate ?? '');
  const [assigneeId, setAssigneeId] = useState(task?.assigneeId ? String(task.assigneeId) : '');
  const [labelIds, setLabelIds] = useState<number[]>(task?.labels.map((l) => l.id) ?? []);
  const [labels, setLabels] = useState<Label[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    labelsApi.list().then(setLabels).catch(() => setLabels([]));
    usersApi.list().then(setUsers).catch(() => setUsers([]));
  }, []);

  const toggleLabel = (id: number) =>
    setLabelIds((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    const body = {
      title,
      description: description || null,
      status,
      priority,
      recurrence,
      dueDate: dueDate || null,
      assigneeId: assigneeId ? Number(assigneeId) : null,
      parentTaskId: task ? task.parentTaskId : parentTaskId ?? null,
      labelIds,
      projectId,
    };
    try {
      if (task) await tasksApi.update(task.id, body);
      else await tasksApi.create(body);
      onSaved();
    } catch (err) {
      setError(errorMessage(err));
      setSaving(false);
    }
  };

  const heading = task ? 'Edit task' : parentTaskId ? 'New subtask' : 'New task';

  return (
    <Modal title={heading} onClose={onClose}>
      <form onSubmit={submit}>
        {error && <div className="error">{error}</div>}
        <label className="field">
          Title
          <input value={title} onChange={(e) => setTitle(e.target.value)} required maxLength={200} autoFocus />
        </label>
        <label className="field">
          Description
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} />
        </label>
        <div className="grid-2">
          <label className="field">
            Status
            <select value={status} onChange={(e) => setStatus(e.target.value as TaskStatus)}>
              {STATUSES.map((s) => <option key={s} value={s}>{STATUS_LABEL[s]}</option>)}
            </select>
          </label>
          <label className="field">
            Priority
            <select value={priority} onChange={(e) => setPriority(e.target.value as Priority)}>
              {PRIORITIES.map((p) => <option key={p} value={p}>{PRIORITY_LABEL[p]}</option>)}
            </select>
          </label>
          <label className="field">
            Due date
            <input type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
          </label>
          <label className="field">
            Repeats
            <select value={recurrence} onChange={(e) => setRecurrence(e.target.value as Recurrence)}>
              {RECURRENCES.map((r) => <option key={r} value={r}>{RECURRENCE_LABEL[r]}</option>)}
            </select>
          </label>
        </div>
        <label className="field">
          Assignee
          <select value={assigneeId} onChange={(e) => setAssigneeId(e.target.value)}>
            <option value="">Unassigned</option>
            {users.map((u) => <option key={u.id} value={u.id}>{u.name}</option>)}
          </select>
        </label>
        <div className="field">
          Labels
          {labels.length === 0 && <span className="muted">No labels yet. Create some on the Labels page.</span>}
          <div className="label-pick">
            {labels.map((l) => (
              <label key={l.id}>
                <input type="checkbox" checked={labelIds.includes(l.id)} onChange={() => toggleLabel(l.id)} />
                <span style={{ background: l.color }}>{l.name}</span>
              </label>
            ))}
          </div>
        </div>
        <div className="row">
          <span className="spacer" />
          <button type="button" className="btn" onClick={onClose}>Cancel</button>
          <button className="btn primary" disabled={saving}>{task ? 'Save changes' : 'Create task'}</button>
        </div>
      </form>
    </Modal>
  );
}
