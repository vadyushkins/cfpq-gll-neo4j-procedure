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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetReachabilitiesTests {

    private Driver driver;
    private Neo4j embeddedDatabaseServer;

    private static Stream<Arguments> provideTests() {
        return Stream.of(
            Arguments.of("skos", 144, 30),
            Arguments.of("generations", 129, 12),
            Arguments.of("travel", 131, 52),
            Arguments.of("univ", 179, 25),
            Arguments.of("atom", 291, 6),
            Arguments.of("biomedical", 341, 47),
            Arguments.of("foaf", 256, 36),
            Arguments.of("people", 337, 51),
            Arguments.of("funding", 778, 58),
            Arguments.of("wine", 733, 565),
            Arguments.of("pizza", 671, 1356),
            Arguments.of("core", 1323, 204),
            Arguments.of("pathways", 6238, 884),
            Arguments.of("enzyme", 48815, 396)
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
    @MethodSource("provideTests")
    public void runTest(String graph, int numberOfNodes, int expectedResultsSize) throws IOException {
        Path grammarPath = Paths.get("src", "test", "resources", "grammar/g1.txt");
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

            List<Record> results = session.run(
                "MATCH (u:Node)"
                    + String.format(" CALL cfpq.gll.getReachabilities([u], '%s')", grammar)
                    + " YIELD first, second"
                    + " RETURN first, second"
            ).list();

            assertThat(results.size()).isEqualTo(expectedResultsSize);
        }
    }
}
