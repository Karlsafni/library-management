import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Server {
    private Library library = new Library();

    public void start() throws Exception {
        String host = System.getenv().getOrDefault("HOST", "localhost");
        String portValue = System.getenv().getOrDefault("PORT", "8080");
        int port = Integer.parseInt(portValue);

        HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);

        server.createContext("/addBook", exchange -> {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, 0);
                exchange.close();
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            String title = extractValue(body, "title");
            String author = extractValue(body, "author");
            String copiesStr = extractValue(body, "copies");
            int copies = 1;
            try {
                if (!copiesStr.isEmpty()) {
                    copies = Integer.parseInt(copiesStr);
                }
            } catch (NumberFormatException e) {
                // default to 1
            }

            library.addBook(new Book(title, author, copies));

            sendResponse(exchange, "Book Added Successfully");
        });

        server.createContext("/books", exchange -> {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, 0);
                exchange.close();
                return;
            }

            StringBuilder html = new StringBuilder();
            html.append("<table class='bookTable'><thead><tr><th>Book Name</th><th>Author Name</th>");
            html.append("<th>No of Copies</th><th>Borrowed By</th><th>Action</th></tr></thead><tbody>");

            for (Book book : library.getBooks()) {
                int borrowedCount = 0;
                for (Borrower b : library.getBorrowers()) {
                    if (b.getBookTitle().equals(book.getTitle()) && b.getBookAuthor().equals(book.getAuthor())) {
                        borrowedCount++;
                    }
                }

                html.append("<tr class='bookItem'>")
                        .append("<td>").append(escapeHtml(book.getTitle())).append("</td>")
                        .append("<td>").append(escapeHtml(book.getAuthor())).append("</td>")
                        .append("<td>").append(book.getCopies()).append("</td>")
                        .append("<td>").append(borrowedCount).append(" ").append(borrowedCount == 1 ? "person" : "persons").append("</td>");

                html.append("<td><button type='button' class='borrow-book-btn' data-title='")
                        .append(escapeHtml(book.getTitle()))
                        .append("' data-author='").append(escapeHtml(book.getAuthor()))
                        .append("'>Borrow</button> ");

                html.append("<button type='button' class='delete-book-btn' data-title='")
                        .append(escapeHtml(book.getTitle()))
                        .append("' data-author='").append(escapeHtml(book.getAuthor()))
                        .append("'>Delete</button></td></tr>");
            }

            html.append("</tbody></table>");

            if (library.getBooks().isEmpty()) {
                html.append("<p>No books available.</p>");
            }

            sendResponse(exchange, html.toString());
        });

        server.createContext("/deleteBook", exchange -> {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, 0);
                exchange.close();
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String title = extractValue(body, "title");
            String author = extractValue(body, "author");

            library.removeBook(title, author);
            sendResponse(exchange, "Book Deleted Successfully");
        });

        server.createContext("/borrowBook", exchange -> {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, 0);
                exchange.close();
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String title = extractValue(body, "title");
            String author = extractValue(body, "author");
            String borrower = extractValue(body, "borrower");
            String startDate = extractValue(body, "startDate");
            String endDate = extractValue(body, "endDate");

            if (borrower.isEmpty()) {
                sendResponse(exchange, "Borrower name is required.");
                return;
            }

            Book targetBook = null;
            for (Book b : library.getBooks()) {
                if (b.getTitle().equals(title) && b.getAuthor().equals(author)) {
                    targetBook = b;
                    break;
                }
            }

            if (targetBook == null) {
                sendResponse(exchange, "Book not found.");
                return;
            }

            if (targetBook.getCopies() <= 0) {
                sendResponse(exchange, "No copies available to borrow.");
                return;
            }

            targetBook.setCopies(targetBook.getCopies() - 1);
            library.addBorrower(new Borrower(borrower, title, author, startDate, endDate));

            sendResponse(exchange, "Book Borrowed Successfully");
        });

        server.createContext("/borrowers", exchange -> {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, 0);
                exchange.close();
                return;
            }

            StringBuilder html = new StringBuilder();
            html.append("<table class='bookTable'><thead><tr><th>Borrower's Name</th><th>Book Name</th>");
            html.append("<th>Author Name</th><th>Start Date</th><th>End Date</th><th>Action</th></tr></thead><tbody>");

            for (Borrower b : library.getBorrowers()) {
                html.append("<tr class='bookItem'>")
                        .append("<td>").append(escapeHtml(b.getName())).append("</td>")
                        .append("<td>").append(escapeHtml(b.getBookTitle())).append("</td>")
                        .append("<td>").append(escapeHtml(b.getBookAuthor())).append("</td>")
                        .append("<td>").append(escapeHtml(b.getStartDate())).append("</td>")
                        .append("<td>").append(escapeHtml(b.getEndDate())).append("</td>");

                html.append("<td><button type='button' class='return-book-btn' data-borrower='")
                        .append(escapeHtml(b.getName()))
                        .append("' data-title='").append(escapeHtml(b.getBookTitle()))
                        .append("' data-author='").append(escapeHtml(b.getBookAuthor()))
                        .append("'>Return</button></td></tr>");
            }

            html.append("</tbody></table>");

            if (library.getBorrowers().isEmpty()) {
                html.append("<p>No borrowers active.</p>");
            }

            sendResponse(exchange, html.toString());
        });

        server.createContext("/returnBook", exchange -> {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, 0);
                exchange.close();
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String title = extractValue(body, "title");
            String author = extractValue(body, "author");
            String borrower = extractValue(body, "borrower");

            library.removeBorrower(borrower, title, author);

            Book targetBook = null;
            for (Book b : library.getBooks()) {
                if (b.getTitle().equals(title) && b.getAuthor().equals(author)) {
                    targetBook = b;
                    break;
                }
            }

            if (targetBook != null) {
                targetBook.setCopies(targetBook.getCopies() + 1);
            } else {
                library.addBook(new Book(title, author, 1));
            }

            sendResponse(exchange, "Book Returned Successfully");
        });

        server.start();
        System.out.println("Server running on http://" + host + ":" + port);
    }

    private void sendResponse(com.sun.net.httpserver.HttpExchange exchange, String text) throws IOException {
        byte[] responseBytes = text.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(responseBytes);
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String extractValue(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex == -1)
            return "";

        int colonIndex = json.indexOf(":", keyIndex);
        int firstQuote = json.indexOf("\"", colonIndex + 1);
        int secondQuote = json.indexOf("\"", firstQuote + 1);

        if (firstQuote == -1 || secondQuote == -1)
            return "";
        return json.substring(firstQuote + 1, secondQuote);
    }
}
