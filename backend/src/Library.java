import java.util.ArrayList;
import java.util.List;

public class Library {
   private List<Book> books = new ArrayList<>();

   public void addBook(Book book) {
      books.add(book);
   }

   public void removeBook(String title, String author) {
      books.removeIf(book -> title.equals(book.getTitle()) && author.equals(book.getAuthor()));
   }

   public List<Book> getBooks() {
      return books;
   }
}