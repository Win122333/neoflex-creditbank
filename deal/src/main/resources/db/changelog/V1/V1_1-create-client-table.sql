CREATE TABLE client(
    client_id UUID PRIMARY KEY,
    account_number VARCHAR(255),
    birth_date DATE NOT NULL,
    dependent_amount INTEGER,
    email VARCHAR(255) NOT NULL,
    employment_id JSONB,
    first_name VARCHAR(255) NOT NULL,
    gender VARCHAR(255),
    last_name VARCHAR(255) NOT NULL,
    marital_status VARCHAR(255),
    middle_name VARCHAR(255),
    passport_id JSONB
)