CREATE DATABASE IF NOT EXISTS surplus_food_marketplace;
USE surplus_food_marketplace;

CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(40) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    phone VARCHAR(30),
    password_hash VARCHAR(255) NOT NULL,
    account_status ENUM('PENDING_VERIFICATION','ACTIVE','BLOCKED','DELETED') NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_refresh_token_user (user_id),
    INDEX idx_refresh_token_expires_at (expires_at)
);

CREATE TABLE businesses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    business_name VARCHAR(160) NOT NULL,
    business_type ENUM('GROCERY_STORE','RESTAURANT','HOTEL','BAKERY','CAFE','SUPERMARKET') NOT NULL,
    license_number VARCHAR(100),
    address_line VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(30) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_business_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE UNIQUE INDEX uk_business_owner ON businesses(owner_id);

CREATE TABLE ngo_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    organization_name VARCHAR(160) NOT NULL,
    registration_number VARCHAR(100),
    address_line VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_ngo_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE food_listings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name VARCHAR(140) NOT NULL,
    description TEXT,
    quantity INT NOT NULL,
    available_quantity INT NOT NULL,
    original_price DECIMAL(10, 2),
    discount_price DECIMAL(10, 2),
    listing_type ENUM('DISCOUNT_SALE','FREE_DONATION') NOT NULL,
    vegetarian BOOLEAN NOT NULL DEFAULT FALSE,
    vegan BOOLEAN NOT NULL DEFAULT FALSE,
    expiry_time DATETIME NOT NULL,
    pickup_start_time DATETIME NOT NULL,
    pickup_end_time DATETIME NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    status ENUM('ACTIVE','SOLD_OUT','EXPIRED','REMOVED') NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_listing_business FOREIGN KEY (business_id) REFERENCES businesses(id),
    CONSTRAINT fk_listing_category FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_listing_geo_status (status, latitude, longitude),
    INDEX idx_listing_expiry (expiry_time)
);

CREATE TABLE food_listing_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    listing_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    cloudinary_public_id VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_listing_image_listing FOREIGN KEY (listing_id) REFERENCES food_listings(id)
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consumer_id BIGINT NOT NULL,
    listing_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING_PAYMENT','PAID','ACCEPTED','READY_FOR_PICKUP','COMPLETED','CANCELLED','FAILED') NOT NULL,
    pickup_code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_consumer FOREIGN KEY (consumer_id) REFERENCES users(id),
    CONSTRAINT fk_order_listing FOREIGN KEY (listing_id) REFERENCES food_listings(id)
);

CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL UNIQUE,
    stripe_payment_intent_id VARCHAR(255) NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'usd',
    status ENUM('REQUIRES_PAYMENT_METHOD','PROCESSING','SUCCEEDED','FAILED','REFUNDED') NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE donations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ngo_id BIGINT NOT NULL,
    listing_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status ENUM('CLAIMED','APPROVED','PICKUP_SCHEDULED','PICKED_UP','CANCELLED') NOT NULL DEFAULT 'CLAIMED',
    confirmation_code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_donation_ngo FOREIGN KEY (ngo_id) REFERENCES ngo_profiles(id),
    CONSTRAINT fk_donation_listing FOREIGN KEY (listing_id) REFERENCES food_listings(id)
);

CREATE TABLE pickup_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT,
    donation_id BIGINT,
    pickup_time DATETIME NOT NULL,
    status ENUM('SCHEDULED','IN_PROGRESS','COMPLETED','MISSED','CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    notes VARCHAR(500),
    CONSTRAINT fk_pickup_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_pickup_donation FOREIGN KEY (donation_id) REFERENCES donations(id),
    CONSTRAINT chk_pickup_owner CHECK ((order_id IS NOT NULL AND donation_id IS NULL) OR (order_id IS NULL AND donation_id IS NOT NULL))
);

CREATE TABLE reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consumer_id BIGINT NOT NULL,
    business_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL UNIQUE,
    rating TINYINT NOT NULL,
    comment VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_consumer FOREIGN KEY (consumer_id) REFERENCES users(id),
    CONSTRAINT fk_review_business FOREIGN KEY (business_id) REFERENCES businesses(id),
    CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE TABLE wishlist (
    user_id BIGINT NOT NULL,
    business_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, business_id),
    CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_wishlist_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type ENUM('NEW_FOOD_NEARBY','DONATION_AVAILABLE','ORDER_ACCEPTED','PICKUP_REMINDER','LISTING_EXPIRED','PAYMENT_UPDATE') NOT NULL,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    order_id BIGINT,
    donation_id BIGINT,
    transaction_type ENUM('SALE','DONATION','REFUND') NOT NULL,
    amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status ENUM('PENDING','SUCCESS','FAILED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_business FOREIGN KEY (business_id) REFERENCES businesses(id),
    CONSTRAINT fk_transaction_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_transaction_donation FOREIGN KEY (donation_id) REFERENCES donations(id)
);

CREATE TABLE complaints (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL,
    business_id BIGINT,
    listing_id BIGINT,
    subject VARCHAR(180) NOT NULL,
    description VARCHAR(1200) NOT NULL,
    status ENUM('OPEN','UNDER_REVIEW','RESOLVED','REJECTED') NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaint_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
    CONSTRAINT fk_complaint_business FOREIGN KEY (business_id) REFERENCES businesses(id),
    CONSTRAINT fk_complaint_listing FOREIGN KEY (listing_id) REFERENCES food_listings(id)
);

INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'Platform administrator'),
('ROLE_BUSINESS_OWNER', 'Business account that lists surplus food'),
('ROLE_CONSUMER', 'Consumer account that buys discounted food'),
('ROLE_NGO', 'NGO or shelter account that claims donations');

INSERT INTO categories (name) VALUES
('Bakery'),
('Prepared Meals'),
('Groceries'),
('Produce'),
('Dairy'),
('Beverages');
