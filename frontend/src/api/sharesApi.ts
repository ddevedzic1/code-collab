import api from './axios';
import { isAppError } from '../lib/normalizeError';
import type {
  Share,
  ShareCreateRequest,
  SharedSnippet,
  ShareUpdateRequest,
  ShareUser,
  ShareUserCreateRequest,
} from '../types/share';

export const sharesApi = {
  /**
   * Returns the snippet's active share, or `null` when none exists.
   * A 404 from the backend is the valid "no share yet" state, not an error.
   */
  getShare: async (snippetId: string): Promise<Share | null> => {
    try {
      const { data } = await api.get<Share>(`/snippets/${snippetId}/share`);
      return data;
    } catch (error) {
      if (isAppError(error) && error.status === 404) {
        return null;
      }
      throw error;
    }
  },

  createShare: async (
    snippetId: string,
    body: ShareCreateRequest
  ): Promise<Share> => {
    const { data } = await api.post<Share>(
      `/snippets/${snippetId}/share`,
      body
    );
    return data;
  },

  updateShare: async (
    shareId: string,
    body: ShareUpdateRequest
  ): Promise<Share> => {
    const { data } = await api.patch<Share>(`/shares/${shareId}`, body);
    return data;
  },

  deleteShare: async (shareId: string): Promise<void> => {
    await api.delete(`/shares/${shareId}`);
  },

  getShareByToken: async (token: string): Promise<SharedSnippet> => {
    const { data } = await api.get<SharedSnippet>(
      `/shares/by-token/${encodeURIComponent(token)}`
    );
    return data;
  },

  listShareUsers: async (shareId: string): Promise<ShareUser[]> => {
    const { data } = await api.get<ShareUser[]>(`/shares/${shareId}/users`);
    return data;
  },

  addShareUser: async (
    shareId: string,
    body: ShareUserCreateRequest
  ): Promise<ShareUser> => {
    const { data } = await api.post<ShareUser>(
      `/shares/${shareId}/users`,
      body
    );
    return data;
  },

  removeShareUser: async (shareId: string, userId: string): Promise<void> => {
    await api.delete(`/shares/${shareId}/users/${userId}`);
  },

  removeAllShareUsers: async (shareId: string): Promise<void> => {
    await api.delete(`/shares/${shareId}/users`);
  },
};
