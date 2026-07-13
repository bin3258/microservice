-- Tạo database và dữ liệu mẫu cho XAMPP / MariaDB
CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS user_db;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS category_db;
CREATE DATABASE IF NOT EXISTS post_db;
CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS cart_db;
CREATE DATABASE IF NOT EXISTS inventory_db;
CREATE DATABASE IF NOT EXISTS payment_db;
CREATE DATABASE IF NOT EXISTS customer_db;
CREATE DATABASE IF NOT EXISTS address_db;

USE product_db;
CREATE TABLE IF NOT EXISTS products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  img VARCHAR(255) DEFAULT NULL,
  category_id BIGINT NOT NULL
);

INSERT INTO products (id, name, price, img, category_id) VALUES
  (1, 'Apple iPhone', 699.00, 'https://example.com/images/iphone.jpg', 1),
  (2, 'Samsung Galaxy', 599.00, 'https://example.com/images/galaxy.jpg', 1),
  (3, 'Google Pixel', 499.00, 'https://example.com/images/pixel.jpg', 1)
ON DUPLICATE KEY UPDATE
  name=VALUES(name),
  price=VALUES(price),
  img=VALUES(img),
  category_id=VALUES(category_id);

USE category_db;
DROP TABLE IF EXISTS categories;
CREATE TABLE IF NOT EXISTS categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255) DEFAULT NULL
);

INSERT INTO categories (id, name, description) VALUES
  (1, 'Electronics', 'Electronic products'),
  (2, 'Books', 'Books and magazines'),
  (3, 'Lifestyle', 'Lifestyle and accessories')
ON DUPLICATE KEY UPDATE
  name=VALUES(name),
  description=VALUES(description);

USE post_db;
DROP TABLE IF EXISTS posts;
CREATE TABLE IF NOT EXISTS posts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  img VARCHAR(255) DEFAULT NULL,
  category_id BIGINT NOT NULL,
  status VARCHAR(50) NOT NULL
);

INSERT INTO posts (id, title, content, img, category_id, status) VALUES
  (1, 'New product launch', 'We are launching a new electronics product line.', 'https://example.com/images/post-launch.jpg', 1, 'PUBLISHED'),
  (2, 'Reading list', 'Recommended books for this quarter.', 'https://example.com/images/post-books.jpg', 2, 'DRAFT'),
  (3, 'Lifestyle tips', 'Daily habits for a better routine.', 'https://example.com/images/post-lifestyle.jpg', 3, 'PUBLISHED')
ON DUPLICATE KEY UPDATE
  title=VALUES(title),
  content=VALUES(content),
  img=VALUES(img),
  category_id=VALUES(category_id),
  status=VALUES(status);

USE user_db;
DROP TABLE IF EXISTS users;
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  phone VARCHAR(50) NOT NULL,
  role VARCHAR(50) NOT NULL
);

INSERT INTO users (id, name, email, phone, role) VALUES
  (1, 'Nguyen Van A', 'vana@example.com', '0900000001', 'ADMIN'),
  (2, 'Tran Thi B', 'thib@example.com', '0900000002', 'MANAGER'),
  (3, 'Le Van C', 'vanc@example.com', '0900000003', 'CUSTOMER')
ON DUPLICATE KEY UPDATE
  name=VALUES(name),
  email=VALUES(email),
  phone=VALUES(phone),
  role=VALUES(role);

USE order_db;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
CREATE TABLE IF NOT EXISTS orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  user_name VARCHAR(255) DEFAULT NULL,
  user_email VARCHAR(255) DEFAULT NULL,
  user_phone VARCHAR(50) DEFAULT NULL,
  total_quantity INT NOT NULL,
  total_price DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(255) DEFAULT NULL,
  product_img VARCHAR(255) DEFAULT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  line_total DECIMAL(10,2) NOT NULL,
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

INSERT INTO orders (id, user_id, user_name, user_email, user_phone, total_quantity, total_price) VALUES
  (1, 1, 'Nguyen Van A', 'vana@example.com', '0900000001', 3, 1897.00),
  (2, 2, 'Tran Thi B', 'thib@example.com', '0900000002', 3, 1797.00)
ON DUPLICATE KEY UPDATE
  user_id=VALUES(user_id),
  user_name=VALUES(user_name),
  user_email=VALUES(user_email),
  user_phone=VALUES(user_phone),
  total_quantity=VALUES(total_quantity),
  total_price=VALUES(total_price);

INSERT INTO order_items (id, order_id, product_id, product_name, product_img, quantity, unit_price, line_total) VALUES
  (1, 1, 1, 'Apple iPhone', 'https://example.com/images/iphone.jpg', 2, 699.00, 1398.00),
  (2, 1, 3, 'Google Pixel', 'https://example.com/images/pixel.jpg', 1, 499.00, 499.00),
  (3, 2, 2, 'Samsung Galaxy', 'https://example.com/images/galaxy.jpg', 1, 599.00, 599.00),
  (4, 2, 3, 'Google Pixel', 'https://example.com/images/pixel.jpg', 2, 499.00, 998.00)
ON DUPLICATE KEY UPDATE
  order_id=VALUES(order_id),
  product_id=VALUES(product_id),
  product_name=VALUES(product_name),
  product_img=VALUES(product_img),
  quantity=VALUES(quantity),
  unit_price=VALUES(unit_price),
  line_total=VALUES(line_total);

-- ===================================================================
-- Auth DB
-- ===================================================================
USE auth_db;
CREATE TABLE IF NOT EXISTS auth_users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
  enabled TINYINT(1) NOT NULL DEFAULT 1
);

INSERT INTO auth_users (username, password_hash, email, role) VALUES
  ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@example.com', 'ADMIN'),
  ('customer1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'customer1@example.com', 'CUSTOMER')
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- ===================================================================
-- Cart DB
-- ===================================================================
USE cart_db;
CREATE TABLE IF NOT EXISTS cart_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(255) DEFAULT NULL,
  product_img VARCHAR(255) DEFAULT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(10,2) DEFAULT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================================
-- Inventory DB
-- ===================================================================
USE inventory_db;
CREATE TABLE IF NOT EXISTS inventory (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL UNIQUE,
  quantity INT NOT NULL DEFAULT 0,
  reserved_quantity INT NOT NULL DEFAULT 0,
  version INT DEFAULT 0
);

INSERT INTO inventory (product_id, quantity, reserved_quantity) VALUES
  (1, 100, 0),
  (2, 50, 0),
  (3, 75, 0)
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity);

-- ===================================================================
-- Customer DB
-- ===================================================================
USE customer_db;
CREATE TABLE IF NOT EXISTS customers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  auth_user_id BIGINT NOT NULL UNIQUE,
  full_name VARCHAR(100) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================================
-- Address DB
-- ===================================================================
USE address_db;
CREATE TABLE IF NOT EXISTS addresses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  street VARCHAR(200) NOT NULL,
  ward VARCHAR(100) DEFAULT NULL,
  city VARCHAR(100) NOT NULL,
  is_default TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================================
-- Payment DB
-- ===================================================================
USE payment_db;
CREATE TABLE IF NOT EXISTS payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL UNIQUE,
  amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  transaction_id VARCHAR(255) DEFAULT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
