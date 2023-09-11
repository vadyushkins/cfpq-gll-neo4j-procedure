package cfpq.gll.withsppf.sppf;

import cfpq.gll.rsm.symbol.Symbol;
import org.neo4j.graphdb.Node;

import java.util.Objects;

public class SPPFNode {
    public Node leftExtent;
    public Node rightExtent;
    public int hashCode;

    public SPPFNode(Node leftExtent, Node rightExtent) {
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
        return leftExtent == sppfNode.leftExtent && rightExtent == sppfNode.rightExtent;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public boolean hasSymbol(Symbol symbol) {
        return false;
    }
}
