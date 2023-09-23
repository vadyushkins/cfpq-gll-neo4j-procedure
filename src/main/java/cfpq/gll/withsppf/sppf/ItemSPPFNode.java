package cfpq.gll.withsppf.sppf;

import cfpq.gll.graph.Neo4jNode;
import cfpq.gll.rsm.RSMState;

import java.util.Objects;

public class ItemSPPFNode extends ParentSPPFNode {
    public RSMState rsmState;

    public ItemSPPFNode(Neo4jNode leftExtent, Neo4jNode rightExtent, RSMState rsmState) {
        super(leftExtent, rightExtent);
        this.rsmState = rsmState;
        this.hashCode = Objects.hash(leftExtent, rightExtent, rsmState);
    }

    @Override
    public String toString() {
        return "ItemSPPFNode{" +
            "rsmState=" + rsmState +
            ", leftExtent=" + leftExtent +
            ", rightExtent=" + rightExtent +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemSPPFNode that)) return false;
        if (!super.equals(o)) return false;
        return rsmState.equals(that.rsmState);
    }
}
