package cfpq.gll.benchmarks;

import cfpq.gll.GetReachabilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetReachabilitiesRSMBenchmarksRPQ {

    private Driver driver;
    private Neo4j embeddedDatabaseServer;

    private static Stream<Arguments> provideBenchmarks() {
        // <GRAPH_NAME> <COLD_TRIES> <REAL_TRIES>
        return Stream.of(
            Arguments.of("core", 1, 1),
            Arguments.of("pathways", 1, 1),
            Arguments.of("enzyme", 1, 1),
            Arguments.of("eclass", 1, 1),
            Arguments.of("go", 1, 1),
            Arguments.of("geospecies", 1, 1),
            Arguments.of("taxonomy", 1, 1)
        );
    }

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
            .withDisabledServer()
            .withProcedure(GetReachabilities.class)
            .withConfig(GraphDatabaseSettings.memory_transaction_global_max_size, Long.parseLong("0"))
            .withConfig(GraphDatabaseSettings.log_queries, GraphDatabaseSettings.LogQueryLevel.OFF)
            .build();

        this.driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());
    }

    @AfterAll
    void closeDriver() {
        this.driver.close();
        this.embeddedDatabaseServer.close();
    }

    @AfterEach
    void cleanDb() {
        try (Session session = driver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
        }
    }

    @ParameterizedTest
    @MethodSource("provideBenchmarks")
    public void runTest(
        String graph,
        int coldTries,
        int realTries
    ) throws IOException {
        final String reg1 = Files.readString(
            Paths.get("src", "test", "resources", "cfpq", "gll", "benchmarks", "grammar", "rsm", graph, "reg1.txt")
        ).replace("\n", ";");
        final String reg2 = Files.readString(
            Paths.get("src", "test", "resources", "cfpq", "gll", "benchmarks", "grammar", "rsm", graph, "reg2.txt")
        ).replace("\n", ";");
        final String reg3 = Files.readString(
            Paths.get("src", "test", "resources", "cfpq", "gll", "benchmarks", "grammar", "rsm", graph, "reg3.txt")
        ).replace("\n", ";");
        final String reg4 = Files.readString(
            Paths.get("src", "test", "resources", "cfpq", "gll", "benchmarks", "grammar", "rsm", graph, "reg4.txt")
        ).replace("\n", ";");

        try (Session session = driver.session()) {
            session.run(
                "CREATE INDEX node_id_index IF NOT EXISTS"
                    + " FOR (n:Node)"
                    + " ON (n.id)"
            );

            String nodesPath =
                Paths.get("src", "test", "resources", "cfpq", "gll", "benchmarks", "graph", graph, "nodes.csv").toFile().getAbsolutePath();
            session.run(
                String.format("LOAD CSV WITH HEADERS FROM 'file:///%s' as row", nodesPath)
                    + " CREATE (:Node {id:toInteger(row.NodeID)})"
            );
            System.out.println("NODES LOADED");

            for (File file : Paths.get("src", "test", "resources", "cfpq", "gll", "benchmarks", "graph", graph).toFile().listFiles()) {
                String name = file.getName();
                String path = file.getAbsolutePath();
                String label = name.substring(0, name.length() - 4);
                if (name.equals("nodes.csv")) {
                    continue;
                }
                session.run(
                    String.format("LOAD CSV WITH HEADERS FROM 'file:///%s' as row", path)
                        + " MATCH (u:Node {id:toInteger(row.From)})"
                        + " MATCH (v:Node {id:toInteger(row.To)})"
                        + String.format(" MERGE (u)-[:%s]->(v)", label)
                        + String.format(" MERGE (v)-[:%s_r]->(u)", label)
                );
            }
            System.out.println("EDGES LOADED");

            ArrayList<Integer> selectedNodes = new ArrayList<>();
            try (Scanner scanner = new Scanner(
                Paths.get("src", "test", "resources", "cfpq", "gll", "benchmarks", "selected_nodes", graph + ".csv")
            )) {
                while (scanner.hasNextLine()) {
                    selectedNodes.add(Integer.parseInt(scanner.nextLine()));
                }
            }
            int selectedNodesSize = selectedNodes.size();
            System.out.println("SELECTED " + selectedNodesSize + " NODES");

            for (String grammarName : new String[]{"reg1", "reg2", "reg3", "reg4"}) {
                System.out.println(grammarName + " QUERY STARTED");

                String grammar = reg1;
                if (grammarName.equals("reg2")) {
                    grammar = reg2;
                }
                if (grammarName.equals("reg3")) {
                    grammar = reg3;
                }
                if (grammarName.equals("reg4")) {
                    grammar = reg4;
                }

                File resultsDir = Paths.get("src", "test", "resources", "cfpq", "gll", "benchmarks", "results", "rsm_rpq", graph).toFile();
                resultsDir.mkdirs();

                for (int chunkSize : new int[]{1, 10, 100}) {
                    PrintWriter fileWriter = new PrintWriter(new FileWriter(
                        resultsDir.getAbsolutePath() + String.format("/%s_%s.csv", grammarName, chunkSize),
                        true
                    ));

                    for (int chunkStart = 0; chunkStart < selectedNodesSize; chunkStart += chunkSize) {
                        int chunkEnd = Math.min(selectedNodesSize, chunkStart + chunkSize);

                        StringBuilder matchNodes = new StringBuilder("MATCH ");
                        StringBuilder callNodes = new StringBuilder();
                        for (int nodeId = chunkStart; nodeId < chunkEnd; ++nodeId) {
                            if (nodeId != chunkStart) {
                                matchNodes.append(",");
                                callNodes.append(",");
                            }
                            matchNodes.append(
                                String.format("(n%s:Node{id:%s})", nodeId, selectedNodes.get(nodeId))
                            );
                            callNodes.append(
                                String.format("n%s", nodeId)
                            );
                        }

                        String query = matchNodes
                            + String.format(" CALL cfpq.gll.getReachabilities([%s], '%s')", callNodes, grammar)
                            + " YIELD first, second"
                            + " RETURN first, second";

                        for (int test = 0; test < coldTries; ++test) {  // COLD TRIES
                            List<Record> results = session.run(query).list();
                            assertThat(results.size()).isNotNull();
                        }

                        for (int test = 0; test < realTries; ++test) {  // REAL TRIES
                            long startTime = System.nanoTime();
                            List<Record> results = session.run(query).list();
                            long endTime = System.nanoTime();

                            assertThat(results.size()).isNotNull();

                            fileWriter.println(endTime - startTime);
                        }
                    }

                    fileWriter.close();

                    System.out.println("CHUNKS OF SIZE " + chunkSize + " COMPLETED");
                }
                System.out.println(grammarName + " QUERY COMPLETED");
            }
        }
    }
}
