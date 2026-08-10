import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

public class DatabaseManager {
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found in classpath.");
        }
    }

    public static Connection getConnection() throws SQLException {
        String dbUrl = System.getenv("DATABASE_URL");
        if (dbUrl == null || dbUrl.isEmpty()) {
            // Default connection string if none is provided
            dbUrl = "jdbc:postgresql://localhost:5432/library";
        }

        if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
            try {
                String cleanUrl = dbUrl.substring(dbUrl.indexOf("://") + 3);
                int atIndex = cleanUrl.indexOf("@");
                if (atIndex != -1) {
                    String userInfo = cleanUrl.substring(0, atIndex);
                    String hostAndDb = cleanUrl.substring(atIndex + 1);

                    String[] userParts = userInfo.split(":");
                    String user = userParts[0];
                    String password = userParts.length > 1 ? userParts[1] : "";

                    String jdbcUrl = "jdbc:postgresql://" + hostAndDb;

                    Properties props = new Properties();
                    props.setProperty("user", user);
                    props.setProperty("password", password);
                    
                    // Render internal database hostnames do not contain a dot '.'
                    // e.g. "dpg-d9sun8qfngtc7384dm30-a" has no dot, while external hostnames do
                    String hostname = hostAndDb.split("/")[0];
                    boolean isInternal = !hostname.contains(".");

                    if (!isInternal) {
                        props.setProperty("ssl", "true");
                        props.setProperty("sslmode", "require");
                        props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
                    }

                    return DriverManager.getConnection(jdbcUrl, props);
                }
            } catch (Exception e) {
                System.err.println("Error parsing DATABASE_URL: " + e.getMessage());
            }
            
            // Fallback: If parsing fails, try converting the URL schema prefix
            String convertedUrl = dbUrl.replaceFirst("postgres://", "jdbc:postgresql://")
                                       .replaceFirst("postgresql://", "jdbc:postgresql://");
            return DriverManager.getConnection(convertedUrl);
        }

        return DriverManager.getConnection(dbUrl);
    }

    public static void initializeDatabase() {
        System.out.println("Initializing database schema...");
        try (InputStream is = DatabaseManager.class.getClassLoader().getResourceAsStream("db/schema.sql")) {
            if (is == null) {
                System.err.println("Could not find db/schema.sql in classpath resources");
                return;
            }

            // Read the SQL statements from the resource stream
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            String schemaSql = scanner.hasNext() ? scanner.next() : "";
            
            if (schemaSql.isEmpty()) {
                System.out.println("schema.sql is empty, skipping initialization.");
                return;
            }

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Split SQL by semicolon and execute each statement individually
                String[] queries = schemaSql.split(";");
                for (String query : queries) {
                    String trimmedQuery = query.trim();
                    if (!trimmedQuery.isEmpty()) {
                        stmt.execute(trimmedQuery);
                    }
                }
                System.out.println("Database schema initialized successfully.");
                
            } catch (SQLException e) {
                System.err.println("SQL execution error during database schema initialization: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Failed to read schema.sql: " + e.getMessage());
        }
    }
}
