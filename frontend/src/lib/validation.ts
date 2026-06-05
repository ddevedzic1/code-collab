export const MIN_USERNAME_LENGTH = 3;
export const MIN_PASSWORD_LENGTH = 8;
export const MAX_FIELD_LENGTH = 300;

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const isEmail = (value: string): boolean => EMAIL_REGEX.test(value);

export const isBlank = (value: string): boolean => value.trim().length === 0;

/** A field error map: field name -> error message (absent when valid). */
export type FieldErrors<T extends string> = Partial<Record<T, string>>;

export const validateUsername = (username: string): string | undefined => {
  if (isBlank(username)) {
    return 'Username is required.';
  }
  if (username.trim().length < MIN_USERNAME_LENGTH) {
    return `Username must be at least ${MIN_USERNAME_LENGTH} characters.`;
  }
  if (username.length > MAX_FIELD_LENGTH) {
    return `Username must be at most ${MAX_FIELD_LENGTH} characters.`;
  }
  return undefined;
};

export const validateEmail = (email: string): string | undefined => {
  if (isBlank(email)) {
    return 'Email is required.';
  }
  if (!isEmail(email)) {
    return 'Email address is not valid.';
  }
  return undefined;
};

export const validatePassword = (password: string): string | undefined => {
  if (isBlank(password)) {
    return 'Password is required.';
  }
  if (password.length < MIN_PASSWORD_LENGTH) {
    return `Password must be at least ${MIN_PASSWORD_LENGTH} characters.`;
  }
  return undefined;
};

export const validatePasswordConfirm = (
  password: string,
  confirm: string
): string | undefined => {
  if (isBlank(confirm)) {
    return 'Please confirm your password.';
  }
  if (password !== confirm) {
    return 'Passwords do not match.';
  }
  return undefined;
};

export const validateRequired = (
  value: string,
  label: string
): string | undefined => {
  if (isBlank(value)) {
    return `${label} is required.`;
  }
  return undefined;
};
