import { ResultRow } from './resultRow';

export const resultData: ResultRow[] = [
  {
    resultId: 101,
    status: 'Success',
    timestamp: '2025-10-14T10:00:00Z',
    delivered: true
  },
  {
    resultId: 102,
    status: 'Pending',
    timestamp: '2025-10-15T10:05:00Z',
    delivered: false
  },
  {
    resultId: 103,
    status: 'Failed',
    timestamp: '2025-10-15T10:10:00Z',
    delivered: false
  },
  {
    resultId: 104,
    status: 'Success',
    timestamp: '2025-10-15T10:15:00Z',
    delivered: true
  },
  {
    resultId: 105,
    status: 'Pending',
    timestamp: '2025-10-15T10:20:00Z',
    delivered: false
  }
];
