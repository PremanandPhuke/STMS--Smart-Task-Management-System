import { useCallback, useEffect, useState } from 'react';
import { tasksApi } from '../api/services';
import { errorMessage } from '../api/client';
import { DueDate, LabelChips, PriorityBadge, ScoreMeter, StatusBadge } from '../components/Badges';
import TaskDetailModal from '../components/TaskDetailModal';
import { PRIORITIES, PRIORITY_LABEL, STATUSES, STATUS_LABEL } from '../api/types';
import type { PageResponse, Priority, Task, TaskStatus } from '../api/types';

export default function TasksPage() {
  const [result, setResult] = useState<PageResponse<Task> | null>(null);
  const [q, setQ] = useState('');
  const [status, setStatus] = useState<TaskStatus | ''>('');
  const [priority, setPriority] = useState<Priority | ''>('');
  const [sort, setSort] = useState('priorityScore,desc');
  const [page, setPage] = useState(0);
  const [error, setError] = useState('');
  const [openTaskId, setOpenTaskId] = useState<number | null>(null);

  const load = useCallback(async () => {
    try {
      setResult(await tasksApi.list({ q, status, priority, sort, page, size: 10 }));
      setError('');
    } catch (err) {
      setError(errorMessage(err));
    }
  }, [q, status, priority, sort, page]);

  useEffect(() => {
    load();
  }, [load]);

  const resetPage = <T,>(setter: (v: T) => void) => (v: T) => {
    setter(v);
    setPage(0);
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1>All tasks</h1>
          <p>Everything you own or are assigned to, across projects.</p>
        </div>
      </div>

      <div className="filters">
        <input placeholder="Search by title" value={q} onChange={(e) => resetPage(setQ)(e.target.value)} />
        <select value={status} onChange={(e) => resetPage(setStatus)(e.target.value as TaskStatus | '')}>
          <option value="">Any status</option>
          {STATUSES.map((s) => <option key={s} value={s}>{STATUS_LABEL[s]}</option>)}
        </select>
        <select value={priority} onChange={(e) => resetPage(setPriority)(e.target.value as Priority | '')}>
          <option value="">Any priority</option>
          {PRIORITIES.map((p) => <option key={p} value={p}>{PRIORITY_LABEL[p]}</option>)}
        </select>
        <select value={sort} onChange={(e) => resetPage(setSort)(e.target.value)}>
          <option value="priorityScore,desc">Smart priority (highest first)</option>
          <option value="dueDate,asc">Due date (soonest first)</option>
          <option value="createdAt,desc">Newest first</option>
          <option value="title,asc">Title (A to Z)</option>
        </select>
      </div>

      {error && <div className="error">{error}</div>}
      {result?.content.length === 0 && <div className="empty">No tasks match these filters.</div>}

      {result && result.content.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead>
              <tr><th>Task</th><th>Project</th><th>Status</th><th>Priority</th><th>Due</th><th>Score</th></tr>
            </thead>
            <tbody>
              {result.content.map((t) => (
                <tr key={t.id} className="click" onClick={() => setOpenTaskId(t.id)}>
                  <td><strong>{t.title}</strong><div><LabelChips labels={t.labels} /></div></td>
                  <td>{t.projectName}</td>
                  <td><StatusBadge status={t.status} /></td>
                  <td><PriorityBadge priority={t.priority} /></td>
                  <td><DueDate date={t.dueDate} overdue={t.overdue} /></td>
                  <td><ScoreMeter score={t.priorityScore} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {result && result.totalPages > 1 && (
        <div className="pager">
          <span>Page {result.page + 1} of {result.totalPages} ({result.totalElements} tasks)</span>
          <button className="btn small" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
          <button className="btn small" disabled={page + 1 >= result.totalPages} onClick={() => setPage(page + 1)}>Next</button>
        </div>
      )}

      {openTaskId !== null && (
        <TaskDetailModal taskId={openTaskId} onClose={() => setOpenTaskId(null)} onChanged={load} />
      )}
    </>
  );
}
