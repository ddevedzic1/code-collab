import { Navigate, useParams } from 'react-router-dom';
import { AppLayout } from '../components/AppLayout';
import { Editor } from '../features/editor/Editor';

export const EditorPage = () => {
  const { snippetId } = useParams<{ snippetId: string }>();

  if (!snippetId) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <AppLayout fluid>
      <Editor snippetId={snippetId} />
    </AppLayout>
  );
};
