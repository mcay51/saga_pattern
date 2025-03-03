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

-- Örnek tamamlanmış siparişler
INSERT INTO orders (id, customer_id, product_id, quantity, total_amount, status, created_at, updated_at) 
VALUES (1, 1, 1, 1, 8999.99, 'COMPLETED', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO orders (id, customer_id, product_id, quantity, total_amount, status, created_at, updated_at) 
VALUES (2, 2, 2, 1, 5999.99, 'COMPLETED', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Örnek işlem sürecindeki siparişler
INSERT INTO orders (id, customer_id, product_id, quantity, total_amount, status, created_at, updated_at) 
VALUES (3, 3, 3, 1, 3999.99, 'STOCK_CHECKED', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Örnek başarısız siparişler
INSERT INTO orders (id, customer_id, product_id, quantity, total_amount, status, created_at, updated_at) 
VALUES (4, 4, 1, 2, 17999.98, 'STOCK_FAILED', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Örnek ödeme kayıtları
INSERT INTO payments (id, order_id, customer_id, amount, status, created_at, updated_at) 
VALUES (1, 1, 1, 8999.99, 'COMPLETED', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO payments (id, order_id, customer_id, amount, status, created_at, updated_at) 
VALUES (2, 2, 2, 5999.99, 'COMPLETED', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO payments (id, order_id, customer_id, amount, status, created_at, updated_at) 
VALUES (3, 3, 3, 3999.99, 'PENDING', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO payments (id, order_id, customer_id, amount, status, created_at, updated_at) 
VALUES (4, 4, 4, 17999.98, 'FAILED', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Bildirim kayıtları için örnek veriler
INSERT INTO notifications (id, customer_id, message, type, created_at, is_read) 
VALUES (1, 1, 'Siparişiniz başarıyla tamamlandı ve teslim edildi.', 'ORDER_COMPLETED', CURRENT_TIMESTAMP(), false);
INSERT INTO notifications (id, customer_id, message, type, created_at, is_read) 
VALUES (2, 2, 'Siparişiniz kargoya verildi, yakında teslim edilecek.', 'ORDER_CONFIRMED', CURRENT_TIMESTAMP(), false);
INSERT INTO notifications (id, customer_id, message, type, created_at, is_read) 
VALUES (3, 3, 'Siparişiniz için stok rezerve edildi, ödeme bekleniyor.', 'STOCK_RESERVED', CURRENT_TIMESTAMP(), false);
INSERT INTO notifications (id, customer_id, message, type, created_at, is_read) 
VALUES (4, 4, 'Siparişiniz için yeterli stok bulunamadı, işlem iptal edildi.', 'STOCK_FAILED', CURRENT_TIMESTAMP(), false); 