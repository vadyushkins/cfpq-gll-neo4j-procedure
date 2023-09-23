package cfpq.gll.withsppf.sppf;

import cfpq.gll.graph.Neo4jNode;

import java.util.HashSet;
import java.util.Objects;

public class ParentSPPFNode extends SPPFNode {
    public HashSet<PackedSPPFNode> kids = new HashSet<>();

    public ParentSPPFNode(Neo4jNode leftExtent, Neo4jNode rightExtent) {
        super(leftExtent, rightExtent);
        this.hashCode = Objects.hash(leftExtent, rightExtent);
    }

    @Override
    public String toString() {
        return "ParentSPPFNode{" +
            "leftExtent=" + leftExtent +
            ", rightExtent=" + rightExtent +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParentSPPFNode)) return false;
        return super.equals(o);
    }
}
