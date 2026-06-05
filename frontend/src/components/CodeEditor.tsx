import { useMemo } from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { oneDark } from '@codemirror/theme-one-dark';
import { EditorView } from '@codemirror/view';
import { Box } from '@chakra-ui/react';
import { getLanguageExtension } from '../lib/codemirrorLanguages';

interface CodeEditorProps {
  value: string;
  onChange?: (value: string) => void;
  languageCode?: string;
  readOnly?: boolean;
  height?: string;
  placeholder?: string;
}

export const CodeEditor = ({
  value,
  onChange,
  languageCode,
  readOnly = false,
  height = '100%',
  placeholder,
}: CodeEditorProps) => {
  // Rebuild extensions only when the language changes, not on every keystroke.
  const extensions = useMemo(
    () => [...getLanguageExtension(languageCode), EditorView.lineWrapping],
    [languageCode]
  );

  return (
    <Box
      h={height}
      overflow="hidden"
      borderWidth="1px"
      borderColor="gray.700"
      borderRadius="md"
      sx={{
        '.cm-editor': { height: '100%' },
        '.cm-scroller': { fontFamily: 'mono', fontSize: '13px' },
        '.cm-editor.cm-focused': { outline: 'none' },
      }}
    >
      <CodeMirror
        value={value}
        height="100%"
        theme={oneDark}
        extensions={extensions}
        editable={!readOnly}
        readOnly={readOnly}
        placeholder={placeholder}
        onChange={onChange}
        basicSetup={{
          lineNumbers: true,
          highlightActiveLine: !readOnly,
          highlightActiveLineGutter: !readOnly,
          foldGutter: true,
        }}
      />
    </Box>
  );
};
