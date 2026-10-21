public class Items { // Itemsという名前のクラス（プログラムのまとまり）を作ります
    public static void main(String[] args) { // Javaが最初に実行する場所を用意します
        String[] todos = { "牛乳を買う", "卵を買う", "パンを買う", "掃除をする" }; // Todo（やること）の文字列3件を配列（順番に並べた入れ物）に入れます
        boolean[] done = { true, false, false, false }; // 各Todoが済んだかをboolean（真偽値）で記録します
        for (int i = 0; i < todos.length; i++) { // i（数えるための変数）で配列の最後まで1件ずつ繰り返します
            if (todos[i].isEmpty()) { // 今の項目が空の文字列かどうかを調べます
                continue; // 空なら出力せず、次の項目に進みます
            } // 空の項目を調べる範囲をここで終わります
            String mark = done[i] ? "[済] " : ""; // 済んでいれば印を用意し、まだなら空にします
            System.out.println("<li>" + mark + todos[i] + "</li>"); // 印とTodoを<li>と</li>で囲んで1行出力します
        } // 繰り返しの範囲をここで終わります
    } // mainメソッド（実行開始地点）の範囲をここで終わります
} // Itemsクラスの範囲をここで終わります
