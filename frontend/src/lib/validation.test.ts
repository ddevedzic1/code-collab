import { describe, it, expect } from 'vitest';
import {
  isEmail,
  validateUsername,
  validateEmail,
  validatePassword,
  validatePasswordConfirm,
  validateRequired,
} from './validation';

describe('isEmail', () => {
  it.each([
    ['user@example.com', true],
    ['a.b-c@sub.domain.co', true],
    ['no-at-sign', false],
    ['missing@domain', false],
    ['@nolocal.com', false],
    ['spaces in@email.com', false],
    ['', false],
  ])('isEmail(%s) === %s', (value, expected) => {
    expect(isEmail(value)).toBe(expected);
  });
});

describe('validateUsername', () => {
  it('requires a non-blank value', () => {
    expect(validateUsername('   ')).toMatch(/required/i);
  });

  it('enforces the minimum length', () => {
    expect(validateUsername('ab')).toMatch(/at least 3/i);
  });

  it('accepts a valid username', () => {
    expect(validateUsername('alice')).toBeUndefined();
  });

  it('rejects an over-long username', () => {
    expect(validateUsername('a'.repeat(301))).toMatch(/at most 300/i);
  });
});

describe('validatePassword', () => {
  it('requires a value and enforces minimum length', () => {
    expect(validatePassword('')).toMatch(/required/i);
    expect(validatePassword('short')).toMatch(/at least 8/i);
    expect(validatePassword('longenough')).toBeUndefined();
  });
});

describe('validatePasswordConfirm', () => {
  it('requires confirmation', () => {
    expect(validatePasswordConfirm('password1', '')).toMatch(/confirm/i);
  });

  it('detects a mismatch', () => {
    expect(validatePasswordConfirm('password1', 'password2')).toMatch(/do not match/i);
  });

  it('passes when both match', () => {
    expect(validatePasswordConfirm('password1', 'password1')).toBeUndefined();
  });
});

describe('validateEmail', () => {
  it('reports missing and invalid emails distinctly', () => {
    expect(validateEmail('')).toMatch(/required/i);
    expect(validateEmail('bad')).toMatch(/not valid/i);
    expect(validateEmail('ok@example.com')).toBeUndefined();
  });
});

describe('validateRequired', () => {
  it('uses the provided label in the message', () => {
    expect(validateRequired('  ', 'Title')).toBe('Title is required.');
    expect(validateRequired('something', 'Title')).toBeUndefined();
  });
});
