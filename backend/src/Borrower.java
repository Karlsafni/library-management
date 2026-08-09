public class Borrower {
    private String name;
    private String bookTitle;
    private String bookAuthor;

    public Borrower(String name, String bookTitle, String bookAuthor) {
        this.name = name;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
    }

    public String getName() {
        return name;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }
}
