-- ユーザー
CREATE TABLE m_users(
    f_user_id int NOT NULL,
    f_username varchar(100) NOT NULL ,
    f_display_name varchar(100) NOT NULL ,
    f_password_hash varchar(255) not null ,
    f_last_login_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP not NULL,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP not null,
    f_status smallint default 1,
    f_email_verified smallint default 0,
    f_phone_verified smallint default 0,
    f_two_factor_verified smallint default 0,
    f_identity_verified smallint default 0,
    primary key (f_user_id)
);

-- ユーザー情報
CREATE TABLE t_user_info(
    f_user_id int not null ,
    f_last_name varchar(100),
    f_first_name varchar(100),
    f_last_name_kana varchar(100),
    f_first_name_kana varchar(100),
    f_birth_date date,
    f_gender smallint default 0,
    f_phone_number varchar(50),
    f_oreated_at timestamp not null default CURRENT_TIMESTAMP ,
    f_updated_at timestamp not null default CURRENT_TIMESTAMP,
    primary key (f_user_id)
);

-- ユーザー住所
CREATE TABLE t_user_address(
    f_address_id int not null generated always as IDENTITY,
    f_user_id int not null,
    f_postal_code varchar(100) not null,
    f_prefecture varchar(100) not null,
    f_city varchar(100) not null ,
    f_address_line1 varchar(100) not null ,
    f_address_line2 varchar(100) not null ,
    f_country_code varchar(2) not null default 'JP',
    f_is_default smallint not null default 0,
    f_created_at timestamp not null default CURRENT_TIMESTAMP,
    f_updated_at timestamp not null default CURRENT_TIMESTAMP,
    primary key (f_address_id)
);

-- ユーザープロフィール
CREATE TABLE t_user_profile(
    f_user_id INT not null ,
    f_text_profile_text text,
    f_profile_image_url varchar(500),
    f_rating_count int default 0,
    f_rating_sum int default 0,
    f_sales_count int default 0,
    f_purchases_count int default 0,
    f_following_count int default 0,
    f_followers_count int default 0,
    f_created_at timestamp not null default CURRENT_TIMESTAMP,
    f_updated_at timestamp not null default CURRENT_TIMESTAMP,
    PRIMARY KEY(f_user_id)
);

-- ユーザー支払い情報
CREATE TABLE t_user_pay_info(
    f_pay_info_id int not null GENERATED ALWAYS AS IDENTITY,
    f_user_id int not null ,
    f_pay_type smallint not null ,
    f_external_customer_id varchar(100),
    external_payment_method_id varchar(100),
    f_masked_info varchar(20),
    f_pay_name varchar(50),
    f_is_default smallint default 0,
    f_created_at timestamp not null default CURRENT_TIMESTAMP,
    f_updated_at timestamp not null default CURRENT_TIMESTAMP,
    primary key (f_pay_info_id)
);

-- ユーザー名変更履歴
CREATE TABLE username_history(
    f_history_id bigint not null GENERATED ALWAYS AS IDENTITY primary key,
    f_user_id int not null ,
    f_old_username varchar(50) not null ,
    f_new_username varchar(50) not null ,
    f_changed_at timestamp
);

-- 商品
CREATE TABLE t_items (
    f_item_id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1),
    f_item_name char(20) NOT NULL,
    f_quantity int DEFAULT 0,
    f_user_id bigint NOT NULL,
    f_categories_id bigint NOT NULL
);

-- 注文
CREATE TABLE t_orders(
    f_order_id bigint primary key ,
    f_listing_id bigint,
    f_buyer_id bigint,
    f_ordered_at timestamp default CURRENT_TIMESTAMP,
    f_payment_id bigint GENERATED ALWAYS AS IDENTITY,
    f_payment_data timestamp default CURRENT_TIMESTAMP,
    f_amount decimal(10,2),
    f_payment_method varchar(20),
    f_payment_status  varchar(20)
);

-- レビュー
CREATE TABLE t_reviews(
    f_review_id char(10) PRIMARY KEY,
    f_rating integer,
    f_comment text,
    f_posted_at timestamp default current_timestamp,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_user_id bigint not null ,
    f_item_id bigint not null
);

-- 更新日時更新関数
CREATE OR REPLACE FUNCTION update_f_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.f_updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- コメント
CREATE TABLE t_comments
(
    f_comment_id      bigint primary key GENERATED ALWAYS AS IDENTITY,
    f_listing_id      bigint,
    f_user_id         bigint,
    f_comment_content text not null,
    f_create_at       timestamp default current_timestamp,
    f_updated_at      timestamp default current_timestamp,
    f_read_status     smallint
);

CREATE TRIGGER set_timestamp
BEFORE UPDATE ON t_comments
FOR EACH ROW
EXECUTE FUNCTION update_f_updated_at();

-- メッセージ
CREATE TABLE t_messages(
    f_message_id bigint primary key ,
    f_sender_id bigint,
    f_receiver_id bigint,
    f_thread_id bigint,
    f_message_content text,
    f_create_at timestamp default current_timestamp,
    f_updated_at timestamp default current_timestamp,
    f_read_status smallint
);

CREATE TRIGGER set_f_updated_at
BEFORE UPDATE ON t_messages
FOR EACH ROW
EXECUTE FUNCTION update_f_updated_at();

-- 配送
CREATE TABLE t_shipments(
    f_shipments_id bigint GENERATED ALWAYS AS IDENTITY primary key not null,
    f_order_id bigint not null ,
    f_carrier int,
    tracking_no varchar(50) not null ,
    f_address varchar(8) not null ,
    f_delivery_status int not null ,
    f_shipped_at timestamp not null default current_timestamp,
    f_delivered_at timestamp not null default current_timestamp,
    f_updated_at timestamp default current_timestamp
);

-- 商品画像
CREATE TABLE t_items_images(
    f_image_id bigint GENERATED ALWAYS AS IDENTITY primary key,
    f_listing_id bigint,
    f_image_url varchar(200),
    f_display_order int,
    f_created_at timestamp default current_timestamp,
    f_status int
);

-- ユーザーお気に入り
CREATE TABLE t_user_favorites(
    f_favorite_id bigint primary key ,
    f_created_at timestamp not null default current_timestamp,
    f_is_active boolean default true,
    f_user_id bigint not null ,
    f_item_id bigint not null 
);

-- 都道府県マスタ
CREATE TABLE t_prefecture_l(
    f_prefecture_id bigint primary key not null ,
    f_prefecture_jpname varchar(10) not null ,
    f_latitude_longitude varchar(50) not null ,
    f_romaj varchar(30) not null ,
    f_address_id int not null generated always as identity 
);
