Dưới góc độ Kiến trúc sư trưởng lập kế hoạch (Master Implementation Plan - Chế độ Cao), đây là bản đánh giá toàn diện và kế hoạch nâng cấp chi tiết cho dự án **Applicant Tracking System (ATS) pj7** (Spring Boot 3.5 / Java 21 / MySQL 8.0 / React 19 / Vite 7 / TailwindCSS) — hệ thống quản lý tuyển dụng cấp doanh nghiệp đòi hỏi tính toàn vẹn dữ liệu, kiểm soát phân quyền chặt chẽ, chống gian lận phỏng vấn và bảo mật giao dịch tuyển dụng — được tích hợp tinh hoa từ bộ 12 Agent/Skill chuẩn mực từ PJ1.

---

## 1. Đánh giá hiện trạng & Mục tiêu nâng cấp ATS pj7

- **Đặc thù cốt lõi:** ATS quản lý toàn bộ vòng đời ứng viên từ lúc nộp hồ sơ, sàng lọc sơ bộ, đánh giá phỏng vấn nhiều vòng, phê duyệt thư mời nhận việc (Offer letter) cho đến khi chính thức gia nhập (Onboarding/Hired).
- **Thách thức kiến trúc:**
  1. *Anchoring Bias & Data Leakage trong Phỏng vấn:* Hiện tượng người phỏng vấn sau bị ảnh hưởng điểm số hoặc nhận xét của người phỏng vấn trước (Anchoring Bias). Cần cơ chế Blind Evaluation tuyệt đối trước khi tất cả hội đồng phỏng vấn nộp phiếu điểm.
  2. *Separation of Duties (SoD) & Conflict of Interest:* Rủi ro tự tạo và tự duyệt lương/thư mời nhận việc (Self-Approval), hoặc phê duyệt đồng thời gây sai lệch số lượng tuyển dụng (Headcount Over-hiring).
  3. *Illegal Stage Transitions & Pipeline Corruption:* Các bước nhảy tắt không hợp lệ trong phễu tuyển dụng (như nhảy thẳng từ `APPLIED` lên `OFFER`, hoặc ứng viên bị `REJECTED` lại được chuyển thành `HIRED`).
  4. *Audit Trail & Token Security:* Truy vết kiểm toán mạo danh ("System User"), tấn công giả mạo token chuỗi (Refresh Token Hijacking), và khai thác lỗ hổng tiêm mã qua file CV/tệp đính kèm độc hại (Magic-Byte Bypass / CSV Formula Injection).
- **Mục tiêu:** Áp dụng trọn vẹn 12 Agent/Skill chuẩn mực, chuẩn hóa máy trạng thái phễu tuyển dụng (Pipeline & Stage Machine), thiết lập cơ chế Blind Interview Scoring công bằng, kiên cố hóa quy trình Offer với Separation of Duties và Optimistic Locking (`@Version`), nâng cấp bảo mật toàn diện với Token Reuse Detection và Magic-Byte Validation, đồng thời nâng tầm giao diện React 19 / Vite theo tiêu chuẩn UI/UX Pro Max.

---

## 2. Bảng tổng hợp ứng dụng 12 Skill vào ATS pj7

| STT | Tên Agent / Skill | Vai trò cốt lõi trong ATS pj7 | Mục tiêu áp dụng cụ thể |
|---|---|---|---|
| 01 | **Domain Modeling Agent** | Thiết kế Bounded Context Tuyển dụng & DDD State Machine | Định hình 5 Bounded Contexts: Candidate, Job, Interview, Offer, Audit; thiết lập các Enums trạng thái chặt chẽ (`ApplicationStage`, `OfferStatus`, `InterviewStatus`). |
| 02 | **Security & Hardening Skill** | Separation of Duties, Token Reuse Detection & Magic Bytes | Ngăn người tạo Offer tự duyệt (HTTP 403); thu hồi chuỗi token khi phát hiện tái sử dụng; kiểm tra chữ ký nhị phân file CV (`%PDF-`, `PK\x03\x04`); khử mã độc CSV Injection. |
| 03 | **Concurrency Control Skill** | Khóa Lạc quan (@Version) & Chống Race Condition | Thêm trường `@Version private Long version` trong bảng `Offer` và `Application`, triệt tiêu xung đột phê duyệt đồng thời và bảo đảm không vượt Headcount. |
| 04 | **Performance & Latency Agent** | Tối ưu hóa Truy vấn JPA, N+1 Query & Indexing | Sử dụng `JOIN FETCH` và Entity Graphs trong Spring Data JPA; index các khóa ngoại `application_id`, `job_id`, `candidate_id`; phân trang Server-side phân cấp. |
| 05 | **Node / Backend Engine Skill** | JVM 21 Virtual Threads & Non-blocking File Streaming | Khai thác hiệu năng Java 21 LTS trên Spring Boot 3.5.11; xử lý xuất báo cáo Excel/PDF (OpenPDF, Apache POI) bằng cơ chế stream an toàn bộ nhớ heap. |
| 06 | **UI/UX Pro Max Skill** | Thiết kế Giao diện Tuyển dụng Chuyên nghiệp React 19 | Xây dựng Kanban Board kéo thả mượt mà, phân loại màu sắc stage rõ ràng, thiết kế thẻ ứng viên chuẩn hóa, hỗ trợ phím tắt, thông báo Sonner Toast trực quan. |
| 07 | **Karpathy Verification Skill** | Test Suite Bảo mật & State Machine Mockito | Bộ kiểm thử tự động 23 bài test cô lập bao phủ các trường hợp ranh giới: chuyển stage sai, tự duyệt offer, nộp tiêu chí phỏng vấn ngoài danh mục template. |
| 08 | **Verification & Observability** | Kiểm toán Toàn vẹn (JPA Auditing) & Audit Logging | Tích hợp `AuditorAware<UUID>` định danh chính xác người thực hiện; annotation `@LogAudit` ghi lại chi tiết IP thực (hỗ trợ `X-Forwarded-For`) và che dấu dữ liệu nhạy cảm. |
| 09 | **Resilience & Circuit Breaker** | Graceful Error Handling & Business Exceptions | Chuẩn hóa tầng ngoại lệ `BusinessException` với HTTP Status mã hóa chi tiết (400 Bad Request, 403 Forbidden, 409 Conflict, 404 Not Found). |
| 10 | **Documentation & OpenAPI Sync** | Đặc tả API Tuyển dụng & Ma trận Phân quyền RBAC | Đồng bộ tài liệu Swagger/OpenAPI v3 cho hơn 40 endpoints quản lý ứng viên, phỏng vấn, scorecard và thư mời nhận việc. |
| 11 | **CI/CD & Deployment Automation** | Docker Compose Orchestration & Multi-Stage Builds | Tối ưu hóa Dockerfile đóng gói backend JAR và frontend Nginx; điều phối MySQL 8.0, Backend và Frontend sẵn sàng chạy với 1 câu lệnh. |
| 12 | **Code Quality & Clean Architecture** | Clean Architecture, DTO Mapping & Strict Linting | Tách bạch rõ ranh giới Controller -> Service -> Repository; sử dụng MapStruct 1.6.3 loại bỏ mã ánh xạ thủ công; tuân thủ nghiêm ngặt ESLint và TypeScript 5.9. |

---

## 3. Kế hoạch nâng cấp chi tiết từng bước (Step-by-Step Master Implementation Plan)

### Giai đoạn 1: Kiên cố hóa Pipeline Tuyển dụng & Máy trạng thái Tuyệt đối
*Ứng dụng: Domain Modeling + Concurrency Control + Karpathy Verification*
1. **Quy tắc Chuyển dịch Trạng thái Tuyến tính (Linear State Machine):**
   - Luồng chuẩn: `APPLIED -> SCREENING -> INTERVIEW -> OFFER -> HIRED`.
   - Chặn tuyệt đối bước nhảy cóc: Nếu ứng viên đang ở `APPLIED` mà yêu cầu chuyển sang `OFFER` ➔ Lập tức chặn lại và trả về lỗi `HTTP 409 CONFLICT`.
   - Chặn khôi phục trạng thái phi logic: Nếu hồ sơ đã ở `REJECTED`, cấm chuyển trực tiếp sang `HIRED` mà không qua quy trình tái mở hồ sơ chính thống.
2. **Bắt buộc Lý do Từ chối (Mandatory Rejection Reasons):**
   - Khi từ chối ứng viên ở bất kỳ vòng nào (`REJECTED`), hệ thống bắt buộc trường `rejectionReason` không được để trống (trả về `HTTP 400 BAD_REQUEST` nếu rỗng hoặc chỉ có khoảng trắng).
3. **Rào chắn Rút lui Hồ sơ (Withdrawal Guards):**
   - Ứng viên có thể xin rút hồ sơ (`WITHDRAWN`) khi hồ sơ đang trong trạng thái xử lý; nhưng một khi đã hoàn tất tuyển dụng (`HIRED`), không cho phép chuyển ngược về `WITHDRAWN` mà phải thực hiện thủ tục thôi việc nhân sự.

### Giai đoạn 2: Đánh giá Phỏng vấn Ẩn danh (Fair & Blind Interview Scoring)
*Ứng dụng: Security & Hardening + Domain Modeling + Clean Architecture*
1. **Blind Evaluation (Chống Thiên kiến Neo điểm):**
   - Mỗi người phỏng vấn (Interviewer) chỉ được xem và đánh giá bảng điểm của chính mình.
   - Điểm số và nhận xét của các đồng nghiệp trong cùng hội đồng phỏng vấn hoàn toàn bị ẩn cho đến khi buổi phỏng vấn chính thức hoàn tất.
2. **Điều kiện Hoàn tất Nhiều Người Phỏng vấn (Multi-Interviewer Guard):**
   - Trạng thái buổi phỏng vấn vẫn duy trì ở mức `SCHEDULED` cho đến khi tất cả các thành viên trong hội đồng đã nộp đầy đủ đánh giá cá nhân. Không một cá nhân nào có thể tự ý kết thúc buổi phỏng vấn sớm.
3. **Chống Tiêm Tiêu chí Ngoài Danh mục (Scorecard Template Validation):**
   - Nghiêm cấm nộp điểm cho các `criterionId` không thuộc về `ScorecardTemplate` được gán cho buổi phỏng vấn đó (trả về lỗi `HTTP 400 BAD_REQUEST`).
4. **Phân quyền Sở hữu Điểm (Principal-based IDOR Prevention):**
   - Chỉ người dùng được gán vào danh sách người phỏng vấn mới có quyền gửi điểm (trả về `HTTP 403 FORBIDDEN` cho người ngoài).

### Giai đoạn 3: Tách bạch Trách nhiệm Thư mời Nhận việc (Offer Separation of Duties) & Khóa Lạc quan
*Ứng dụng: Concurrency Control + Security & Hardening + Performance*
1. **Separation of Duties (Ngăn chặn Tự Duyệt):**
   - Vòng đời thư mời: `DRAFT -> PENDING_APPROVAL -> APPROVED -> SENT -> ACCEPTED / DECLINED / EXPIRED`.
   - Người tạo thư mời nhận việc (`createdBy`) bị cấm hoàn toàn hành động phê duyệt hoặc từ chối chính thư mời do mình tạo ra (trả về `HTTP 403 FORBIDDEN`).
2. **Khóa Lạc quan Chống Tranh chấp Phê duyệt (@Version):**
   - Tích hợp `@Version private Long version` trên thực thể `Offer`.
   - Nếu hai người quản lý cùng mở một thư mời và phê duyệt cùng lúc, giao dịch của người thứ hai sẽ nhận `OptimisticLockingFailureException`, ngăn ngừa tình trạng duplicate approval hoặc race condition.
3. **Phân định Tuyển dụng Thực tế (Real Onboarding Transition):**
   - Hành động phê duyệt thư mời (`APPROVED`) chỉ thay đổi trạng thái của Offer, KHÔNG tự ý chuyển ứng viên sang `HIRED`.
   - Chỉ khi ứng viên chính thức xác nhận đồng ý (`ACCEPTED`), hệ thống mới kích hoạt chuyển dịch trạng thái ứng viên sang `HIRED`. Ngược lại nếu ứng viên từ chối (`DECLINED`), ứng viên được chuyển sang `REJECTED` kèm lý do.

### Giai đoạn 4: Bảo mật Tệp đính kèm, Chuỗi Token & Kiểm toán
*Ứng dụng: Security & Hardening + Verification & Observability + UI/UX Pro Max*
1. **Kiểm tra Chữ ký Nhị phân (Magic-Byte File Validation):**
   - Kiểm tra trực tiếp các byte đầu tiên của file CV tải lên (`%PDF-` cho PDF, `PK\x03\x04` cho DOCX/ZIP, JPEG, PNG, WEBP), không tin tưởng đuôi file hoặc MIME type do client gửi lên.
2. **Token Reuse Detection (Chống Đánh cắp Phiên):**
   - Lưu trữ dấu vết chuỗi refresh token; nếu phát hiện một token đã thu hồi được gửi lại, hệ thống lập tức thu hồi toàn bộ chuỗi token của tài khoản đó.
3. **Khử Mã độc CSV Formula Injection:**
   - Tự động thêm tiền tố nháy đơn `'` cho bất kỳ trường dữ liệu nào bắt đầu bằng `=`, `+`, `-`, `@`, `\t`, `\r` trước khi xuất file CSV hoặc Excel.

---

## 4. Tiêu chí nghiệm thu (Acceptance Criteria)

- **Độ bao phủ Kiểm thử:** 100% các kịch bản kiểm thử bảo mật cốt lõi (23 tests: Pipeline State Machine, Separation of Duties, Blind Evaluation, Token Reuse, Magic-Byte) chạy thành công với **0 Failure, 0 Error**.
- **Tính toàn vẹn Dữ liệu:** Không có bất kỳ trường hợp nào xảy ra việc nhảy cóc giai đoạn tuyển dụng hoặc tự phê duyệt thư mời nhận việc.
- **Biên dịch & Build:** Cả backend (Maven Java 21) và frontend (React 19 / Vite 7) đều biên dịch thành công 100% với **0 Error**.
- **Hiệu năng & Trải nghiệm:** Tải trang danh sách ứng viên dưới **200ms**, hỗ trợ đầy đủ các thao tác Kanban kéo thả và giao diện thích ứng chuẩn WCAG AA.

