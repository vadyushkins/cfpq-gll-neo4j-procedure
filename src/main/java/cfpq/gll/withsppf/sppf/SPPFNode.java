package cfpq.gll.withsppf.sppf;

import cfpq.gll.graph.Neo4jNode;

import java.util.Objects;

public class SPPFNode {
    public Neo4jNode leftExtent;
    public Neo4jNode rightExtent;
    public int hashCode;

    public SPPFNode(Neo4jNode leftExtent, Neo4jNode rightExtent) {
        this.leftExtent = leftExtent;
        this.rightExtent = rightExtent;
        this.hashCode = Objects.hash(leftExtent, rightExtent);
    }

    @Override
    public String toString() {
        return "SPPFNode{" +
            "leftExtent=" + leftExtent +
            ", rightExtent=" + rightExtent +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SPPFNode sppfNode)) return false;
        return leftExtent.equals(sppfNode.leftExtent) && rightExtent.equals(sppfNode.rightExtent);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

}
