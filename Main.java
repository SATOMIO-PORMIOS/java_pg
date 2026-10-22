import java.util.ArrayList; // Todoを複数まとめて扱うための入れ物を読み込みます。
import java.util.List; // Todoを順番に扱えるリスト（並んだ一覧）の型を読み込みます。

public class Main { // javacとjavaから実行するクラス（処理をまとめる枠）です。
    public static void main(String[] args) { // プログラムを実行したとき最初に動く場所です。
        List<Todo> todos = new ArrayList<>(); // Todoを入れるリスト（並んだ一覧）を作ります。
        todos.add(new Todo("牛乳を買う", false)); // 未完了のTodoを1件追加します。
        todos.add(new Todo("ゴミを出す", true)); // 完了済みのTodoを1件追加します。
        todos.add(new Todo("ヨーグルトを買う", false)); // 完了済みのTodoを1件追加します。
        for (Todo todo : todos) { // リストからTodoを1件ずつ取り出して繰り返します。
            System.out.println(todo.toItem()); // HTML風の1行にしてターミナルへ表示します。
        }
    }
}

class Todo { // Todo（やること1件）の情報と処理をまとめるクラスです。
    private final String title; // やることのタイトル（文字列）を保存します。
    private final boolean done; // 済んだかどうか（trueは済み、falseは未完了）を保存します。

    Todo(String title, boolean done) { // タイトルと完了状態を受け取ってTodoを作ります。
        this.title = title; // 受け取ったタイトルをこのTodoに保存します。
        this.done = done; // 受け取った完了状態をこのTodoに保存します。
    }

    String toItem() { // Todoを指定された<li>形式の文字列に変換します。
        if (done) { // 済んでいるかを確認します。
            return "<li>【済】" + title + "</li>"; // 済んでいれば[済]を付けて返します。
        }
        return "<li>" + title + "</li>"; // 未完了ならタイトルだけを<li>で囲んで返します。
    }
}
