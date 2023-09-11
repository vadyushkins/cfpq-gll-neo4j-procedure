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
                        "id=(?<idValue>.*)," +
                        "nonterminal=Nonterminal\\((?<nonterminalValue>.*)\\)," +
                        "isStart=(?<isStartValue>.*)," +
                        "isFinal=(?<isFinalValue>.*)" +
                        "\\)$"
        );

        Pattern rsmStateRegex = Pattern.compile(
                "^State\\(" +
                        "id=(?<idValue>.*)," +
                        "nonterminal=Nonterminal\\((?<nonterminalValue>.*)\\)," +
                        "isStart=(?<isStartValue>.*)," +
                        "isFinal=(?<isFinalValue>.*)" +
                        "\\)$"
        );

        Pattern rsmTerminalEdgeRegex = Pattern.compile(
                "^TerminalEdge\\(" +
                        "tail=(?<tailValue>.*)," +
                        "head=(?<headValue>.*)," +
                        "terminal=Terminal\\((?<terminalValue>.*)\\)" +
                        "\\)$"
        );

        Pattern rsmNonterminalEdgeRegex = Pattern.compile(
                "^NonterminalEdge\\(" +
                        "tail=(?<tailValue>.*)," +
                        "head=(?<headValue>.*)," +
                        "nonterminal=Nonterminal\\((?<nonterminalValue>.*)\\)" +
                        "\\)$"
        );

        for (String line : grammar.split(";")) {
            Matcher startStateRegexMatcher = startStateRegex.matcher(line);
            if (startStateRegexMatcher.matches()) {
                startRSMState = new RSMStateBuilder()
                        .setId(Integer.parseInt(startStateRegexMatcher.group("idValue")))
                        .setNonterminal(makeNonterminal(startStateRegexMatcher.group("nonterminalValue")))
                        .setIsStart(Boolean.parseBoolean(startStateRegexMatcher.group("isStartValue")))
                        .setIsFinal(Boolean.parseBoolean(startStateRegexMatcher.group("isFinalValue")))
                        .createRSMState();
                if (!rsmStates.containsKey(startRSMState)) {
                    rsmStates.put(startRSMState.id, startRSMState);
                }
                continue;
            }

            Matcher rsmStateRegexMatcher = rsmStateRegex.matcher(line);
            if (rsmStateRegexMatcher.matches()) {
                RSMState rsmState = new RSMStateBuilder()
                        .setId(Integer.parseInt(rsmStateRegexMatcher.group("idValue")))
                        .setNonterminal(makeNonterminal(rsmStateRegexMatcher.group("nonterminalValue")))
                        .setIsStart(Boolean.parseBoolean(rsmStateRegexMatcher.group("isStartValue")))
                        .setIsFinal(Boolean.parseBoolean(rsmStateRegexMatcher.group("isFinalValue")))
                        .createRSMState();
                if (!rsmStates.containsKey(rsmState.id)) {
                    rsmStates.put(rsmState.id, rsmState);
                }
                continue;
            }

            Matcher rsmTerminalEdgeRegexMatcher = rsmTerminalEdgeRegex.matcher(line);
            if (rsmTerminalEdgeRegexMatcher.matches()) {
                RSMState tailRSMState = rsmStates.get(Integer.parseInt(rsmTerminalEdgeRegexMatcher.group("tailValue")));
                RSMState headRSMState = rsmStates.get(Integer.parseInt(rsmTerminalEdgeRegexMatcher.group("headValue")));
                tailRSMState.addTerminalEdge(
                        new RSMTerminalEdge(
                                new Terminal(rsmTerminalEdgeRegexMatcher.group("terminalValue")),
                                headRSMState
                        )
                );
                continue;
            }

            Matcher rsmNonterminalEdgeRegexMatcher = rsmNonterminalEdgeRegex.matcher(line);
            if (rsmNonterminalEdgeRegexMatcher.matches()) {
                RSMState tailRSMState = rsmStates.get(Integer.parseInt(rsmNonterminalEdgeRegexMatcher.group("tailValue")));
                RSMState headRSMState = rsmStates.get(Integer.parseInt(rsmNonterminalEdgeRegexMatcher.group("headValue")));
                tailRSMState.addNonterminalEdge(
                        new RSMNonterminalEdge(
                                makeNonterminal(rsmNonterminalEdgeRegexMatcher.group("terminalValue")),
                                headRSMState
                        )
                );
            }
        }

        return startRSMState;
    }
}
