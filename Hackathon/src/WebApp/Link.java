package WebApp;

import java.util.ArrayList;
import java.util.HashMap;

public class Link {
    String label;
    Node tail;
    Node head;
    boolean inheritance;

    public Link(String theLabel, String theTail,
                String theHead, SemanticNet sn) {
        label = theLabel;
        HashMap<String, Node> nodesNameTable = sn.getNodesNameTable();
        ArrayList<Node> nodes = sn.getNodes();

        tail = (Node) nodesNameTable.get(theTail);
        if (tail == null) {
            tail = new Node(theTail);
            nodes.add(tail);
            nodesNameTable.put(theTail, tail);
        }

        head = (Node) nodesNameTable.get(theHead);
        if (head == null) {
            head = new Node(theHead);
            nodes.add(head);
            nodesNameTable.put(theHead, head);
        }
        inheritance = false;
    }

    // For constructing query.
    public Link(String theLabel, String theTail, String theHead) {
        label = theLabel;
        tail = new Node(theTail);
        head = new Node(theHead);
        inheritance = false;
    }

    public void setInheritance(boolean value) {
        inheritance = value;
    }

    public Node getTail() {
        return tail;
    }

    public Node getHead() {
        return head;
    }

    public String getLabel() {
        return label;
    }

    public String getFullName() {
        return tail.getName() + " " + label + " " + head.getName();
    }

    public String toString() {
        String result = tail.getName() + "  =" + label + "=>  " + head.getName();
        if (!inheritance) {
            return result;
        } else {
            return "( " + result + " )";
        }
    }
}
