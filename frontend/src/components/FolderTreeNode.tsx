import type { FolderNode } from '../store/folderStore'

type FolderTreeNodeProps = {
  node: FolderNode
  level: number
  expanded: boolean
  hasChildren: boolean
  onToggle: (folderId: string) => void
  onStudy: (folderId: string, folderName: string) => void
  onNewFolder: (folderId: string) => void
  onDelete: (folderId: string, folderName: string) => void
}

export function FolderTreeNode({
  node,
  level,
  expanded,
  hasChildren,
  onToggle,
  onStudy,
  onNewFolder,
  onDelete,
}: FolderTreeNodeProps) {
  return (
    <div
      className="group flex items-center gap-2 rounded px-2 py-1 text-sm hover:bg-slate-100"
      style={{ paddingLeft: `${level * 14 + 8}px` }}
    >
      <button
        aria-label={expanded ? `Collapse ${node.name}` : `Expand ${node.name}`}
        className="h-5 w-5 shrink-0 rounded text-slate-500 hover:bg-slate-200"
        onClick={(event) => {
          event.stopPropagation()
          if (hasChildren) {
            onToggle(node.id)
          }
        }}
        type="button"
      >
        {hasChildren ? (expanded ? 'v' : '>') : ''}
      </button>

      <button
        className="min-w-0 shrink text-left text-slate-800 hover:underline"
        onClick={() => onStudy(node.id, node.name)}
        type="button"
      >
        <span className="truncate">{node.name}</span>
      </button>

      <div className="ml-auto flex items-center gap-1">
        <span className="rounded bg-blue-100 px-2 py-0.5 text-xs text-blue-700">{node.newCards}</span>
        <span className="rounded bg-amber-100 px-2 py-0.5 text-xs text-amber-700">{node.learningCards}</span>
        <span className="rounded bg-emerald-100 px-2 py-0.5 text-xs text-emerald-700">{node.masteredCards}</span>
      </div>

      <div className="flex items-center gap-1 opacity-0 transition group-hover:opacity-100">
        <button
          aria-label={`Create child folder in ${node.name}`}
          className="rounded border border-slate-300 px-1.5 py-0.5 text-xs text-slate-600 hover:bg-slate-50"
          onClick={() => onNewFolder(node.id)}
          type="button"
        >
          +
        </button>
        <button
          aria-label={`Delete ${node.name}`}
          className="rounded border border-rose-300 px-1.5 py-0.5 text-xs text-rose-600 hover:bg-rose-50"
          onClick={() => onDelete(node.id, node.name)}
          type="button"
        >
          x
        </button>
      </div>
    </div>
  )
}

