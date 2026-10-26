import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// ブラウザから操作できる簡単なTodoアプリ
public class App {
    static List<Todo> todos = new ArrayList<>();
    static int nextId = 1;

    public static void main(String[] args) throws Exception {
        todos.add(new Todo(nextId++, "牛乳"));
        Todo egg = new Todo(nextId++, "卵");
        egg.setDone(true);
        todos.add(egg);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String message;

            if (path.equals("/add") && method.equals("POST")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String value = body.substring(5);
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8);
                todos.add(new Todo(nextId, title));
                nextId++;
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/done") && method.equals("GET")) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=") && query.length() > 3) {
                    try {
                        int id = Integer.parseInt(query.substring(3));
                        for (Todo todo : todos) {
                            if (todo.getId() == id) {
                                todo.setDone(!todo.isDone());
                                break;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // 数字ではないIDの場合は何もしません。
                    }
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/delete") && method.equals("GET")) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=") && query.length() > 3) {
                    try {
                        int id = Integer.parseInt(query.substring(3));
                        todos.removeIf(todo -> todo.getId() == id);
                    } catch (NumberFormatException e) {
                        // 数字ではないIDの場合は何もしません。
                    }
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/")) {
                String html = "<!doctype html><html lang='ja'><head><meta charset='UTF-8'>"
                        + "<title>買い物リスト</title><style>body{max-width:640px;margin:2rem auto;padding:0 1rem;"
                        + "font-size:16px}</style></head><body><h1>買い物リスト</h1>"
                        + "<form method='post' action='/add'><input name='todo'><button>追加</button></form>";
                if (todos.isEmpty()) {
                    html += "<p>今日は買い物リストがありません</p>";
                } else {
                    html += "<ul>";
                    for (Todo todo : todos) {
                        String title = todo.getTitle();
                        if (todo.isDone()) {
                            title = "<s>" + title + "</s>";
                        }
                        // ☆追加：完了状態に合わせてリンク名を切り替えます。
                        String doneLinkText = todo.isDone() ? "未完了に戻す" : "完了";
                        html += "<li>" + todo.getId() + ". " + title + " <a href='/done?id=" + todo.getId()
                                + "'>" + doneLinkText + "</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>";
                    }
                    html += "</ul>";
                }
                html += "</body></html>";
                message = html;
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            } else {
                message = "ページが見つかりません";
            }

            if (!path.equals("/")) {
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            }
            byte[] responseBody = message.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBody.length);
            exchange.getResponseBody().write(responseBody);
            exchange.getResponseBody().close();
        });
        server.start();
        System.out.println("サーバー起動: http://localhost:8080 (停止するには Ctrl+C)");
    }
}

// TodoのID、タイトル、完了状態をまとめて保持するクラス
class Todo {
    private final int id;
    private final String title;
    private boolean done;

    Todo(int id, String title) {
        this.id = id;
        this.title = title;
        this.done = false;
    }

    int getId() {
        return id;
    }

    String getTitle() {
        return title;
    }

    boolean isDone() {
        return done;
    }

    void setDone(boolean done) {
        this.done = done;
    }
}
