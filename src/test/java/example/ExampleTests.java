package example;

import org.junit.jupiter.api.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExampleTests {

    private Driver driver;
    private Neo4j embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
            .withDisabledServer()
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
