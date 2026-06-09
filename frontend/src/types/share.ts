import type { SnippetLanguage } from './language';

export const ShareType = {
  PUBLIC_LINK: 'PUBLIC_LINK',
  USER: 'USER',
} as const;
export type ShareType = (typeof ShareType)[keyof typeof ShareType];

export const Permission = {
  READ_ONLY: 'READ_ONLY',
  EDIT: 'EDIT',
} as const;
export type Permission = (typeof Permission)[keyof typeof Permission];

const PERMISSION_LABELS: Record<Permission, string> = {
  [Permission.READ_ONLY]: 'Read only',
  [Permission.EDIT]: 'Can edit',
};

/** Human-readable label for a permission, used across share UI. */
export const permissionLabel = (permission: Permission): string =>
  PERMISSION_LABELS[permission];

export interface Share {
  id: string;
  snippetId: string;
  shareToken: string;
  shareType: ShareType;
  permission: Permission;
}

export interface ShareCreateRequest {
  shareType: ShareType;
  permission: Permission;
}

export interface ShareUpdateRequest {
  shareType?: ShareType;
  permission?: Permission;
}

export interface ShareUser {
  id: string;
  userId: string;
  permission: Permission;
}

export interface ShareUserCreateRequest {
  userId: string;
  permission: Permission;
}

/** Response of the public `/shares/by-token/:token` endpoint. */
export interface SharedSnippet {
  snippetId: string;
  title: string;
  content: string;
  language: SnippetLanguage;
  permission: Permission;
}
