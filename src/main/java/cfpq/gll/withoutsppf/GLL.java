package cfpq.gll.withoutsppf;

import cfpq.gll.graph.Neo4jNode;
import cfpq.gll.rsm.RSMNonterminalEdge;
import cfpq.gll.rsm.RSMState;
import cfpq.gll.rsm.RSMTerminalEdge;
import cfpq.gll.rsm.symbol.Nonterminal;
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
    public HashMap<GSSNode, HashSet<Neo4jNode>> poppedGSSNodes = new HashMap<>();
    public HashMap<GSSNode, GSSNode> createdGSSNodes = new HashMap<>();
    public HashMap<Node, HashSet<Node>> parseResult = new HashMap<>();

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

    public HashMap<Node, HashSet<Node>> parse() {
        for (Node graphNode : startGraphNodes) {
            Neo4jNode node = new Neo4jNode(graphNode);
            queue.add(
                startState,
                getOrCreateGSSNode(startState.nonterminal, node),
                node
            );
        }

        while (!queue.isEmpty()) {
            DescriptorsQueue.Descriptor descriptor = queue.next();
            parse(descriptor.rsmState, descriptor.pos, descriptor.gssNode);
        }

        return parseResult;
    }

    public void parse(RSMState state, Neo4jNode pos, GSSNode gssNode) {
        for (RSMTerminalEdge rsmEdge : state.outgoingTerminalEdges) {
            pos.node.getRelationships(Direction.OUTGOING, RelationshipType.withName(rsmEdge.terminal.value)).stream().forEach(relationship -> {
                Neo4jNode head = new Neo4jNode(relationship.getEndNode());
                queue.add(
                    rsmEdge.head,
                    gssNode,
                    head
                );
            });
        }

        for (RSMNonterminalEdge rsmEdge : state.outgoingNonterminalEdges) {
            queue.add(
                rsmEdge.nonterminal.startState,
                createGSSNode(rsmEdge.nonterminal, rsmEdge.head, gssNode, pos),
                pos
            );
        }

        if (state.isFinal) {
            pop(gssNode, pos);
        }
    }

    public void pop(GSSNode gssNode, Neo4jNode pos) {
        if (
            gssNode.nonterminal == startState.nonterminal &&
                startGraphNodes.contains(gssNode.pos.node)
        ) {
            if (!parseResult.containsKey(gssNode.pos.node)) {
                parseResult.put(gssNode.pos.node, new HashSet<>());
            }
            parseResult.get(gssNode.pos.node).add(pos.node);
        }

        if (!poppedGSSNodes.containsKey(gssNode)) {
            poppedGSSNodes.put(gssNode, new HashSet<>());
        }
        poppedGSSNodes.get(gssNode).add(pos);

        for (Map.Entry<RSMState, HashSet<GSSNode>> e : gssNode.edges.entrySet()) {
            for (GSSNode u : e.getValue()) {
                queue.add(
                    e.getKey(),
                    u,
                    pos
                );
            }
        }
    }

    public GSSNode createGSSNode(
        Nonterminal nonterminal,
        RSMState state,
        GSSNode gssNode,
        Neo4jNode pos
    ) {
        GSSNode v = getOrCreateGSSNode(nonterminal, pos);

        if (v.addEdge(state, gssNode)) {
            if (poppedGSSNodes.containsKey(v)) {
                for (Neo4jNode z : poppedGSSNodes.get(v)) {
                    queue.add(state, gssNode, z);
                }
            }
        }

        return v;
    }
}
