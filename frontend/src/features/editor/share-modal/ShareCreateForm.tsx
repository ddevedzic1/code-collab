import { useState } from 'react';
import {
  FormControl,
  FormLabel,
  Radio,
  RadioGroup,
  Stack,
  Text,
  VStack,
  useToast,
} from '@chakra-ui/react';
import { LoadingButton } from '../../../components/LoadingButton';
import { errorToast, successToast } from '../../../components/toast';
import { isAppError } from '../../../lib/normalizeError';
import {
  Permission,
  ShareType,
  type ShareCreateRequest,
} from '../../../types/share';

interface ShareCreateFormProps {
  onCreate: (body: ShareCreateRequest) => Promise<unknown>;
}

export const ShareCreateForm = ({ onCreate }: ShareCreateFormProps) => {
  const toast = useToast();
  const [shareType, setShareType] = useState<ShareType>(ShareType.PUBLIC_LINK);
  const [permission, setPermission] = useState<Permission>(
    Permission.READ_ONLY
  );
  const [submitting, setSubmitting] = useState(false);

  const handleCreate = async () => {
    setSubmitting(true);
    try {
      await onCreate({ shareType, permission });
      toast(successToast('Share created.'));
    } catch (error) {
      if (isAppError(error)) {
        toast(errorToast(error));
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <VStack align="stretch" spacing={5}>
      <Text fontSize="sm" color="textColor.medium">
        This snippet isn&apos;t shared yet. Create a share to generate a link.
      </Text>

      <FormControl>
        <FormLabel fontSize="sm">Share type</FormLabel>
        <RadioGroup
          value={shareType}
          onChange={value => setShareType(value as ShareType)}
        >
          <Stack spacing={2}>
            <Radio value={ShareType.PUBLIC_LINK}>
              Public link — anyone with the link
            </Radio>
            <Radio value={ShareType.USER}>
              Specific users — only people you add
            </Radio>
          </Stack>
        </RadioGroup>
      </FormControl>

      <FormControl>
        <FormLabel fontSize="sm">Default permission</FormLabel>
        <RadioGroup
          value={permission}
          onChange={value => setPermission(value as Permission)}
        >
          <Stack direction="row" spacing={4}>
            <Radio value={Permission.READ_ONLY}>Read only</Radio>
            <Radio value={Permission.EDIT}>Can edit</Radio>
          </Stack>
        </RadioGroup>
      </FormControl>

      <LoadingButton
        colorScheme="blue"
        onClick={handleCreate}
        isLoading={submitting}
        alignSelf="flex-start"
      >
        Create share
      </LoadingButton>
    </VStack>
  );
};
