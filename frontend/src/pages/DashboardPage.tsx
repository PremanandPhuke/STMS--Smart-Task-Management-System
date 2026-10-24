import { useEffect, useState } from 'react';
import { dashboardApi } from '../api/services';
import { errorMessage } from '../api/client';
import { DueDate, PriorityBadge, ScoreMeter } from '../components/Badges';
import TaskDetailModal from '../components/TaskDetailModal';
import { PRIORITIES, PRIORITY_LABEL } from '../api/types';
import type { Dashboard } from '../api/types';

export default function DashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);
  const [error, setError] = useState('');
  const [openTaskId, setOpenTaskId] = useState<number | null>(null);

  const load = () => dashboardApi.get().then(setData).catch((e) => setError(errorMessage(e)));

  useEffect(() => {
    load();
  }, []);

  if (error) return <div className="error">{error}</div>;
  if (!data) return <p className="muted">Loading your dashboard…</p>;

  const maxOpen = Math.max(1, ...Object.values(data.openByPriority));

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Dashboard</h1>
          <p>Your workload at a glance.</p>
        </div>
      </div>

      <div className="stats">
        <div className="stat"><strong>{data.total}</strong><span>Total tasks</span></div>
        <div className="stat"><strong>{data.todo}</strong><span>To do</span></div>
        <div className="stat"><strong>{data.inProgress}</strong><span>In progress</span></div>
        <div className="stat"><strong>{data.done}</strong><span>Done</span></div>
        <div className={data.overdue > 0 ? 'stat alert' : 'stat'}><strong>{data.overdue}</strong><span>Overdue</span></div>
        <div className="stat"><strong>{data.dueToday}</strong><span>Due today</span></div>
        <div className="stat"><strong>{data.dueThisWeek}</strong><span>Due in 7 days</span></div>
        <div className="stat"><strong>{data.completedLast7Days}</strong><span>Done this week</span></div>
      </div>

      <div className="two-col">
        <div className="panel">
          <h2>Open tasks by priority</h2>
          {PRIORITIES.map((p) => (
            <div className="bar-row" key={p}>
              <span>{PRIORITY_LABEL[p]}</span>
              <div className="bar"><span style={{ width: `${((data.openByPriority[p] ?? 0) / maxOpen) * 100}%` }} /></div>
              <strong>{data.openByPriority[p] ?? 0}</strong>
            </div>
          ))}
        </div>

        <div className="panel">
          <h2>Do these first</h2>
          {data.topPriorityTasks.length === 0 && <p className="muted">Nothing open. Enjoy the quiet.</p>}
          {data.topPriorityTasks.map((t) => (
            <div className="task-line" key={t.id} onClick={() => setOpenTaskId(t.id)}>
              <span className="title">{t.title}<br /><span className="muted" style={{ fontWeight: 400, fontSize: '.82rem' }}>{t.projectName}</span></span>
              <PriorityBadge priority={t.priority} />
              <DueDate date={t.dueDate} overdue={t.overdue} />
              <ScoreMeter score={t.priorityScore} />
            </div>
          ))}
        </div>
      </div>

      {openTaskId !== null && (
        <TaskDetailModal taskId={openTaskId} onClose={() => setOpenTaskId(null)} onChanged={load} />
      )}
    </>
  );
}
