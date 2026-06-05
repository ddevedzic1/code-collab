/**
 * Programming language. The `/languages` endpoint returns a plain array of
 * these (not paginated). The nested `language` object on a snippet only
 * carries the first four fields.
 */
export interface Language {
  id: string;
  code: string;
  name: string;
  version: string;
  runtimeImage?: string;
  startDate?: string;
  endDate?: string | null;
}

/** The trimmed language object embedded in snippet and share responses. */
export interface SnippetLanguage {
  id: string;
  code: string;
  name: string;
  version: string;
}
