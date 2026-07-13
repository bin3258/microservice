# Phan tich monolith va cach tach microservice

## 1. Monolith ban dau

He thong quan ly ban hang don gian trong monolith thuong co 3 phan nghiep vu:

- `Product`: quan ly san pham, gia ban.
- `User`: quan ly thong tin khach hang.
- `Order`: tao don hang, tinh tong tien, lien ket san pham va nguoi mua.

Trong mo hinh monolithic:

- Tat ca module nam chung trong 1 codebase va thuong dung chung 1 database.
- `Order` truy cap truc tiep bang san pham va bang nguoi dung trong cung ung dung.
- Uu diem la de lam luc dau, nhung khi mo rong se gap kho tach trach nhiem nghiep vu va kho deploy doc lap.

## 2. Cach tach sang microservice

Project duoc tach thanh 5 service nghiep vu va 2 thanh phan ha tang:

- `product-service`: quan ly san pham, su dung `product_db`.
- `user-service`: quan ly khach hang, su dung `user_db`.
- `order-service`: quan ly don hang, su dung `order_db`.
- `category-service`: quan ly danh muc, su dung `category_db`.
- `post-service`: quan ly bai viet, su dung `post_db`.
- `api-gateway`: diem vao thong nhat cho client.
- `eureka-server`: dich vu dang ky va phat hien service.

Nguyen tac tach:

- Moi service so huu database rieng.
- `order-service` luu snapshot thong tin user va san pham cho tung don hang.
- Khi tao don hang, `order-service` goi API sang `product-service` va `user-service` qua OpenFeign de xac thuc va lay snapshot.
- Khi xem don hang, `order-service` doc truc tiep du lieu da luu trong `order_db`.

## 3. Luong xu ly tao don hang

1. Client goi `POST /api/orders`.
2. `order-service` dung `UserClient` kiem tra `userId`.
3. `order-service` dung `ProductClient` lay thong tin tung san pham.
4. `order-service` tinh `lineTotal`, `totalQuantity`, `totalPrice`.
5. `order-service` luu don hang va danh sach chi tiet vao `order_db` kem snapshot user/san pham.
6. Response tra ve tu du lieu local, khong phu thuoc vao trang thai moi nhat cua service khac.
7. Sau khi transaction luu don hang thanh cong, `order-service` publish event `order.created` len RabbitMQ de `user-service` xu ly bat dong bo.

## 4. Ket qua trong project hien tai

- Da bo sung `OpenFeign` cho `order-service`.
- Da them `User Service` day du voi API va database.
- Da mo rong `Order Service` de dat nhieu san pham trong 1 don.
- Response don hang hien thi day du thong tin nguoi dung, thong tin san pham, so luong tung san pham, tong so luong va tong tien don hang.
- Da bo sung RabbitMQ de xu ly bat dong bo sau khi tao don hang, hien tai `user-service` nhan event va ghi log thong bao.
