import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { projectsApi, tasksApi } from '../api/services';
import { errorMessage } from '../api/client';
import { DueDate, LabelChips, PriorityBadge, ScoreMeter } from '../components/Badges';
import TaskDetailModal from '../components/TaskDetailModal';
import TaskFormModal from '../components/TaskFormModal';
import { STATUSES, STATUS_LABEL } from '../api/types';
import type { Project, Task, TaskStatus } from '../api/types';

export default function ProjectBoardPage() {
  const projectId = Number(useParams().id);
  const [project, setProject] = useState<Project | null>(null);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [search, setSearch] = useState('');
  const [error, setError] = useState('');
  const [creating, setCreating] = useState(false);
  const [openTaskId, setOpenTaskId] = useState<number | null>(null);
  const [dragOver, setDragOver] = useState<TaskStatus | null>(null);

  const load = useCallback(async () => {
    try {
      setProject(await projectsApi.get(projectId));
      const page = await tasksApi.list({ projectId, q: search, size: 100, sort: 'priorityScore,desc' });
      setTasks(page.content.filter((t) => t.parentTaskId === null)); // subtasks live inside their parent
      setError('');
    } catch (err) {
      setError(errorMessage(err));
    }
  }, [projectId, search]);

  useEffect(() => {
    load();
  }, [load]);

  const moveTask = async (taskId: number, status: TaskStatus) => {
    const task = tasks.find((t) => t.id === taskId);
    if (!task || task.status === status) return;
    try {
      await tasksApi.setStatus(taskId, status);
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <>
      <div className="page-head">
        <div>
          <p><Link to="/projects">Projects</Link> / {project?.name ?? '…'}</p>
          <h1>{project?.name ?? 'Board'}</h1>
          {project?.description && <p>{project.description}</p>}
        </div>
        <div className="row">
          <input
            placeholder="Search tasks"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{ width: 200 }}
          />
          <button className="btn primary" onClick={() => setCreating(true)}>New task</button>
        </div>
      </div>

      {error && <div className="error">{error}</div>}

      <div className="board">
        {STATUSES.map((status) => {
          const column = tasks.filter((t) => t.status === status);
          return (
            <div
              key={status}
              className={dragOver === status ? 'column drag-over' : 'column'}
              onDragOver={(e) => {
                e.preventDefault();
                setDragOver(status);
              }}
              onDragLeave={() => setDragOver(null)}
              onDrop={(e) => {
                e.preventDefault();
                setDragOver(null);
                moveTask(Number(e.dataTransfer.getData('text/plain')), status);
              }}
            >
              <h3>{STATUS_LABEL[status]} <span className="muted">{column.length}</span></h3>
              {column.length === 0 && <p className="muted" style={{ padding: '0 .25rem' }}>Drop a task here.</p>}
              {column.map((t) => (
                <div
                  key={t.id}
                  className={`task-card b-${t.priority}`}
                  draggable
                  onDragStart={(e) => e.dataTransfer.setData('text/plain', String(t.id))}
                  onClick={() => setOpenTaskId(t.id)}
                >
                  <div className="t">{t.title}</div>
                  <div><LabelChips labels={t.labels} /></div>
                  <div className="meta">
                    <PriorityBadge priority={t.priority} />
                    <DueDate date={t.dueDate} overdue={t.overdue} />
                    <ScoreMeter score={t.priorityScore} />
                  </div>
                  {t.assigneeName && <div className="due" style={{ marginTop: '.35rem' }}>Assigned to {t.assigneeName}</div>}
                </div>
              ))}
            </div>
          );
        })}
      </div>

      {creating && (
        <TaskFormModal
          projectId={projectId}
          onClose={() => setCreating(false)}
          onSaved={() => {
            setCreating(false);
            load();
          }}
        />
      )}
      {openTaskId !== null && (
        <TaskDetailModal taskId={openTaskId} onClose={() => setOpenTaskId(null)} onChanged={load} />
      )}
    </>
  );
}
