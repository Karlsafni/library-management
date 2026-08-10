CREATE TABLE IF NOT EXISTS books (
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    copies INT NOT NULL DEFAULT 1,
    PRIMARY KEY (title, author)
);

CREATE TABLE IF NOT EXISTS members (
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    alt_phone VARCHAR(50),
    address TEXT,
    PRIMARY KEY (name, phone)
);

CREATE TABLE IF NOT EXISTS borrowers (
    name VARCHAR(255) NOT NULL,
    book_title VARCHAR(255) NOT NULL,
    book_author VARCHAR(255) NOT NULL,
    start_date VARCHAR(100),
    end_date VARCHAR(100),
    PRIMARY KEY (name, book_title, book_author)
);
