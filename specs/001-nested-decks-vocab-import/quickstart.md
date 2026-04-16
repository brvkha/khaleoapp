# Quickstart: Folder Tree View

## Developer Requirements
- Flyway migration scripts
- React + Zustand for rebuilding the frontend store.
- Spring Boot JPA entity modifications.

### Backend Changes setup
1. Run local postgres and generate a new flyway migration script `VX__add_parent_id_to_folders.sql` under `/backend/src/main/resources/db/migration/`.
2. Update Java backend models:
   - Make `parent_id` nullable in `Folder`.
   - Implement `GET /api/folders/tree` fetching flat data and using Java Stream API to build trees and compute aggregations.
3. Test recursive validation: Add a unit test verifying `FolderService` prevents depth > 6 and converts new nodes to siblings.

### Frontend Changes setup
1. Adjust the `Folder` interface within React `types.ts` to reflect the recursive structure.
2. Build the `FolderTreeNode` component implementing the UI/UX line-height, hover effect, expand/collapse toggles, and status badges.
3. Trigger `onStudy(folderId)` when clicking the folder name.
4. Modify breadcrumbs for deep study sessions to display `Parent > Child > Grandchild`.
