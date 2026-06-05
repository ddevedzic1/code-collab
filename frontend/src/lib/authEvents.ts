/**
 * Tiny module-level event bus that lets the axios interceptor (which lives
 * outside React and cannot navigate) announce a 401 to the AuthProvider.
 *
 * The interceptor only *emits*; the AuthProvider decides what to do
 * (ignore during bootstrap, or clear auth + redirect mid-session).
 */
type SessionExpiredListener = () => void;

const listeners = new Set<SessionExpiredListener>();

/** Called by the axios interceptor whenever a request returns 401. */
export const emitSessionExpired = (): void => {
  listeners.forEach(listener => listener());
};

/** Subscribe to session-expired events. Returns an unsubscribe function. */
export const onSessionExpired = (
  listener: SessionExpiredListener
): (() => void) => {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
};
