import api from './axios';
import type { Language } from '../types/language';

export const languagesApi = {
  /** Returns a plain array (this endpoint is not paginated). */
  listLanguages: async (): Promise<Language[]> => {
    const { data } = await api.get<Language[]>('/languages');
    return data;
  },
};
