# Semantic storage

Semantic storage is implemented and deployed as semantic triplestore implementing
well known [RDF4J framework](http://rdf4j.org/).
RDF4J framework (former known as Sesame) is a powerful Java framework
for processing and handling RDF data. This includes creating, parsing, scalable storage,
reasoning and querying with RDF and Linked Data.
It offers an easy-to-use API that can be connected to all leading RDF database solutions.

RDF4J API serves also as definition of standard interface for managing RDF data.
This means, the different semantic databases - the triplestores - use to implement this API.
This means, the underlying semantic database technology can be easily replaced
without changing the code. The only requirement is, that semantic database
implements the RDF4J API.

In VICINITY, the [GraphDB](http://graphdb.ontotext.com/documentation/free/)
semantic database is currently used.