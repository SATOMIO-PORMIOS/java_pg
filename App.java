
// Webサーバーを使うための道具を読み込みます。
import com.sun.net.httpserver.HttpServer;
// 待ち受けるポート番号を指定する道具を読み込みます。
import java.net.InetSocketAddress;
// URL用に変換された文字を元に戻す道具を読み込みます。
import java.net.URLDecoder;
// UTF-8の文字コードを使う道具を読み込みます。
import java.nio.charset.StandardCharsets;
// リストを作って文字列を並べる道具を読み込みます。
import java.util.ArrayList;
// 文字列の一覧を扱う型を読み込みます。
import java.util.List;

// アプリケーション全体を表すクラスです。
public class App {
    // プログラムの開始位置です。
    public static void main(String[] args) throws Exception {
        // 8080番ポートでリクエストを待ち受けるサーバーを作ります。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // Todoを保存する空の一覧です。
        List<String> todos = new ArrayList<>();
        // すべてのパスへのアクセスを受け取る処理を登録します。
        server.createContext("/", exchange -> {
            // アクセスされたパスを取り出します。
            String path = exchange.getRequestURI().getPath();
            // GETかPOSTかを取り出します。
            String method = exchange.getRequestMethod();
            // 応答する文字列を入れる変数です。
            String message;
            // URLから受け取った名前を入れる変数です。
            String name = "";

            // POSTで /add に届いたTodoを一覧に追加します。
            if (path.equals("/add") && method.equals("POST")) {
                // 送信された内容をUTF-8で読み込みます。
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                // 先頭の todo= を取り除きます。
                String value = body.substring(5);
                // URL形式から日本語を元に戻します。
                String todo = URLDecoder.decode(value, StandardCharsets.UTF_8);
                // 空欄でなければTodoを一覧に追加します。
                if (!todo.isEmpty()) {
                    todos.add(todo);
                }
                // 戻り先をトップページに設定します。
                exchange.getResponseHeaders().set("Location", "/");
                // トップページへ移動する応答を返します。
                exchange.sendResponseHeaders(303, -1);
                // 通信を閉じます。
                exchange.close();
                // この分岐の処理を終えます。
                return;
            } else if (path.equals("/")) {
                // Todoの入力フォームと一覧を含むHTMLを作ります。
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>";
                // 一覧にあるTodoを1件ずつHTMLに追加します。
                for (String todo : todos) {
                    html += "<li>" + todo + "</li>";
                }
                // HTMLの一覧を閉じます。
                html += "</ul>";
                // 応答する内容を設定します。
                message = html;
                // 応答をUTF-8のHTMLにします。
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            } else if (path.equals("/hello")) {
                // URLのクエリ部分を取り出します。
                String query = exchange.getRequestURI().getRawQuery();
                // nameが指定されていれば、URL形式から名前を取り出します。
                if (query != null && query.startsWith("name=")) {
                    name = URLDecoder.decode(query.substring(5), StandardCharsets.UTF_8);
                }
                // 名前を挨拶の文字列につなげます。
                message = "こんにちは、" + name + "さん！";
            } else if (path.equals("/bye")) {
                // URLのクエリ部分を取り出します。
                String query = exchange.getRequestURI().getRawQuery();
                // nameが指定されていれば、URL形式から名前を取り出します。
                if (query != null && query.startsWith("name=")) {
                    name = URLDecoder.decode(query.substring(5), StandardCharsets.UTF_8);
                }
                // 名前を別れの挨拶につなげます。
                message = "さようなら、" + name + "さん！";
            } else {
                // 該当するページがないことを伝えます。
                message = "ページが見つかりません";
            }
            // 通常応答の文字コードをUTF-8としてブラウザーに伝えます。
            if (!path.equals("/")) {
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            }
            // 応答する文字列をUTF-8のデータに変換します。
            byte[] responseBody = message.getBytes(StandardCharsets.UTF_8);
            // 正常応答とデータの長さを伝えます。
            exchange.sendResponseHeaders(200, responseBody.length);
            // データをブラウザーへ送ります。
            exchange.getResponseBody().write(responseBody);
            // 送信を終えて接続を閉じます。
            exchange.getResponseBody().close();
        });
        // サーバーを起動して待ち受けを始めます。
        server.start();
        // 起動したことと開くURLを表示します。
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）");
    }
}
