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

As of [croissant-0.0.2-20240501.010936-8.jar](https://s01.oss.sonatype.org/content/groups/staging/io/gdcc/croissant/0.0.2-SNAPSHOT/croissant-0.0.2-20240501.010936-8.jar), large number of files are definitely a performance problem.

`time curl 'https://beta.dataverse.org/api/datasets/export?exporter=croissant&persistentId=doi%3A10.5072/FK2/F1MCGB'` took 45 seconds...

```
real    0m44.743s
user    0m0.019s
sys     0m0.022s
```

... and the only information in the file was `{"status":"ERROR","message":"Export Failed"}`.

The error was:

```
[2024-05-01T14:08:09.304+0000] [Payara 6.2023.7] [WARNING] [] [edu.harvard.iq.dataverse.export.ExportService] [tid: _ThreadID=88 _ThreadName=http-thread-pool::jk-connector(3)] [timeMillis: 1714572489304] [levelValue: 900] [[
  Exception thrown while creating export_croissant.cached : Unknown exception caught during export: java.lang.NullPointerException: Cannot invoke "jakarta.json.JsonString.getString()" because the return value of "org.eclipse.parsson.JsonObjectBuilderImpl$JsonObjectImpl.getJsonString(String)" is null]]

[2024-05-01T14:08:09.347+0000] [Payara 6.2023.7] [WARNING] [] [edu.harvard.iq.dataverse.api.Datasets] [tid: _ThreadID=88 _ThreadName=http-thread-pool::jk-connector(3)] [timeMillis: 1714572489347] [levelValue: 900] [[
  Failed to export the dataset as croissant]]
```

### Can summary statistics go into Croissant?

No, but we opened https://github.com/mlcommons/croissant/issues/640 about this. See also "potential areas of work" in the [Croissant Task Force Minutes] for 2024-04-01. 

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

If you update the jar but not the dataset and want to see the changes, you can reexport all datasets or a specific dataset per https://guides.dataverse.org/en/6.1/admin/metadataexport.html#batch-exports-through-the-api

```
curl http://localhost:8080/api/admin/metadata/reExportAll
curl http://localhost:8080/api/admin/metadata/:persistentId/reExportDataset?persistentId=doi:10.5072/FK2/DZRHUP
```

### To use the Croissant jar in non-Docker

Same as above but use a JVM option in domain.xml such as the example below.

```
<jvm-options>-Ddataverse.spi.exporters.directory=/home/dataverse/dataverse-exporters/croissant/target</jvm-options>
```
