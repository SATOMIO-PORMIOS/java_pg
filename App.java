import com.sun.net.httpserver.HttpServer; // Webサーバーを使うための道具を読み込みます。
import java.net.InetSocketAddress; // 待ち受けるポート番号を指定する道具を読み込みます。

public class App { // Appという名前のプログラムを定義します。
    public static void main(String[] args) throws Exception { // プログラムの開始位置を定義します。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートで待ち受けるサーバーを作ります。
        server.createContext("/", exchange -> { // 「/」へのアクセスを受け取る処理を登録します。
            String message = "Hello, Server! こんにちは!"; // ブラウザーに返す文字を用意します。
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 返す内容がUTF-8の文字だと伝えます。
            byte[] body = message.getBytes("UTF-8"); // 文字をUTF-8のデータに変換します。
            exchange.sendResponseHeaders(200, body.length); // 正常応答とデータの長さを伝えます。
            exchange.getResponseBody().write(body); // データをブラウザーへ送ります。
            exchange.getResponseBody().close(); // 送信を終えて接続を閉じます。
        }); // 「/」へのアクセス処理の登録を終えます。
        server.start(); // サーバーを起動して待ち受けを始めます。
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動したことと開くURLを表示します。
    } // mainメソッドの定義を終えます。
} // Appクラスの定義を終えます。
