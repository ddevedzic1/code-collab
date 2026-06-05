import axios from 'axios';
import { normalizeError } from '../lib/normalizeError';
import { emitSessionExpired } from '../lib/authEvents';

/**
 * Central axios instance. Every request goes through the API gateway.
 *
 * `withCredentials: true` is mandatory: the session lives in a JSESSIONID
 * cookie that must be sent with every request. Without it, the cookie is
 * neither stored nor sent and all protected endpoints return 401.
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.response.use(
  response => response,
  error => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      emitSessionExpired();
    }
    return Promise.reject(normalizeError(error));
  }
);

export default api;
