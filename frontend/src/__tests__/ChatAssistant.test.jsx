import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import ChatAssistant from '../components/ChatAssistant.jsx';
import { AuthProvider } from '../context/AuthContext.jsx';

function renderAssistant() {
  return render(
    <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <AuthProvider>
        <ChatAssistant />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('ChatAssistant', () => {
  afterEach(() => vi.unstubAllGlobals());

  it('asks the AI service and shows the suggested service with its price', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      text: () => Promise.resolve(JSON.stringify({
        reply: 'Sounds like worn brake pads. A mechanic will confirm.',
        mode: 'ai',
        suggestions: [{ code: 'BRAKE_SERVICE', name: 'Brake pads & discs', priceFrom: 1800, priceTo: 4500 }],
      })),
    });
    vi.stubGlobal('fetch', fetchMock);

    renderAssistant();
    fireEvent.click(screen.getByRole('button', { name: 'Grinding noise when I brake' }));

    expect(await screen.findByText('Sounds like worn brake pads. A mechanic will confirm.')).toBeInTheDocument();
    expect(screen.getByText('Brake pads & discs')).toBeInTheDocument();

    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toMatch(/\/api\/ai\/diagnose$/);
    expect(JSON.parse(options.body)).toEqual({ message: 'Grinding noise when I brake' });

    // Visitors who aren't logged in are sent to log in first, then straight to booking
    expect(screen.getByRole('link', { name: 'Book this' }).getAttribute('href'))
      .toBe('/login?next=%2Fmy-car%3Fservice%3DBRAKE_SERVICE');
  });

  it('shows a helpful error when the server is down', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')));

    renderAssistant();
    fireEvent.change(screen.getByLabelText('Describe your car problem'), { target: { value: 'Car makes a noise' } });
    fireEvent.click(screen.getByRole('button', { name: 'Ask' }));

    expect(await screen.findByRole('alert')).toHaveTextContent("Can't reach the server");
  });
});
