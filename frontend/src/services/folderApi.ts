import { requestJson } from './apiClient'

export type FolderTreeNodeDto = {
  id: string
  name: string
  totalCards: number
  newCards: number
  learningCards: number
  masteredCards: number
  children: FolderTreeNodeDto[]
}

export async function getFolderTree(): Promise<FolderTreeNodeDto[]> {
  return requestJson<FolderTreeNodeDto[]>('/api/folders/tree')
}

export async function createFolder(payload: {
  name: string
  parentId: string | null
}): Promise<FolderTreeNodeDto> {
  return requestJson<FolderTreeNodeDto>('/api/folders', {
    method: 'POST',
    body: JSON.stringify({
      name: payload.name,
      parent_id: payload.parentId,
    }),
  })
}

export async function deleteFolder(folderId: string): Promise<void> {
  await requestJson(`/api/folders/${folderId}`, {
    method: 'DELETE',
  })
}

