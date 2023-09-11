package cfpq.gll.withsppf.sppf;

import org.neo4j.graphdb.Node;

import java.util.HashSet;

public class ParentSPPFNode extends SPPFNode {
    public HashSet<PackedSPPFNode> kids = new HashSet<>();

    public ParentSPPFNode(Node leftExtent, Node rightExtent) {
        super(leftExtent, rightExtent);
    }

    @Override
    public String toString() {
        return "ParentSPPFNode{" +
                "leftExtent=" + leftExtent +
                ", rightExtent=" + rightExtent +
                '}';
    }
}
