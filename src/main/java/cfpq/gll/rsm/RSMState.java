package cfpq.gll.rsm;

import cfpq.gll.rsm.symbol.Nonterminal;

import java.util.ArrayList;

public class RSMState {
    public Integer id;
    public Nonterminal nonterminal;
    public boolean isStart;
    public boolean isFinal;
    public Integer hashCode;
    public ArrayList<RSMTerminalEdge> outgoingTerminalEdges = new ArrayList<>();
    public ArrayList<RSMNonterminalEdge> outgoingNonterminalEdges = new ArrayList<>();

    public RSMState(Integer id, Nonterminal nonterminal, boolean isStart, boolean isFinal) {
        this.id = id;
        this.nonterminal = nonterminal;
        this.isStart = isStart;
        this.isFinal = isFinal;
        this.hashCode = id;
    }

    @Override
    public String toString() {
        return "RSMState{" +
            "id=" + id +
            ", nonterminal=" + nonterminal +
            ", isStart=" + isStart +
            ", isFinal=" + isFinal +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RSMState rsmState)) return false;
        return id.equals(rsmState.id);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public void addTerminalEdge(RSMTerminalEdge edge) {
        if (!outgoingTerminalEdges.contains(edge)) {
            outgoingTerminalEdges.add(edge);
        }
    }

    public void addNonterminalEdge(RSMNonterminalEdge edge) {
        if (!outgoingNonterminalEdges.contains(edge)) {
            outgoingNonterminalEdges.add(edge);
        }
    }
}
