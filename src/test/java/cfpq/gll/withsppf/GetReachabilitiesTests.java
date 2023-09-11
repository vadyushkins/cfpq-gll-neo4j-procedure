package cfpq.gll.withsppf;

import org.junit.jupiter.api.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetReachabilitiesTests {

    private Driver driver;
    private Neo4j embeddedDatabaseServer;

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

    @Test
    public void test1() {
        final String grammar =
                "StartState(id=0,nonterminal=Nonterminal(S),isStart=true,isFinal=false);" +
                        "State(id=1,nonterminal=Nonterminal(S),isStart=false,isFinal=true);" +
                        "TerminalEdge(tail=0,head=1,terminal=Terminal(a));";

        try (Session session = driver.session()) {
            session.run("CREATE (:Person {id:0})-[:a]->(:Movie {id:1})");

            Record record = session.run(
                    "MATCH (u:Person {id:0}) " +
                            String.format("CALL cfpq.gll.withsppf.getReachabilities([u], '%s')", grammar) +
                            "YIELD first, second " +
                            "RETURN first, second").single();

            assertThat(record.get("first").get("id").asInt()).isEqualTo(0);
            assertThat(record.get("second").get("id").asInt()).isEqualTo(1);
        }
    }
}
