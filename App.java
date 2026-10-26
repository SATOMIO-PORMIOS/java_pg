import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// 簡易なTodoアプリをHTTPサーバーとして動かします。
public class App {
    // Todo一覧と、次に割り当てるIDを管理します。
    static List<Todo> todos = new ArrayList<>();
    static int nextId = 1; // 次に新しいTodoへ割り当てるIDです。

    // プログラムの開始位置です。
    public static void main(String[] args) throws Exception {
        // 初期表示用のサンプルTodoを登録します。
        todos.add(new Todo(nextId++, "牛乳を買う"));
        Todo egg = new Todo(nextId++, "卵を買う");
        egg.setDone(true);
        todos.add(egg);

        // 8080番ポートでHTTPリクエストを待ち受けます。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // すべてのパスへのリクエストをこの処理で受け取ります。
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String message;

            // フォーム送信ならTodoを追加し、一覧ページへ移動します。
            if (path.equals("/add") && method.equals("POST")) {
                // 送信されたフォーム本文をUTF-8で読み込みます。
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                // フォーム本文の「todo=」を除き、URLデコードします。
                String value = body.substring(5);
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8);
                // 入力されたタイトルでTodoを作成し、一覧に追加します。
                todos.add(new Todo(nextId, title));
                nextId++;
                // 追加後にトップページへ戻すリダイレクトを返します。
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
                // ★追加: /done にGETで来たとき、指定IDのTodoを完了にします。
            } else if (path.equals("/done") && method.equals("GET")) {
                // ★追加: URLの ?id=... 部分を取得します。
                String query = exchange.getRequestURI().getQuery();
                // ★追加: idが正しい形式で含まれている場合だけ処理します。
                if (query != null && query.startsWith("id=") && query.length() > 3) {
                    try {
                        // ★追加: idの文字列を整数に変換します。
                        int id = Integer.parseInt(query.substring(3));
                        // ★追加: IDが一致するTodoを完了にして、検索を終えます。
                        for (Todo todo : todos) {
                            if (todo.getId() == id) {
                                todo.setDone(true);
                                break;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // ★追加: 数字でないidは無視し、一覧へ戻します。
                    }
                }
                // ★追加: 処理後は一覧へリダイレクトします。
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
                // ★追加: /delete にGETで来たとき、指定IDのTodoを削除します。
            } else if (path.equals("/delete") && method.equals("GET")) {
                // ★追加: URLの ?id=... 部分を取得します。
                String query = exchange.getRequestURI().getQuery();
                // ★追加: idが正しい形式で含まれている場合だけ処理します。
                if (query != null && query.startsWith("id=") && query.length() > 3) {
                    try {
                        // ★追加: idの文字列を整数に変換します。
                        int id = Integer.parseInt(query.substring(3));
                        // ★追加: URLのIDと一致するTodoだけを一覧から取り除きます。
                        todos.removeIf(todo -> todo.getId() == id);
                    } catch (NumberFormatException e) {
                        // ★追加: 数字でないidは無視し、一覧へ戻します。
                    }
                }
                // ★追加: 処理後は一覧へリダイレクトします。
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
                // ルートパスではフォームとTodo一覧を表示します。
            } else if (path.equals("/")) {
                // Todo入力フォームと一覧を含むHTMLを作成します。
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>";
                // Todoを1件ずつHTMLの一覧項目にします。
                for (Todo todo : todos) {
                    String title = todo.getTitle();
                    if (todo.isDone()) {
                        title = "<s>" + title + "</s>";
                    }
                    // ★追加: TodoのIDを使った完了リンクと削除リンクを行に表示します。
                    html += "<li>" + todo.getId() + ". " + title + " <a href='/done?id=" + todo.getId()
                            + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>";
                }
                html += "</ul>";
                message = html;
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            } else {
                message = "ページが見つかりません";
            }

            // ルート以外の応答をプレーンテキストとして返します。
            if (!path.equals("/")) {
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            }
            // 応答をUTF-8のバイト列に変換して送信します。
            byte[] responseBody = message.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBody.length);
            exchange.getResponseBody().write(responseBody);
            exchange.getResponseBody().close();
        });
        // サーバーを起動し、リクエストの受付を開始します。
        server.start();
        System.out.println("サーバー起動: http://localhost:8080 (停止するには Ctrl+C)");
    }
}

// TodoのID、タイトル、完了状態を保持します。
class Todo {
    // Todoを識別する変更されないIDです。
    private final int id;
    // Todoの変更されないタイトルです。
    private final String title;
    // Todoが完了しているかどうかを保持します。
    private boolean done;

    // IDとタイトルを受け取り、未完了状態でTodoを初期化します。
    Todo(int id, String title) {
        this.id = id;
        this.title = title;
        this.done = false;
    }

    // TodoのIDを返します。
    int getId() {
        return id;
    }

    // Todoのタイトルを返します。
    String getTitle() {
        return title;
    }

    // Todoの完了状態を返します。
    boolean isDone() {
        return done;
    }

    // Todoの完了状態を更新します。
    void setDone(boolean done) {
        this.done = done;
    }
}
