import { useEffect, useState } from 'react';
import { api, formatPrice } from '../api.js';

export default function PriceList() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    api('/api/catalog').then(setItems).catch((e) => setError(e.message));
  }, []);

  if (error) return <p className="form-error">{error}</p>;
  if (items.length === 0) return <p className="muted">Loading prices…</p>;

  return (
    <table className="price-board">
      <thead>
        <tr>
          <th scope="col">Service</th>
          <th scope="col">Time</th>
          <th scope="col" className="num">Price</th>
        </tr>
      </thead>
      <tbody>
        {items.map((item) => (
          <tr key={item.code}>
            <td>
              <strong>{item.name}</strong>
              <span>{item.description}</span>
            </td>
            <td className="time">{item.estimatedTime}</td>
            <td className="num">{formatPrice(item.priceFrom, item.priceTo)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
