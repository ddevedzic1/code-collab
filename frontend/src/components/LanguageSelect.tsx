import { Select, type SelectProps } from '@chakra-ui/react';
import { useLanguages } from '../hooks/useLanguages';
import type { SnippetLanguage } from '../types/language';

interface LanguageSelectProps extends Omit<SelectProps, 'children'> {
  /** Currently selected language id (empty string for "no selection"). */
  value: string;
  /**
   * A language to guarantee is present in the options even if the languages
   * list failed to load (e.g. the snippet's current language in the editor).
   */
  fallbackLanguage?: SnippetLanguage;
  /** Show an "All languages" option (used by the dashboard filter). */
  includeAllOption?: boolean;
}

export const LanguageSelect = ({
  value,
  fallbackLanguage,
  includeAllOption = false,
  placeholder,
  ...props
}: LanguageSelectProps) => {
  const { languages, loading } = useLanguages();

  const hasFallbackInList =
    !fallbackLanguage || languages.some(lang => lang.id === fallbackLanguage.id);

  const options = hasFallbackInList
    ? languages
    : [...languages, fallbackLanguage];

  const isEmpty = options.length === 0 && !loading;

  return (
    <Select
      value={value}
      placeholder={
        placeholder ?? (includeAllOption ? 'All languages' : 'Select a language')
      }
      isDisabled={props.isDisabled || isEmpty}
      {...props}
    >
      {options.map(language => (
        <option key={language.id} value={language.id}>
          {language.name}
          {language.version ? ` (${language.version})` : ''}
        </option>
      ))}
    </Select>
  );
};
