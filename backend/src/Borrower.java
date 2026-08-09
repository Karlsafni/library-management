public class Borrower {
    private String name;
    private String bookTitle;
    private String bookAuthor;
    private String startDate;
    private String endDate;

    public Borrower(String name, String bookTitle, String bookAuthor, String startDate, String endDate) {
        this.name = name;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
