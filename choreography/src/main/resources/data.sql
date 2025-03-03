-- Müşteri verilerini ekleme
INSERT INTO customers (id, name, email, phone, balance) VALUES (1, 'Ahmet Yılmaz', 'ahmet.yilmaz@example.com', '5551234567', 1000.00);
INSERT INTO customers (id, name, email, phone, balance) VALUES (2, 'Ayşe Demir', 'ayse.demir@example.com', '5552345678', 1500.00);
INSERT INTO customers (id, name, email, phone, balance) VALUES (3, 'Mehmet Kaya', 'mehmet.kaya@example.com', '5553456789', 2000.00);
INSERT INTO customers (id, name, email, phone, balance) VALUES (4, 'Zeynep Çelik', 'zeynep.celik@example.com', '5554567890', 2500.00);
INSERT INTO customers (id, name, email, phone, balance) VALUES (5, 'Mustafa Şahin', 'mustafa.sahin@example.com', '5555678901', 3000.00);

-- Ürün verilerini ekleme
INSERT INTO products (id, name, description, price, stock_quantity) VALUES (1, 'Laptop', 'Yüksek performanslı dizüstü bilgisayar', 8999.99, 50);
INSERT INTO products (id, name, description, price, stock_quantity) VALUES (2, 'Akıllı Telefon', 'Son model akıllı telefon', 5999.99, 100);
INSERT INTO products (id, name, description, price, stock_quantity) VALUES (3, 'Tablet', 'Taşınabilir tablet bilgisayar', 3999.99, 75);
INSERT INTO products (id, name, description, price, stock_quantity) VALUES (4, 'Kulaklık', 'Kablosuz gürültü önleyici kulaklık', 1499.99, 200);
INSERT INTO products (id, name, description, price, stock_quantity) VALUES (5, 'Akıllı Saat', 'Fitness takipli akıllı saat', 1999.99, 150);

-- Sipariş durumları için örnek veriler
INSERT INTO order_status (id, status) VALUES (1, 'CREATED');
INSERT INTO order_status (id, status) VALUES (2, 'STOCK_RESERVED');
INSERT INTO order_status (id, status) VALUES (3, 'PAYMENT_COMPLETED');
INSERT INTO order_status (id, status) VALUES (4, 'SHIPPED');
INSERT INTO order_status (id, status) VALUES (5, 'DELIVERED');
INSERT INTO order_status (id, status) VALUES (6, 'CANCELLED');
INSERT INTO order_status (id, status) VALUES (7, 'STOCK_FAILED');
INSERT INTO order_status (id, status) VALUES (8, 'PAYMENT_FAILED');

-- Örnek tamamlanmış siparişler
INSERT INTO orders (id, customer_id, order_date, total_amount, status_id) 
VALUES (1, 1, CURRENT_TIMESTAMP(), 8999.99, 5);
INSERT INTO order_items (id, order_id, product_id, quantity, price) 
VALUES (1, 1, 1, 1, 8999.99);

INSERT INTO orders (id, customer_id, order_date, total_amount, status_id) 
VALUES (2, 2, CURRENT_TIMESTAMP(), 7499.98, 4);
INSERT INTO order_items (id, order_id, product_id, quantity, price) 
VALUES (2, 2, 2, 1, 5999.99);
INSERT INTO order_items (id, order_id, product_id, quantity, price) 
VALUES (3, 2, 4, 1, 1499.99);

-- Örnek işlem sürecindeki siparişler
INSERT INTO orders (id, customer_id, order_date, total_amount, status_id) 
VALUES (3, 3, CURRENT_TIMESTAMP(), 3999.99, 2);
INSERT INTO order_items (id, order_id, product_id, quantity, price) 
VALUES (4, 3, 3, 1, 3999.99);

-- Örnek başarısız siparişler
INSERT INTO orders (id, customer_id, order_date, total_amount, status_id) 
VALUES (4, 4, CURRENT_TIMESTAMP(), 19999.95, 7);
INSERT INTO order_items (id, order_id, product_id, quantity, price) 
VALUES (5, 4, 1, 2, 17999.98);
INSERT INTO order_items (id, order_id, product_id, quantity, price) 
VALUES (6, 4, 5, 1, 1999.99);

-- Örnek ödeme kayıtları
INSERT INTO payments (id, order_id, amount, payment_date, status) 
VALUES (1, 1, 8999.99, CURRENT_TIMESTAMP(), 'COMPLETED');
INSERT INTO payments (id, order_id, amount, payment_date, status) 
VALUES (2, 2, 7499.98, CURRENT_TIMESTAMP(), 'COMPLETED');
INSERT INTO payments (id, order_id, amount, payment_date, status) 
VALUES (3, 3, 3999.99, CURRENT_TIMESTAMP(), 'PENDING');
INSERT INTO payments (id, order_id, amount, payment_date, status) 
VALUES (4, 4, 19999.95, CURRENT_TIMESTAMP(), 'FAILED');

-- Stok işlemleri için örnek veriler
INSERT INTO stock_transactions (id, order_id, product_id, quantity, transaction_date, status) 
VALUES (1, 1, 1, 1, CURRENT_TIMESTAMP(), 'RESERVED');
INSERT INTO stock_transactions (id, order_id, product_id, quantity, transaction_date, status) 
VALUES (2, 1, 1, 1, CURRENT_TIMESTAMP(), 'COMMITTED');

INSERT INTO stock_transactions (id, order_id, product_id, quantity, transaction_date, status) 
VALUES (3, 2, 2, 1, CURRENT_TIMESTAMP(), 'RESERVED');
INSERT INTO stock_transactions (id, order_id, product_id, quantity, transaction_date, status) 
VALUES (4, 2, 2, 1, CURRENT_TIMESTAMP(), 'COMMITTED');
INSERT INTO stock_transactions (id, order_id, product_id, quantity, transaction_date, status) 
VALUES (5, 2, 4, 1, CURRENT_TIMESTAMP(), 'RESERVED');
INSERT INTO stock_transactions (id, order_id, product_id, quantity, transaction_date, status) 
VALUES (6, 2, 4, 1, CURRENT_TIMESTAMP(), 'COMMITTED');

INSERT INTO stock_transactions (id, order_id, product_id, quantity, transaction_date, status) 
VALUES (7, 3, 3, 1, CURRENT_TIMESTAMP(), 'RESERVED');

INSERT INTO stock_transactions (id, order_id, product_id, quantity, transaction_date, status) 
VALUES (8, 4, 1, 2, CURRENT_TIMESTAMP(), 'FAILED');
INSERT INTO stock_transactions (id, order_id, product_id, quantity, transaction_date, status) 
VALUES (9, 4, 5, 1, CURRENT_TIMESTAMP(), 'FAILED');

-- Bildirim kayıtları için örnek veriler
INSERT INTO notifications (id, customer_id, order_id, message, notification_date, status) 
VALUES (1, 1, 1, 'Siparişiniz başarıyla tamamlandı ve teslim edildi.', CURRENT_TIMESTAMP(), 'SENT');
INSERT INTO notifications (id, customer_id, order_id, message, notification_date, status) 
VALUES (2, 2, 2, 'Siparişiniz kargoya verildi, yakında teslim edilecek.', CURRENT_TIMESTAMP(), 'SENT');
INSERT INTO notifications (id, customer_id, order_id, message, notification_date, status) 
VALUES (3, 3, 3, 'Siparişiniz için stok rezerve edildi, ödeme bekleniyor.', CURRENT_TIMESTAMP(), 'SENT');
INSERT INTO notifications (id, customer_id, order_id, message, notification_date, status) 
VALUES (4, 4, 4, 'Siparişiniz için yeterli stok bulunamadı, işlem iptal edildi.', CURRENT_TIMESTAMP(), 'SENT'); 