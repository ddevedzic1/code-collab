import { useState } from 'react';
import {
  FormControl,
  FormLabel,
  Radio,
  RadioGroup,
  Stack,
  Text,
  VStack,
} from '@chakra-ui/react';
import { LoadingButton } from '../../../components/LoadingButton';
import { useSubmit } from '../../../hooks/useSubmit';
import {
  Permission,
  ShareType,
  type ShareCreateRequest,
} from '../../../types/share';

interface ShareCreateFormProps {
  onCreate: (body: ShareCreateRequest) => Promise<unknown>;
}

export const ShareCreateForm = ({ onCreate }: ShareCreateFormProps) => {
  const { submitting, run } = useSubmit();
  const [shareType, setShareType] = useState<ShareType>(ShareType.PUBLIC_LINK);
  const [permission, setPermission] = useState<Permission>(
    Permission.READ_ONLY
  );

  const handleCreate = async () => {
    await run(() => onCreate({ shareType, permission }), {
      successMessage: 'Share created.',
    });
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
