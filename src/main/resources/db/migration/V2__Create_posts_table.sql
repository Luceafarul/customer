CREATE TABLE "posts" (
    "id" BIGSERIAL PRIMARY KEY,
    "content" VARCHAR NOT NULL,
    "created_at" TIMESTAMP NOT NULL,
    "user_id" INTEGER REFERENCES customers
);