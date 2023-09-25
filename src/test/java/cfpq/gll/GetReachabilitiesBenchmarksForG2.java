package cfpq.gll;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetReachabilitiesBenchmarksForG2 {

    private Driver driver;
    private Neo4j embeddedDatabaseServer;

    private static Stream<Arguments> provideBenchmarks() {
        // <GRAPH_NAME> <NUMBER_OF_NODES> <EXPECTED_ANSWER> <COLD_TRIES> <REAL_TRIES>
        return Stream.of(
            Arguments.of("skos", 144, 1, 3, 20),
            Arguments.of("generations", 129, 0, 3, 20),
            Arguments.of("travel", 131, 36, 3, 20),
            Arguments.of("univ", 179, 36, 3, 20),
            Arguments.of("atom", 291, 122, 3, 20),
            Arguments.of("biomedical", 341, 122, 3, 20),
            Arguments.of("foaf", 256, 10, 3, 20),
            Arguments.of("people", 337, 35, 3, 20),
            Arguments.of("funding", 778, 91, 3, 20),
            Arguments.of("wine", 733, 178, 3, 20),
            Arguments.of("pizza", 671, 435, 3, 20),
            Arguments.of("core", 1323, 214, 3, 20),
            Arguments.of("pathways", 6238, 3117, 3, 20),
            Arguments.of("enzyme", 48815, 8163, 3, 20),
            Arguments.of("eclass", 239111, 96163, 1, 2),
            Arguments.of("go_hierarchy", 45007, 738937, 1, 2),
            Arguments.of("go", 582929, 659501, 1, 2),
            Arguments.of("taxonomy", 5728398, 2112637, 1, 2)
        );
    }

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
            .withDisabledServer()
            .withProcedure(GetReachabilities.class)
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
        int numberOfNodes,
        int expectedResultsSize,
        int coldTries,
        int realTries
    ) throws IOException {
        Path grammarPath = Paths.get("src", "test", "resources", "grammar/g2.txt");
        final String grammar = Files.readString(grammarPath).replace("\n", ";");

        try (Session session = driver.session()) {
            session.run(
                "CREATE INDEX node_id_index IF NOT EXISTS"
                    + " FOR (n:Node)"
                    + " ON (n.id)"
            );

            for (int nodeId = 0; nodeId < numberOfNodes; ++nodeId) {
                session.run(
                    String.format("CREATE (:Node {id:toInteger(%s)})", nodeId)
                );
            }

            for (File file : Paths.get("src", "test", "resources", "graph", graph).toFile().listFiles()) {
                String name = file.getName();
                String path = file.getAbsolutePath();
                String label = name.substring(0, name.length() - 4);
                session.run(
                    String.format("LOAD CSV WITH HEADERS FROM 'file:///%s' as row", path)
                        + " MATCH (u:Node {id:toInteger(row.From)})"
                        + " MATCH (v:Node {id:toInteger(row.To)})"
                        + String.format(" MERGE (u)-[:%s]->(v)", label)
                        + String.format(" MERGE (v)-[:%s_r]->(u)", label)
                );
            }

            File resultsDir = Paths.get("src", "test", "resources", "results", "getReachabilities", graph).toFile();
            resultsDir.mkdirs();

            var fileWriter = new PrintWriter(new FileWriter(resultsDir.getAbsolutePath() + "/g2.txt", true));

            for (var test = 0; test < coldTries; ++test) {  // COLD TRIES
                long startTime = System.nanoTime();
                List<Record> results = session.run(
                    "MATCH (u:Node)"
                        + String.format(" CALL cfpq.gll.getReachabilities([u], '%s')", grammar)
                        + " YIELD first, second"
                        + " RETURN first, second"
                ).list();
                long endTime = System.nanoTime();

                assertThat(results.size()).isEqualTo(expectedResultsSize);
            }

            for (var test = 0; test < realTries; ++test) {  // REAL TRIES
                long startTime = System.nanoTime();
                List<Record> results = session.run(
                    "MATCH (u:Node)"
                        + String.format(" CALL cfpq.gll.getReachabilities([u], '%s')", grammar)
                        + " YIELD first, second"
                        + " RETURN first, second"
                ).list();
                long endTime = System.nanoTime();

                assertThat(results.size()).isEqualTo(expectedResultsSize);

                fileWriter.println(endTime - startTime);
            }

            fileWriter.close();
        }
    }
}
