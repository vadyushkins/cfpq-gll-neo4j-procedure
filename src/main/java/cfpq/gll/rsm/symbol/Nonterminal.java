package cfpq.gll.rsm.symbol;

import cfpq.gll.rsm.RSMState;

public class Nonterminal implements Symbol {
    public String name;
    public RSMState startState;
    public Integer hashCode;

    public Nonterminal(String name) {
        this.name = name;
        this.hashCode = name.hashCode();
    }

    @Override
    public String toString() {
        return "Nonterminal{" +
            "name='" + name + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nonterminal that)) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
