package cfpq.gll.withsppf.sppf;

import cfpq.gll.rsm.RSMState;
import org.neo4j.graphdb.Node;

import java.util.Objects;

public class PackedSPPFNode {
    public Node pivot;
    public RSMState rsmState;
    public SPPFNode leftSPPFNode;
    public SPPFNode rightSPPFNode;
    public Integer hashCode;

    public PackedSPPFNode(Node pivot, RSMState rsmState, SPPFNode leftSPPFNode, SPPFNode rightSPPFNode) {
        this.pivot = pivot;
        this.rsmState = rsmState;
        this.leftSPPFNode = leftSPPFNode;
        this.rightSPPFNode = rightSPPFNode;
        this.hashCode = Objects.hash(pivot, rsmState, leftSPPFNode, rightSPPFNode);
    }

    @Override
    public String toString() {
        return "PackedSPPFNode{" +
                "pivot=" + pivot +
                ", rsmState=" + rsmState +
                ", leftSPPFNode=" + leftSPPFNode +
                ", rightSPPFNode=" + rightSPPFNode +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PackedSPPFNode that)) return false;
        return pivot == that.pivot && rsmState.equals(that.rsmState) && Objects.equals(leftSPPFNode, that.leftSPPFNode) && Objects.equals(rightSPPFNode, that.rightSPPFNode);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
