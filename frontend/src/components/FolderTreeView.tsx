import { type ReactElement, useMemo, useState } from 'react'
import type { FolderNode } from '../store/folderStore'
import { FolderTreeNode } from './FolderTreeNode'

type FolderTreeViewProps = {
  nodes: FolderNode[]
  expanded: Record<string, boolean>
  onToggle: (folderId: string) => void
  onStudy: (folderId: string, folderName: string) => void
  onCreate: (name: string, parentId: string | null) => Promise<void>
  onDelete: (folderId: string, folderName: string) => Promise<void>
}

export function FolderTreeView({
  nodes,
  expanded,
  onToggle,
  onStudy,
  onCreate,
  onDelete,
}: FolderTreeViewProps) {
  const [creatingParentId, setCreatingParentId] = useState<string | null>(null)
  const [creatingRoot, setCreatingRoot] = useState(false)
  const [nameInput, setNameInput] = useState('')

  const rootVisible = useMemo(() => nodes.length > 0, [nodes.length])

  const saveFolder = async () => {
    const name = nameInput.trim()
    if (!name) {
      return
    }
    await onCreate(name, creatingParentId)
    setNameInput('')
    setCreatingParentId(null)
    setCreatingRoot(false)
  }

  const renderNodes = (currentNodes: FolderNode[], level: number): ReactElement[] => {
    const rows: ReactElement[] = []
    for (const node of currentNodes) {
      const isExpanded = expanded[node.id] ?? true
      const hasChildren = node.children.length > 0

      rows.push(
        <div key={node.id}>
          <FolderTreeNode
            expanded={isExpanded}
            hasChildren={hasChildren}
            level={level}
            node={node}
            onDelete={(folderId, folderName) => {
              void onDelete(folderId, folderName)
            }}
            onNewFolder={(folderId) => {
              setCreatingParentId(folderId)
              setCreatingRoot(false)
              setNameInput('')
            }}
            onStudy={onStudy}
            onToggle={onToggle}
          />

          {creatingParentId === node.id ? (
            <div className="flex items-center gap-2 py-1" style={{ paddingLeft: `${(level + 1) * 14 + 30}px` }}>
              <input
                aria-label="New folder name"
                className="w-64 rounded border border-slate-300 px-2 py-1 text-sm"
                onChange={(event) => setNameInput(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    event.preventDefault()
                    void saveFolder()
                  }
                  if (event.key === 'Escape') {
                    setCreatingParentId(null)
                    setCreatingRoot(false)
                    setNameInput('')
                  }
                }}
                placeholder="New folder name"
                value={nameInput}
              />
              <button className="rounded bg-emerald-600 px-2 py-1 text-xs text-white" onClick={() => void saveFolder()} type="button">
                Save
              </button>
              <button
                className="rounded border border-slate-300 px-2 py-1 text-xs"
                onClick={() => {
                  setCreatingParentId(null)
                  setCreatingRoot(false)
                  setNameInput('')
                }}
                type="button"
              >
                Cancel
              </button>
            </div>
          ) : null}

          {hasChildren && isExpanded ? renderNodes(node.children, level + 1) : null}
        </div>,
      )
    }
    return rows
  }

  return (
    <section className="rounded border border-slate-200 bg-white p-2">
      <div className="mb-2 flex items-center justify-between px-2">
        <h2 className="text-sm font-semibold text-slate-700">Folders</h2>
        <button
          className="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
          onClick={() => {
            setCreatingParentId(null)
            setCreatingRoot(true)
            setNameInput('')
          }}
          type="button"
        >
          + New Folder
        </button>
      </div>

      {creatingRoot && creatingParentId === null ? (
        <div className="mb-2 flex items-center gap-2 px-2">
          <input
            aria-label="New root folder name"
            className="w-64 rounded border border-slate-300 px-2 py-1 text-sm"
            onChange={(event) => setNameInput(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                event.preventDefault()
                void saveFolder()
              }
              if (event.key === 'Escape') {
                setCreatingRoot(false)
                setNameInput('')
              }
            }}
            placeholder="New root folder name"
            value={nameInput}
          />
          <button className="rounded bg-emerald-600 px-2 py-1 text-xs text-white" onClick={() => void saveFolder()} type="button">
            Save
          </button>
          <button
            className="rounded border border-slate-300 px-2 py-1 text-xs"
            onClick={() => {
              setCreatingRoot(false)
              setNameInput('')
            }}
            type="button"
          >
            Cancel
          </button>
        </div>
      ) : null}

      {rootVisible ? renderNodes(nodes, 0) : <p className="px-2 py-4 text-sm text-slate-500">No folders yet.</p>}
    </section>
  )
}



