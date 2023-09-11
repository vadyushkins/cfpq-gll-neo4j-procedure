package cfpq.gll.withsppf.sppf;

import cfpq.gll.rsm.RSMState;
import org.neo4j.graphdb.Node;

public class PackedSPPFNodeBuilder {
    private Node pivot;
    private RSMState rsmState;
    private SPPFNode leftSPPFNode = null;
    private SPPFNode rightSPPFNode = null;

    public PackedSPPFNodeBuilder setPivot(Node pivot) {
        this.pivot = pivot;
        return this;
    }

    public PackedSPPFNodeBuilder setRsmState(RSMState rsmState) {
        this.rsmState = rsmState;
        return this;
    }

    public PackedSPPFNodeBuilder setLeftSPPFNode(SPPFNode leftSPPFNode) {
        this.leftSPPFNode = leftSPPFNode;
        return this;
    }

    public PackedSPPFNodeBuilder setRightSPPFNode(SPPFNode rightSPPFNode) {
        this.rightSPPFNode = rightSPPFNode;
        return this;
    }

    public PackedSPPFNode createPackedSPPFNode() {
        return new PackedSPPFNode(pivot, rsmState, leftSPPFNode, rightSPPFNode);
    }
}