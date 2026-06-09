import { useCallback, useEffect, useMemo, useState } from 'react';
import { Box, Flex, useDisclosure, useToast } from '@chakra-ui/react';
import { EditorTopBar } from './components/EditorTopBar';
import { RightPanelTabs } from './components/RightPanelTabs';
import { ShareModal } from './share-modal/ShareModal';
import { CodeEditor } from '../../components/CodeEditor';
import { Spinner } from '../../components/Spinner';
import { ErrorMessage } from '../../components/ErrorMessage';
import { useSnippet } from '../../hooks/useSnippet';
import { useExecution } from '../../hooks/useExecution';
import { snippetsApi } from '../../api/snippetsApi';
import { errorToast, successToast } from '../../components/toast';
import { isAppError } from '../../lib/normalizeError';
import { useLanguages } from '../../hooks/useLanguages';
import { isTerminalStatus } from '../../types/execution';
import type { Execution } from '../../types/execution';
import type { Snippet } from '../../types/snippet';

const OUTPUT_TAB = 0;

interface EditorViewProps {
  snippet: Snippet;
  onSnippetSaved: (snippet: Snippet) => void;
}

const EditorView = ({ snippet, onSnippetSaved }: EditorViewProps) => {
  const toast = useToast();
  const shareModal = useDisclosure();
  const { languages } = useLanguages();

  const [title, setTitle] = useState(snippet.title);
  const [content, setContent] = useState(snippet.content);
  const [languageId, setLanguageId] = useState(snippet.language.id);
  const [saving, setSaving] = useState(false);

  const [tabIndex, setTabIndex] = useState(OUTPUT_TAB);
  const [historyRefreshToken, setHistoryRefreshToken] = useState(0);

  const { execution, isRunning, error: executionError, run, loadExisting } =
    useExecution(snippet.id);

  useEffect(() => {
    if (execution && isTerminalStatus(execution.status)) {
      setHistoryRefreshToken(token => token + 1);
    }
  }, [execution?.status, execution?.id]); // eslint-disable-line react-hooks/exhaustive-deps

  const isDirty =
    title !== snippet.title ||
    content !== snippet.content ||
    languageId !== snippet.language.id;

  const selectedLanguage = useMemo(
    () =>
      languages.find(lang => lang.id === languageId) ?? snippet.language,
    [languages, languageId, snippet.language]
  );

  const handleSave = useCallback(async () => {
    if (saving || !isDirty) {
      return;
    }
    setSaving(true);
    try {
      const updated = await snippetsApi.updateSnippet(snippet.id, {
        title: title.trim(),
        content,
        languageId,
      });
      onSnippetSaved(updated);
      toast(successToast('Snippet saved.'));
    } catch (error) {
      if (isAppError(error)) {
        toast(errorToast(error));
      }
    } finally {
      setSaving(false);
    }
  }, [
    saving,
    isDirty,
    snippet.id,
    title,
    content,
    languageId,
    onSnippetSaved,
    toast,
  ]);

  const handleRun = useCallback(async () => {
    setTabIndex(OUTPUT_TAB);
    if (isDirty) {
      await handleSave();
    }
    await run(snippet.id);
    setHistoryRefreshToken(token => token + 1);
  }, [isDirty, handleSave, run, snippet.id]);

  const handleSelectExecution = useCallback(
    (selected: Execution) => {
      loadExisting(selected);
      setTabIndex(OUTPUT_TAB);
    },
    [loadExisting]
  );

  return (
    <Flex direction="column" h="100%">
      <EditorTopBar
        title={title}
        onTitleCommit={setTitle}
        languageId={languageId}
        currentLanguage={snippet.language}
        onLanguageChange={setLanguageId}
        onSave={handleSave}
        onRun={handleRun}
        onShare={shareModal.onOpen}
        isDirty={isDirty}
        isSaving={saving}
        isRunning={isRunning}
      />

      <Flex flex="1" overflow="hidden">
        <Box flex="1" p={3} overflow="hidden">
          <CodeEditor
            value={content}
            onChange={setContent}
            languageCode={selectedLanguage.code}
            height="100%"
            placeholder="Write your code here…"
          />
        </Box>

        <Box
          w="420px"
          borderLeftWidth="1px"
          borderColor="gray.700"
          bg="gray.800"
          flexShrink={0}
        >
          <RightPanelTabs
            snippetId={snippet.id}
            execution={execution}
            executionError={executionError}
            historyRefreshToken={historyRefreshToken}
            tabIndex={tabIndex}
            onTabChange={setTabIndex}
            onSelectExecution={handleSelectExecution}
          />
        </Box>
      </Flex>

      <ShareModal
        snippetId={snippet.id}
        isOpen={shareModal.isOpen}
        onClose={shareModal.onClose}
      />
    </Flex>
  );
};

interface EditorProps {
  snippetId: string;
}

export const Editor = ({ snippetId }: EditorProps) => {
  const { snippet, loading, error, setSnippet } = useSnippet(snippetId);

  if (loading) {
    return <Spinner full label="Loading snippet…" />;
  }

  if (error || !snippet) {
    return (
      <Box maxW="640px" mx="auto" mt={10} px={6}>
        <ErrorMessage
          error={error ?? 'This snippet could not be loaded.'}
        />
      </Box>
    );
  }

  return (
    <EditorView key={snippet.id} snippet={snippet} onSnippetSaved={setSnippet} />
  );
};
