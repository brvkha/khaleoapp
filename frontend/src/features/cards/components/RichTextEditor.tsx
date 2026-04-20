import { useEditor, EditorContent } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import { useEffect } from 'react'

type RichTextEditorProps = {
  value: string
  onChange: (value: string) => void
  placeholder?: string
}

export function RichTextEditor({ value, onChange, placeholder }: RichTextEditorProps) {
  const editor = useEditor({
    extensions: [StarterKit],
    content: value,
    onUpdate: ({ editor: activeEditor }) => onChange(activeEditor.getHTML()),
    editorProps: {
      attributes: {
        class:
          'min-h-24 w-full rounded border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-200',
      },
    },
  })

  useEffect(() => {
    if (!editor) {
      return
    }
    if (editor.getHTML() !== value) {
      editor.commands.setContent(value, false)
    }
  }, [editor, value])

  return (
    <div>
      {placeholder ? <p className="mb-1 text-xs text-slate-500">{placeholder}</p> : null}
      <EditorContent editor={editor} />
    </div>
  )
}


