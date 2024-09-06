# Dataverse Exporters

A collection of community-developed metadata exporters that conform to the [Dataverse](https://dataverse.org) Exporter Service Provider Interface (SPI).

Community members are encouraged to contribute to the documentation, add crosslinks, and create additional exporters!
See below for more information.

In this repository you will find the Parent POM for all exporters as well as a very simple example exporter (see `/example`).

## How to get support for exporters

All the projects below are made by the community for the community.
While IQSS might contribute, please don't pester them with support request.

You are welcome to ask questions in the chat: https://chat.dataverse.org
Please ask development related questions in #dev, anything else in #community or #troubleshoot.

In case of a concrete problem with one of the exporters, please create a reproducible issue in the respective GitHub project.

## List of known exporters

### Community supported GDCC projects

Quick links:
- Explore a [list of GDCC exporter GitHub repositories](https://github.com/orgs/gdcc/repositories?q=props.topic%3A%22Metadata+Export%22).
- Overview of [all GDCC exporters on Maven Central](https://central.sonatype.com/namespace/io.gdcc.export)

| Name/Source                                                 | Description                                                                                                                                  | Format                                            | Identifier     | Download |
|-------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|----------------|----------|
| [Example](./example)                                        | A minimalistic example, may be used as template                                                                                              | JSON                                              | dataverse_json | -        |
| [Debug](https://github.com/gdcc/exporter-debug)             | Dumps all possible JSON and XML data available to export plugins                                                                             | JSON, XML                                         | debug          | [JAR file](https://repo1.maven.org/maven2/io/gdcc/export/debug/) |
| [DDI-PDF](https://github.com/gdcc/exporter-ddipdf)          | Export DDI metadata as a printable PDF file                                                                                                  | PDF with [DDI metadata](https://ddialliance.org/) | pdf            | TBD      |
| [Croissant](https://github.com/gdcc/exporter-croissant)     | Export metadata as linked data following [Croissant ontology](https://docs.mlcommons.org/croissant/docs/croissant-spec.html)                 | JSON-LD                                           | croissant      | [JAR file](https://repo1.maven.org/maven2/io/gdcc/export/croissant/) |
| [RO-Crate](https://github.com/gdcc/exporter-ro-crate)       | Export metadata as linked data following [RO-Crate ontology](https://www.researchobject.org/ro-crate/specification/1.1/appendix/jsonld.html) | JSON-LD                                           | rocrate_json   | TBD      |
| [Transformer](https://github.com/gdcc/exporter-transformer) | This exporter allows you to have up to 100 exporters using a single pre-built JAR file. Included examples: Hello World!, Debug, Croissant, RO-Crate, DDI-PDF, and more! You can add new exporters by adding directories into the exporters directory and placing (and editing) the `config.json` and the `transformer` files in it (see the [README](https://github.com/gdcc/exporter-transformer?tab=readme-ov-file)). | JSON(-LD), XML, HTML, PDF, etc. | (varies)       | [JAR file](https://repo1.maven.org/maven2/io/gdcc/export/exporter-transformer/1.0.10/exporter-transformer-1.0.10-jar-with-dependencies.jar) |

### Other projects

If you have an exporter plugin that cannot or shall not be transferred to GDCC, feel free to open an issue or pull request to let us know.

## Creating new exporters

Documentation of the exporter SPI is available under [Advanced Installation](https://guides.dataverse.org/en/latest/installation/advanced.html#installing-external-metadata-exporters) and [Metadata Export](https://guides.dataverse.org/en/latest/developers/metadataexport.html). 

### What are GDCC supported combinations of libraries and Dataverse?

Version support matrix:

| Supported | Exporter Parent POM | Dataverse SPI | Dataverse Software |
|-----------|---------------------|---------------|--------------------|
| ❌         | ❌                   | 1.0.0         | 5.14               |
| ✅         | 2.0.0               | 2.0.0         | 6.0 - recent       |

Other combinations might work. Your mileage may vary.

### How to contribute an exporter to the GDCC

In order to add your Dataverse Metadata Export Plugin to https://github.com/gdcc, please follow these steps:

- Use the parent POM provided in your build system
- Add unit and integration tests for your exporter to validate functionality
- Setup GitHub Actions to continuously verify functionality
- Enable the Renovate Bot or Dependency Bot to update your dependencies regularly
- Write documentation for your exporter. Especially make sure to indicate supported Dataverse software versions!
- Open a contribution request at https://github.com/gdcc/dataverse-exporters/issues/new to get access to a repository, be included in the list above and enable releases.
- Release your exporter on Maven Central. Credentials will be provided by GDCC admins. See below for more details.
- Keep your exporter code well maintained. Keep a changelog.

Please note that abandoned projects can and will be archived to ensure smooth operations for admins/users.

### How to release an exporter in the name of GDCC

Once you received your exporter repository under https://github.com/gdcc, you can enable release workflows.
Simply copy these files to your repository and ... (To Be Done)

Link to [Dataverse SPI](https://central.sonatype.com/artifact/io.gdcc/dataverse-spi)
