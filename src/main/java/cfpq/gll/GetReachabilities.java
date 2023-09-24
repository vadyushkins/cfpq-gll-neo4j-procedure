package cfpq.gll;

import cfpq.gll.rsm.RSMBuilder;
import cfpq.gll.rsm.RSMState;
import cfpq.gll.withoutsppf.GLL;
import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

public class GetReachabilities {

    @Procedure(name = "cfpq.gll.getReachabilities")
    @Description("Get reachability pairs in graph by cfg.")
    public Stream<GetReachabilities.ReachabilityPair> getReachabilities(
        @Name("nodes") List<Node> nodes,
        @Name("grammar") String grammar
    ) {
        RSMState rsm = new RSMBuilder().createRSM(grammar);
        GLL parser = new GLL(rsm, nodes);

        HashMap<Node, HashSet<Node>> result = parser.parse();

        Stream.Builder<GetReachabilities.ReachabilityPair> results = Stream.builder();

        result.forEach((tail, value) -> value.forEach(head -> results.add(new GetReachabilities.ReachabilityPair(tail,
            head))));

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
