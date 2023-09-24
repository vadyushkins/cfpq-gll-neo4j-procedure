package cfpq.gll.graph;

import org.neo4j.graphdb.Node;

public class Neo4jNode {
    public Node node;
    public Integer hashCode;

    public Neo4jNode(Node node) {
        this.node = node;
        this.hashCode = node.getProperty("id").hashCode();
    }

    @Override
    public String toString() {
        return "Neo4jNode{" +
            "node=" + node +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Neo4jNode neo4jNode)) return false;
        return hashCode.equals(neo4jNode.hashCode);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
