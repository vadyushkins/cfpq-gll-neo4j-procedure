package cfpq.gll.rsm;

import cfpq.gll.rsm.symbol.Nonterminal;

public class RSMNonterminalEdge implements RSMEdge {
    public Nonterminal nonterminal;
    public RSMState head;
    public Integer hashCode;

    public RSMNonterminalEdge(Nonterminal nonterminal, RSMState head) {
        this.nonterminal = nonterminal;
        this.head = head;
        this.hashCode = nonterminal.hashCode;
    }

    @Override
    public String toString() {
        return "RSMNonterminalEdge{" +
            "nonterminal=" + nonterminal +
            ", head=" + head +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RSMNonterminalEdge that)) return false;
        return nonterminal.equals(that.nonterminal) && head.equals(that.head);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
