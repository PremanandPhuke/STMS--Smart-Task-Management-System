import type { Label, Priority, TaskStatus } from '../api/types';
import { PRIORITY_LABEL, STATUS_LABEL } from '../api/types';

export function PriorityBadge({ priority }: { priority: Priority }) {
  return <span className={`badge p-${priority}`}>{PRIORITY_LABEL[priority]}</span>;
}

export function StatusBadge({ status }: { status: TaskStatus }) {
  return <span className={`badge s-${status}`}>{STATUS_LABEL[status]}</span>;
}

export function LabelChips({ labels }: { labels: Label[] }) {
  return (
    <>
      {labels.map((l) => (
        <span key={l.id} className="chip" style={{ background: l.color }}>
          {l.name}
        </span>
      ))}
    </>
  );
}

/** Shows the smart priority score (0-100) as a small bar. */
export function ScoreMeter({ score }: { score: number }) {
  const level = score >= 80 ? 'hot' : score >= 55 ? 'warm' : '';
  return (
    <span className="score" title={`Smart priority score: ${score} / 100`}>
      <span className={`score-bar ${level}`}>
        <span style={{ width: `${score}%` }} />
      </span>
      {score}
    </span>
  );
}

export function formatDate(date: string | null): string {
  if (!date) return 'No due date';
  return new Date(`${date.slice(0, 10)}T00:00:00`).toLocaleDateString(undefined, {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });
}

export function DueDate({ date, overdue }: { date: string | null; overdue: boolean }) {
  return <span className={overdue ? 'due overdue' : 'due'}>{overdue ? 'Overdue · ' : ''}{formatDate(date)}</span>;
}
