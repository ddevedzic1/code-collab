import type { SnippetLanguage } from './language';

export interface Snippet {
  id: string;
  userId: string;
  language: SnippetLanguage;
  title: string;
  content: string;
}

export interface SnippetCreateRequest {
  languageId: string;
  title: string;
  content?: string;
}

export interface SnippetUpdateRequest {
  languageId?: string;
  title?: string;
  content?: string;
}

/** Query parameters for the snippet list endpoint. `page` is zero-indexed. */
export interface SnippetListParams {
  title?: string;
  languageId?: string;
  page?: number;
  size?: number;
  sort?: string;
}
