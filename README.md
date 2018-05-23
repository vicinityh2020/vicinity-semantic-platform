# Semantic discovery platform

The purpose of this component is to enable the semantic interoperability within the VICINITY.
Semantic discovery platform contains the repository of IoT object descriptions,
semantically enhanced using VICINITY ontology (output of Task T2.2).
Semantic platform is the only storage component, where IoT object descriptions
are available.

Semantic discovery platform consists of:
* [semantic storage](docs/STORAGE.md)
* [semantic (discovery, configuration) services](docs/SERVICES.md)

## Deployment

The semantic discovery platform requires to run the semantic storage as
standalone server. Currently we are using GraphDB Free 8.4.1.

Semantic platform itself is the standalone REST server. It comes with the following
directory structure:

```
config/
    json-ld/
        thing.jsonld
logs/
semantic-repository.sh
```

* **config/json-ld/thing.jsonld** is the JSON-LD document used to automatically
translate IoT object description formalized in JSON (see documentation on VICINITY Agent)
into semantic representation. See [semantic services](docs/SERVICES.md) for more information on this transformation.

* **semantic-repository.sh** - guess what!

To run it, just execute script:

```
#!shell
./semantic-repository.sh
```

Semantic repository services are available at endpoint:

```
http://host:PORT/semantic-repository/
```

## Configuration

Feel free to edit the following properties in *semantic-repository.sh*:

```
SERVER_PORT=9004
GRAPHDB_ENDPOINT=http://localhost:9003/repositories/vicinity-test
JSONLD_SCHEMA_LOCATION=file:///full-path-to/thing.jsonld
```

