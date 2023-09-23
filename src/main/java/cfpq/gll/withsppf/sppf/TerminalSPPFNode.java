package cfpq.gll.withsppf.sppf;

import cfpq.gll.graph.Neo4jNode;
import cfpq.gll.rsm.symbol.Terminal;

import java.util.Objects;

public class TerminalSPPFNode extends SPPFNode {
    public Terminal terminal;

    public TerminalSPPFNode(Neo4jNode leftExtent, Neo4jNode rightExtent, Terminal terminal) {
        super(leftExtent, rightExtent);
        this.terminal = terminal;
        this.hashCode = Objects.hash(leftExtent, rightExtent, terminal);
    }

    @Override
    public String toString() {
        return "TerminalSPPFNode{" +
            "terminal=" + terminal +
            ", leftExtent=" + leftExtent +
            ", rightExtent=" + rightExtent +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TerminalSPPFNode that)) return false;
        if (!super.equals(o)) return false;
        return terminal.equals(that.terminal);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
