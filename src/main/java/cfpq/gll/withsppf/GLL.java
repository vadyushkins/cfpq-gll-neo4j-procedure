package cfpq.gll.withsppf;

import cfpq.gll.rsm.RSMNonterminalEdge;
import cfpq.gll.rsm.RSMState;
import cfpq.gll.rsm.RSMTerminalEdge;
import cfpq.gll.rsm.symbol.Nonterminal;
import cfpq.gll.rsm.symbol.Terminal;
import cfpq.gll.withsppf.sppf.*;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.internal.helpers.collection.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class GLL {
    public RSMState startState;
    public List<Node> startGraphNodes;

    public DescriptorsQueue queue = new DescriptorsQueue();
    public HashMap<GSSNode, HashSet<SPPFNode>> poppedGSSNodes = new HashMap<>();
    public HashMap<GSSNode, GSSNode> createdGSSNodes = new HashMap<>();
    public HashMap<SPPFNode, SPPFNode> createdSPPFNodes = new HashMap<>();
    public HashMap<Node, HashMap<Node, SPPFNode>> parseResult = new HashMap<>();

    public GLL(RSMState startState, List<Node> startGraphNodes) {
        this.startState = startState;
        this.startGraphNodes = startGraphNodes;
    }

    public GSSNode getOrCreateGSSNode(Nonterminal nonterminal, Node pos) {
        GSSNode gssNode = new GSSNode(nonterminal, pos);
        if (!createdGSSNodes.containsKey(gssNode)) {
            createdGSSNodes.put(gssNode, gssNode);
        }
        return createdGSSNodes.get(gssNode);
    }

    public HashMap<Node, HashMap<Node, SPPFNode>> parse() {
        for (Node graphNode : startGraphNodes) {
            queue.add(startState, getOrCreateGSSNode(startState.nonterminal, graphNode), null, graphNode);
        }

        while (!queue.isEmpty()) {
            DescriptorsQueue.Descriptor descriptor = queue.next();
            parse(descriptor.rsmState, descriptor.gssNode, descriptor.sppfNode, descriptor.pos);
        }

        return parseResult;
    }

    public void parse(RSMState state, GSSNode gssNode, SPPFNode sppfNode, Node pos) {
        SPPFNode curSPPFNode;

        if (state.isStart && state.isFinal) {
            curSPPFNode = getNodeP(state, sppfNode, getOrCreateItemSPPFNode(state, pos, pos));
        } else {
            curSPPFNode = sppfNode;
        }

        for (RSMTerminalEdge rsmEdge : state.outgoingTerminalEdges) {
            pos.getRelationships(Direction.OUTGOING, RelationshipType.withName(rsmEdge.terminal.value)).stream().forEach(relationship -> {
                Node head = relationship.getEndNode();
                queue.add(
                        rsmEdge.head,
                        gssNode,
                        getNodeP(
                                rsmEdge.head,
                                curSPPFNode,
                                getOrCreateTerminalSPPFNode(
                                        rsmEdge.terminal,
                                        pos,
                                        head
                                )
                        ),
                        head
                );
            });
        }

        for (RSMNonterminalEdge rsmEdge : state.outgoingNonterminalEdges) {
            queue.add(
                    rsmEdge.nonterminal.startState,
                    createGSSNode(rsmEdge.nonterminal, rsmEdge.head, gssNode, curSPPFNode, pos),
                    null,
                    pos
            );
        }

        if (state.isFinal) {
            pop(gssNode, curSPPFNode, pos);
        }
    }

    public void pop(GSSNode gssNode, SPPFNode sppfNode, Node pos) {
        if (!poppedGSSNodes.containsKey(gssNode)) {
            poppedGSSNodes.put(gssNode, new HashSet<>());
        }
        poppedGSSNodes.get(gssNode).add(sppfNode);
        for (Map.Entry<Pair<RSMState, SPPFNode>, HashSet<GSSNode>> e : gssNode.edges.entrySet()) {
            for (GSSNode u : e.getValue()) {
                queue.add(
                        e.getKey().first(),
                        u,
                        getNodeP(
                                e.getKey().first(),
                                e.getKey().other(),
                                sppfNode
                        ),
                        pos
                );
            }
        }
    }

    public GSSNode createGSSNode(
            Nonterminal nonterminal,
            RSMState state,
            GSSNode gssNode,
            SPPFNode sppfNode,
            Node pos
    ) {
        GSSNode v = getOrCreateGSSNode(nonterminal, pos);

        if (v.addEdge(state, sppfNode, gssNode)) {
            if (poppedGSSNodes.containsKey(v)) {
                for (SPPFNode z : poppedGSSNodes.get(v)) {
                    queue.add(
                            state,
                            gssNode,
                            getNodeP(
                                    state,
                                    sppfNode,
                                    z
                            ),
                            z.rightExtent
                    );
                }
            }
        }

        return v;
    }

    public SPPFNode getNodeP(RSMState state, SPPFNode sppfNode, SPPFNode nextSPPFNode) {
        Node leftExtent = (null == sppfNode) || (null == sppfNode.leftExtent)
                ? nextSPPFNode.leftExtent
                : sppfNode.leftExtent;

        Node rightExtent = nextSPPFNode.rightExtent;

        ParentSPPFNode y = state.isFinal
                ? getOrCreateSymbolSPPFNode(state.nonterminal, leftExtent, rightExtent)
                : getOrCreateItemSPPFNode(state, leftExtent, rightExtent);

        y.kids.add(
                new PackedSPPFNode(
                        nextSPPFNode.leftExtent,
                        state,
                        sppfNode,
                        nextSPPFNode
                )
        );

        return y;
    }

    public SPPFNode getOrCreateTerminalSPPFNode(
            Terminal terminal,
            Node leftExtent,
            Node rightExtent
    ) {
        TerminalSPPFNode y = new TerminalSPPFNode(leftExtent, rightExtent, terminal);
        if (!createdSPPFNodes.containsKey(y)) {
            createdSPPFNodes.put(y, y);
        }
        return createdSPPFNodes.get(y);
    }

    public ParentSPPFNode getOrCreateItemSPPFNode(
            RSMState state,
            Node leftExtent,
            Node rightExtent
    ) {
        ItemSPPFNode y = new ItemSPPFNode(leftExtent, rightExtent, state);
        if (!createdSPPFNodes.containsKey(y)) {
            createdSPPFNodes.put(y, y);
        }
        return (ParentSPPFNode) createdSPPFNodes.get(y);
    }

    public SymbolSPPFNode getOrCreateSymbolSPPFNode(
            Nonterminal nonterminal,
            Node leftExtent,
            Node rightExtent
    ) {
        SymbolSPPFNode y = new SymbolSPPFNode(leftExtent, rightExtent, nonterminal);
        if (!createdSPPFNodes.containsKey(y)) {
            createdSPPFNodes.put(y, y);
        }
        SymbolSPPFNode result = (SymbolSPPFNode) createdSPPFNodes.get(y);
        if (
                nonterminal == startState.nonterminal &&
                        startGraphNodes.contains(leftExtent)
        ) {
            if (!parseResult.containsKey(leftExtent)) {
                parseResult.put(leftExtent, new HashMap<>());
            }
            parseResult.get(leftExtent).put(rightExtent, result);
        }
        return result;
    }
}
