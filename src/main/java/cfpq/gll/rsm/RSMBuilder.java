package cfpq.gll.rsm;

import cfpq.gll.rsm.symbol.Nonterminal;
import cfpq.gll.rsm.symbol.Terminal;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RSMBuilder {
    public RSMState startRSMState;
    public HashMap<Integer, RSMState> rsmStates = new HashMap<>();
    public HashMap<Nonterminal, Nonterminal> nonterminals = new HashMap<>();

    public RSMState makeRSMState(
        Integer id,
        Nonterminal nonterminal,
        boolean isStart,
        boolean isFinal
    ) {
        RSMState y = new RSMState(id, nonterminal, isStart, isFinal);
        if (!rsmStates.containsKey(y.hashCode)) {
            rsmStates.put(y.hashCode, y);
        }
        return rsmStates.get(y.hashCode);
    }

    public Nonterminal makeNonterminal(String name) {
        Nonterminal y = new Nonterminal(name);
        if (!nonterminals.containsKey(y)) {
            nonterminals.put(y, y);
        }
        return nonterminals.get(y);
    }

    public RSMState createRSM(String grammar) {
        Pattern startStateRegex = Pattern.compile(
            "^StartState\\(" +
                "id=(?<id>.*)," +
                "nonterminal=Nonterminal\\((?<nonterminal>.*)\\)," +
                "isStart=(?<isStart>.*)," +
                "isFinal=(?<isFinal>.*)" +
                "\\)$"
        );

        Pattern rsmStateRegex = Pattern.compile(
            "^State\\(" +
                "id=(?<id>.*)," +
                "nonterminal=Nonterminal\\((?<nonterminal>.*)\\)," +
                "isStart=(?<isStart>.*)," +
                "isFinal=(?<isFinal>.*)" +
                "\\)$"
        );

        Pattern rsmTerminalEdgeRegex = Pattern.compile(
            "^TerminalEdge\\(" +
                "tail=(?<tail>.*)," +
                "head=(?<head>.*)," +
                "terminal=Terminal\\((?<terminal>.*)\\)" +
                "\\)$"
        );

        Pattern rsmNonterminalEdgeRegex = Pattern.compile(
            "^NonterminalEdge\\(" +
                "tail=(?<tail>.*)," +
                "head=(?<head>.*)," +
                "nonterminal=Nonterminal\\((?<nonterminal>.*)\\)" +
                "\\)$"
        );

        for (String line : grammar.split(";")) {
            Matcher startStateRegexMatcher = startStateRegex.matcher(line);
            if (startStateRegexMatcher.matches()) {
                Nonterminal tmpNonterminal = makeNonterminal(startStateRegexMatcher.group("nonterminal"));
                startRSMState = makeRSMState(
                    Integer.parseInt(startStateRegexMatcher.group("id")),
                    tmpNonterminal,
                    Boolean.parseBoolean(startStateRegexMatcher.group("isStart")),
                    Boolean.parseBoolean(startStateRegexMatcher.group("isFinal"))
                );
                if (startRSMState.isStart) {
                    tmpNonterminal.startState = startRSMState;
                }
                continue;
            }

            Matcher rsmStateRegexMatcher = rsmStateRegex.matcher(line);
            if (rsmStateRegexMatcher.matches()) {
                Nonterminal tmpNonterminal = makeNonterminal(rsmStateRegexMatcher.group("nonterminal"));
                RSMState rsmState = makeRSMState(
                    Integer.parseInt(rsmStateRegexMatcher.group("id")),
                    tmpNonterminal,
                    Boolean.parseBoolean(rsmStateRegexMatcher.group("isStart")),
                    Boolean.parseBoolean(rsmStateRegexMatcher.group("isFinal"))
                );
                if (rsmState.isStart) {
                    tmpNonterminal.startState = rsmState;
                }
                continue;
            }

            Matcher rsmTerminalEdgeRegexMatcher = rsmTerminalEdgeRegex.matcher(line);
            if (rsmTerminalEdgeRegexMatcher.matches()) {
                RSMState tailRSMState = rsmStates.get(Integer.parseInt(rsmTerminalEdgeRegexMatcher.group("tail")));
                RSMState headRSMState = rsmStates.get(Integer.parseInt(rsmTerminalEdgeRegexMatcher.group("head")));
                tailRSMState.addTerminalEdge(
                    new RSMTerminalEdge(
                        new Terminal(rsmTerminalEdgeRegexMatcher.group("terminal")),
                        headRSMState
                    )
                );
                continue;
            }

            Matcher rsmNonterminalEdgeRegexMatcher = rsmNonterminalEdgeRegex.matcher(line);
            if (rsmNonterminalEdgeRegexMatcher.matches()) {
                RSMState tailRSMState = rsmStates.get(Integer.parseInt(rsmNonterminalEdgeRegexMatcher.group("tail")));
                RSMState headRSMState = rsmStates.get(Integer.parseInt(rsmNonterminalEdgeRegexMatcher.group("head")));
                tailRSMState.addNonterminalEdge(
                    new RSMNonterminalEdge(
                        makeNonterminal(rsmNonterminalEdgeRegexMatcher.group("nonterminal")),
                        headRSMState
                    )
                );
            }
        }

        return startRSMState;
    }
}
