# Dataverse Exporters

This is a collection of community-developed external metadata exporters that can be used with [Dataverse](https://dataverse.org).

## Installation

Generally speaking, you will be downloading a .jar (Java Archive) file and placing it into the directory you have configured Dataverse to use. See [External Metadata Exporters] in the Dataverse Installation Guide for details.

[External Metadata Exporters]: https://dataverse-guide--10533.org.readthedocs.build/en/10533/installation/advanced.html#external-metadata-exporters

## Exporters in this repo

- [Croissant](croissant): a high-level format for machine learning datasets.
- [MyJSON](dataverse-spi-export-examples): a simple example of replacing an existing exporter.

## Compatibility note

The latest versions of Dataverse use version 2.0.0 of the exporter Service Provider Interface (SPI) and all of the exporters above are compatible with it.

The only version of Dataverse that used version 1.0.0 of the SPI was Dataverse 5.14. You can back up to commit [dd1d9b] if you are intersted in version 1.0.0.

[dd1d9b]: https://github.com/gdcc/dataverse-exporters/tree/dd1d9b26e2e2c6d14de5681e0596cca9823b1622

## Developing exporters

You are welcome to contribute additional exporters! Documentation of the exporter Service Provider Interface (SPI) is available under [Metadata Export] in the Developer Guide.

[Metadata Export]: https://guides.dataverse.org/en/latest/developers/metadataexport.html