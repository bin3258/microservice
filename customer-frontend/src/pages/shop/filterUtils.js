export function parseRam(ram) {
  if (!ram) return null;
  const match = ram.match(/(\d+)\s*GB/i);
  return match ? parseInt(match[1]) : null;
}

export function parseStorage(storage) {
  if (!storage) return null;
  const match = storage.match(/(\d+)\s*GB/i);
  return match ? parseInt(match[1]) : null;
}

export function parseBattery(battery) {
  if (!battery) return null;
  const match = battery.match(/(\d+)/);
  return match ? parseInt(match[1]) : null;
}

export function categorizeScreen(resolution) {
  if (!resolution) return null;

  const match = resolution.match(/(\d+)\s*[x×]\s*(\d+)/i);
  if (match) {
    const dim = Math.min(parseInt(match[1]), parseInt(match[2]));
    if (dim >= 2160) return '4K';
    if (dim >= 1440) return '2K+';
    if (dim >= 1080) return 'FullHD+';
    if (dim >= 720) return 'HD';
    return 'Khác';
  }

  const text = resolution.toLowerCase();
  if (text.includes('fullhd') || text.includes('full hd') || text.includes('fullhd+')) return 'FullHD+';
  if (text.includes('hd')) return 'HD';
  if (text.includes('1.5k') || text.includes('1,5k')) return '1.5K';
  if (text.includes('4k') || text.includes('2160')) return '4K';
  if (text.includes('2k') || text.includes('qhd') || text.includes('1440')) return '2K+';

  return 'Khác';
}

export const RAM_OPTIONS = [
  { label: 'Dưới 4GB', value: 'lt4', match: (v) => v !== null && v < 4 },
  { label: '4GB', value: '4', match: (v) => v === 4 },
  { label: '6GB', value: '6', match: (v) => v === 6 },
  { label: '8GB', value: '8', match: (v) => v === 8 },
  { label: '12GB', value: '12', match: (v) => v === 12 },
  { label: '16GB+', value: '16plus', match: (v) => v !== null && v >= 16 },
];

export const STORAGE_OPTIONS = [
  { label: '64GB', value: '64', match: (v) => v === 64 },
  { label: '128GB', value: '128', match: (v) => v === 128 },
  { label: '256GB', value: '256', match: (v) => v === 256 },
  { label: '512GB+', value: '512plus', match: (v) => v !== null && v >= 512 },
];

export const BATTERY_OPTIONS = [
  { label: 'Dưới 4000 mAh', value: 'lt4000', match: (v) => v !== null && v < 4000 },
  { label: '4000 - 5000 mAh', value: '4000-5000', match: (v) => v !== null && v >= 4000 && v <= 5000 },
  { label: 'Trên 5000 mAh', value: 'gt5000', match: (v) => v !== null && v > 5000 },
];

export const SCREEN_OPTIONS = [
  { label: 'HD', value: 'HD' },
  { label: 'FullHD+', value: 'FullHD+' },
  { label: '1.5K', value: '1.5K' },
  { label: '2K+', value: '2K+' },
  { label: '4K', value: '4K' },
];
