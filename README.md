
<h1 align="center"> CFPQ GLL Neo4j Stored Procedure</h1>
  
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
Once the code is compiled, the JAR file needs to be put into the plugin's directory of Neo4j root folder. To call the stored procedure the following Cypher query can be used:
```
CALL cfpq.gll.getReachabilities(nodes, q)
```
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
reg1  = (𝑎 | 𝑏)∗
```
```
reg2  = 𝑎∗ 𝑏∗ 
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

**Evaluation**
The starting sets for the multiple sources querying are generated from all vertices of a graph with a random permutation. Chunks of size 1, 10, and 100 were used.

Results for queries G<sub>1</sub>, G<sub>2</sub> and Geo are presented below respectively. 

[g1_result.pdf](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/files/12828215/g1_result.pdf)

[g2_result.pdf](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/files/12828217/g2_result.pdf)

[geo_result.pdf](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/files/12828218/geo_result.pdf)

[reg1_rpq_result.pdf](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/files/12828219/reg1_rpq_result.pdf)

[reg2_rpq_result.pdf](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/files/12828222/reg2_rpq_result.pdf)

[reg3_rpq_result.pdf](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/files/12828223/reg3_rpq_result.pdf)

[reg4_rpq_result.pdf](https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/files/12828224/reg4_rpq_result.pdf)

