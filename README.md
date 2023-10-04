
<h1 align="center"> CFPQ GLL Neo4j Stored Procedure</h1>
  
# About
This is the implementation of the GLL-based context-free path querying (CFPQ) algorithm.  Proposed solution solves both reachability and all paths problems for multiple sources cases.

# Usage
## Build

## Stored procedure usage
Once code is compiled, the JAR file is needed to be put to plugins directory of Neo4j root folder. To call the stored procedure the following Cypher query can be used:
```
CALL cfpq.gll.getReachabilities(nodes, q)
```
