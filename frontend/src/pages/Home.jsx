import ChatAssistant from '../components/ChatAssistant.jsx';
import DemoAccounts from '../components/DemoAccounts.jsx';
import PriceList from '../components/PriceList.jsx';
import { useAuth } from '../context/AuthContext.jsx';

export default function Home() {
  const { user } = useAuth();

  return (
    <>
      <section className="hero">
        <div className="hero-copy">
          <h1>What&rsquo;s your car doing?</h1>
          <p className="lead">
            Tell the assistant about the noise, the warning light or the smell. It checks our workshop
            guide, explains the likely cause and gives you a price before you book.
          </p>
          {!user && <DemoAccounts />}
        </div>
        <ChatAssistant />
      </section>

      <section className="section">
        <div className="section-head">
          <h2>Workshop prices</h2>
          <p className="muted">Ranges depend on your car. We confirm the final quote before starting any work.</p>
        </div>
        <PriceList />
      </section>
    </>
  );
}
