# Transformer Exporter

Example exporter using the [JSON Transformer](https://github.com/erykKul/json-transformer) library for transformation of metadata JSON to OAI ORE JSON format. The transformation has two steps:
- [pre_transformer.json](/transformer-example/src/main/resources/pre_transformer.json): changes the metadata citation block into a more JSON Pointer friendly structure. This transformer uses the [map_metadata_fields.js](/transformer-example/src/main/resources/js/map_metadata_fields.js) JavaScript for the metadata citation block transformation.
- [transformer.json](/transformer-example/src/main/resources/transformer.json): copies the metadata fields from the pre-transformed format into OAI ORE format. This transformer uses the [flatten.js](/transformer-example/src/main/resources/js/flatten.js) JavaScript that (at the end of the transformation) eliminates the empty arrays, replaces arrays having only one element with JSON objects, and filters out the `NULL` values.

The test contains an example transformation from a [test dataset metadata JSON](/transformer-example/src/test/resources/source.json) into a [transformed document](/transformer-example/src/test/resources/result.json). Also, the [pre-transformed intermediary JSON](/transformer-example/src/test/resources/pre_transformed.json) is included in the test resources.

When the built jar is placed in the `dataverse.spi.exporters.directory` directory on the server, this exporter will look for the transformation files in the exporters' directory subfolder having the same name as the jar file (the part before the `.jar`). If the transformation files are not there yet, the exporter will copy them from its resources to that location. You can either place your own transformation files there before deploying the jar, use the default files as provided by this example exporter, or overwrite them later.

All the transformation logic is contained in the transformer JSON files and the JavaScript files as used by those JSON transformers. The documentation on how to write such transformers can be found on the [JSON Transformer](https://github.com/erykKul/json-transformer?tab=readme-ov-file#json-transformer) library GitHub page.

Suppose now that this example does not work for your installation as intended because your installation does not have the persistent identifiers for individual files, i.e., it has only persistent identifiers on the dataset level. In this case, you can adapt the relevant parts of the [transformer.json](/transformer-example/src/main/resources/transformer.json) to address these differences (and replace the reference to the demo Dataverse with the references to your own installation):

```json
...
        {
            "sourcePointer": "/persistentUrl",
            "resultPointer": "/@id",
            "expressions": [
                "script(res = 'https://demo.dataverse.org/api/datasets/export?exporter=OAI_ORE&persistentId=' + x)"
            ]
        },
...
        {
            "sourcePointer": "/datasetVersion/files[i]/dataFile",
            "resultPointer": "/ore:describes/ore:aggregates[i]/@id",
            "expressions": [
                "script(res = 'https://demo.dataverse.org/file.xhtml?fileId=' + x.id)"
            ]
        },
        {
            "sourcePointer": "/datasetVersion/files[i]/dataFile",
            "resultPointer": "/ore:describes/ore:aggregates[i]/schema:sameAs",
            "expressions": [
                "script(res = 'https://demo.dataverse.org/api/access/datafile/' + x.id)"
            ]
        },
...

        {
            "sourcePointer": "/datasetVersion/files[i]/dataFile",
            "resultPointer": "/ore:describes/schema:hasPart",
            "expressions": [
                "script(res = 'https://demo.dataverse.org/file.xhtml?fileId=' + x.id)"
            ]
        },
...
```
