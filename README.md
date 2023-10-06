
<h1 align="center"> CFPQ GLL Neo4j Stored Procedure</h1>

<div align="center">

![Java CI with Maven](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/actions/workflows/build.yml/badge.svg) 

</div>
  
# About
This is the implementation of the GLL-based context-free path querying (CFPQ) algorithm. The proposed algorithm solves both the reachability-only and the all-paths problems for the all-pairs and the multiple sources cases. It handles queries in Extended Backus-Naur Form (EBNF) using Recursive State Machines (RSM)

# Usage
## Build

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

```shell
mvn clean package
```

This will produce a jar-file,`target/cfpq-gll-neo4j-procedure-1.0.0.jar`,
that can be deployed in the `plugin` directory of your Neo4j instance.

## Stored procedure usage
Once the code is compiled, the JAR file needs to be put into the plugin's directory of Neo4j root folder.
To call the stored procedure the following Cypher query can be used, 
```
CALL cfpq.gll.getReachabilities(nodes, rsm)
```
where `nodes` is a collection of start nodes, and `rsm` is a string representation of RSM specified over relations types.

## Example
### Create graph using Cypher

```cypher
CREATE (n1:Node{id:1})
CREATE (n2:Node{id:2})
CREATE (n3:Node{id:3})
CREATE (n4:Node{id:4})

CREATE (n1)-[:a]->(n2)
CREATE (n2)-[:a]->(n3)
CREATE (n3)-[:a]->(n1)

CREATE (n3)-[:b]->(n4)
CREATE (n4)-[:b]->(n3)
```

### Run the query

```cypher
WITH
    'StartState(id=0,nonterminal=Nonterminal(S),isStart=true,isFinal=true);' +
    'TerminalEdge(tail=0,head=0,terminal=Terminal(a));' +
    'TerminalEdge(tail=0,head=0,terminal=Terminal(b));' as rsm
MATCH (n1:Node{id:1})
CALL cfpq.gll.getReachabilities([n1], rsm)
YIELD first, second
RETURN first.id, second.id
```

### Result

| first | second |
|-------|--------|
| 1     | 1      |
| 1     | 2      |
| 1     | 3      |
| 1     | 4      |

# Performance
The evaluation of real-world graphs demonstrates that the utilization of RSMs increases the performance of query evaluation. Being implemented as a stored procedure for Neo4j, our solution demonstrates better performance than a similar solution for RedisGraph. The performance of the solution of regular path queries is comparable with the performance of the native Neo4j solution, and in some cases, it requires significantly less memory.

**Machine configuration**: PC with Ubuntu 20.04, Intel Core i7-6700 3.40GHz CPU, DDR4 64Gb RAM.

**Environment configuration**: 
* OpenJDK 64-Bit Server VM Corretto-17.0.8.8.1 (build 17.0.8.1+8-LTS, mixed mode, sharing).
* JVM heap configuration: 55Gb both xms and xmx.
* Neo4j 5.0.12 is used. Almost all configurations are default except one:
     * memory_transaction_global_max_size parameter is set to 0, which means unlimited memory usage per transaction.
 
## Graphs

The graph data is selected from [CFPQ_Data dataset](https://github.com/JetBrains-Research/CFPQ_Data). Graphs related to RDF analysis problems were taken.

A detailed description of the graphs is listed below.

**RDF analysis** 

| Graph name   |   \|*V*\| |     \|*E*\| |  #subClassOf |      #type |  #broaderTransitive |
|:------------:|----------:|------------:|-------------:|-----------:|--------------------:|
| Core         |     1 323 |       2 752 |          178 |          0 |                   0 |
| Pathways     |     6 238 |      12 363 |        3 117 |      3 118 |                   0 |
| Go hierarchy |    45 007 |     490 109 |      490 109 |          0 |                   0 |
| Enzyme       |    48 815 |      86 543 |        8 163 |     14 989 |               8 156 | 
| Eclass_514en |   239 111 |     360 248 |       90 962 |     72 517 |                   0 | 
| Geospecies   |   450 609 |   2 201 532 |            0 |     89 065 |              20 867 | 
| Go           |   582 929 |   1 437 437 |       94 514 |    226 481 |                   0 | 
| Taxonomy     | 5 728 398 |  14 922 125 |    2 112 637 |  2 508 635 |                   0 |

**Regular queries**
Regular queries were generated using a well-established set of templates for RPQ algorithms evaluation. Four nontrivial templates (that contain compositions of Kleene star and union) that are expressible in Cypher syntax to be able to compare native Neo4j querying algorithm with this solution were chosen.

Used templates are presented below. 
```
reg1  = (𝑎 | 𝑏)*
```
```
reg2  = 𝑎* 𝑏*
```

```
reg3  = (𝑎 | 𝑏 | 𝑐)+
```

```
reg4  = (𝑎 | 𝑏)+ (𝑐 | 𝑑)+
```

Respective path patterns expressed in Cypher are presented below.

```
reg1 = ()-[:a| :b]->{0,}()
```

```
reg2 = ()-[:a]->{0,}()-[:b]->{0,}()
```

```
reg3 = ()-[:a | :b | :c]->{1,}()
```

```
reg4 = ()-[:a | :b]->{1,}()-[:c | :d]->{1,}()
```

**Context-Free Queries**
All queries used in the evaluation are variants of same-generation query. The inverse of an ```x``` relation and the respective edge is denoted as ```x_r```.

<br/>

Grammars used for **RDF** graphs:

**G<sub>1</sub>**
```
S -> subClassOf_r S subClassOf | subClassOf_r subClassOf 
     | type_r S type | type_r type
```
**G<sub>2</sub>**
```
S -> subClassOf_r S subClassOf | subClassOf
```

  **Geo**
```
S -> broaderTransitive S broaderTransitive_r
     | broaderTransitive broaderTransitive_r 
```

<br/>

Respective RSMs are presented below.

<img src="https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/assets/31728695/e7f23dd4-8bd4-45cf-9cc6-0d3a0b3d2fed" width="400">
<img src="https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/assets/31728695/a04a7eb7-f42e-4b72-aa94-d884a247a143" width="500">
<img src="https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/assets/31728695/83371945-ebd7-4c01-b102-66456abe857a" width="500">

### Evaluation

The starting sets for the multiple sources querying are generated from all vertices of a graph with a random permutation. Chunks of size 1, 10, and 100 were used.

Results for queries G<sub>1</sub>, G<sub>2</sub> and Geo are presented below respectively. The performance results are compared with matrix-based CFPQ algorithm implemented in RedisGraph by Arseniy Terekhov et al in [paper](https://dblp.org/rec/conf/edbt/TerekhovPAZG21).
![g1_result-1](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/assets/31728695/510fae3f-b503-49c0-9f96-f2c65ac2bd1d)
<p align="center">
<b>Multiple sources CFPQ reachability results for queries related to RDF analysis and G<sub>1</sub></b>
</p>

![g2_result-1](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/assets/31728695/d25e5d06-f6fc-443c-8117-642f516b5e5a)
<p align="center">
<b>Multiple sources CFPQ reachability results for queries related to RDF analysis and G<sub>2</sub></b>
</p>

![geo_result-1](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/assets/31728695/9739552f-bbe0-46b0-9126-a3ce1f339774)
<p align="center">
<b>Multiple sources CFPQ reachability results for queries related to RDF analysis and Geo</sub></b>
</p>

---

Results for queries reg1, reg2, reg3 and reg4 are presented below respectively. The performance results were compared with native Neo4j solution.
![reg1_rpq_result-1](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/assets/31728695/5b01e9b6-efb0-44c5-9454-5ae64b9a7b3d)
<p align="center">
<b>Multiple sources RPQ reachability results for queries related to RDF analysis and reg1 (native solution failed with OOM on last two graphs)</b>
</p>

![reg2_rpq_result-1](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/assets/31728695/a65692de-5e07-4294-8bd1-5356e6aaa265)
<p align="center">
<b>Multiple sources RPQ reachability results for queries related to RDF analysis and reg2 (native solution failed with OOM on last two graphs)</sub></b>
</p>

![reg3_rpq_result-1](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/assets/31728695/3b753a8a-1cc0-4804-a830-e843d1ee384b)
<p align="center">
<b>Multiple sources RPQ reachability results for queries related to RDF analysis and reg3 (native solution failed with OOM on last two graphs)</b>
</p>

![reg4_rpq_result-1](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/assets/31728695/9962fafa-572e-4c69-b7f3-aa9b3c9416fb)
<p align="center">
<b>Multiple sources RPQ reachability results for queries related to RDF analysis and reg4 (native solution failed with OOM on last two graphs)</b>
</p>

## License 
This project is licensed under Apache License 2.0. License text can be found in the 
[license file](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/blob/main/LICENSE)
