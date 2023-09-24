package cfpq.gll;


import cfpq.gll.rsm.RSMBuilder;
import cfpq.gll.rsm.RSMState;
import cfpq.gll.withsppf.GLL;
import cfpq.gll.withsppf.sppf.SPPFNode;
import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class GetPaths {
    @Procedure(name = "cfpq.gll.getPaths")
    @Description("Get reachability pairs in graph by cfg.")
    public Stream<ReachabilityPair> getPaths(
        @Name("nodes") List<Node> nodes,
        @Name("grammar") String grammar
    ) {
        RSMState rsm = new RSMBuilder().createRSM(grammar);
        GLL parser = new GLL(rsm, nodes);

        HashMap<Node, HashMap<Node, SPPFNode>> result = parser.parse();

        Stream.Builder<ReachabilityPair> results = Stream.builder();

        result.forEach((tail, value) -> value.keySet().forEach(head -> results.add(new ReachabilityPair(tail, head))));

        return results.build();
    }

    public static class ReachabilityPair {
        public Node first;
        public Node second;

        public ReachabilityPair(Node first, Node second) {
            this.first = first;
            this.second = second;
        }
    }
}
