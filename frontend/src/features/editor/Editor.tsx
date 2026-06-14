import { useCallback, useEffect, useState } from 'react';
import { Box, Flex, useDisclosure, useToast } from '@chakra-ui/react';
import { EditorTopBar } from './components/EditorTopBar';
import { RightPanelTabs } from './components/RightPanelTabs';
import { ShareModal } from './share-modal/ShareModal';
import { CodeEditor } from '../../components/CodeEditor';
import { Spinner } from '../../components/Spinner';
import { ErrorMessage } from '../../components/ErrorMessage';
import { errorToast } from '../../components/toast';
import { useSnippet } from '../../hooks/useSnippet';
import { useExecution } from '../../hooks/useExecution';
import { useSubmit } from '../../hooks/useSubmit';
import { snippetsApi } from '../../api/snippetsApi';
import { isTerminalStatus } from '../../types/execution';
import type { Execution } from '../../types/execution';
import type { Snippet } from '../../types/snippet';

const OUTPUT_TAB = 0;

interface EditorViewProps {
  snippet: Snippet;
  onSnippetSaved: (snippet: Snippet) => void;
}

const EditorView = ({ snippet, onSnippetSaved }: EditorViewProps) => {
  const shareModal = useDisclosure();
  const toast = useToast();

  const [title, setTitle] = useState(snippet.title);
  const [content, setContent] = useState(snippet.content);
  const { submitting: saving, run: runSave } = useSubmit();

  const [tabIndex, setTabIndex] = useState(OUTPUT_TAB);
  const [historyRefreshToken, setHistoryRefreshToken] = useState(0);

  const { execution, isRunning, run, loadExisting } = useExecution(snippet.id, {
    onError: error => toast(errorToast(error)),
  });

  useEffect(() => {
    if (execution && isTerminalStatus(execution.status)) {
      setHistoryRefreshToken(token => token + 1);
    }
  }, [execution?.status, execution?.id]); // eslint-disable-line react-hooks/exhaustive-deps

  const isDirty =
    title !== snippet.title || content !== snippet.content;

  const handleSave = useCallback(async () => {
    await runSave(
      () =>
        snippetsApi.updateSnippet(snippet.id, {
          title: title.trim(),
          content,
        }),
      {
        guard: () => isDirty,
        successMessage: 'Snippet saved.',
        onSuccess: updated => onSnippetSaved(updated),
      }
    );
  }, [runSave, isDirty, snippet.id, title, content, onSnippetSaved]);

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

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 's') {
        event.preventDefault();
        handleSave();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [handleSave]);

  return (
    <Flex direction="column" h="100%">
      <EditorTopBar
        title={title}
        onTitleCommit={setTitle}
        language={snippet.language}
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
            languageCode={snippet.language.code}
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
