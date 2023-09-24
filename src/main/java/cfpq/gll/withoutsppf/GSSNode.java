package cfpq.gll.withoutsppf;

import cfpq.gll.graph.Neo4jNode;
import cfpq.gll.rsm.RSMState;
import cfpq.gll.rsm.symbol.Nonterminal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class GSSNode {
    public Nonterminal nonterminal;
    public Neo4jNode pos;
    public Integer hashCode;
    public HashMap<RSMState, HashSet<GSSNode>> edges = new HashMap<>();

    public GSSNode(Nonterminal nonterminal, Neo4jNode pos) {
        this.nonterminal = nonterminal;
        this.pos = pos;
        this.hashCode = Objects.hash(nonterminal, pos);
    }

    public boolean addEdge(RSMState rsmState, GSSNode gssNode) {
        if (!edges.containsKey(rsmState)) {
            edges.put(rsmState, new HashSet<>());
        }
        return edges.get(rsmState).add(gssNode);
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
        return nonterminal.equals(gssNode.nonterminal) && pos.equals(gssNode.pos);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
