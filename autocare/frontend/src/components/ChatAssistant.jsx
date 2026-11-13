import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, formatPrice } from '../api.js';
import { useAuth } from '../context/AuthContext.jsx';

const EXAMPLES = [
  'Grinding noise when I brake',
  'Aircon only blows warm air',
  'Check engine light came on',
  'Car pulls to the left',
];

export default function ChatAssistant() {
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const logRef = useRef(null);

  useEffect(() => {
    if (logRef.current) logRef.current.scrollTop = logRef.current.scrollHeight;
  }, [messages, loading]);

  const ask = async (text) => {
    const question = text.trim();
    if (!question || loading) return;
    setError('');
    setInput('');
    setMessages((m) => [...m, { from: 'you', text: question }]);
    setLoading(true);
    try {
      const answer = await api('/api/ai/diagnose', { method: 'POST', body: { message: question } });
      setMessages((m) => [...m, { from: 'ai', text: answer.reply, suggestions: answer.suggestions, mode: answer.mode }]);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const bookLink = (code) => {
    const target = `/my-car?service=${encodeURIComponent(code)}`;
    if (user?.role === 'CUSTOMER' || user?.role === 'ADMIN') return target;
    return `/login?next=${encodeURIComponent(target)}`;
  };

  return (
    <section className="assistant" aria-label="Car problem assistant">
      <div className="assistant-log" ref={logRef} aria-live="polite">
        {messages.length === 0 && (
          <div className="assistant-empty">
            <p>Describe what you hear, feel or see. For example:</p>
            <div className="example-list">
              {EXAMPLES.map((example) => (
                <button key={example} type="button" className="chip" onClick={() => ask(example)}>
                  {example}
                </button>
              ))}
            </div>
          </div>
        )}

        {messages.map((message, i) => (
          <div key={i} className={`bubble bubble-${message.from}`}>
            <p>{message.text}</p>
            {message.suggestions?.length > 0 && (
              <ul className="suggestions">
                {message.suggestions.map((s) => (
                  <li key={s.code}>
                    <div>
                      <strong>{s.name}</strong>
                      <span className="price">{formatPrice(s.priceFrom, s.priceTo)}</span>
                    </div>
                    <Link to={bookLink(s.code)} className="button button-small">Book this</Link>
                  </li>
                ))}
              </ul>
            )}
            {message.mode === 'offline' && (
              <p className="bubble-note">Answered from the workshop guide (AI model offline).</p>
            )}
          </div>
        ))}

        {loading && <div className="bubble bubble-ai bubble-typing">Checking the workshop guide…</div>}
      </div>

      {error && <p className="form-error" role="alert">{error}</p>}

      <form
        className="assistant-form"
        onSubmit={(e) => {
          e.preventDefault();
          ask(input);
        }}
      >
        <label htmlFor="problem" className="visually-hidden">Describe your car problem</label>
        <input
          id="problem"
          value={input}
          maxLength={500}
          placeholder="e.g. my steering wheel shakes when I brake on the highway"
          onChange={(e) => setInput(e.target.value)}
          disabled={loading}
        />
        <button type="submit" className="button" disabled={loading || !input.trim()}>
          Ask
        </button>
      </form>
    </section>
  );
}
