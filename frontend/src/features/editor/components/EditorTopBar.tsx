import {
  Box,
  Button,
  HStack,
  IconButton,
  Tooltip,
} from '@chakra-ui/react';
import { FiArrowLeft, FiPlay, FiSave, FiShare2 } from 'react-icons/fi';
import { useNavigate } from 'react-router-dom';
import { ControlledInput } from '../../../components/ControlledInput';
import { LanguageSelect } from '../../../components/LanguageSelect';
import { LoadingButton } from '../../../components/LoadingButton';
import type { SnippetLanguage } from '../../../types/language';

interface EditorTopBarProps {
  title: string;
  onTitleCommit: (title: string) => void;
  languageId: string;
  currentLanguage: SnippetLanguage;
  onLanguageChange: (languageId: string) => void;
  onSave: () => void;
  onRun: () => void;
  onShare: () => void;
  isDirty: boolean;
  isSaving: boolean;
  isRunning: boolean;
}

export const EditorTopBar = ({
  title,
  onTitleCommit,
  languageId,
  currentLanguage,
  onLanguageChange,
  onSave,
  onRun,
  onShare,
  isDirty,
  isSaving,
  isRunning,
}: EditorTopBarProps) => {
  const navigate = useNavigate();

  return (
    <HStack
      px={4}
      h="56px"
      borderBottomWidth="1px"
      borderColor="gray.700"
      bg="gray.800"
      spacing={3}
      flexShrink={0}
    >
      <Tooltip label="Back to dashboard">
        <IconButton
          aria-label="Back to dashboard"
          icon={<FiArrowLeft />}
          size="sm"
          variant="ghost"
          onClick={() => navigate('/dashboard')}
        />
      </Tooltip>

      <Box maxW="360px" flex="1">
        <ControlledInput
          defaultValue={title}
          onCommit={onTitleCommit}
          variant="unstyled"
          fontWeight="semibold"
          fontSize="md"
          px={2}
          _hover={{ bg: 'gray.700' }}
          borderRadius="md"
          aria-label="Snippet title"
        />
      </Box>

      <LanguageSelect
        value={languageId}
        fallbackLanguage={currentLanguage}
        w="200px"
        onChange={event => onLanguageChange(event.target.value)}
      />

      <Box flex="1" />

      <LoadingButton
        leftIcon={<FiSave />}
        variant="outline"
        borderColor="gray.700"
        onClick={onSave}
        isLoading={isSaving}
        isDisabled={!isDirty}
      >
        Save
      </LoadingButton>

      <LoadingButton
        leftIcon={<FiPlay />}
        colorScheme="green"
        onClick={onRun}
        isLoading={isRunning}
      >
        Run
      </LoadingButton>

      <Button leftIcon={<FiShare2 />} colorScheme="blue" onClick={onShare}>
        Share
      </Button>
    </HStack>
  );
};
