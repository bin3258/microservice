# Báo Cáo Thực Hành: Tích Hợp Eureka Server Và API Gateway Cho Hệ Thống Microservice

## 1. Tên bài thực hành
Tích hợp Eureka Server, API Gateway và mở rộng các luồng nghiệp vụ cho hệ thống microservice bán hàng - nội dung.

## 2. Mục tiêu bài thực hành
- Xây dựng `Eureka Server` để quản lý đăng ký và phát hiện service.
- Đăng ký toàn bộ service vào Eureka để giao tiếp qua tên service thay vì địa chỉ IP/port cố định.
- Xây dựng `API Gateway` làm điểm vào duy nhất cho hệ thống.
- Kiểm tra lại các chức năng đã có ở các lab trước thông qua Gateway.
- Mở rộng thêm các luồng:
  - Quản lý chi tiết đơn hàng.
  - Quản lý danh mục sản phẩm.
  - Quản lý danh mục bài viết.
  - Quản lý user và phân quyền theo vai trò.
- Tích hợp lại `OpenFeign` và `RabbitMQ` để các service giao tiếp đồng bộ và bất đồng bộ.

## 3. Kiến trúc hệ thống
- `eureka-server`: cổng đăng ký service.
- `api-gateway`: cổng truy cập thống nhất.
- `order-service`: tạo và xem chi tiết đơn hàng.
- `product-service`: quản lý sản phẩm, tra cứu theo danh mục.
- `category-service`: quản lý danh mục sản phẩm.
- `post-service`: quản lý bài viết.
- `user-service`: quản lý user và vai trò.

## 4. Các bước thực hiện

### 4.1 Xây dựng Eureka Server
1. Tạo project Spring Boot cho `eureka-server`.
2. Thêm dependency `spring-cloud-starter-netflix-eureka-server`.
3. Cấu hình port `8761`.
4. Thêm annotation `@EnableEurekaServer`.
5. Chạy ứng dụng.
6. Truy cập `http://localhost:8761` để kiểm tra Dashboard.

**Ảnh minh họa**
- [ ] Ảnh 1: Cấu hình `pom.xml` của `eureka-server`
- [ ] Ảnh 2: Code `@EnableEurekaServer`
- [ ] Ảnh 3: Eureka Dashboard sau khi chạy

### 4.2 Đăng ký các service vào Eureka
1. Thêm dependency Eureka Client vào từng service.
2. Cấu hình `spring.application.name` cho từng service.
3. Cấu hình `eureka.client.service-url.defaultZone`.
4. Chạy từng service.
5. Kiểm tra các service đã xuất hiện trên Eureka Dashboard.

**Ảnh minh họa**
- [ ] Ảnh 4: `order-service` đăng ký thành công
- [ ] Ảnh 5: `product-service` đăng ký thành công
- [ ] Ảnh 6: `user-service` đăng ký thành công
- [ ] Ảnh 7: `category-service` đăng ký thành công
- [ ] Ảnh 8: `post-service` đăng ký thành công

### 4.3 Xây dựng API Gateway
1. Tạo project `api-gateway`.
2. Thêm dependency `spring-cloud-starter-gateway`.
3. Cấu hình port `8080`.
4. Định nghĩa route đến từng service qua `lb://service-name`.
5. Chạy Gateway và kiểm tra truy cập API qua một cổng duy nhất.

**Ảnh minh họa**
- [ ] Ảnh 9: Cấu hình route của Gateway
- [ ] Ảnh 10: Gateway chạy thành công
- [ ] Ảnh 11: Gọi API `orders` qua Gateway
- [ ] Ảnh 12: Gọi API `products` qua Gateway

### 4.4 Kiểm tra các chức năng của các lab trước
1. Tạo đơn hàng qua `order-service`.
2. Xem chi tiết đơn hàng.
3. Tạo và tra cứu sản phẩm.
4. Tạo và tra cứu user.
5. Kiểm tra RabbitMQ nhận event sau khi tạo đơn hàng.
6. Kiểm tra Feign gọi giữa các service qua Eureka.

**Ảnh minh họa**
- [ ] Ảnh 13: Tạo đơn hàng thành công
- [ ] Ảnh 14: Chi tiết đơn hàng trả về đầy đủ user và product
- [ ] Ảnh 15: Danh sách category
- [ ] Ảnh 16: Danh sách post
- [ ] Ảnh 17: Log RabbitMQ khi tạo order/post/category

## 5. Kết luận
Sau khi áp dụng Eureka Server và API Gateway, toàn bộ hệ thống có thể:
- Đăng ký và khám phá service tự động.
- Truy cập thống nhất qua một Gateway duy nhất.
- Kết hợp đồng bộ bằng OpenFeign và bất đồng bộ bằng RabbitMQ.
- Mở rộng thêm các luồng quản lý nghiệp vụ mà không làm rối kiến trúc hiện tại.
