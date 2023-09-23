package cfpq.gll.withsppf;

import cfpq.gll.graph.Neo4jNode;
import cfpq.gll.rsm.RSMState;
import cfpq.gll.rsm.symbol.Nonterminal;
import cfpq.gll.withsppf.sppf.SPPFNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class GSSNode {
    public Nonterminal nonterminal;
    public Neo4jNode pos;
    public Integer hashCode;
    public HashMap<Pair, HashSet<GSSNode>> edges = new HashMap<>();

    public GSSNode(Nonterminal nonterminal, Neo4jNode pos) {
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
        return nonterminal.equals(gssNode.nonterminal) && pos.equals(gssNode.pos);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public boolean addEdge(RSMState rsmState, SPPFNode sppfNode, GSSNode gssNode) {
        var label = new Pair(rsmState, sppfNode);
        if (!edges.containsKey(label)) {
            edges.put(label, new HashSet<>());
        }
        return edges.get(label).add(gssNode);
    }

    static class Pair {
        public RSMState rsmState;
        public SPPFNode sppfNode;
        public Integer hashCode;

        public Pair(RSMState rsmState, SPPFNode sppfNode) {
            this.rsmState = rsmState;
            this.sppfNode = sppfNode;
            this.hashCode = Objects.hash(rsmState, sppfNode);
        }

        @Override
        public String toString() {
            return "Pair{" +
                "state=" + rsmState +
                ", node=" + sppfNode +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair pair)) return false;
            return rsmState.equals(pair.rsmState) && Objects.equals(sppfNode, pair.sppfNode);
        }

        @Override
        public int hashCode() {
            return this.hashCode;
        }

    }
}
