package cfpq.gll.rsm;

import cfpq.gll.rsm.symbol.Terminal;

public class RSMTerminalEdge implements RSMEdge {
    public Terminal terminal;
    public RSMState head;
    public Integer hashCode;

    public RSMTerminalEdge(Terminal terminal, RSMState head) {
        this.terminal = terminal;
        this.head = head;
        this.hashCode = terminal.hashCode;
    }

    @Override
    public String toString() {
        return "RSMTerminalEdge{" +
            "terminal=" + terminal +
            ", head=" + head +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RSMTerminalEdge that)) return false;
        return terminal.equals(that.terminal) && head.equals(that.head);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
