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
  /** May be null for USER shares, where per-user permissions apply instead. */
  permission: Permission | null;
}

export interface ShareCreateRequest {
  shareType: ShareType;
  /** Required only for PUBLIC_LINK shares; ignored for USER shares. */
  permission?: Permission;
}

export interface ShareUpdateRequest {
  shareType?: ShareType;
  permission?: Permission;
}

export interface ShareUser {
  id: string;
  userId: string;
  /** Present only for users added through the username flow; may be null otherwise. */
  username: string | null;
  permission: Permission;
}

export interface ShareUserCreateRequest {
  username: string;
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
