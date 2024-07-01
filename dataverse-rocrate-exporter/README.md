# Customizable RO-Crate Metadata Exporter for Dataverse
A customizable external metadata exporter for [Dataverse](https://dataverse.org) for exporting dataset metadata as an [RO-Crate JsonLD](https://www.researchobject.org/ro-crate/1.1/appendix/jsonld.html). 

RO-Crate is a "community effort to establish a lightweight approach to packaging research data with their metadata [...] based on schema.org annotations in JSON-LD" ([Research Object Crate](https://w3id.org/ro/crate)). 

A problem in creating an RO-Crate metadata exporter for Dataverse is that the default Dataverse metadata blocks do not exactly map to the *must*s and *should*s of the RO-Crate metadata [specifications](https://www.researchobject.org/ro-crate/specification.html). Moreover, Dataverse enables installations to create their custom metadata blocks. While this gives Dataverse installations great flexibility, it adds another level of complication to RO-Crate metadata export, as an exporter with hardcoded mappings will be insufficient for some installations. 

This exporter provides a customizable solution to address these issues. It reads the mappings between dataset metadata from a CSV file, which can be edited with a spreadsheet editor to customize the exporter's output. 


# Customization 
The export is customized by editing the file named **dataverse2ro-crate.csv**. The file needs to be in the same folder as the exporter's .jar file. 

The CSV is organized into entity types to facilitate referencing entities from other entities. Each entity starts with a row that containing the **entityName** and fields **source** and **sourceField** containing path from where the properties of the entity will be taken from. The following fields contain mappings between the property that appears in the ro-crate-metadata.json (**targetPropertyName**) and the field or the value which will be used to populate that field **value**. 

For example:

| entityName | targetPropertyName | source                                 | sourceField | value                       | 
| ---------- | ------------------ | -------------------------------------- | ----------- | --------------------------- | 
| Author     |                    | datasetVersion/metadataBlocks/citation | author      |                             | 
|            | @id                |                                        |             | authorIdentifier            | 
|            | @type              |                                        |             | "Person"                    |
|            | name               |                                        |             | authorName                  |
|            | affiliation        |                                        |             | refersTo:AuthorAffiliation  |
...

The first row indicates that the properties of Author entity will be taken from the **author** citation metadata block. The following properties will be mapped for each **author**.

The second and fourth rows map **@id** of the author entity in ro-crate-metadata.json to **authorIdentifier** of each author, and **name** to **authorName**. The third row, **@type** will be **Person** for each author without need for mapping, indicated by putting the string in quotation marks (**"Person"**).

The last row for this entity, **affiliation**, is a separate contextual entity for the organization whose **@id** needs to appear for that author's **affiliation** property. The reference to **AuthorAffiliation** entity is indicated with a **refersTo:** prefix (**refersTo:AuthorAffiliation**). Then the **AuthorAffiliation** can be defined separately:

| entityName         | targetPropertyName | source                                 | sourceField | value                       | 
| ------------------ | ------------------ | -------------------------------------- | ----------- | --------------------------- | 
| ...                | ...                | ...                                    | ...         | ...                         |
| AuthorAffiliation  |                    | datasetVersion/metadataBlocks/citation | author      |                             | 
|                    | @id                |                                        |             | authorAffiliation           | 
|                    | @type              |                                        |             | "Organization"              |
| ...                | ...                | ...                                    | ...         | ...                         |

Some entities appear only once but need to take values from separate metadata fields. For example, the dataset needs **persistentUrl** from dataset for **identifier**, **publicationDate** from **datasetVersion**... For such entities, leave the **source** and **sourceField** empty in the first column, and fill those fields for each property separately:

| entityName         | targetPropertyName                     | source                                 | sourceField   | value                       | 
| ------------------ | -------------------------------------- | -------------------------------------- | ------------- | --------------------------- | 
| Root               |                                        |                                        |               |                             | 
|                    | @id                                    |                                        |               | "./"                        | 
|                    | @type                                  |                                        |               | "Dataset"                   |
|                    | identifier                             |                                        |               | persistentUrl               | 
|                    | name                                   | datasetVersion/metadataBlocks/citation |               | title                       |
|                    | description                            | datasetVersion/metadataBlocks/citation | dsDescription | dsDescriptionValue          |
|                    | license                                |                                        |               | refersTo:License            |
|                    | author                                 |                                        |               | refersTo:Author             |
| ...                | ...                                    | ...                                    | ...           | ...                         |

As seen in this example, each dataset need to take values from diverse sources such as the dataset itself (indicated as an empty **source**), dataset version (**datasetVersion**), citation metadata block (**datasetVersion/metadataBlocks/citation**)...

The default CSV provided with the exporter can be used out-of-the-box. For reference about the default metadata blocks of Dataverse and what properties are contained in other fields, please refer to [Dataverse documentation](https://guides.dataverse.org/en/latest/user/appendix.html) and the JSON metadata export in Dataverse. 

# Data entities
Mapping of data entities is currently hardcoded, thus cannot be included in the customization csv. 