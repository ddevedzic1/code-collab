import { describe, it, expect } from 'vitest';
import { buildParams } from './buildParams';

describe('buildParams', () => {
  it('keeps defined non-empty values', () => {
    expect(
      buildParams({ title: 'hello', page: 0, size: 20 })
    ).toEqual({ title: 'hello', page: 0, size: 20 });
  });

  it('drops undefined and null values', () => {
    expect(
      buildParams({ title: undefined, languageId: null, page: 1 })
    ).toEqual({ page: 1 });
  });

  it('drops blank / whitespace-only strings', () => {
    expect(buildParams({ title: '', sort: '   ', page: 2 })).toEqual({
      page: 2,
    });
  });

  it('keeps zero (a meaningful page number)', () => {
    expect(buildParams({ page: 0 })).toEqual({ page: 0 });
  });
});
