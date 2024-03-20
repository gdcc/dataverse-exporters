# Croissant Exporter for Dataverse

## To run tests

```    
mvn test
```

## To validate

```
./validate.sh
```

## To build the Croissant jar

```    
mvn package
```

## To use the Croissant jar in Docker

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

## To use the Croissant jar in non-Docker

Same as above but use a JVM option in domain.xml such as the example below.

```
<jvm-options>-Ddataverse.spi.exporters.directory=/home/dataverse/dataverse-exporters/croissant/target</jvm-options>
```
