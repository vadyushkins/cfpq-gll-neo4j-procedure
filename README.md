
<h1 align="center"> CFPQ GLL Neo4j Stored Procedure</h1>
  
# About
This is the implementation of the GLL-based context-free path querying (CFPQ) algorithm. The proposed algorithm solves both the reachability-only and the all-paths problems for the all-pairs and the multiple sources cases. It handles queries in Extended Backus-Naur Form (EBNF) using Recursive State Machines (RSM)

# Usage
## Build

## Stored procedure usage
Once code is compiled, the JAR file is needed to be put to plugins directory of Neo4j root folder. To call the stored procedure the following Cypher query can be used:
```
CALL cfpq.gll.getReachabilities(nodes, q)
```
# Performance
The evaluation on real-world graphs demonstrates that utilization of RSMs increases performance of query evaluation. Being implemented as a stored procedure for Neo4j, our solution demonstrates better performance than a similar solution for RedisGraph. Performance of the solution of regular path queries is comparable with performance of native Neo4j solution, and in some cases it requires significantly less memory.

**Machine configuration**: PC with Ubuntu 20.04, Intel Core i7-6700 3.40GHz CPU, DDR4 64Gb RAM.

**Enviroment configuration**: 
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
Regular queries were generated using well-established set of templates for RPQ algorithms evaluation. Four nontrivial templates (that contains compositions of Kleene star and union) that expressible in Cypher syntax to be able to compare native Neo4j querying algorithm with this solution were chosen.

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
All queries used in evaluation are variants of same-generation query. The inverse of an ```x``` relation and the respective edge is denoted as ```x_r```.

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

Respective RSM-s are presented below.

<img src="https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/assets/31728695/e7f23dd4-8bd4-45cf-9cc6-0d3a0b3d2fed" width="400">
<img src="https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/assets/31728695/a04a7eb7-f42e-4b72-aa94-d884a247a143" width="500">
<img src="https://github.com/vadyushkins/cfpq-gll-neo4j-procedure/assets/31728695/83371945-ebd7-4c01-b102-66456abe857a" width="500">
