import { useState } from 'react';
import {
  Box,
  Button,
  Divider,
  FormControl,
  FormLabel,
  HStack,
  IconButton,
  Input,
  InputGroup,
  InputRightElement,
  Select,
  Tooltip,
  useDisclosure,
  useToast,
  VStack,
} from '@chakra-ui/react';
import { FiCheck, FiCopy } from 'react-icons/fi';
import { ShareUsersPanel } from './ShareUsersPanel';
import { ConfirmDialog } from '../../../components/ConfirmDialog';
import { copyToClipboard } from '../../../lib/clipboard';
import { errorToast } from '../../../components/toast';
import { useSubmit } from '../../../hooks/useSubmit';
import {
  Permission,
  ShareType,
  type Share,
  type ShareUpdateRequest,
} from '../../../types/share';

interface ShareDetailsProps {
  share: Share;
  onUpdate: (body: ShareUpdateRequest) => Promise<Share>;
  onDelete: () => Promise<void>;
}

export const ShareDetails = ({
  share,
  onUpdate,
  onDelete,
}: ShareDetailsProps) => {
  const toast = useToast();
  const deleteDialog = useDisclosure();
  const typeSubmit = useSubmit();
  const permissionSubmit = useSubmit();
  const deleteSubmit = useSubmit();
  const [copied, setCopied] = useState(false);

  const shareUrl = `${window.location.origin}/s/${share.shareToken}`;

  const handleCopy = async () => {
    const ok = await copyToClipboard(shareUrl);
    if (ok) {
      setCopied(true);
      window.setTimeout(() => setCopied(false), 1500);
    } else {
      toast(
        errorToast({
          code: 'unknown-error',
          message: 'Could not copy the link. Please copy it manually.',
          isNetwork: false,
        })
      );
    }
  };

  const handleDelete = async () => {
    await deleteSubmit.run(() => onDelete(), {
      successMessage: 'Share deleted.',
      onSuccess: () => deleteDialog.onClose(),
    });
  };

  return (
    <VStack align="stretch" spacing={5}>
      <FormControl>
        <FormLabel fontSize="sm">Share link</FormLabel>
        <InputGroup>
          <Input value={shareUrl} isReadOnly fontSize="sm" pr="2.5rem" />
          <InputRightElement>
            <Tooltip label={copied ? 'Copied!' : 'Copy link'}>
              <IconButton
                aria-label="Copy link"
                icon={copied ? <FiCheck /> : <FiCopy />}
                size="sm"
                variant="ghost"
                color={copied ? 'green.300' : undefined}
                onClick={handleCopy}
              />
            </Tooltip>
          </InputRightElement>
        </InputGroup>
      </FormControl>

      <HStack spacing={4} align="end">
        <FormControl>
          <FormLabel fontSize="sm">Share type</FormLabel>
          <Select
            value={share.shareType}
            isDisabled={typeSubmit.submitting}
            onChange={event =>
              typeSubmit.run(
                () =>
                  onUpdate({ shareType: event.target.value as ShareType }),
                { successMessage: 'Share updated.' }
              )
            }
          >
            <option value={ShareType.PUBLIC_LINK}>Public link</option>
            <option value={ShareType.USER}>Specific users</option>
          </Select>
        </FormControl>

        <FormControl>
          <FormLabel fontSize="sm">Default permission</FormLabel>
          <Select
            value={share.permission}
            isDisabled={permissionSubmit.submitting}
            onChange={event =>
              permissionSubmit.run(
                () =>
                  onUpdate({ permission: event.target.value as Permission }),
                { successMessage: 'Share updated.' }
              )
            }
          >
            <option value={Permission.READ_ONLY}>Read only</option>
            <option value={Permission.EDIT}>Can edit</option>
          </Select>
        </FormControl>
      </HStack>

      {share.shareType === ShareType.USER ? (
        <ShareUsersPanel shareId={share.id} />
      ) : null}

      <Divider borderColor="gray.700" />

      <Box>
        <Button
          colorScheme="red"
          variant="outline"
          size="sm"
          onClick={deleteDialog.onOpen}
        >
          Delete share
        </Button>
      </Box>

      <ConfirmDialog
        isOpen={deleteDialog.isOpen}
        onClose={deleteDialog.onClose}
        onConfirm={handleDelete}
        isLoading={deleteSubmit.submitting}
        title="Delete share"
        body="The share link will stop working and any user access will be removed. Continue?"
        confirmLabel="Delete share"
      />
    </VStack>
  );
};
