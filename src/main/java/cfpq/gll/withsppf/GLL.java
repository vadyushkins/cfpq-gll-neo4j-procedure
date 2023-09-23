package cfpq.gll.withsppf;

import cfpq.gll.graph.Neo4jNode;
import cfpq.gll.rsm.RSMNonterminalEdge;
import cfpq.gll.rsm.RSMState;
import cfpq.gll.rsm.RSMTerminalEdge;
import cfpq.gll.rsm.symbol.Nonterminal;
import cfpq.gll.rsm.symbol.Terminal;
import cfpq.gll.withsppf.sppf.*;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

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
    public HashMap<PackedSPPFNode, PackedSPPFNode> createdPackedSPPFNodes = new HashMap<>();
    public HashMap<Node, HashMap<Node, SPPFNode>> parseResult = new HashMap<>();

    public GLL(RSMState startState, List<Node> startGraphNodes) {
        this.startState = startState;
        this.startGraphNodes = startGraphNodes;
    }

    public GSSNode getOrCreateGSSNode(Nonterminal nonterminal, Neo4jNode pos) {
        GSSNode gssNode = new GSSNode(nonterminal, pos);
        if (!createdGSSNodes.containsKey(gssNode)) {
            createdGSSNodes.put(gssNode, gssNode);
        }
        return createdGSSNodes.get(gssNode);
    }

    public HashMap<Node, HashMap<Node, SPPFNode>> parse() {
        for (Node graphNode : startGraphNodes) {
            queue.add(startState, getOrCreateGSSNode(startState.nonterminal, new Neo4jNode(graphNode)), null,
                new Neo4jNode(graphNode));
        }

        while (!queue.isEmpty()) {
            DescriptorsQueue.Descriptor descriptor = queue.next();
            parse(descriptor.rsmState, descriptor.gssNode, descriptor.sppfNode, descriptor.pos);
        }

        return parseResult;
    }

    public void parse(RSMState state, GSSNode gssNode, SPPFNode sppfNode, Neo4jNode pos) {
        SPPFNode curSPPFNode;

        if (state.isStart && state.isFinal) {
            curSPPFNode = getNodeP(state, sppfNode, getOrCreateItemSPPFNode(state, pos, pos));
        } else {
            curSPPFNode = sppfNode;
        }

        for (RSMTerminalEdge rsmEdge : state.outgoingTerminalEdges) {
            pos.node.getRelationships(Direction.OUTGOING, RelationshipType.withName(rsmEdge.terminal.value)).stream().forEach(relationship -> {
                Neo4jNode head = new Neo4jNode(relationship.getEndNode());
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

    public void pop(GSSNode gssNode, SPPFNode sppfNode, Neo4jNode pos) {
        if (!poppedGSSNodes.containsKey(gssNode)) {
            poppedGSSNodes.put(gssNode, new HashSet<>());
        }
        poppedGSSNodes.get(gssNode).add(sppfNode);
        for (Map.Entry<GSSNode.Pair, HashSet<GSSNode>> e : gssNode.edges.entrySet()) {
            for (GSSNode u : e.getValue()) {
                queue.add(
                    e.getKey().rsmState,
                    u,
                    getNodeP(
                        e.getKey().rsmState,
                        e.getKey().sppfNode,
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
        Neo4jNode pos
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
        Neo4jNode leftExtent = (null == sppfNode) || (null == sppfNode.leftExtent)
            ? nextSPPFNode.leftExtent
            : sppfNode.leftExtent;

        Neo4jNode rightExtent = nextSPPFNode.rightExtent;

        ParentSPPFNode y = state.isFinal
            ? getOrCreateSymbolSPPFNode(state.nonterminal, leftExtent, rightExtent)
            : getOrCreateItemSPPFNode(state, leftExtent, rightExtent);

        y.kids.add(
            getOrCreatePackedSPPFNode(
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
        Neo4jNode leftExtent,
        Neo4jNode rightExtent
    ) {
        TerminalSPPFNode y = new TerminalSPPFNode(leftExtent, rightExtent, terminal);
        if (!createdSPPFNodes.containsKey(y)) {
            createdSPPFNodes.put(y, y);
        }
        return createdSPPFNodes.get(y);
    }

    public ParentSPPFNode getOrCreateItemSPPFNode(
        RSMState state,
        Neo4jNode leftExtent,
        Neo4jNode rightExtent
    ) {
        ItemSPPFNode y = new ItemSPPFNode(leftExtent, rightExtent, state);
        if (!createdSPPFNodes.containsKey(y)) {
            createdSPPFNodes.put(y, y);
        }
        return (ParentSPPFNode) createdSPPFNodes.get(y);
    }

    public PackedSPPFNode getOrCreatePackedSPPFNode(
        Neo4jNode pivot,
        RSMState rsmState,
        SPPFNode leftSPPFNode,
        SPPFNode rightSPPFNode
    ) {
        PackedSPPFNode y = new PackedSPPFNode(pivot, rsmState, leftSPPFNode, rightSPPFNode);
        if (!createdPackedSPPFNodes.containsKey(y)) {
            createdPackedSPPFNodes.put(y, y);
        }
        return createdPackedSPPFNodes.get(y);
    }

    public SymbolSPPFNode getOrCreateSymbolSPPFNode(
        Nonterminal nonterminal,
        Neo4jNode leftExtent,
        Neo4jNode rightExtent
    ) {
        SymbolSPPFNode y = new SymbolSPPFNode(leftExtent, rightExtent, nonterminal);
        if (!createdSPPFNodes.containsKey(y)) {
            createdSPPFNodes.put(y, y);
        }
        SymbolSPPFNode result = (SymbolSPPFNode) createdSPPFNodes.get(y);
        if (
            nonterminal == startState.nonterminal &&
                startGraphNodes.contains(leftExtent.node)
        ) {
            if (!parseResult.containsKey(leftExtent.node)) {
                parseResult.put(leftExtent.node, new HashMap<>());
            }
            parseResult.get(leftExtent.node).put(rightExtent.node, result);
        }
        return result;
    }
}
