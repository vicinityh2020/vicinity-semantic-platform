# Semantic services

Semantic platform is implemented as the standalone REST server, implementing
several semantic services. Lets go one by one.

## Populating the IoT objects repository

Semantic repository serves as the only storage of semantically enriched IoT object
descriptions. This semantic representation is used to fire the semantic queries to
lookup for IoT objects (e.g. get me all sensors monitoring the temperature).

The population of semantic repository is the part of Agent Services
discovery process. Once the Adapter provides the list of IoT objects it exposes,
each new IoT object is stored in semantic triplestore.

IoT object descriptions comes as JSON object in VICINITY Common Thing Description format.
This JSON object has to be translated into semantic triples, which will be inserted
into triplestore.

For this transformation we use the JSON-LD document, where the mappings between
Thing Description JSON and related ontology properties are specified. Once the
triples are generated, they are stored in the triplestore in separate named graph.
Using separate named graph enables to retrieve whole bunch of triples representing the IoT object by the single query.
This triples are loaded into the graph, which enables to easily access
all parts of semantic representation.

### Validation

The part of IoT object population is the strong validation, consisting of:
* Syntactic validation of JSON structure. The same validator is used as in Agent Service.
* Semantic validation of annotations. All annotations used are checked.

If validation fails, it responds with the list of all errors in human readable form, so
developers may adjust their thing description content.

### Serialization

IoT objects are transformed back to JSON representation in VICINITY Common Thing Description.
It is used each time any component requires the description ot IoT object. The object
is found in semantic model, and translated from triples into JSON.

Serialization into JSON is performed as the sequence of graph operations on semantic
model, where each part of relevant subgraphs are programatically translated into
JSON construct. This transformation, unfortunately, can not be fully automatic
(in declarative way), because there is couple of logical rules for constructing
some parts of JSON objects.


### Population API

#### Create:

Brand new semantically enriched individual representing the IoT object is created.

```
POST: /td/create
```

payload is the the JSON object containing the Thing Description in VICINITY Common
Thing Description format.

Response is the created IoT object translated from its semantic model back into
JSON in VICINITY Common Thing Description format

#### Update:

The whole content for individual representing the IoT object is relpaced by new one.

```
PUT: /td/create
```

payload is the the JSON object containing the Thing Description in VICINITY Common
Thing Description format.

Response is the created IoT object translated from its semantic model back into
JSON in VICINITY Common Thing Description format


#### Delete:

The individual representing the IoT object is removed from semantic model.

```
DELETE: /td/remove/{oid}
```

## SPARQL Query interface

Each semantic repository must have this one, no useless comments to it.
SPARQL query interface:
* consumes the query in [W3C SPARQL Query format](https://www.w3.org/TR/rdf-sparql-query/).
* produces the result in [W3C SPARQL JSON Result format](https://www.w3.org/TR/rdf-sparql-json-res/).

Available at:

```
POST: /sparql
```

Payload is always the object:
```
{
    "query": "your SPARQL query goes here"
}
```


## Semantic annotations

This service is used by Neighbourhood manager to perform very basic semantic
validation, if needed. This service returns the structured lists of
 all semantic annotations used in IoT thing descriptions.

The endpoint:

```
GET: /annotations
```

The response example:

```
{
	"data": {
		"service": ["core:Application", "core:Service"],
		"device": ["adapters:HumiditySensor", "adapters:WeightScale", "core:Device"]
		"property": ["adapters:Motion", "adapters:OnOff"]
	},
	"status": "success"
}
```

## Semantic validation

The part of tools for developers. When developers create the thing descriptions
in configuration of their Adapters, they are free to completely validate in advance
this thing descriptions in syntactic (the structure is checked)
and also semantic way (the annotations are checked). See [validation](#validation).

...to be implemented