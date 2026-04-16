# Yêu Cầu Tính Năng: Cấu Trúc Cây Thư Mục (Tree View) Cho Ứng Dụng Flashcard

## 1. Tổng Quan (Overview)
Cần tái cấu trúc hệ thống quản lý thư mục flashcard từ dạng phẳng (flat) sang dạng cây lồng nhau (Tree structure) mang phong cách UI của các IDE (như IntelliJ / VS Code). Stack sử dụng: React (Frontend) và Spring Boot + JPA (Backend).

## 2. Quy Tắc Nghiệp Vụ (Business Logic)
- **Giới hạn cấp độ (Depth Limit):** Cây thư mục có độ sâu tối đa là 6 cấp. Từ cấp thứ 6 trở đi, nếu user tạo thêm thư mục con, tự động chuyển thành thư mục ngang hàng (sibling) cùng cấp 6.
- **Tính độc quyền của thẻ (Card Assignment):** Một thẻ từ vựng (Flashcard) chỉ thuộc về duy nhất 1 thư mục (Leaf node hoặc Parent node đều được).
- **Cộng dồn số lượng (Aggregation):** Tổng số lượng thẻ hiển thị ở một thư mục cha = Số thẻ nằm trực tiếp trong nó + Tất cả thẻ nằm trong các thư mục con, cháu của nó. Phân loại rõ: Thẻ Mới, Đang Học, Đã Thuộc.

## 3. Yêu Cầu Frontend (React)
Thiết kế Component `FolderTreeView` với các yêu cầu:
- **UI/UX Style:** Giao diện gọn gàng, line-height hẹp. Hiệu ứng hover nguyên dòng.
- **Tạo thư mục mới (Inline Creation):** Cung cấp icon "New Folder" tại mỗi node (hoặc gốc). Khi click, sẽ hiện một ô nhập text (text box) trực tiếp dưới/bên trong cây để điền tên.
- **Xóa thư mục (Delete Folder):** Cung cấp icon "Thùng rác" hoặc Context Menu. Hỗ trợ xóa thư mục (nghiệp vụ backend sẽ định nghĩa logic xóa con/thẻ tương ứng).
- **Tương tác 1 (Toggle Expand/Collapse):** Phía bên trái cùng có icon mũi tên `>` (đóng) và `v` (mở). Bấm vào **chỉ** làm nhiệm vụ đóng/mở thư mục, không kích hoạt sự kiện khác.
- **Tương tác 2 (Study Mode):** Bấm vào **Tên thư mục** sẽ trigger hàm `onStudy(folderId)`. Chức năng này sẽ gom toàn bộ thẻ của thư mục đó (và các con của nó) để vào chế độ học.
- **Hiển thị số liệu (Right-aligned):** Nằm sát lề phải là các badge số lượng thẻ:
  - Màu xanh dương: Tổng thẻ Mới.
  - Màu cam: Tổng thẻ Đang học.
  - Màu xanh lá: Tổng thẻ Đã thuộc.
  - Khi thư mục cha đóng lại, nó VẪN phải hiển thị tổng số thẻ đã cộng dồn.

## 4. Yêu Cầu Backend (Spring Boot & Database)
- **Database Schema (Adjacency List):**
  - Bảng `Folder`: `id`, `name`, `parent_id` (Khóa ngoại tham chiếu chính nó).
  - Bảng `Flashcard`: `id`, `word`, `status` (NEW, LEARNING, MASTERED), `folder_id`.
- **API Requirements:**
  - Viết API `GET /api/folders/tree`. Trả về cấu trúc JSON đệ quy (nested array).
  - Tối ưu hóa truy vấn: Khuyến khích fetch toàn bộ thư mục của User lên RAM và xử lý build tree + tính tổng Aggregation bằng Java Stream API (vì dữ liệu thư mục của 1 user thường không quá lớn và giới hạn ở 6 cấp), tránh query đệ quy N+1 dưới database.
  - Viết validation khi `POST /api/folders`: Kiểm tra xem thư mục cha truyền vào đã đạt độ sâu cấp 6 chưa. Nếu đã là cấp 6, gán `parent_id` của thư mục mới bằng chính `parent_id` của thư mục cha truyền vào (để làm sibling).

## 5. Dữ liệu JSON Trả Về Mong Đợi (Response format)
```json
[
  {
    "id": 1,
    "name": "IELTS",
    "totalCards": 150,
    "newCards": 50,
    "learningCards": 80,
    "masteredCards": 20,
    "children": [
      {
        "id": 2,
        "name": "Cam 7",
        "totalCards": 80,
        "newCards": 30,
        "learningCards": 40,
        "masteredCards": 10,
        "children": [] 
      }
    ]
  }
]
```
