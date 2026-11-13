import { describe, expect, it } from 'vitest';
import { formatPrice } from '../api.js';

describe('formatPrice', () => {
  it('shows a price range in Rands', () => {
    expect(formatPrice(1800, 4500)).toMatch(/^R1\s?800 – R4\s?500$/);
  });

  it('shows a single price when both ends match', () => {
    expect(formatPrice(550, 550)).toBe('R550');
  });

  it('handles a missing price list', () => {
    expect(formatPrice(null, null)).toBe('Price on inspection');
  });
});
