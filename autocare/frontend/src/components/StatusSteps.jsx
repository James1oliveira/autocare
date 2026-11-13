import { STATUS_LABELS } from '../api.js';

const STEPS = ['BOOKED', 'IN_PROGRESS', 'READY', 'COLLECTED'];

/** Shows where a job is in the workshop: Booked, In progress, Ready, Collected. */
export default function StatusSteps({ status }) {
  if (status === 'CANCELLED') {
    return <p className="status-cancelled">This booking was cancelled.</p>;
  }
  const current = STEPS.indexOf(status);
  return (
    <ol className="steps" aria-label={`Status: ${STATUS_LABELS[status]}`}>
      {STEPS.map((step, i) => {
        let state = 'todo';
        if (i < current) state = 'done';
        if (i === current) state = 'current';
        return (
          <li key={step} className={`step step-${state}`} aria-current={state === 'current' ? 'step' : undefined}>
            {STATUS_LABELS[step]}
          </li>
        );
      })}
    </ol>
  );
}
