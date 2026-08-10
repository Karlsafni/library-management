import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Library {

    public void addBook(Book book) {
        String sql = "INSERT INTO books (title, author, copies) VALUES (?, ?, ?) " +
                     "ON CONFLICT (title, author) DO UPDATE SET copies = books.copies + EXCLUDED.copies";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setInt(3, book.getCopies());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding book: " + e.getMessage());
        }
    }

    public void removeBook(String title, String author) {
        String sql = "DELETE FROM books WHERE title = ? AND author = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing book: " + e.getMessage());
        }
    }

    public List<Book> getBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT title, author, copies FROM books ORDER BY title, author";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(new Book(
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("copies")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving books: " + e.getMessage());
        }
        return books;
    }

    public List<Borrower> getBorrowers() {
        List<Borrower> borrowers = new ArrayList<>();
        String sql = "SELECT name, book_title, book_author, start_date, end_date FROM borrowers ORDER BY name";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                borrowers.add(new Borrower(
                    rs.getString("name"),
                    rs.getString("book_title"),
                    rs.getString("book_author"),
                    rs.getString("start_date"),
                    rs.getString("end_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving borrowers: " + e.getMessage());
        }
        return borrowers;
    }

    public void addBorrower(Borrower borrower) {
        String sql = "INSERT INTO borrowers (name, book_title, book_author, start_date, end_date) VALUES (?, ?, ?, ?, ?) " +
                     "ON CONFLICT (name, book_title, book_author) DO NOTHING";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, borrower.getName());
            pstmt.setString(2, borrower.getBookTitle());
            pstmt.setString(3, borrower.getBookAuthor());
            pstmt.setString(4, borrower.getStartDate());
            pstmt.setString(5, borrower.getEndDate());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding borrower: " + e.getMessage());
        }
    }

    public void removeBorrower(String name, String title, String author) {
        String sql = "DELETE FROM borrowers WHERE name = ? AND book_title = ? AND book_author = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, title);
            pstmt.setString(3, author);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing borrower: " + e.getMessage());
        }
    }

    public void addMember(Member member) {
        String sql = "INSERT INTO members (name, phone, alt_phone, address) VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT (name, phone) DO NOTHING";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getPhone());
            pstmt.setString(3, member.getAltPhone());
            pstmt.setString(4, member.getAddress());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding member: " + e.getMessage());
        }
    }

    public void removeMember(String name, String phone) {
        String sql = "DELETE FROM members WHERE name = ? AND phone = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing member: " + e.getMessage());
        }
    }

    public List<Member> getMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT name, phone, alt_phone, address FROM members ORDER BY name";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                members.add(new Member(
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getString("alt_phone"),
                    rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving members: " + e.getMessage());
        }
        return members;
    }

    // High level transactional methods for borrowing & returning books to ensure consistency

    public String borrowBook(String borrowerName, String title, String author, String startDate, String endDate) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // start transaction

            // 1. Check if book exists and has copies
            String checkBookSql = "SELECT copies FROM books WHERE title = ? AND author = ? FOR UPDATE";
            int copies = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(checkBookSql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, author);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        copies = rs.getInt("copies");
                    }
                }
            }

            if (copies == -1) {
                conn.rollback();
                return "Book not found.";
            }

            if (copies <= 0) {
                conn.rollback();
                return "No copies available to borrow.";
            }

            // 2. Decrement copies
            String updateBookSql = "UPDATE books SET copies = copies - 1 WHERE title = ? AND author = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateBookSql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, author);
                pstmt.executeUpdate();
            }

            // 3. Add borrower record
            String addBorrowerSql = "INSERT INTO borrowers (name, book_title, book_author, start_date, end_date) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(addBorrowerSql)) {
                pstmt.setString(1, borrowerName);
                pstmt.setString(2, title);
                pstmt.setString(3, author);
                pstmt.setString(4, startDate);
                pstmt.setString(5, endDate);
                pstmt.executeUpdate();
            }

            conn.commit();
            return "Book Borrowed Successfully";
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error borrowing book: " + e.getMessage());
            return "Database error: " + e.getMessage();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Connection close failed: " + closeEx.getMessage());
                }
            }
        }
    }

    public void returnBook(String borrowerName, String title, String author) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // start transaction

            // 1. Remove from borrowers
            String deleteBorrowerSql = "DELETE FROM borrowers WHERE name = ? AND book_title = ? AND book_author = ?";
            int rowsDeleted = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(deleteBorrowerSql)) {
                pstmt.setString(1, borrowerName);
                pstmt.setString(2, title);
                pstmt.setString(3, author);
                rowsDeleted = pstmt.executeUpdate();
            }

            if (rowsDeleted == 0) {
                // If there's no matching borrower, rollback and return
                conn.rollback();
                return;
            }

            // 2. Increment book copies (or insert if not exists)
            String upsertBookSql = "INSERT INTO books (title, author, copies) VALUES (?, ?, 1) " +
                                   "ON CONFLICT (title, author) DO UPDATE SET copies = books.copies + 1";
            try (PreparedStatement pstmt = conn.prepareStatement(upsertBookSql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, author);
                pstmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error returning book: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Connection close failed: " + closeEx.getMessage());
                }
            }
        }
    }
}