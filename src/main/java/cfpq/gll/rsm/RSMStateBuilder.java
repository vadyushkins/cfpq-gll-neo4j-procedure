package cfpq.gll.rsm;

import cfpq.gll.rsm.symbol.Nonterminal;

public class RSMStateBuilder {
    private Integer id;
    private Nonterminal nonterminal;
    private boolean isStart = false;
    private boolean isFinal = false;

    public RSMStateBuilder() {
    }

    public RSMStateBuilder setId(Integer id) {
        this.id = id;
        return this;
    }

    public RSMStateBuilder setNonterminal(Nonterminal nonterminal) {
        this.nonterminal = nonterminal;
        return this;
    }

    public RSMStateBuilder setIsStart(boolean isStart) {
        if (isStart) {
            this.isStart = true;
        }
        return this;
    }

    public RSMStateBuilder setIsFinal(boolean isFinal) {
        if (isFinal) {
            this.isFinal = true;
        }
        return this;
    }

    public RSMState createRSMState() {
        return new RSMState(id, nonterminal, isStart, isFinal);
    }
}