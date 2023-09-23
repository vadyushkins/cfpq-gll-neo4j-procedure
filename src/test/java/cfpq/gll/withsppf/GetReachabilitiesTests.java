package cfpq.gll.withsppf;

import org.junit.jupiter.api.*;
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

    private static Stream<Arguments> provideBenchmarks() {
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
    @MethodSource("provideBenchmarks")
    public void runBenchmark(String graph, int numberOfNodes, int expectedResultsSize) throws IOException {
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
                    + String.format(" CALL cfpq.gll.withsppf.getReachabilities([u], '%s')", grammar)
                    + " YIELD first, second"
                    + " RETURN first, second"
            ).list();

            assertThat(results.size()).isEqualTo(expectedResultsSize);
        }
    }

    @Test
    public void testWithIndex() throws IOException {
        Path grammarPath = Paths.get("src", "test", "resources", "grammar/g1.txt");
        final String grammar = Files.readString(grammarPath).replace("\n", ";");

        try (Session session = driver.session()) {
            session.run(
                "CREATE INDEX node_id_index IF NOT EXISTS"
                    + " FOR (n:Node)"
                    + " ON (n.id)"
            );

            for (int nodeId = 0; nodeId < 3; ++nodeId) {
                session.run(
                    String.format("CREATE (:Node {id:toInteger(%s)})", nodeId)
                );
            }

            for (File file : Paths.get("src", "test", "resources", "example/").toFile().listFiles()) {
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
                "MATCH (u:Node {id:0})"
                    + String.format(" CALL cfpq.gll.withsppf.getReachabilities([u], '%s')", grammar)
                    + " YIELD first, second"
                    + " RETURN first, second"
            ).list();

            assertThat(results.get(0).get("first").get("id").asInt()).isEqualTo(0);
            assertThat(results.get(0).get("second").get("id").asInt()).isEqualTo(0);

            assertThat(results.get(1).get("first").get("id").asInt()).isEqualTo(0);
            assertThat(results.get(1).get("second").get("id").asInt()).isEqualTo(2);
        }
    }

    @Test
    public void testManualGraphManualGrammar() {
        final String grammar =
            "StartState(id=0,nonterminal=Nonterminal(S),isStart=true,isFinal=false);" +
                "State(id=1,nonterminal=Nonterminal(S),isStart=false,isFinal=true);" +
                "TerminalEdge(tail=0,head=1,terminal=Terminal(a));";

        try (Session session = driver.session()) {
            String graphPath = Paths.get("src", "test", "resources", "graph/nodes.csv").toFile().getAbsolutePath();
            session.run(
                String.format("LOAD CSV WITH HEADERS FROM 'file:///%s' as row", graphPath)
                    + " MERGE (:Node {id:toInteger(row.From)})-[:a]->(:Node {id:toInteger(row.To)})"
            );

            List<Record> record = session.run(
                "MATCH (u:Node {id:0}) " +
                    String.format("CALL cfpq.gll.withsppf.getReachabilities([u], '%s')", grammar) +
                    "YIELD first, second " +
                    "RETURN first, second").list();

            assertThat(record.get(0).get("first").get("id").asInt()).isEqualTo(0);
            assertThat(record.get(0).get("second").get("id").asInt()).isEqualTo(1);
        }
    }

    @Test
    public void testLoadNodes() {
        try (Session session = driver.session()) {
            String graphPath = Paths.get("src", "test", "resources", "graph/nodes.csv").toFile().getAbsolutePath();
            session.run(
                String.format("LOAD CSV WITH HEADERS FROM 'file:///%s' as row", graphPath)
                    + " MERGE (u:Node {id:toInteger(row.From)})"
                    + " MERGE (v:Node {id:toInteger(row.To)})"
            );

            List<Record> nodes = session.run(
                "MATCH (n:Node)"
                    + " RETURN n"
            ).list();

            assertThat(nodes.get(0).get(0).get("id").asInt()).isEqualTo(0);
            assertThat(nodes.get(1).get(0).get("id").asInt()).isEqualTo(1);
        }
    }

    @Test
    public void testLoadRelationships() {
        try (Session session = driver.session()) {
            String graphPath = Paths.get("src", "test", "resources", "graph/relationships.csv").toFile().getAbsolutePath();
            session.run(
                String.format("LOAD CSV WITH HEADERS FROM 'file:///%s' as row", graphPath)
                    + " MERGE (:Node {id:toInteger(row.From)})-[:a {label:row.Label}]->(:Node {id:toInteger(row.To)})"
            );

            List<Record> relationships = session.run(
                "MATCH (:Node {id: 0})-[r]->()"
                    + " RETURN r"
            ).list();

            assertThat(relationships.get(0).get(0).asRelationship().startNodeId()).isEqualTo(0);
            assertThat(relationships.get(0).get(0).asRelationship().endNodeId()).isEqualTo(1);
            assertThat(relationships.get(0).get(0).asRelationship().type()).isEqualTo("a");
            assertThat(relationships.get(0).get(0).asRelationship().get("label").asString()).isEqualTo("Label");
        }
    }
}
