import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection; // ★ SQLite接続に使用します
import java.sql.DriverManager; // ★ JDBC接続に使用します
import java.sql.PreparedStatement; // ★ 更新系SQLに使用します
import java.sql.ResultSet; // ★ 一覧の取得に使用します
import java.sql.SQLException; // ★ SQLエラーに使用します
import java.sql.Statement; // ★ 表の作成に使用します
import java.util.ArrayList;
import java.util.List;

// ブラウザから操作できる簡単なTodoアプリ
public class App {
    static final String DB_URL = "jdbc:sqlite:todos.db"; // ★ 保存先をSQLiteに変更します

    public static void main(String[] args) throws Exception {
        createTable(); // ★ 起動時にtodos表を用意します

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/todos", exchange -> { // JSON一覧用の入口を追加します
            if (!exchange.getRequestMethod().equals("GET")) { // GET以外のメソッドを確認します
                exchange.getResponseHeaders().set("Allow", "GET"); // 許可するメソッドを伝えます
                exchange.sendResponseHeaders(405, -1); // GET以外は許可しません
                exchange.close(); // 応答を終了します
                return; // 一覧の処理へ進みません
            } // メソッドの確認を終えます
            String json = todosToJson(selectTodos()); // DBの全TodoをJSONに変換します
            byte[] responseBody = json.getBytes(StandardCharsets.UTF_8); // JSONをUTF-8のバイト列にします
            exchange.getResponseHeaders().set("Content-Type", "application/json"); // charsetなしでJSONの種類を指定します
            exchange.sendResponseHeaders(200, responseBody.length); // 成功と本文の長さを送ります
            exchange.getResponseBody().write(responseBody); // JSON本文を送ります
            exchange.close(); // 応答を終了します
        }); // JSON一覧用の入口の登録を終えます
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String message;

            if (path.equals("/add") && method.equals("POST")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String value = body.substring(5);
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8);
                insertTodo(title); // ★ 追加したTodoをINSERTします
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/done") && method.equals("GET")) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=") && query.length() > 3) {
                    try {
                        int id = Integer.parseInt(query.substring(3));
                        toggleDone(id); // ★ 完了状態をUPDATEで切り替えます
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
                        deleteTodo(id); // ★ 指定したTodoをDELETEします
                    } catch (NumberFormatException e) {
                        // 数字ではないIDの場合は何もしません。
                    }
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/")) {
                List<Todo> todos = selectTodos(); // ★ 一覧は表示のたびにSELECTします
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

    static void createTable() throws SQLException { // ★ 初回起動でも保存先を作成します
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteに接続します
             Statement statement = connection.createStatement()) { // ★ 表作成用のSQLを実行します
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS todos (id INTEGER PRIMARY KEY, title TEXT, done INTEGER)"); // ★ 指定された表を作成します
        }
    }

    static void insertTodo(String title) throws IOException { // ★ Todo追加用のメソッドです
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteに接続します
             PreparedStatement statement = connection.prepareStatement("INSERT INTO todos (title, done) VALUES (?, 0)")) { // ★ 値を安全に渡します
            statement.setString(1, title); // ★ タイトルを設定します
            statement.executeUpdate(); // ★ 1件追加します
        } catch (SQLException e) { throw new IOException(e); } // ★ HTTP処理にSQLエラーを伝えます
    }

    static void toggleDone(int id) throws IOException { // ★ 完了状態切替用のメソッドです
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteに接続します
             PreparedStatement statement = connection.prepareStatement("UPDATE todos SET done = CASE WHEN done = 0 THEN 1 ELSE 0 END WHERE id = ?")) { // ★ 指定IDだけ更新します
            statement.setInt(1, id); // ★ 対象IDを設定します
            statement.executeUpdate(); // ★ 完了状態を切り替えます
        } catch (SQLException e) { throw new IOException(e); } // ★ HTTP処理にSQLエラーを伝えます
    }

    static void deleteTodo(int id) throws IOException { // ★ Todo削除用のメソッドです
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteに接続します
             PreparedStatement statement = connection.prepareStatement("DELETE FROM todos WHERE id = ?")) { // ★ 指定IDだけ削除します
            statement.setInt(1, id); // ★ 対象IDを設定します
            statement.executeUpdate(); // ★ 1件削除します
        } catch (SQLException e) { throw new IOException(e); } // ★ HTTP処理にSQLエラーを伝えます
    }

    static List<Todo> selectTodos() throws IOException { // ★ Todo一覧取得用のメソッドです
        List<Todo> todos = new ArrayList<>(); // ★ SELECT結果を表示用に集めます
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteに接続します
             PreparedStatement statement = connection.prepareStatement("SELECT id, title, done FROM todos ORDER BY id"); // ★ 全件をID順に取得します
             ResultSet rows = statement.executeQuery()) { // ★ SELECTを実行します
            while (rows.next()) { // ★ 各行をTodoに戻します
                Todo todo = new Todo(rows.getInt("id"), rows.getString("title")); // ★ IDとタイトルを読みます
                todo.setDone(rows.getInt("done") != 0); // ★ 完了状態を読みます
                todos.add(todo); // ★ 一覧に追加します
            }
        } catch (SQLException e) { throw new IOException(e); } // ★ HTTP処理にSQLエラーを伝えます
        return todos; // ★ SELECTした一覧を返します
    }

    static String todosToJson(List<Todo> todos) { // Todo一覧をJSON配列に変換します
        StringBuilder json = new StringBuilder("["); // 配列の開始記号を追加します
        for (int i = 0; i < todos.size(); i++) { // Todoを順番に処理します
            if (i > 0) json.append(','); // 2件目以降の前に区切りを追加します
            Todo todo = todos.get(i); // 現在のTodoを取得します
            json.append("{\"title\":\"").append(escapeJson(todo.getTitle())); // エスケープしたタイトルを追加します
            json.append("\",\"done\":").append(todo.isDone()).append('}'); // 真偽値を追加してオブジェクトを閉じます
        } // Todoの処理を終えます
        return json.append(']').toString(); // 配列を閉じてJSONを返します
    } // JSON配列への変換を終えます

    static String escapeJson(String value) { // JSON文字列内の特殊文字をエスケープします
        StringBuilder escaped = new StringBuilder(); // 変換後の文字列を保持します
        for (int i = 0; i < value.length(); i++) { // タイトルを1文字ずつ調べます
            char c = value.charAt(i); // 現在の文字を取得します
            switch (c) { // 特殊文字ごとに処理を分けます
                case '"': escaped.append("\\\""); break; // 引用符をエスケープします
                case '\\': escaped.append("\\\\"); break; // バックスラッシュをエスケープします
                case '\n': escaped.append("\\n"); break; // 改行をエスケープします
                case '\r': escaped.append("\\r"); break; // 復帰をエスケープします
                case '\t': escaped.append("\\t"); break; // タブをエスケープします
                case '\b': escaped.append("\\b"); break; // バックスペースをエスケープします
                case '\f': escaped.append("\\f"); break; // 改ページをエスケープします
                default: // その他の文字を処理します
                    if (c < 0x20) escaped.append(String.format("\\u%04x", (int) c)); // 残りの制御文字をUnicode表記にします
                    else escaped.append(c); // 通常の文字はそのまま追加します
            } // 文字ごとの処理を終えます
        } // タイトルの確認を終えます
        return escaped.toString(); // エスケープ済みの文字列を返します
    } // JSON文字列のエスケープを終えます
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
