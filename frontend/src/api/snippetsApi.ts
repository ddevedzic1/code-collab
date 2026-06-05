import api from './axios';
import { buildParams } from '../lib/buildParams';
import type { PageResult } from '../types/api';
import type {
  Snippet,
  SnippetCreateRequest,
  SnippetListParams,
  SnippetUpdateRequest,
} from '../types/snippet';

export const snippetsApi = {
  listSnippets: async (
    params: SnippetListParams
  ): Promise<PageResult<Snippet>> => {
    const { data } = await api.get<PageResult<Snippet>>('/snippets', {
      params: buildParams({ ...params }),
    });
    return data;
  },

  getSnippet: async (id: string): Promise<Snippet> => {
    const { data } = await api.get<Snippet>(`/snippets/${id}`);
    return data;
  },

  createSnippet: async (body: SnippetCreateRequest): Promise<Snippet> => {
    const { data } = await api.post<Snippet>('/snippets', body);
    return data;
  },

  updateSnippet: async (
    id: string,
    body: SnippetUpdateRequest
  ): Promise<Snippet> => {
    const { data } = await api.patch<Snippet>(`/snippets/${id}`, body);
    return data;
  },

  deleteSnippet: async (id: string): Promise<void> => {
    await api.delete(`/snippets/${id}`);
  },
};
