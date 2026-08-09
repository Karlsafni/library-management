import java.util.ArrayList;
import java.util.List;

public class Library {
    private List<Book> books = new ArrayList<>();
    private List<Borrower> borrowers = new ArrayList<>();

    public void addBook(Book book) {
        for (Book b : books) {
            if (b.getTitle().equals(book.getTitle()) && b.getAuthor().equals(book.getAuthor())) {
                b.setCopies(b.getCopies() + book.getCopies());
                return;
            }
        }
        books.add(book);
    }

    public void removeBook(String title, String author) {
        books.removeIf(book -> title.equals(book.getTitle()) && author.equals(book.getAuthor()));
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<Borrower> getBorrowers() {
        return borrowers;
    }

    public void addBorrower(Borrower borrower) {
        borrowers.add(borrower);
    }

    public void removeBorrower(String name, String title, String author) {
        borrowers.removeIf(b -> name.equals(b.getName()) && title.equals(b.getBookTitle()) && author.equals(b.getBookAuthor()));
    }
}