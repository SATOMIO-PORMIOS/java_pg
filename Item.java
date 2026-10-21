// Itemという名前のプログラムです。
public class Item {
    // ここからプログラムを実行します。
    public static void main(String[] args) {
        // titleという文字列（文字の並び）に「牛乳を買う」を入れます。
        String title = "牛乳を買う";
        boolean done = true;
        int count = 3;
        // +で文字をつなげて、HTMLの1行分の文字列（文字の並び）を作ります。
        String html = "<li>" + title + "</li>";
        // 作ったHTML文字列をターミナル（文字を表示する画面）に出します。
        System.out.println(html);
        System.out.println(done);
        System.out.println("いま" + count + "件");
    }
}