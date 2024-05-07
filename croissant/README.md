# Croissant Exporter for Dataverse

[Croissant][] is a high-level format for machine learning datasets. See the [Croissant writeup] in the guides for how this format is used in Dataverse and how to enable it.

[Croissant]: https://github.com/mlcommons/croissant
[Croissant writeup]: https://dataverse-guide--10533.org.readthedocs.build/en/10533/installation/advanced.html#croissant-croissant

## Differences between Schema.org JSON-LD and Croissant

- In Dataverse's implementation of Schema.org JSON-LD, we duplicated some fields. In Croissant, "author" has been dropped in favor of "creator" and "provider" has been dropped in favor of "publisher".

## Open questions

Croissant is a new format and there are a number of open questions about it. (Developers may find additional open question in the code flagged with "TODO".)

### Can file paths be included?

We opened https://github.com/mlcommons/croissant/issues/639 about this. It's not clear where to put file paths (called `directoryLabel` in Dataverse code).

### Can ingested files have multiple URLs to download various formats?

We opened https://github.com/mlcommons/croissant/issues/641 about this. Perhaps there should be a FileObject for each format offered?

### What if the Croissant file is huge, due to many files (distribution) and columns (recordSet)?

Good question. We already have a similar problem with our Schema.org JSON-LD format. We opened https://github.com/mlcommons/croissant/issues/646 about it.

For a [test dataset][] with 10,000 files (images), the Schema.org file is 1.6 MB and the Croissant file is 2.5 MB.

For a [large dataset][] with 25,310 files (images), the Schema.org file is 4.4 MB and the Croissant file is 7.1 MB.

Datasets with many columns from ingested files are another potential problem.

Google's [guidelines on structured data][] don't indicate any size limit but we should take care not to make pages too heavy.

[test dataset]: https://github.com/IQSS/dataverse-sample-data/pull/42
[large dataset]: https://dataverse.harvard.edu/dataset.xhtml?persistentId=doi:10.7910/DVN/3CTMKP
[guidelines on structured data]: https://developers.google.com/search/docs/appearance/structured-data/sd-policies

### Can summary statistics go into Croissant?

No, but we opened https://github.com/mlcommons/croissant/issues/640 about this. See also "potential areas of work" in the [Croissant Task Force Minutes] for 2024-04-01. 

### FileObject is not a known valid target type for the distribution property?

We are aware that you could see the error "http://mlcommons.org/croissant/FileObject is not a known valid target type for the distribution property" at https://validator.schema.org if you try to validate the Croissant export.

## Developer documentation

The rest of this documentation is for developers of the Dataverse Croissant exporter, including how to build and test it.

### The Croissant spec and task force

You can find the Croissant [spec] on the [Croissant] website. Consider joining the Croissant Task Force to ask questions (see [Croissant Task Force Minutes] for previous meetings). Don't be shy about opening issues at https://github.com/mlcommons/croissant/issues

[spec]: https://mlcommons.github.io/croissant/docs/croissant-spec.html
[Croissant Task Force Minutes]: https://docs.google.com/document/d/1OINP9AmphhAa3J9sw87QtpY9Hb4_Ym9Zxf1_ycF1_04/edit?usp=sharing

### To run tests

```    
mvn test
```

### To validate

```
./validate.sh
```

Note that [mlcroissant][] is changing! Sometimes in backward-incompatible ways! If you upgrade, you may see an error like this:

```
WARNING: The JSON-LD `@context` is not standard. Refer to the official @context (e.g., from the example datasets in https://github.com/mlcommons/croissant/tree/main/datasets/1.0).
```

As the error indicates, go to https://github.com/mlcommons/croissant/tree/main/datasets/1.0 and find a dataset such as "titanic" and copy the latest `@context` from there. Note that this may be different (but more up to date) than the version in the spec at https://mlcommons.github.io/croissant/docs/croissant-spec.html#appendix-1-json-ld-context

In short, we are trusting the output of `mlcroissant validate --jsonld` over the spec.

For additional validation you can use https://search.google.com/test/rich-results and https://validator.schema.org which are linked from https://developers.google.com/search/docs/appearance/structured-data

[mlcroissant]: https://pypi.org/project/mlcroissant/

### To build the Croissant jar

```    
mvn package
```

### To use the Croissant jar in Docker

Under "environment" in the compose file, add the following.

```
DATAVERSE_SPI_EXPORTERS_DIRECTORY: "/dv/exporters"
```

Then create an `exporters` directory and copy the jar into it (dev example shown).

```
mkdir docker-dev-volumes/app/data/exporters
cp target/*jar docker-dev-volumes/app/data/exporters
```

Then stop and start the containers. On a dataset, click "Metadata" then "Export Metadata" and in the dropdown you should see "Croissant" listed.

If you update the jar but not the dataset and want to see the changes, you can reexport all datasets or a specific dataset per https://guides.dataverse.org/en/6.2/admin/metadataexport.html#batch-exports-through-the-api

```
curl http://localhost:8080/api/admin/metadata/reExportAll
curl http://localhost:8080/api/admin/metadata/:persistentId/reExportDataset?persistentId=doi:10.5072/FK2/DZRHUP
```

### To use the Croissant jar in non-Docker

Same as above but use a JVM option in domain.xml such as the example below.

```
<jvm-options>-Ddataverse.spi.exporters.directory=/home/dataverse/dataverse-exporters/croissant/target</jvm-options>
```

### Differences from pyDataverse

A pyDataverse implementation is underway at https://github.com/Dans-labs/pyDataverse/tree/croissant and https://github.com/Dans-labs/pyDataverse/tree/semantic-mappings

The "croissant" branch does the following:

- For files, for `@id`, the database id (e.g. `f26148`) is used instead of the filename (e.g. `README.md`).
- `sc:Person` is used instead of `Person`.
- `url` is used.
- `dateCreated` is used.

### Difference from Schema.org JSON-LD

Dataverse's Schema.org implementation does the following:

- `@context` is much more minimal, only showing "http://schema.org".
- Type is `sc:Dataset instead of `cr:Dataset`.
- No `conformsTo` (`"conformsTo": "http://mlcommons.org/croissant/1.0"`)
- No `citeAs`
- No `recordSet`
- For `version`, "1" is used instead of "1.0.0".
- Under `distribution` the type is `DataDownload` instead of `cr:FileObject`.
- Under `distribution` there is no `@id`.
- Under `distribution` there is no `md5`.
- Has the following (but Croissant doesn't):
  - `@id`
  - `identifier`
  - `author` (duplicate of `creator`)
  - `provider` (duplicate of `publisher`)
