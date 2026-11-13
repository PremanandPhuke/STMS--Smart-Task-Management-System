export function Spinner({ size = 22 }: { size?: number }) {
  return <span className="spinner" style={{ width: size, height: size }} role="status" aria-label="Loading" />;
}

export function Skeleton({ width = '100%', height = 14 }: { width?: number | string; height?: number | string }) {
  return <span className="skeleton" style={{ width, height }} aria-hidden="true" />;
}

const gap = (px: number) => <div style={{ height: px }} />;

export function PageLoader({ label = 'Loading…' }: { label?: string }) {
  return (
    <div className="page-loader">
      <Spinner size={34} />
      <span>{label}</span>
    </div>
  );
}

export function DashboardSkeleton() {
  return (
    <div aria-busy="true" aria-label="Loading dashboard">
      <div className="page-head">
        <div>
          <Skeleton width={170} height={28} />
          {gap(8)}
          <Skeleton width={230} />
        </div>
      </div>
      <div className="stats">
        {Array.from({ length: 8 }).map((_, i) => (
          <div className="stat" key={i}>
            <Skeleton width={48} height={28} />
            {gap(8)}
            <Skeleton width={90} height={12} />
          </div>
        ))}
      </div>
      <div className="two-col">
        <div className="panel">
          <Skeleton width={190} height={18} />
          {gap(16)}
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} style={{ marginBottom: 14 }}><Skeleton height={10} /></div>
          ))}
        </div>
        <div className="panel">
          <Skeleton width={140} height={18} />
          {gap(12)}
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} style={{ padding: '.65rem 0' }}>
              <Skeleton width={`${60 + ((i * 11) % 30)}%`} height={14} />
              {gap(6)}
              <Skeleton width="30%" height={11} />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export function BoardSkeleton() {
  return (
    <div className="board" aria-busy="true" aria-label="Loading board">
      {[3, 2, 2].map((cards, col) => (
        <div className="column" key={col}>
          <Skeleton width={110} height={16} />
          {gap(12)}
          {Array.from({ length: cards }).map((_, i) => (
            <div className="task-card" key={i} style={{ cursor: 'default' }}>
              <Skeleton width="80%" height={15} />
              {gap(10)}
              <Skeleton width="45%" height={12} />
              {gap(8)}
              <Skeleton width="65%" height={12} />
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}

export function TableSkeleton({ rows = 6 }: { rows?: number }) {
  return (
    <div className="table-wrap" aria-busy="true" aria-label="Loading tasks">
      <table>
        <tbody>
          {Array.from({ length: rows }).map((_, i) => (
            <tr key={i}>
              <td style={{ width: '38%' }}><Skeleton width={`${55 + ((i * 13) % 35)}%`} /></td>
              <td><Skeleton width="70%" /></td>
              <td><Skeleton width={70} /></td>
              <td><Skeleton width={60} /></td>
              <td><Skeleton width={80} /></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function CardsSkeleton({ count = 3 }: { count?: number }) {
  return (
    <div className="cards" aria-busy="true" aria-label="Loading projects">
      {Array.from({ length: count }).map((_, i) => (
        <div className="panel" key={i}>
          <Skeleton width="60%" height={20} />
          {gap(12)}
          <Skeleton />
          {gap(6)}
          <Skeleton width="75%" />
          {gap(16)}
          <Skeleton width={110} height={28} />
        </div>
      ))}
    </div>
  );
}

export function ListSkeleton({ rows = 4 }: { rows?: number }) {
  return (
    <div className="panel" aria-busy="true" aria-label="Loading">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} style={{ padding: '.6rem 0' }}><Skeleton width={`${40 + ((i * 17) % 40)}%`} /></div>
      ))}
    </div>
  );
}