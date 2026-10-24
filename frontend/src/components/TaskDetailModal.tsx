import { useCallback, useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import Modal from './Modal';
import TaskFormModal from './TaskFormModal';
import { DueDate, LabelChips, PriorityBadge, ScoreMeter } from './Badges';
import { commentsApi, tasksApi } from '../api/services';
import { errorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { RECURRENCE_LABEL, STATUSES, STATUS_LABEL } from '../api/types';
import type { Comment, Task, TaskStatus } from '../api/types';

interface Props {
  taskId: number;
  onClose: () => void;
  onChanged: () => void; // tell the parent page to reload its list
}

export default function TaskDetailModal({ taskId, onClose, onChanged }: Props) {
  const { user } = useAuth();
  const [task, setTask] = useState<Task | null>(null);
  const [subtasks, setSubtasks] = useState<Task[]>([]);
  const [comments, setComments] = useState<Comment[]>([]);
  const [newComment, setNewComment] = useState('');
  const [newSubtask, setNewSubtask] = useState('');
  const [editing, setEditing] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      const t = await tasksApi.get(taskId);
      setTask(t);
      setComments(await commentsApi.list(taskId));
      setSubtasks(t.parentTaskId ? [] : await tasksApi.subtasks(taskId));
    } catch (err) {
      setError(errorMessage(err));
    }
  }, [taskId]);

  useEffect(() => {
    load();
  }, [load]);

  const changed = async () => {
    await load();
    onChanged();
  };

  const changeStatus = async (status: TaskStatus) => {
    try {
      await tasksApi.setStatus(taskId, status);
      await changed();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const remove = async () => {
    if (!window.confirm('Delete this task and its subtasks?')) return;
    try {
      await tasksApi.remove(taskId);
      onChanged();
      onClose();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const addComment = async (e: FormEvent) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    try {
      await commentsApi.add(taskId, newComment);
      setNewComment('');
      setComments(await commentsApi.list(taskId));
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const addSubtask = async (e: FormEvent) => {
    e.preventDefault();
    if (!task || !newSubtask.trim()) return;
    try {
      await tasksApi.create({
        title: newSubtask,
        projectId: task.projectId,
        parentTaskId: task.id,
        priority: task.priority,
      });
      setNewSubtask('');
      await changed();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const toggleSubtask = async (s: Task) => {
    try {
      await tasksApi.setStatus(s.id, s.status === 'DONE' ? 'TODO' : 'DONE');
      await changed();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const deleteComment = async (id: number) => {
    await commentsApi.remove(id);
    setComments(await commentsApi.list(taskId));
  };

  if (!task) {
    return (
      <Modal title="Task" onClose={onClose}>
        {error ? <div className="error">{error}</div> : <p className="muted">Loading…</p>}
      </Modal>
    );
  }

  return (
    <Modal title={task.title} onClose={onClose} wide>
      {error && <div className="error">{error}</div>}

      <div className="row" style={{ marginBottom: '.75rem' }}>
        <PriorityBadge priority={task.priority} />
        <ScoreMeter score={task.priorityScore} />
        <DueDate date={task.dueDate} overdue={task.overdue} />
        <span className="muted">{task.projectName}</span>
        <span className="muted">Assignee: {task.assigneeName ?? 'nobody'}</span>
        {task.recurrence !== 'NONE' && <span className="badge">{RECURRENCE_LABEL[task.recurrence]}</span>}
      </div>
      <div style={{ marginBottom: '.75rem' }}><LabelChips labels={task.labels} /></div>
      {task.description && <p>{task.description}</p>}

      <div className="row">
        <label className="row" style={{ gap: '.4rem' }}>
          Status
          <select style={{ width: 'auto' }} value={task.status} onChange={(e) => changeStatus(e.target.value as TaskStatus)}>
            {STATUSES.map((s) => <option key={s} value={s}>{STATUS_LABEL[s]}</option>)}
          </select>
        </label>
        <span className="spacer" />
        {task.canManage && <button className="btn" onClick={() => setEditing(true)}>Edit</button>}
        {task.canManage && <button className="btn danger" onClick={remove}>Delete</button>}
      </div>

      {!task.parentTaskId && (
        <div className="section">
          <h3>Subtasks</h3>
          {subtasks.length === 0 && <p className="muted">No subtasks yet.</p>}
          {subtasks.map((s) => (
            <label key={s.id} className="row" style={{ padding: '.25rem 0' }}>
              <input type="checkbox" checked={s.status === 'DONE'} onChange={() => toggleSubtask(s)} />
              <span style={{ textDecoration: s.status === 'DONE' ? 'line-through' : 'none' }}>{s.title}</span>
            </label>
          ))}
          {task.canManage && (
            <form className="row" onSubmit={addSubtask} style={{ marginTop: '.5rem' }}>
              <input placeholder="Add a subtask" value={newSubtask} onChange={(e) => setNewSubtask(e.target.value)} />
              <button className="btn">Add subtask</button>
            </form>
          )}
        </div>
      )}

      <div className="section">
        <h3>Comments</h3>
        {comments.length === 0 && <p className="muted">No comments yet.</p>}
        {comments.map((c) => (
          <div key={c.id} className="comment">
            <span className="who">{c.authorName}</span>{' '}
            <span className="muted">{new Date(c.createdAt).toLocaleString()}</span>
            {(user?.role === 'ADMIN' || user?.id === c.authorId) && (
              <button className="btn small" style={{ float: 'right' }} onClick={() => deleteComment(c.id)}>Delete</button>
            )}
            <div>{c.content}</div>
          </div>
        ))}
        <form className="row" onSubmit={addComment} style={{ marginTop: '.6rem' }}>
          <input placeholder="Write a comment" value={newComment} onChange={(e) => setNewComment(e.target.value)} />
          <button className="btn primary">Post comment</button>
        </form>
      </div>

      {editing && (
        <TaskFormModal
          projectId={task.projectId}
          task={task}
          onClose={() => setEditing(false)}
          onSaved={async () => {
            setEditing(false);
            await changed();
          }}
        />
      )}
    </Modal>
  );
}
