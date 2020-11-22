CREATE TABLE "posts"
(
    "id"          BIGSERIAL PRIMARY KEY,
    "content"     VARCHAR   NOT NULL,
    "created_at"  TIMESTAMP NOT NULL,
    "customer_id" INTEGER REFERENCES customers
);