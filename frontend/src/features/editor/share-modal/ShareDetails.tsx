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
import { errorToast, successToast } from '../../../components/toast';
import { isAppError } from '../../../lib/normalizeError';
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
  const [copied, setCopied] = useState(false);
  const [savingType, setSavingType] = useState(false);
  const [savingPermission, setSavingPermission] = useState(false);
  const [deleting, setDeleting] = useState(false);

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

  const handleChange = async (
    body: ShareUpdateRequest,
    setSaving: (saving: boolean) => void
  ) => {
    setSaving(true);
    try {
      await onUpdate(body);
      toast(successToast('Share updated.'));
    } catch (error) {
      if (isAppError(error)) {
        toast(errorToast(error));
      }
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await onDelete();
      deleteDialog.onClose();
      toast(successToast('Share deleted.'));
    } catch (error) {
      if (isAppError(error)) {
        toast(errorToast(error));
      }
    } finally {
      setDeleting(false);
    }
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
            isDisabled={savingType}
            onChange={event =>
              handleChange(
                { shareType: event.target.value as ShareType },
                setSavingType
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
            isDisabled={savingPermission}
            onChange={event =>
              handleChange(
                { permission: event.target.value as Permission },
                setSavingPermission
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
        isLoading={deleting}
        title="Delete share"
        body="The share link will stop working and any user access will be removed. Continue?"
        confirmLabel="Delete share"
      />
    </VStack>
  );
};
