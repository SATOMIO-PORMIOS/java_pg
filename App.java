
// Webサーバーを使うための道具を読み込みます。
import com.sun.net.httpserver.HttpServer;
// 待ち受けるポート番号を指定する道具を読み込みます。
import java.net.InetSocketAddress;
// URL用に変換された文字を元に戻す道具を読み込みます。
import java.net.URLDecoder;
// リストを作って文字列を並べる道具を読み込みます。
import java.util.ArrayList;
// 文字列の一覧を扱う型を読み込みます。
import java.util.List;

// Appという名前のプログラムを定義します。
public class App {
    // プログラムの開始位置を定義します。
    public static void main(String[] args) throws Exception {
        // 8080番ポートで待ち受けるサーバーを作ります。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // 「/」へのアクセスを受け取る処理を登録します。
        server.createContext("/", exchange -> {
            // アクセスされたパスを取り出します。
            String path = exchange.getRequestURI().getPath();
            // 返す文字を入れる変数を用意します。
            String message;
            // URLから受け取った名前を入れる変数を用意します。
            String name = "";
            // 通常の応答形式をUTF-8のテキストにします。
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            // パスが「/hello」か比べます。
            if (path.equals("/hello")) {
                // if (path == "/hello") {
                // URLのクエリ部分を取り出します。
                String query = exchange.getRequestURI().getRawQuery();
                // name= の後ろを切り出します。
                // queryの中身をターミナルに表示します。
                System.out.println("query = " + query);
                // queryの先頭5文字（name=）を除いた部分をnameに入れます。
                name = query.substring(5);
                // URL用に変換された名前をUTF-8で元に戻します。
                name = URLDecoder.decode(name, "UTF-8");
                // 名前を挨拶の文字列につなげます。
                message = "こんにちは、" + name + "さん！";
                // パスが「/todos」か比べます。
            } else if (path.equals("/todos")) {
                // Todoの文字列を入れるリストを作ります。
                List<String> todos = new ArrayList<>();
                todos.add("自分の今日のTodo");
                // 1件目のTodoをリストに追加します。
                todos.add("牛乳を買う");
                // 2件目のTodoをリストに追加します。
                todos.add("卵を買う");
                // 3件目のTodoをリストに追加します。
                todos.add("パンを買う");
                // HTMLのリストを開きます。
                String html = "<ul>";
                // リストのTodoを1件ずつ取り出します。
                for (String todo : todos) {
                    // TodoをHTMLの項目として追加します。
                    html += "<li>" + todo + "</li>";
                }
                // HTMLのリストを閉じます。
                html += "</ul>";
                // 組み立てたHTMLを応答にします。
                message = html;
                // /todosの応答形式をUTF-8のHTMLにします。
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                // パスが「/bye」か比べます。
            } else if (path.equals("/bye")) {
                // URLのクエリ部分を取り出します。
                String query = exchange.getRequestURI().getRawQuery();
                // queryの先頭5文字（name=）を除いた部分をnameに入れます。
                name = query.substring(5);
                // URL用に変換された名前をUTF-8で元に戻します。
                name = URLDecoder.decode(name, "UTF-8");
                message = "さようなら、" + name + "さん！";
            } else if (path.equals("/")) {
                message = "トップページです";
            } else {
                message = "ページが見つかりません";
            }
            // 文字をUTF-8のデータに変換します。
            byte[] body = message.getBytes("UTF-8");
            // 正常応答とデータの長さを伝えます。
            exchange.sendResponseHeaders(200, body.length);
            // データをブラウザーへ送ります。
            exchange.getResponseBody().write(body);
            // 送信を終えて接続を閉じます。
            exchange.getResponseBody().close();
        });
        // サーバーを起動して待ち受けを始めます。
        server.start();
        // 起動したことと開くURLを表示します。
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）");
    }
}
