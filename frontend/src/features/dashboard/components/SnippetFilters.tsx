import {
  HStack,
  Input,
  InputGroup,
  InputLeftElement,
} from '@chakra-ui/react';
import { FiSearch } from 'react-icons/fi';
import { LanguageSelect } from '../../../components/LanguageSelect';

interface SnippetFiltersProps {
  title: string;
  onTitleChange: (value: string) => void;
  languageId: string;
  onLanguageChange: (value: string) => void;
}

export const SnippetFilters = ({
  title,
  onTitleChange,
  languageId,
  onLanguageChange,
}: SnippetFiltersProps) => (
  <HStack spacing={3} w="100%">
    <InputGroup maxW="360px">
      <InputLeftElement pointerEvents="none">
        <FiSearch color="var(--chakra-colors-textColor-medium)" />
      </InputLeftElement>
      <Input
        placeholder="Search by title…"
        value={title}
        onChange={event => onTitleChange(event.target.value)}
      />
    </InputGroup>

    <LanguageSelect
      value={languageId}
      includeAllOption
      maxW="220px"
      onChange={event => onLanguageChange(event.target.value)}
    />
  </HStack>
);
