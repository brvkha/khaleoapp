import { create } from 'zustand'
import { createFolder, deleteFolder, getFolderTree, type FolderTreeNodeDto } from '../services/folderApi'

export type FolderNode = FolderTreeNodeDto

type FolderStore = {
  nodes: FolderNode[]
  expanded: Record<string, boolean>
  loading: boolean
  error: string
  loadTree: () => Promise<void>
  toggleExpanded: (folderId: string) => void
  createNode: (name: string, parentId: string | null) => Promise<void>
  deleteNode: (folderId: string) => Promise<void>
  getBreadcrumb: (folderId: string) => FolderNode[]
}

function flatten(nodes: FolderNode[], acc: Map<string, FolderNode>, parentById: Map<string, string | null>, parentId: string | null): void {
  for (const node of nodes) {
    acc.set(node.id, node)
    parentById.set(node.id, parentId)
    flatten(node.children, acc, parentById, node.id)
  }
}

export const useFolderStore = create<FolderStore>((set, get) => ({
  nodes: [],
  expanded: {},
  loading: false,
  error: '',
  loadTree: async () => {
    set({ loading: true, error: '' })
    try {
      const nodes = await getFolderTree()
      set((state) => ({
        nodes,
        loading: false,
        expanded: Object.keys(state.expanded).length === 0 ? Object.fromEntries(nodes.map((node) => [node.id, true])) : state.expanded,
      }))
    } catch (error) {
      set({ loading: false, error: error instanceof Error ? error.message : 'Failed to load folders' })
    }
  },
  toggleExpanded: (folderId) => {
    set((state) => ({
      expanded: {
        ...state.expanded,
        [folderId]: !state.expanded[folderId],
      },
    }))
  },
  createNode: async (name, parentId) => {
    await createFolder({ name, parentId })
    await get().loadTree()
  },
  deleteNode: async (folderId) => {
    await deleteFolder(folderId)
    await get().loadTree()
  },
  getBreadcrumb: (folderId) => {
    const byId = new Map<string, FolderNode>()
    const parentById = new Map<string, string | null>()
    flatten(get().nodes, byId, parentById, null)

    const breadcrumb: FolderNode[] = []
    let cursor: string | null = folderId
    while (cursor) {
      const node = byId.get(cursor)
      if (!node) {
        break
      }
      breadcrumb.push(node)
      cursor = parentById.get(cursor) ?? null
    }
    breadcrumb.reverse()
    return breadcrumb
  },
}))

