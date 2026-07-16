# Microservice Demo 2 — Hệ thống thương mại điện tử Microservices

## Giới thiệu

Hệ thống thương mại điện tử được xây dựng theo kiến trúc microservices với 17 services độc lập, triển khai qua Docker Compose. Dự án phục vụ mục đích học tập, nghiên cứu và áp dụng các pattern của microservices như Service Discovery, API Gateway, Feign Client, Event-Driven (RabbitMQ), Database per Service.

## Công nghệ sử dụng

| Nhóm | Công nghệ | Phiên bản |
|------|-----------|-----------|
| Ngôn ngữ | Java | 17 |
| Framework | Spring Boot | 3.5.14 |
| Framework | Spring Cloud | 2025.0.0 |
| Service Discovery | Netflix Eureka Server | — |
| API Gateway | Spring Cloud Gateway | — |
| Giao tiếp đồng bộ | Spring Cloud OpenFeign | — |
| Message Broker | RabbitMQ | 3.13.7 |
| CSDL quan hệ | MariaDB | 10.11 |
| CSDL NoSQL | MongoDB | 7 |
| Cache / Token Blacklist | Redis | 7-alpine |
| Full-text Search | Elasticsearch | 8.12.2 |
| PDF Generation | Flying Saucer PDF (OpenPDF) | 9.4.0 |
| Build Tool | Maven | 3.9.9 |
| Container | Docker / Docker Compose | — |
| Cổng thanh toán | VNPay Sandbox | — |
| Mail Server | Gmail SMTP | — |

## Kiến trúc tổng thể

```
┌─────────────────────────────────────────────────────┐
│                    CLIENT (React)                    │
│                 http://localhost:3001                │
└───────────────────────┬─────────────────────────────┘
                        │ HTTP
                        ▼
┌──────────────────────────────────────────────────────┐
│                 API GATEWAY (:8080)                   │
│  ┌──────────────────────────────────────────────────┐│
│  │  JwtAuthGatewayFilter (Global Filter)            ││
│  │  - Public paths: /api/auth/**, /api/products/**  ││
│  │  - Yêu cầu Bearer token với các path còn lại     ││
│  │  - Inject X-User-Id, X-User-Role headers         ││
│  │  21 routes → lb://service-name                   ││
│  └──────────────────────────────────────────────────┘│
└───────────────────────┬──────────────────────────────┘
                        │
          ┌─────────────┼──────────────────┐
          ▼             ▼                  ▼
   ┌──────────┐  ┌──────────┐      ┌──────────────┐
   │  Eureka  │  │  Feign   │      │   RabbitMQ   │
   │  :8761   │  │ Clients  │      │   (Events)   │
   └──────────┘  └──────────┘      └──────┬───────┘
                                           │
                                     ┌─────┴─────┐
                                     ▼           ▼
                                ┌────────┐ ┌──────────┐
                                │notification│ │user-svc  │
                                │-service  │ │ (consumer)│
                                └─────────┘ └──────────┘

┌──────────────── Services (MongoDB) ──────────────────┐
│                                                        │
│  product-service  user-service     order-service       │
│  :8081            :8082            :8083               │
│  product_db       user_db          order_db            │
│                                                        │
└────────────────────────────────────────────────────────┘

┌──────────────── Services (MariaDB) ───────────────────┐
│                                                        │
│  Auth-service   Cart-service   Inventory-service      │
│  :8086          :8087          :8089                   │
│  auth_db        cart_db        inventory_db            │
│                                                        │
│  Payment-service  discount-service  review-service     │
│  :8090            :8095             :8096              │
│  payment_db       discount_db       review_db          │
│                                                        │
│  category-service  post-service  Customer-service     │
│  :8084             :8085         :8091                 │
│  category_db       post_db       customer_db           │
│                                                        │
│  Address-service                                       │
│  :8092                                                  │
│  address_db                                            │
│                                                        │
└────────────────────────────────────────────────────────┘

┌──────────────── Services hỗ trợ ─────────────────────┐
│                                                        │
│  Search-service      notification-service              │
│  :8088 (ES)          :8093 (no DB, consumer)           │
│                                                        │
└────────────────────────────────────────────────────────┘

┌────────────────── Infrastructure ─────────────────────┐
│  MariaDB :3306   MongoDB :27017   Redis :6379         │
│  Elasticsearch :9200   RabbitMQ :5672   Adminer :8082  │
└────────────────────────────────────────────────────────┘
```

## Danh sách services

### Infrastructure

| Service | Port | Chức năng | Ghi chú |
|---------|:----:|-----------|---------|
| **eureka-server** | 8761 | Service Discovery — tất cả service đăng ký tại đây | Dashboard tại / |
| **api-gateway** | 8080 | API Gateway — entry point duy nhất, JWT global filter, CORS | 21 routes |

### Business (MongoDB)

| Service | Port | Database | Chức năng chính |
|---------|:----:|----------|-----------------|
| **product-service** | 8081 | `product_db` | CRUD sản phẩm, banner, upload hình, lọc danh mục |
| **user-service** | 8082 | `user_db` | CRUD user, phân quyền, consumer RabbitMQ events |
| **order-service** | 8083 | `order_db` | Tạo đơn hàng, snapshot user+product, reserve stock, discount, xuất PDF |

### Business (MariaDB)

| Service | Port | Database | Chức năng chính |
|---------|:----:|----------|-----------------|
| **Auth-service** | 8086 | `auth_db` + Redis | Đăng ký, đăng nhập, JWT access+refresh token, logout blacklist, quên mật khẩu |
| **Cart-service** | 8087 | `cart_db` | Giỏ hàng, thêm/sửa/xoá, kiểm tra tồn kho (Feign) |
| **Inventory-service** | 8089 | `inventory_db` | Quản lý tồn kho, warehouse, reserve/confirm/release/cancel |
| **Payment-service** | 8090 | `payment_db` | Thanh toán VNPay (sandbox), IPN handler |
| **discount-service** | 8095 | `discount_db` | CRUD mã giảm giá, validate, mark used, release |
| **review-service** | 8096 | `review_db` | Đánh giá sản phẩm, upload hình, admin reply, kiểm tra đã mua |
| **category-service** | 8084 | `category_db` | CRUD danh mục sản phẩm, publish CategoryChangedEvent |
| **post-service** | 8085 | `post_db` | CRUD bài viết/blog, upload hình |
| **Customer-service** | 8091 | `customer_db` | Quản lý nhóm khách hàng |
| **Address-service** | 8092 | `address_db` | CRUD địa chỉ giao hàng, default address flag |

### Hỗ trợ

| Service | Port | Database | Chức năng chính |
|---------|:----:|----------|-----------------|
| **Search-service** | 8088 | Elasticsearch | Index product + post, full-text search |
| **notification-service** | 8093 | Không | Consumer RabbitMQ, gửi email SMTP |

## Tài khoản mẫu

| Vai trò | Tên đăng nhập | Mật khẩu |
|---------|---------------|----------|
| **ADMIN** (Quản trị) | `admin@gmail.com` | `123456` |
| **CUSTOMER** (Khách hàng) | `customer1@example.com` | `123` |

> Tài khoản ADMIN có toàn quyền: quản lý sản phẩm, danh mục, đơn hàng, người dùng, mã giảm giá, bài viết.
> Tài khoản CUSTOMER có thể: xem sản phẩm, thêm giỏ hàng, đặt hàng, thanh toán, đánh giá.

## Hướng dẫn cài đặt

### Yêu cầu hệ thống

- **Docker Engine** >= 24.x
- **Docker Compose** >= 2.x
- **Java JDK** 17 (nếu chạy local, không cần nếu dùng Docker)

### Cài đặt nhanh với Docker (khuyến nghị)

```bash
# 1. Clone repository
git clone <repo-url>
cd microservice-demo2

# 2. Cấu hình biến môi trường
cp .env.example .env
# Sửa .env nếu cần: MARIADB_ROOT_PASSWORD, MAIL_USERNAME, MAIL_PASSWORD

# 3. Build và chạy toàn bộ hệ thống (23 containers)
docker compose up -d

# 4. Đợi ~2-3 phút cho tất cả services khởi động xong
# Kiểm tra trạng thái
docker compose ps

# Xem log một service cụ thể
docker compose logs -f Cart-service
```

### Cài đặt Local (không Docker)

**Bước 1:** Cài đặt hạ tầng
- MariaDB 10.11, MongoDB 7, Redis 7, Elasticsearch 8.12.2, RabbitMQ 3.13

**Bước 2:** Tạo database
```bash
mysql -u root -p < create_databases_xampp.sql
```

**Bước 3:** Build và chạy từng service
```bash
# Build toàn bộ
mvn clean package -DskipTests

# Chạy Eureka trước, sau đó Gateway, rồi các service còn lại
java -jar eureka-server/target/*.jar
java -jar api-gateway/target/*.jar
java -jar Auth-service/target/*.jar
# ... tương tự cho các service khác

# Hoặc dùng script start-all (Windows)
.\start-all.ps1
```

### Truy cập hệ thống

| Component | URL | Ghi chú |
|-----------|-----|---------|
| **Frontend** | http://localhost:3001 | Giao diện người dùng |
| **API Gateway** | http://localhost:8080 | Backend API |
| **Eureka Dashboard** | http://localhost:8761 | Service Discovery |
| **RabbitMQ Management** | http://localhost:15672 | Message Broker (guest/guest) |
| **Adminer (DB Tool)** | http://localhost:8082 | Quản lý MariaDB |

## API Endpoints

### Auth-service

| Method | Endpoint | Chức năng | Auth |
|--------|----------|-----------|:----:|
| POST | `/api/auth/register` | Đăng ký tài khoản | ❌ |
| POST | `/api/auth/login` | Đăng nhập, trả JWT | ❌ |
| POST | `/api/auth/refresh` | Refresh token | ❌ |
| POST | `/api/auth/logout` | Đăng xuất (blacklist token) | ✅ |
| POST | `/api/auth/change-password` | Đổi mật khẩu | ✅ |
| POST | `/api/auth/forgot-password` | Quên mật khẩu | ❌ |
| POST | `/api/auth/reset-password` | Reset mật khẩu | ❌ |
| GET | `/api/auth/validate` | Validate token | ❌ |

### Product-service

| Method | Endpoint | Chức năng | Auth |
|--------|----------|-----------|:----:|
| GET | `/api/products` | Danh sách sản phẩm (phân trang, lọc) | ❌ |
| GET | `/api/products/{id}` | Chi tiết sản phẩm | ❌ |
| POST | `/api/products` | Tạo sản phẩm | ADMIN/MANAGER |
| PUT | `/api/products/{id}` | Cập nhật sản phẩm | ADMIN/MANAGER |
| DELETE | `/api/products/{id}` | Xoá sản phẩm | ADMIN/MANAGER |
| GET | `/api/banners` | Danh sách banner | ❌ |

### Cart-service

| Method | Endpoint | Chức năng | Auth |
|--------|----------|-----------|:----:|
| GET | `/api/cart/{userId}` | Xem giỏ hàng | ✅ |
| POST | `/api/cart/{userId}` | Thêm sản phẩm + kiểm tra tồn kho | ✅ |
| PUT | `/api/cart/items/{itemId}` | Cập nhật số lượng | ✅ |
| DELETE | `/api/cart/items/{itemId}` | Xoá sản phẩm khỏi giỏ | ✅ |
| DELETE | `/api/cart/{userId}` | Xoá toàn bộ giỏ hàng | ✅ |

### Order-service

| Method | Endpoint | Chức năng | Auth |
|--------|----------|-----------|:----:|
| POST | `/api/orders` | Tạo đơn hàng | ✅ |
| GET | `/api/orders` | Danh sách tất cả đơn | ADMIN/MANAGER |
| GET | `/api/orders/{id}` | Chi tiết đơn hàng | ✅ |
| PUT | `/api/orders/{id}/cancel` | Hủy đơn hàng | ✅ |
| PUT | `/api/orders/{id}/status` | Cập nhật trạng thái | ADMIN/MANAGER |
| GET | `/api/orders/{id}/invoice` | Xuất hoá đơn PDF | ✅ |
| GET | `/api/orders/user/{userId}/has-purchased/{productId}` | Kiểm tra đã mua | ✅ |

### Discount-service

| Method | Endpoint | Chức năng | Auth |
|--------|----------|-----------|:----:|
| GET | `/api/discounts` | Danh sách mã | ADMIN/MANAGER |
| POST | `/api/discounts` | Tạo mã | ADMIN |
| POST | `/api/discounts/validate` | Validate mã cho user | ✅ |
| POST | `/api/discounts/{id}/assign` | Gán mã cho user | ADMIN |

### Các service khác

| Service | Endpoint prefix | Phương thức | Ghi chú |
|---------|----------------|-------------|---------|
| category-service | `/api/categories` | CRUD | GET public, POST/PUT/DELETE cần ADMIN/MANAGER |
| post-service | `/api/posts` | CRUD | GET public, POST/PUT/DELETE cần ADMIN/MANAGER |
| review-service | `/api/reviews` | CRUD | GET public, POST/PUT/DELETE cần login |
| Inventory-service | `/api/inventory`, `/api/warehouses` | CRUD + reserve/confirm | ✅ Cần token |
| Payment-service | `/api/payments` | CRUD + VNPay | ✅ Cần token |
| Search-service | `/api/search` | Tìm kiếm | ❌ Public |
| Address-service | `/api/addresses` | CRUD | ✅ Cần token |
| Customer-service | `/api/customers` | CRUD | ADMIN/MANAGER |
| user-service | `/api/users` | CRUD + role | ADMIN |

## Giao tiếp giữa các service

### Đồng bộ (OpenFeign)

| Service gọi | Service đích | Dữ liệu | Nghiệp vụ |
|-------------|-------------|---------|-----------|
| Cart-service | Inventory-service | Tồn kho | Kiểm tra khi thêm/sửa giỏ hàng |
| order-service | product-service | Thông tin sản phẩm | Snapshot khi tạo đơn |
| order-service | user-service | Thông tin người dùng | Snapshot khi tạo đơn |
| order-service | Inventory-service | Reserve/confirm/release stock | Quản lý tồn kho |
| order-service | discount-service | Validate, mark used, release | Áp dụng mã giảm giá |
| order-service | Payment-service | Thông tin thanh toán | Kiểm tra trạng thái thanh toán |
| product-service | category-service | Tên danh mục | Hiển thị danh mục sản phẩm |
| review-service | order-service | Kiểm tra đã mua | Chỉ cho review khi đã mua hàng |
| Auth-service | user-service | Tạo user | Đăng ký tài khoản |
| Auth-service | notification-service | Gửi email | Quên mật khẩu |
| Payment-service | order-service | Thông tin đơn | Xử lý thanh toán |
| Search-service | product-service + post-service | Danh sách sản phẩm/bài viết | Index lên Elasticsearch |

### Bất đồng bộ (RabbitMQ)

| Event | Producer | Exchange | Routing key | Consumer | Kết quả |
|-------|----------|----------|-------------|----------|---------|
| `OrderCreatedEvent` | order-service | `order.exchange` | `order.created` | notification-service | Gửi email xác nhận đơn hàng |
| `OrderStatusEvent` | order-service | `order.exchange` | `order.status` | notification-service | Gửi email cập nhật trạng thái |
| `CategoryChangedEvent` | category-service | — | — | product-service | Cập nhật category reference |
| `PostCreatedEvent` | post-service | — | — | user-service | Log thông báo |
| `PaymentCompletedEvent` | Payment-service | — | — | Các listener | Xử lý payment completed |

## Cơ sở dữ liệu

### MariaDB (10 databases)

| Database | Service | Bảng chính |
|----------|---------|------------|
| `auth_db` | Auth-service | `auth_users` (id, username, password_hash, email, role, enabled) |
| `cart_db` | Cart-service | `cart_items` (id, user_id, product_id, quantity, unit_price) |
| `inventory_db` | Inventory-service | `inventory`, `warehouses`, `inventory_warehouse` |
| `payment_db` | Payment-service | `payments` (order_id, amount, status, transaction_no) |
| `discount_db` | discount-service | `discounts`, `user_discounts` |
| `review_db` | review-service | `reviews`, `review_stats` |
| `category_db` | category-service | `categories` |
| `post_db` | post-service | `posts`, `post_categories` |
| `customer_db` | Customer-service | `customers`, `customer_groups`, `customer_group_members` |
| `address_db` | Address-service | `addresses` |

### MongoDB (3 databases)

| Database | Service | Collection chính |
|----------|---------|-----------------|
| `product_db` | product-service | `products`, `banners` |
| `user_db` | user-service | `users` |
| `order_db` | order-service | `orders` (embedded items + snapshots) |

### Elasticsearch

| Index | Service | Nội dung |
|-------|---------|----------|
| `products` | Search-service | Sản phẩm (id, name, description, categoryName, price) |
| `posts` | Search-service | Bài viết (id, title, content, tags, author) |

## Biến môi trường

| Biến | Mặc định | Mô tả |
|------|----------|-------|
| `MARIADB_ROOT_PASSWORD` | `root123` | Mật khẩu root MariaDB |
| `MAIL_USERNAME` | — | Email Gmail gửi thông báo |
| `MAIL_PASSWORD` | — | App password Gmail |
| `VNP_TMN_CODE` | `M9XLW4DH` | Mã VNPay sandbox |
| `VNP_HASH_SECRET` | `8TM087422A4KW07CVGE2V0K0Z0Z0XO5O` | Secret VNPay sandbox |
| `JWT_SECRET` | `MySecretKeyForJWTTokenGeneration2026MicroserviceDemo2VeryLong` | Secret ký JWT token |

## Xử lý lỗi thường gặp

### Docker không khởi động được service
```bash
# Kiểm tra log
docker compose logs <service-name> --tail=50

# Rebuild service
docker compose build <service-name>
docker compose up -d <service-name>
```

### Eureka không hiển thị service
- Kiểm tra service đã start chưa: `docker compose ps`
- Kiểm tra log: `docker compose logs <service-name> | grep Eureka`
- Đợi 30-60s cho service đăng ký

### MariaDB connection refused
- Kiểm tra MariaDB đã chạy: `docker compose ps mariadb`
- Kiểm tra log: `docker compose logs mariadb --tail=20`
- Kiểm tra `application-docker.properties` có đúng hostname không

### 401 Unauthorized khi gọi API
- Kiểm tra token còn hạn không (JWT hết hạn sau 1h)
- Gọi lại `POST /api/auth/login` để lấy token mới
- Kiểm tra `Authorization: Bearer <token>` trong header

## Docker Compose

Toàn bộ 23 containers được định nghĩa trong `compose.yaml`. Các lệnh hữu ích:

```bash
# Build và chạy
docker compose up -d

# Xem trạng thái
docker compose ps

# Log một service
docker compose logs -f <service-name>

# Rebuild một service
docker compose build <service-name>
docker compose up -d <service-name>

# Dừng tất cả
docker compose down

# Dừng và xoá volumes (⚠️ mất dữ liệu)
docker compose down -v
```

## Cấu trúc project

```
microservice-demo2/
├── compose.yaml                    # Docker Compose
├── docker-init.sql                 # Seed database
├── .env.example                    # Biến môi trường mẫu
├── pom.xml                         # Parent POM (Spring Boot 3.5.14)
├── start-all.ps1 / start-all.cmd   # Script chạy local
├── create_databases_xampp.sql      # Seed data local
│
├── eureka-server/                  # Service Discovery
├── api-gateway/                    # API Gateway
│
├── Auth-service/                   # Xác thực
├── Cart-service/                   # Giỏ hàng
├── order-service/                  # Đơn hàng + PDF
├── product-service/                # Sản phẩm
├── category-service/               # Danh mục
├── Inventory-service/              # Tồn kho
├── Payment-service/                # Thanh toán
├── discount-service/               # Mã giảm giá
├── review-service/                 # Đánh giá
├── user-service/                   # Người dùng
├── Customer-service/               # Khách hàng
├── Address-service/                # Địa chỉ
├── post-service/                   # Bài viết
├── Search-service/                 # Tìm kiếm
├── notification-service/           # Email
│
└── customer-frontend/              # Frontend React
```

## Tính năng chính

- ✅ Đăng ký, đăng nhập JWT + phân quyền (ADMIN/MANAGER/CUSTOMER)
- ✅ Token blacklist với Redis
- ✅ Quên mật khẩu, đổi mật khẩu
- ✅ Quản lý sản phẩm, danh mục, banner (CRUD + upload hình)
- ✅ Tìm kiếm sản phẩm toàn văn (Elasticsearch)
- ✅ Giỏ hàng + kiểm tra tồn kho thời gian thực (Feign)
- ✅ Đặt hàng + snapshot user/product + áp dụng mã giảm giá
- ✅ Reserve/confirm/release tồn kho
- ✅ Thanh toán VNPay (sandbox)
- ✅ Xuất hoá đơn PDF (Thymeleaf + Flying Saucer)
- ✅ Gửi email xác nhận đơn hàng (RabbitMQ → notification-service → SMTP)
- ✅ Đánh giá sản phẩm kèm hình ảnh
- ✅ Quản lý bài viết/blog
- ✅ Quản lý địa chỉ giao hàng
- ✅ Service Discovery (Eureka)
- ✅ API Gateway + JWT Global Filter
- ✅ Docker Compose (23 containers)

## Đóng góp

1. Fork repository
2. Tạo branch mới: `git checkout -b feature/ten-chuc-nang`
3. Commit: `git commit -m "feat: thêm chức năng ..."`
4. Push: `git push origin feature/ten-chuc-nang`
5. Tạo Pull Request

## Giấy phép

MIT License — 2026 Lê Xuân Tài
