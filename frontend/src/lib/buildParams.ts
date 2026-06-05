/**
 * Build a URLSearchParams-friendly object, omitting undefined / null / empty
 * values so we never send blank query parameters (e.g. `?title=`).
 */
export const buildParams = (
  params: Record<string, string | number | undefined | null>
): Record<string, string | number> => {
  const result: Record<string, string | number> = {};
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null) {
      continue;
    }
    if (typeof value === 'string' && value.trim() === '') {
      continue;
    }
    result[key] = value;
  }
  return result;
};
