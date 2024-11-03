package WebApp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/***
 * SemanticNetクラス
 */
public class SemanticNet {

    private ArrayList<Node> nodes;  // Nodeのリスト
    private HashMap<String, Node> nodesNameTable;  // Node名で検索できるテーブル

    public SemanticNet() {
        nodes = new ArrayList<>();
        nodesNameTable = new HashMap<>();
    }

    // ノードリストとノード名テーブルを返すためのメソッド
    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public HashMap<String, Node> getNodesNameTable() {
        return nodesNameTable;
    }

    public void addLink(Link link) {
        try {
            Connection conn = Database.getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO links (label, tail, head) VALUES (?, ?, ?)");
            ps.setString(1, link.getLabel());
            ps.setString(2, link.getTail().getName());
            ps.setString(3, link.getHead().getName());
            ps.executeUpdate();

            // Nodeに出発・到着リンクを追加
            link.getTail().addDepartFromMeLinks(link);
            link.getHead().addArriveAtMeLinks(link);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Link> getLinks() {
        List<Link> links = new ArrayList<>();
        try {
            Connection conn = Database.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT label, tail, head FROM links");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String label = rs.getString("label");
                String tail = rs.getString("tail");
                String head = rs.getString("head");
                links.add(new Link(label, tail, head, this));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return links;
    }

    public boolean isEmpty() {
        try {
            Connection conn = Database.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM links");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void addInitialLinks() {
        addLink(new Link("is-a", "baseball", "sports", this));
        addLink(new Link("is-a", "Taro", "NIT-student", this));
        addLink(new Link("speciality", "Taro", "AI", this));
        addLink(new Link("is-a", "Ferrari", "car", this));
        addLink(new Link("has-a", "car", "engine", this));
        addLink(new Link("hobby", "Taro", "baseball", this));
        addLink(new Link("own", "Taro", "Ferrari", this));
        addLink(new Link("is-a", "NIT-student", "student", this));
        addLink(new Link("donot", "student", "study", this));
    }

    public String query(ArrayList<Link> queries) {
        System.out.println("*** Query ***");
        for (Link q : queries) {
            System.out.println(q.toString());
        }

        List<List<Map<String, String>>> bindingsList = new ArrayList<>();
        for (Link query : queries) {
            List<Map<String, String>> bindings = queryLink(query);
            if (bindings.isEmpty()) {
                // クエリが失敗した場合
                return "クエリ結果がありません";
            } else {
                bindingsList.add(bindings);
            }
        }

        List<Map<String, String>> results = join(bindingsList);

        if (results.isEmpty()) {
            return "クエリ結果がありません";
        }

        StringBuilder sb = new StringBuilder();
        for (Map<String, String> result : results) {
            sb.append(result.toString()).append("\n");
        }
        return sb.toString();
    }

    public List<Map<String, String>> queryLink(Link theQuery) {
        List<Map<String, String>> bindingsList = new ArrayList<>();
        List<Link> links = getLinks();
        for (Link link : links) {
            Map<String, String> bindings = new HashMap<>();
            if (new Matcher().matching(theQuery.getFullName(), link.getFullName(), bindings)) {
                bindingsList.add(bindings);
            }
        }
        return bindingsList;
    }

    public List<Map<String, String>> join(List<List<Map<String, String>>> bindingsList) {
        int size = bindingsList.size();
        switch (size) {
            case 0:
                // バインディングがない場合
                return new ArrayList<>();
            case 1:
                return bindingsList.get(0);
            case 2:
                return joinBindings(bindingsList.get(0), bindingsList.get(1));
            default:
                List<Map<String, String>> first = bindingsList.get(0);
                bindingsList.remove(0);
                List<Map<String, String>> rest = join(bindingsList);
                return joinBindings(first, rest);
        }
    }

    public List<Map<String, String>> joinBindings(List<Map<String, String>> bindings1, List<Map<String, String>> bindings2) {
        List<Map<String, String>> resultBindings = new ArrayList<>();
        for (Map<String, String> b1 : bindings1) {
            for (Map<String, String> b2 : bindings2) {
                Map<String, String> merged = joinBinding(b1, b2);
                if (merged != null) {
                    resultBindings.add(merged);
                }
            }
        }
        return resultBindings;
    }

    public Map<String, String> joinBinding(Map<String, String> b1, Map<String, String> b2) {
        Map<String, String> result = new HashMap<>(b1);
        for (String key : b2.keySet()) {
            String value1 = result.get(key);
            String value2 = b2.get(key);
            if (value1 != null && !value1.equals(value2)) {
                // 競合がある場合、失敗
                return null;
            } else {
                result.put(key, value2);
            }
        }
        return result;
    }

    public void printLinks() {
        System.out.println("*** Links ***");
        List<Link> links = getLinks();
        for (int i = 0; i < links.size(); i++) {
            System.out.println(((Link) links.get(i)).toString());
        }
    }

    public void printNodes() {
        System.out.println("*** Nodes ***");
        for (int i = 0; i < nodes.size(); i++) {
            System.out.println(((Node) nodes.get(i)).toString());
        }
    }
}
