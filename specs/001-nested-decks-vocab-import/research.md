# Research: Nested Decks

## Database Schema for Hierarchy
- **Decision**: Add a single `parent_id` column to the `decks` table (Adjacency List model).
- **Rationale**: Simple to implement for shallow hierarchies or when sub-deck queries can be managed effectively with a simple self-join or application-level tree construction. Decks in standard study apps rarely traverse past a few levels. Flyway migration can smoothly add the nullable column and foreign key constraint.
- **Alternatives considered**: Closure Table or Nested Sets. Both add additional maintenance overhead during insertions or deletions and are overkill for typical deck nesting which is infrequently deep.

## Fetching Descendant Items (Study Logic)
- **Decision**: Handle tree structure in application memory for fetching descendant cards.
- **Rationale**: The number of decks typically remains small enough that fetching all child deck IDs for a given root is inexpensive. Recursive CTEs could be used at the DB level, but Java application-side tree traversal avoids DB specific syntax and allows easier manipulation.
- **Alternatives considered**: Generating SQL Recursive CTE natively, which would bind the logic heavily to MySQL specific dialect.

## Backend Deck Structure and DTOs
- **Decision**: Deck entity gains `@ManyToOne` `parent` and `@OneToMany` `children` mappings. `DeckDTO` gets optional `parentId` and potentially an array of `children` `DeckDTO`s.
- **Rationale**: JPA cleanly maps self-referencing tables. Structuring DTOs tightly with `children` easily allows frontend to render tree views without extra network calls.
- **Alternatives considered**: Flat list of decks returned to frontend, requiring frontend to rebuild tree. Given DTO mapping is straightforward, backend doing the tree building is fine as well. Let's decide to return flat from an endpoint and build tree on either side. Decided: DB keeps it flat, API returns list, UI builds the tree from `parentId`. Less recursive overhead in DB serialization. Wait, tree view needs recursive stats? We'll calculate recursive stats in Java service.

## Frontend Tree Views and Stats
- **Decision**: Update Zustand store to map decks by ID, creating a derived tree selector that computes recursive total stats (cards to learn/review) by summing children. Frontend renders a collapsible tree. Breadcrumbs are computed by walking up the `parentId` map.
- **Rationale**: Centralized state management in Zustand makes traversing and calculating aggregate states easy and keeps React views completely declarative.
- **Alternatives considered**: Passing all states natively deeply through props, which would be clumsy.


