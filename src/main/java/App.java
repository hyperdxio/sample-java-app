import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/hello", exchange -> handleText(exchange, "Hello from /hello!"));
        server.createContext("/user", new UserHandler());
        server.createContext("/order", new OrderHandler());
        server.createContext("/workflow", new WorkflowHandler());
        server.createContext("/full-run", new FullRunHandler());

        ExecutorService executor = Executors.newFixedThreadPool(4);
        server.setExecutor(executor);
        server.start();
        logger.info("Server running on port 8080");

        // Auto-trigger each route after server starts
        Thread.sleep(1000);

        String[] urls = {
            "http://localhost:8080/full-run"
        };

        for (String url : urls) {
            try {
                logger.info("Auto-calling: {}", url);
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                logger.info("Response from {}: {}", url, res.body());
            } catch (Exception e) {
                logger.error("Failed request to {}", url, e);
            }
        }
    }

    private static void handleText(HttpExchange exchange, String body) throws IOException {
        logger.info("Responding with: {}", body);
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, body.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes());
        }
    }

    static class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String userId = exchange.getRequestURI().getQuery();
            logger.info("Fetching user with query: {}", userId);
            try {
                Thread.sleep(150); // Simulate DB lookup
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handleText(exchange, "User details for " + userId);
        }
    }

    static class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String orderId = exchange.getRequestURI().getQuery();
            logger.info("Fetching order with query: {}", orderId);

            // Simulate an outbound HTTP request
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("https://httpbin.org/delay/1"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                logger.info("Got external response: {}", response.statusCode());
            } catch (Exception e) {
                logger.error("Failed external call", e);
            }

            handleText(exchange, "Order details for " + orderId);
        }
    }

    static class WorkflowHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            logger.info("Starting /workflow");

            try {
                HttpRequest userReq = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/user?id=123"))
                        .GET().build();

                HttpRequest orderReq = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/order?id=999"))
                        .GET().build();

                httpClient.send(userReq, HttpResponse.BodyHandlers.ofString());
                httpClient.send(orderReq, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                logger.error("Workflow step failed", e);
            }

            handleText(exchange, "Workflow completed");
        }
    }

    static class FullRunHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            logger.info("Starting full trace test via /full-run");

            String[] urls = {
                "http://localhost:8080/hello",
                "http://localhost:8080/user?id=123",
                "http://localhost:8080/order?id=999",
                "http://localhost:8080/workflow"
            };

            for (String url : urls) {
                try {
                    logger.info("Calling {}", url);
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build();
                    HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                    logger.info("Result from {}: {}", url, res.body());
                } catch (Exception e) {
                    logger.error("Error calling {}", url, e);
                }
            }

            handleText(exchange, "Full trace test complete.");
        }
    }

    
}