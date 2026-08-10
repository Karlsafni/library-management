public class Main {
    public static void main(String[] args) throws Exception {
        DatabaseManager.initializeDatabase();
        Server server = new Server();
        server.start();
    }
}