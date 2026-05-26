insert into customers (id, customer_code, customer_name, mobile, phone, center, national_code, password_hash, salt, role, enabled) values
('11111111-1111-1111-1111-111111111111', '1001', 'فروشگاه نمونه زرمارکت', '09120000001', '02144000001', 'تهران غرب', '0012345678', '{plain}123456', 'dev-seed', 'CUSTOMER', true),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'admin', 'مدیر سیستم', '09120000002', '02144000002', 'دفتر مرکزی', '0098765432', '{plain}admin123', 'dev-seed', 'ADMIN', true);

insert into product_categories (product_category_code, product_category_name, product_category_image, product_category_image_unselect) values
(10, 'مواد غذایی', 'https://placehold.co/300x300/png?text=Food', 'https://placehold.co/300x300/png?text=Food'),
(20, 'نوشیدنی', 'https://placehold.co/300x300/png?text=Drink', 'https://placehold.co/300x300/png?text=Drink'),
(30, 'بهداشت و شوینده', 'https://placehold.co/300x300/png?text=Health', 'https://placehold.co/300x300/png?text=Health'),
(40, 'تنقلات', 'https://placehold.co/300x300/png?text=Snack', 'https://placehold.co/300x300/png?text=Snack');

insert into products (id, product_name, product_code, full_name_kala1, unit1, unitid1, convert_factor1, full_name_kala2, unit2, convert_factor2, unitid2, product_group_code, price, is_discounts, is_tax, product_image) values
('00000000-0000-0000-0000-000000000101', 'برنج ایرانی ۱۰ کیلویی', 100101, 'کیسه', 'کیسه', 'BAG', 1, 'عدد', 'عدد', 1, 'PCS', 10, 8500000, true, true, 'https://placehold.co/600x400/png?text=Rice'),
('00000000-0000-0000-0000-000000000102', 'روغن آفتابگردان ۱.۸ لیتری', 100102, 'کارتن', 'کارتن', 'BOX', 1, 'بطری', 'بطری', 6, 'BTL', 10, 920000, true, true, 'https://placehold.co/600x400/png?text=Oil'),
('00000000-0000-0000-0000-000000000103', 'رب گوجه فرنگی ۸۰۰ گرمی', 100103, 'کارتن', 'کارتن', 'BOX', 1, 'قوطی', 'قوطی', 12, 'CAN', 10, 420000, true, true, 'https://placehold.co/600x400/png?text=Tomato'),
('00000000-0000-0000-0000-000000000201', 'آب معدنی ۱.۵ لیتری', 200101, 'بسته', 'بسته', 'PACK', 1, 'بطری', 'بطری', 6, 'BTL', 20, 95000, false, true, 'https://placehold.co/600x400/png?text=Water'),
('00000000-0000-0000-0000-000000000202', 'نوشابه خانواده', 200102, 'بسته', 'بسته', 'PACK', 1, 'بطری', 'بطری', 6, 'BTL', 20, 210000, true, true, 'https://placehold.co/600x400/png?text=Soda'),
('00000000-0000-0000-0000-000000000301', 'مایع ظرفشویی ۳ لیتری', 300101, 'کارتن', 'کارتن', 'BOX', 1, 'بطری', 'بطری', 4, 'BTL', 30, 690000, true, true, 'https://placehold.co/600x400/png?text=Detergent'),
('00000000-0000-0000-0000-000000000401', 'چیپس نمکی خانواده', 400101, 'کارتن', 'کارتن', 'BOX', 1, 'بسته', 'بسته', 20, 'PKG', 40, 180000, true, true, 'https://placehold.co/600x400/png?text=Chips');

insert into discounts (id, product_id, discount_percent, from_number, end_number) values
('00000000-0000-0000-0000-000000001001', '00000000-0000-0000-0000-000000000101', 3, 2, 4),
('00000000-0000-0000-0000-000000001002', '00000000-0000-0000-0000-000000000101', 7, 5, 999999),
('00000000-0000-0000-0000-000000001003', '00000000-0000-0000-0000-000000000102', 5, 3, 999999),
('00000000-0000-0000-0000-000000001004', '00000000-0000-0000-0000-000000000202', 4, 5, 999999),
('00000000-0000-0000-0000-000000001005', '00000000-0000-0000-0000-000000000301', 6, 2, 999999),
('00000000-0000-0000-0000-000000001006', '00000000-0000-0000-0000-000000000401', 8, 4, 999999);

insert into banners (id, banner_image, banner_name) values
('00000000-0000-0000-0000-000000002001', 'https://placehold.co/1200x500/png?text=Shop+Market+Spring+Sale', 'فروش ویژه بهاره'),
('00000000-0000-0000-0000-000000002002', 'https://placehold.co/1200x500/png?text=Fast+Delivery', 'ارسال سریع'),
('00000000-0000-0000-0000-000000002003', 'https://placehold.co/1200x500/png?text=Wholesale+Discount', 'تخفیف خرید عمده');

insert into customer_addresses (id, customer_id, center_name, customer_address, customer_mobile, customer_phone) values
('00000000-0000-0000-0000-000000003001', '11111111-1111-1111-1111-111111111111', 'انبار مرکزی', 'تهران، خیابان آزادی، پلاک ۱۰۰، فروشگاه نمونه', '09120000001', '02144000001'),
('00000000-0000-0000-0000-000000003002', '11111111-1111-1111-1111-111111111111', 'شعبه دوم', 'تهران، صادقیه، بلوار فردوس، پلاک ۲۰', '09120000001', '02144000002');

insert into payment_terms (id, name, dead_line, immediate_discount_percent, receipt_discount_percent, cheque_discount_percent, active) values
('16ccab60-279b-410a-90d1-b2673d5d1dd1', 'عرفی', 30, 5, 2, 0, true),
('00000000-0000-0000-0000-000000004001', 'نقدی', 0, 5, 2, 0, true),
('00000000-0000-0000-0000-000000004002', 'چک', 60, 5, 2, 0, true);
