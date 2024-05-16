package be.libis.rdm.export.ROCrate;

import com.google.auto.service.AutoService;
import io.gdcc.spi.export.ExportDataProvider;
import io.gdcc.spi.export.ExportException;
import io.gdcc.spi.export.Exporter;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.core.MediaType;

import com.jayway.jsonpath.JsonPath;

/**
 * An external RO-Crate exporter for Dataverse, with customizable mappings to
 * dataset metadata and metadata blocks.
 */
// This annotation makes the Exporter visible to Dataverse. How it works is well
// documented on the Internet.
@AutoService(Exporter.class)
// All Exporter implementations must implement this interface or the XMLExporter
// interface that extends it.
public class ROCrateExporter implements Exporter {

    // The default path from where the csv is to be read.
    String csvPath = "/exporters/dataverse2ro-crate.csv";

    /*
     * The name of the format it creates. If this format is already provided by a
     * built-in exporter, this Exporter will override the built-in one. (Note that
     * exports are cached, so existing metadata export files are not updated
     * immediately.)
     */
    @Override
    public String getFormatName() {
        return "rocrate_json";
    }

    @Override
    public String getDisplayName(Locale locale) {
        return "RO-Crate";
    }

    @Override
    public Boolean isHarvestable() {
        return false;
    }

    @Override
    public Boolean isAvailableToUsers() {
        return true;
    }

    @Override
    public String getMediaType() {
        return MediaType.APPLICATION_JSON;
    }

    public void setCsvPath(String newPath) {
        /*
         * Setter for the path of the csv containing the mappings between dataset
         * metadata and RO-Crate metadata. Used in tests.
         */
        this.csvPath = newPath;
    }

    static String replaceQuotations(String s) {
        /*
         * Turns single quotes into double quotes for uniformity.
         */
        if ((s.startsWith("\'") && s.endsWith("\'")) || (s.startsWith("\"") && s.endsWith("\""))) {
            s = "\"" + s.substring(3, s.length() - 3).strip() + "\"";
        }
        return s;
    }

    static String getJsonPath(String source, String field) {
        /**
         * Produces a JsonPath string for getting the field in the source in the
         * dataset metadata or metadata blocks.
         * TODO: examples here to clarify: what is source, what is field...
         */
        if (source.isBlank() && field.isBlank()) {
            return "$";
        }
        source = source.replace("/", ".");
        String result = "$";
        if (!source.isEmpty()) {
            result += ".";
            result += source;
        }
        if (source.contains("metadataBlocks") && !source.endsWith("fields")) {
            result += ".fields";
            if (!field.isEmpty()) {
                result += "[?(@.typeName=='" + field + "')]";
                result += ".value";
            } else {

            }
        } else if (!field.isEmpty()) {
            if (!result.endsWith(".")) {
                result += ".";
            }
            result += field;
        }
        return result;
    }

    static String getJsonPath(String source, String field, String valueFrom) {
        /**
         * Produces a JsonPath string for reaching the field in the source in the
         * dataset metadata or metadata blocks. When the valueFrom parameter is included
         * the value will be pulled from that property in dataset metadata.
         */
        String result = getJsonPath(source, field);
        if (result.contains("metadataBlocks") && !valueFrom.isBlank()) {
            result += ".[?(@.typeName=='" + valueFrom + "')]";
            result += ".value";

        }

        return result;

    }

    static public ArrayList<String> addReferredEntityAsContextual(CSV csv, String datasetJson,
            String refersToValueString, Map<String, Object> entityMap) throws Exception {
        /*
         * Referred entities are the contextual entities referred by another property in
         * the RO-Crate metadata.
         * For example: The root entity (dataset) refer to authors in the {author:
         * [{@id: id1}, {@id: id2}...]}
         * each of which must be also added as contextual entities. Likewise those
         * authors may have affiliations.
         * In these case both the Author and Affiliation have the "refersTo:" prefix in
         * the "value" field.
         */
        final ArrayList<String> referredIds;
        String refersToValue = refersToValueString.replace("refersTo:", "");
        if (refersToValue.startsWith("\"")) {
            referredIds = new ArrayList<String>();
            referredIds.add(replaceQuotations(refersToValue));
        } else {
            referredIds = addContextualEntity(csv, datasetJson, refersToValue, entityMap);
        }
        return referredIds;
    }

    static public ArrayList<String> getReferredEntityIds(CSV csv, LinkedHashMap propertyMap, String refersToValueString)
            throws Exception {
        /*
         * Returns the ids of the entities referred by the parent entity.
         */
        final ArrayList<String> referredIds = new ArrayList<String>();
        String refersToValue = refersToValueString.replace("refersTo:", "");
        if (refersToValue.startsWith("\"")) {
            referredIds.add(replaceQuotations(refersToValue));
        } else {
            String valueFrom = csv.getIdFieldName(refersToValue);
            Object propertyValue = propertyMap.get(valueFrom);
            if (propertyValue != null) {
                if (propertyValue instanceof String) {
                    referredIds.add((String) propertyValue);
                } else if (propertyValue instanceof LinkedHashMap
                        && ((LinkedHashMap) propertyValue).keySet().contains("value"))
                    referredIds.add((String) ((LinkedHashMap) propertyValue).get("value"));
            }
        }
        return referredIds;
    }

    static public Object readAndUnpackJsonPath(String jsonString, String jsonPath) {
        /*
         * Read jsonpath and unpack if it consists of a single item within a list
         */
        Object dataObject = JsonPath.read(jsonString, jsonPath);
        while (dataObject instanceof List && ((List) dataObject).size() == 1) {
            dataObject = ((List) dataObject).get(0);
        }
        return dataObject;
    }

    static public void addRootEntity(CSV csv, String jsonString, String entityName, Map<String, Object> entityMap)
            throws Exception {
        /*
         * Adds the entities that are at the root level of the ro-crate-metadata.json
         */
        ArrayList<String> ids = new ArrayList<>();
        int i = 0;
        JsonObjectBuilder entityToAdd = Json.createObjectBuilder();

        ArrayList<Map<String, String>> rows = csv.getRowsByEntity(entityName);
        String id = null;

        for (Map<String, String> row : rows) {
            i++;
            if (i == 1) {
                continue;
            }

            String value = row.get("value");
            if (value.contains("refersTo:")) {
                ArrayList<String> referredIds = addReferredEntityAsContextual(csv, jsonString, value, entityMap);
                JsonArrayBuilder arrayOfIdsToAdd = Json.createArrayBuilder();
                if (referredIds.size() > 1) {
                    for (String referredId : referredIds) {
                        JsonObjectBuilder idToAdd = Json.createObjectBuilder();
                        idToAdd.add("@id", referredId.replace("\"", ""));
                        arrayOfIdsToAdd.add(idToAdd);
                    }
                    entityToAdd.add(row.get("targetPropertyName"), arrayOfIdsToAdd);
                } else if (referredIds.size() == 1) {
                    JsonObjectBuilder idToAdd = Json.createObjectBuilder();
                    idToAdd.add("@id", referredIds.get(0).replace("\"", ""));
                    entityToAdd.add(row.get("targetPropertyName"), idToAdd);
                }

            } else {
                value = replaceQuotations(value);
                if (value.startsWith("\"")) {
                    // fixed value: the values within the quotations are directly taken as a target
                    // value, rather than extracting from the metadata.

                    entityToAdd.add(row.get("targetPropertyName"), value.replace("\"", ""));

                    if (row.get("targetPropertyName").equals("@id")) {
                        id = value.replace("\"", "");
                        ids.add(value);
                    }
                } else {

                    String sourcePath = row.get("source");
                    String sourceField = row.get("sourceField");
                    String targetPropertyName = row.get("targetPropertyName");
                    String valueFrom = row.get("value");

                    String jsonPath = getJsonPath(sourcePath, sourceField, valueFrom);
                    Object dataObject = readAndUnpackJsonPath(jsonString, jsonPath);

                    if (dataObject instanceof LinkedHashMap
                            && ((LinkedHashMap) dataObject).keySet().contains("value")) {
                        dataObject = ((LinkedHashMap) dataObject).get("value");
                    }
                    if (dataObject instanceof String) {
                        // Case: object is a Map
                        // -> directly use it as value
                        entityToAdd.add(targetPropertyName, (String) dataObject);
                    } else if (dataObject instanceof LinkedHashMap) {
                        // Case: object is a Map
                        // -> cast it to Map, get properties
                        LinkedHashMap<String, Object> mapObject = (LinkedHashMap<String, Object>) dataObject;
                        entityToAdd.add(targetPropertyName, (String) mapObject.get(valueFrom));
                    } else if (dataObject instanceof List) {
                        List<Object> listObject = (List<Object>) dataObject;
                        // Case: object is an Array
                        if (listObject.size() == 1 && listObject.get(0) instanceof List) {
                            // list of lists -> unpack it
                            listObject = (List<Object>) listObject.get(0);
                        }
                        if (listObject.size() > 0 && listObject.get(0) instanceof String) {
                            // list of strings -> iterate
                            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
                            for (Object stringObject : listObject) {
                                value = (String) stringObject;
                                jsonArrayBuilder.add(value);
                            }
                            entityToAdd.add(targetPropertyName, jsonArrayBuilder);
                        } else if (listObject.size() > 0 && listObject.get(0) instanceof LinkedHashMap) {
                            // list of maps -> iterate
                            ArrayList<Object> values = new ArrayList<Object>();

                            for (Object valueObject : listObject) {

                                LinkedHashMap<String, Object> mapObject = (LinkedHashMap<String, Object>) valueObject;
                                if (mapObject.keySet().contains(valueFrom)) {
                                    valueObject = mapObject.get(valueFrom);
                                } else if (mapObject.get("typeName").equals(valueFrom)) {
                                    valueObject = mapObject.get("value");
                                } else {
                                    continue;
                                }
                                if (valueObject instanceof LinkedHashMap
                                        && ((LinkedHashMap) valueObject).keySet().contains("value")) {
                                    values.add(((LinkedHashMap) valueObject).get("value").toString());

                                } else {
                                    values.add(valueObject.toString());
                                }

                            }
                            entityToAdd.add(targetPropertyName, values.get(0).toString());
                        }
                    }

                }
            }

        }
        if (id != null) {
            entityMap.put(id, entityToAdd);
        }

    }

    static public ArrayList<String> addContextualEntity(final CSV csv, String jsonString, String entityName,
            Map<String, Object> entityMap) throws Exception {
        /*
         * Adds remaining contextual entities.
         */
        ArrayList<String> ids = new ArrayList<>();
        ArrayList<Map<String, String>> rows = csv.getRowsByEntity(entityName);
        String id = null;
        if (entityName.equals("Root") || entityName.equals("Metadata")) {
            addRootEntity(csv, jsonString, entityName, entityMap);
            return ids;
        }
        int i = 0;

        LinkedHashMap<String, ArrayList<String>> propertyNameValue = new LinkedHashMap<>();
        String sourcePath;
        String sourceField;
        String jsonPath;
        Object dataObject = null;

        for (Map<String, String> row : rows) {
            i++;
            if (i == 1) {
                sourcePath = row.get("source");
                sourceField = row.get("sourceField");
                jsonPath = getJsonPath(sourcePath, sourceField);
                dataObject = readAndUnpackJsonPath(jsonString, jsonPath);
            }
            String targetPropertyName = row.get("targetPropertyName");
            ArrayList<String> targetPropertyValues = new ArrayList<String>();
            for (String value : row.get("value").split(",")) {
                targetPropertyValues.add(value.strip());
            }
            propertyNameValue.put(targetPropertyName, targetPropertyValues);
        }

        if (dataObject instanceof LinkedHashMap) {
            LinkedHashMap mapObject = (LinkedHashMap) dataObject;

            JsonObjectBuilder entityToAdd = Json.createObjectBuilder();
            for (String propertyName : propertyNameValue.keySet()) {
                for (String value : propertyNameValue.get(propertyName)) {
                    if (value.contains("refersTo:")) {

                        ArrayList<String> referredIds = addReferredEntityAsContextual(csv, dataObject.toString(), value,
                                entityMap);
                        JsonArrayBuilder arrayOfIdsToAdd = Json.createArrayBuilder();
                        if (referredIds.size() > 1) {
                            for (String referredId : referredIds) {
                                JsonObjectBuilder idToAdd = Json.createObjectBuilder();
                                idToAdd.add("@id", referredId.replace("\"", ""));
                                arrayOfIdsToAdd.add(idToAdd);
                            }
                            entityToAdd.add(propertyName, arrayOfIdsToAdd);
                        } else if (referredIds.size() == 1) {
                            JsonObjectBuilder idToAdd = Json.createObjectBuilder();
                            idToAdd.add("@id", referredIds.get(0).replace("\"", ""));
                            entityToAdd.add(propertyName, idToAdd);
                        }

                    } else if (mapObject.keySet().contains(value)) {
                        entityToAdd.add(propertyName, (String) mapObject.get(value));
                        if (propertyName.equals("@id")) {
                            ids.add((String) mapObject.get(value));
                            id = (String) mapObject.get(value);
                        }

                        break;
                    }
                }
            }
            if (id != null) {
                entityMap.put(id, entityToAdd);
            }

        } else if (dataObject instanceof String) {

            JsonObjectBuilder entityToAdd = Json.createObjectBuilder();

            for (String propertyName : propertyNameValue.keySet()) {
                for (String value : propertyNameValue.get(propertyName)) {
                    if (propertyName.isBlank()) {
                        continue;
                    }
                    entityToAdd.add(propertyName,
                            (value.startsWith("\"") ? value.replace("\"", "") : (String) dataObject));
                    if (propertyName.equals("@id")) {
                        id = (String) dataObject;
                        ids.add((String) dataObject);
                    }

                }

            }
            if (id != null) {
                entityMap.put(id, entityToAdd);
            }

        } else if (dataObject instanceof ArrayList) {

            for (LinkedHashMap mapObject : (ArrayList<LinkedHashMap>) dataObject) {
                JsonObjectBuilder entityToAdd = Json.createObjectBuilder();

                for (String propertyName : propertyNameValue.keySet()) {
                    for (String value : propertyNameValue.get(propertyName)) {
                        if (value.contains("refersTo:")) {
                            ArrayList<String> referredIds = getReferredEntityIds(csv, mapObject, value);

                            JsonArrayBuilder arrayOfIdsToAdd = Json.createArrayBuilder();
                            if (referredIds.size() > 1) {
                                for (String referredId : referredIds) {
                                    JsonObjectBuilder idToAdd = Json.createObjectBuilder();
                                    idToAdd.add("@id", referredId.replace("\"", ""));
                                    arrayOfIdsToAdd.add(idToAdd);
                                }
                                entityToAdd.add(propertyName, arrayOfIdsToAdd);
                            } else if (referredIds.size() == 1) {
                                JsonObjectBuilder idToAdd = Json.createObjectBuilder();
                                idToAdd.add("@id", referredIds.get(0).replace("\"", ""));
                                entityToAdd.add(propertyName, idToAdd);
                            }

                        } else if (mapObject.keySet().contains(value)) {

                            if (mapObject.get(value) instanceof String) {
                                entityToAdd.add(propertyName, (String) mapObject.get(value));
                                if (propertyName.equals("@id")) {
                                    ids.add((String) mapObject.get(value));
                                }
                                break;
                            } else if (mapObject.get(value) instanceof LinkedHashMap) {
                                entityToAdd.add(propertyName,
                                        (String) ((LinkedHashMap) mapObject.get(value)).get("value"));
                                if (propertyName.equals("@id")) {
                                    id = (String) ((LinkedHashMap) mapObject.get(value)).get("value");
                                    ids.add((String) ((LinkedHashMap) mapObject.get(value)).get("value"));
                                }
                                break;
                            }
                        }
                    }

                }
                if (id != null) {
                    entityMap.put(id, entityToAdd);
                }

            }
        }

        return ids;

    }

    static public void addDataEntities(String datasetString, Map<String, Object> entityMap) {
        /*
         * Adds data entities such as files and folders.
         */
        List<LinkedHashMap<String, Object>> files = (List<LinkedHashMap<String, Object>>) JsonPath.read(datasetString,
                "$.datasetVersion.files");
        final Map<String, LinkedHashMap<String, Object>> fileEntityMap = new LinkedHashMap<String, LinkedHashMap<String, Object>>();

        for (LinkedHashMap<String, Object> file : files) {
            String parentId = ".";

            if (file.get("directoryLabel") != null && !((String) file.get("directoryLabel")).isBlank())
                for (String pathElement : ((String) file.get("directoryLabel")).split("/")) {
                    LinkedHashMap<String, Object> parentEntity = fileEntityMap.get(parentId + "/");
                    if (parentEntity == null) {
                        parentEntity = new LinkedHashMap<String, Object>();
                        parentEntity.put("@id", parentId + "/");
                        parentEntity.put("@type", "Dataset");
                        parentEntity.put("hasPart", new ArrayList<String>());
                    }
                    ArrayList<String> hasPart = (ArrayList<String>) parentEntity.get("hasPart");
                    if (!hasPart.contains(pathElement + "/")) {
                        hasPart.add(pathElement + "/");
                    }

                    parentEntity.put("hasPart", hasPart);
                    fileEntityMap.put(parentId + "/", parentEntity);

                    parentId = pathElement;
                }

            LinkedHashMap<String, Object> parentEntity = fileEntityMap.get(parentId + "/");
            if (parentEntity == null) {
                parentEntity = new LinkedHashMap<String, Object>();
                parentEntity.put("@id", parentId + "/");
                parentEntity.put("@type", "Dataset");
                parentEntity.put("hasPart", new ArrayList<String>());
            }
            ArrayList<String> hasPart = (ArrayList<String>) parentEntity.get("hasPart");
            if (!hasPart.contains((String) file.get("label"))) {
                hasPart.add((String) file.get("label"));
            }
            parentEntity.put("hasPart", hasPart);
            fileEntityMap.put(parentId + "/", parentEntity);
            LinkedHashMap<String, Object> fileEntity = fileEntityMap.get((String) file.get("label"));

            if (fileEntity == null) {
                fileEntity = new LinkedHashMap<String, Object>();
                fileEntity.put("@id", (String) file.get("label"));
                fileEntity.put("@type", "File");
                fileEntityMap.put((String) file.get("label"), fileEntity);
            }

        }
        for (String fileEntityId : fileEntityMap.keySet()) {
            JsonObjectBuilder dataEntity;
            if (entityMap.keySet().contains(fileEntityId)) {
                dataEntity = (JsonObjectBuilder) entityMap.get(fileEntityId);
            } else {
                dataEntity = Json.createObjectBuilder();

            }

            LinkedHashMap<String, Object> currentEntityProperties = fileEntityMap.get(fileEntityId);
            for (String propertyName : currentEntityProperties.keySet()) {
                Object currentProperty = currentEntityProperties.get(propertyName);
                if (currentProperty instanceof String) {
                    dataEntity.add(propertyName, (String) currentProperty);
                } else if (currentProperty instanceof ArrayList && ((ArrayList<String>) currentProperty).size() == 1) {
                    dataEntity.add(propertyName, (String) ((ArrayList<String>) currentProperty).get(0));
                } else if (currentProperty instanceof ArrayList && ((ArrayList<String>) currentProperty).size() > 1) {
                    JsonArrayBuilder propertyArray = Json.createArrayBuilder();
                    for (String property : (ArrayList<String>) currentProperty) {
                        propertyArray.add(property);
                    }
                    dataEntity.add(propertyName, propertyArray);

                }

            }
            entityMap.put(fileEntityId, dataEntity);
        }
    }

    static public ArrayList<String> addEntity(CSV csv, String jsonString, String entityName,
            Map<String, Object> entityMap) throws Exception {
        /*
         * Chooses from addRootEntity, addContextualEntity, addFileEntity depending on
         * the rules on the Csv
         */
        ArrayList<String> ids = new ArrayList<>();
        ArrayList<Map<String, String>> rows = csv.getRowsByEntity(entityName);
        String entitySourcePath;
        String entitySourceField;

        entitySourcePath = rows.get(0).get("source");
        entitySourceField = rows.get(0).get("sourceField");

        if (entitySourcePath.isBlank() && entitySourceField.isBlank()) {
            // is Root?
            addRootEntity(csv, jsonString, entityName, entityMap);
        } else {
            // contextual entity
            addContextualEntity(csv, jsonString, entityName, entityMap);
        }
        return ids;
    }

    static public JsonObject buildROCrate(final CSV csv, final JsonObject datasetJson) throws Exception {
        /*
         * Build RO-Crate from the rules and dataset provided.
         */
        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        final JsonObjectBuilder result = Json.createObjectBuilder();
        final Map<String, Object> entityMap = new LinkedHashMap<String, Object>();

        addEntity(csv, datasetJson.toString(), "Metadata", entityMap);
        addDataEntities(datasetJson.toString(), entityMap);
        result.add("context", "https://w3id.org/ro/crate/1.1/context");
        for (String id : entityMap.keySet()) {
            jsonArrayBuilder.add((JsonObjectBuilder) entityMap.get(id));
        }

        result.add("graph", jsonArrayBuilder);
        return result.build();
    }

    @Override
    public void exportDataset(ExportDataProvider dataProvider, OutputStream outputStream) throws ExportException {
        /*
         * Exports dataset metadata as ro-crate-metadata.json
         */
        try {
            CSV csv = new CSV(this.csvPath);
            JsonObject roCrateJsonLd = buildROCrate(csv, dataProvider.getDatasetJson());
            outputStream.write(roCrateJsonLd.toString().getBytes("UTF8"));
            outputStream.flush();
        } catch (Exception e) {
            throw new ExportException(e.toString());
        }
    }
}