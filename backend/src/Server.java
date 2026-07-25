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

            library.addBook(new Book(title, author));

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
            html.append("<th>Action</th></tr></thead><tbody>");

            for (Book book : library.getBooks()) {
                html.append("<tr class='bookItem'>")
                        .append("<td>").append(escapeHtml(book.getTitle())).append("</td>")
                        .append("<td>").append(escapeHtml(book.getAuthor())).append("</td>");

                html.append("<td><button type='button' class='delete-book-btn' data-title='")
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
