package cfpq.gll.rsm.symbol;

public class Terminal implements Symbol {
    public String value;
    public Integer size;
    public Integer hashCode;

    public Terminal(String value) {
        this.value = value;
        this.size = value.length();
        this.hashCode = value.hashCode();
    }

    @Override
    public String toString() {
        return "Terminal{" +
            "value='" + value + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Terminal terminal)) return false;
        return value.equals(terminal.value);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
