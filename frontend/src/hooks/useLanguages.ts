import { useEffect, useState } from 'react';
import { languagesApi } from '../api/languagesApi';
import type { Language } from '../types/language';
import type { AppError } from '../types/api';

/**
 * Module-level cache: languages rarely change during a session, so the first
 * successful fetch is shared by every consumer. A failed fetch is not cached.
 */
let cache: Language[] | null = null;
let inFlight: Promise<Language[]> | null = null;

const loadLanguages = (): Promise<Language[]> => {
  if (cache) {
    return Promise.resolve(cache);
  }
  if (!inFlight) {
    inFlight = languagesApi
      .listLanguages()
      .then(languages => {
        cache = languages;
        return languages;
      })
      .finally(() => {
        inFlight = null;
      });
  }
  return inFlight;
};

interface UseLanguagesResult {
  languages: Language[];
  loading: boolean;
  error: AppError | null;
}

export const useLanguages = (): UseLanguagesResult => {
  const [languages, setLanguages] = useState<Language[]>(cache ?? []);
  const [loading, setLoading] = useState(cache === null);
  const [error, setError] = useState<AppError | null>(null);

  useEffect(() => {
    if (cache) {
      setLanguages(cache);
      setLoading(false);
      return;
    }

    let active = true;
    setLoading(true);
    loadLanguages()
      .then(result => {
        if (active) {
          setLanguages(result);
        }
      })
      .catch((err: AppError) => {
        if (active) {
          setError(err);
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  return { languages, loading, error };
};
