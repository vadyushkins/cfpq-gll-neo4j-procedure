package cfpq.gll.withsppf;

import cfpq.gll.rsm.RSMState;
import cfpq.gll.rsm.symbol.Nonterminal;
import cfpq.gll.withsppf.sppf.SPPFNode;
import org.neo4j.graphdb.Node;
import org.neo4j.internal.helpers.collection.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class GSSNode {
    public Nonterminal nonterminal;
    public Node pos;
    public Integer hashCode;
    public HashMap<Pair<RSMState, SPPFNode>, HashSet<GSSNode>> edges = new HashMap<>();

    public GSSNode(Nonterminal nonterminal, Node pos) {
        this.nonterminal = nonterminal;
        this.pos = pos;
        this.hashCode = Objects.hash(nonterminal, pos);
    }

    @Override
    public String toString() {
        return "GSSNode{" +
                "nonterminal=" + nonterminal +
                ", pos=" + pos +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GSSNode gssNode)) return false;
        return pos == gssNode.pos && nonterminal.equals(gssNode.nonterminal);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public boolean addEdge(RSMState rsmState, SPPFNode sppfNode, GSSNode gssNode) {
        var label = Pair.pair(rsmState, sppfNode);
        if (!edges.containsKey(label)) {
            edges.put(label, new HashSet<>());
        }
        return edges.get(label).add(gssNode);
    }
}
