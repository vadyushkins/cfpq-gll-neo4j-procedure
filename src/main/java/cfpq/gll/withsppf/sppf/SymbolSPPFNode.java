package cfpq.gll.withsppf.sppf;

import cfpq.gll.graph.Neo4jNode;
import cfpq.gll.rsm.symbol.Nonterminal;

import java.util.Objects;

public class SymbolSPPFNode extends ParentSPPFNode {
    public Nonterminal symbol;

    public SymbolSPPFNode(Neo4jNode leftExtent, Neo4jNode rightExtent, Nonterminal symbol) {
        super(leftExtent, rightExtent);
        this.symbol = symbol;
        this.hashCode = Objects.hash(leftExtent, rightExtent, symbol);
    }

    @Override
    public String toString() {
        return "SymbolSPPFNode{" +
            "symbol=" + symbol +
            ", leftExtent=" + leftExtent +
            ", rightExtent=" + rightExtent +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SymbolSPPFNode that)) return false;
        if (!super.equals(o)) return false;
        return symbol.equals(that.symbol);
    }

}
