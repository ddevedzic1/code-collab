import type { Extension } from '@codemirror/state';
import { python } from '@codemirror/lang-python';
import { javascript } from '@codemirror/lang-javascript';
import { java } from '@codemirror/lang-java';
import { cpp } from '@codemirror/lang-cpp';
import { rust } from '@codemirror/lang-rust';
import { go } from '@codemirror/lang-go';
import { php } from '@codemirror/lang-php';
import { sql } from '@codemirror/lang-sql';

/**
 * Map a backend `language.code` to a CodeMirror language extension factory.
 * Keys are lowercased. Unknown codes fall back to no extension, so the editor
 * still works as a plain-text editor instead of crashing.
 */
const LANGUAGE_FACTORIES: Record<string, () => Extension> = {
  python: python,
  py: python,
  javascript: () => javascript(),
  js: () => javascript(),
  node: () => javascript(),
  nodejs: () => javascript(),
  typescript: () => javascript({ typescript: true }),
  ts: () => javascript({ typescript: true }),
  java: java,
  c: () => cpp(),
  cpp: cpp,
  'c++': cpp,
  rust: rust,
  rs: rust,
  go: go,
  golang: go,
  php: php,
  sql: sql,
};

/**
 * Returns the CodeMirror language extensions for the given language code.
 * Always returns an array so it can be spread directly into `extensions`.
 */
export const getLanguageExtension = (code?: string): Extension[] => {
  if (!code) {
    return [];
  }
  const factory = LANGUAGE_FACTORIES[code.toLowerCase()];
  return factory ? [factory()] : [];
};
