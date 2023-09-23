package cfpq.gll.withsppf;

import cfpq.gll.graph.Neo4jNode;
import cfpq.gll.rsm.RSMState;
import cfpq.gll.withsppf.sppf.SPPFNode;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class DescriptorsQueue {
    public ArrayDeque<Descriptor> todo = new ArrayDeque<>();
    public HashMap<Neo4jNode, HashSet<Descriptor>> created = new HashMap<>();

    public void add(RSMState rsmState, GSSNode gssNode, SPPFNode sppfNode, Neo4jNode pos) {
        var descriptor = new Descriptor(rsmState, gssNode, sppfNode, pos);
        if (!created.containsKey(pos)) {
            created.put(pos, new HashSet<>());
        }
        if (created.get(pos).add(descriptor)) {
            todo.addLast(descriptor);
        }
    }

    public Descriptor next() {
        return todo.removeFirst();
    }

    public boolean isEmpty() {
        return todo.isEmpty();
    }

    static class Descriptor {
        public RSMState rsmState;
        public GSSNode gssNode;
        public SPPFNode sppfNode;
        public Neo4jNode pos;
        public Integer hashCode;

        public Descriptor(RSMState rsmState, GSSNode gssNode, SPPFNode sppfNode, Neo4jNode pos) {
            this.rsmState = rsmState;
            this.gssNode = gssNode;
            this.sppfNode = sppfNode;
            this.pos = pos;
            this.hashCode = Objects.hash(rsmState, gssNode, sppfNode);
        }

        @Override
        public String toString() {
            return "Descriptor{" +
                "rsmState=" + rsmState +
                ", gssNode=" + gssNode +
                ", sppfNode=" + sppfNode +
                ", pos=" + pos +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Descriptor that)) return false;
            return rsmState.equals(that.rsmState) && gssNode.equals(that.gssNode) && Objects.equals(sppfNode, that.sppfNode);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
