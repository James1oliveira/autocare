import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import StatusSteps from '../components/StatusSteps.jsx';

describe('StatusSteps', () => {
  it('marks earlier steps done and highlights the current one', () => {
    render(<StatusSteps status="IN_PROGRESS" />);
    expect(screen.getByText('Booked')).toHaveClass('step-done');
    expect(screen.getByText('In progress')).toHaveAttribute('aria-current', 'step');
    expect(screen.getByText('Ready to collect')).toHaveClass('step-todo');
  });

  it('shows a message for cancelled bookings', () => {
    render(<StatusSteps status="CANCELLED" />);
    expect(screen.getByText('This booking was cancelled.')).toBeInTheDocument();
  });
});
