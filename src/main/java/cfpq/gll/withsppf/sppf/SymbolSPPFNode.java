package cfpq.gll.withsppf.sppf;

import cfpq.gll.rsm.symbol.Symbol;
import org.neo4j.graphdb.Node;

import java.util.Objects;

public class SymbolSPPFNode extends ParentSPPFNode {
    public Symbol symbol;

    public SymbolSPPFNode(Node leftExtent, Node rightExtent, Symbol symbol) {
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

    @Override
    public boolean hasSymbol(Symbol symbol) {
        return this.symbol == symbol;
    }
}
