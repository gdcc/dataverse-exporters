package be.libis.rdm.export.ROCrate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class CSV {
    private LinkedHashMap<String, ArrayList<Map<String, String>>> rowsByEntity; // key: entityName, v: ListofRows

    public Set<String> getEntityNames() {
        /*
         * Gets all of the entities contained in the csv. 
         */
        return rowsByEntity.keySet();
    }

    public CSV(String filename) throws Exception {

        rowsByEntity = readMappingsCsv(filename);
    }

    public ArrayList<Map<String, String>> getRowsByEntity(String entityName) throws Exception {
        /*
         * Returns all the Csv rows containing the entity type. 
         */

        return this.rowsByEntity.get(entityName);
    }

    static LinkedHashMap<String, ArrayList<Map<String, String>>> filterRowsByEntity(
            ArrayList<Map<String, String>> rows) {
            /*
            * Converts the rows in the CSV to a Map of maps
            * entityName -> ArrayList<row>
            * row: columName -> value
            */
        LinkedHashMap<String, ArrayList<Map<String, String>>> result = new LinkedHashMap<String, ArrayList<Map<String, String>>>();
        String entityName = null;
        ArrayList<Map<String, String>> currentEntityRows = null;

        for (Map<String, String> row : rows) {
            if (row.get("entityName") != null && !row.get("entityName").isBlank()) {
                entityName = row.get("entityName");
                currentEntityRows = result.get(entityName);
            }
            ;

            if (currentEntityRows == null) {
                currentEntityRows = new ArrayList<Map<String, String>>();
            }
            currentEntityRows.add(row);
            if (entityName != null) {
                result.put(entityName, currentEntityRows);
            }
        }

        return result;

    }

    public String getIdFieldName(String entityName) throws Exception {
        /*
         * Returns the name of the property/field containing the id of the entity. 
         * This is set in the CSV. 
         * For example, id field could be set as ORCID for author.
         */
        Map<String, String> idRow = getIdRow(entityName);
        return idRow.get("value");
    }

    public Map<String, String> getIdRow(String entityName) throws Exception {
        Map<String, String> result = null;
        ArrayList<Map<String, String>> rows = getRowsByEntity(entityName);
        for (Map<String, String> row : rows) {
            if (row.get("targetPropertyName").equals("@id")) {
                result = row;
                break;
            }
        }
        return result;
    }

    static String replaceDoubleUnderWithAt(String s) {
        /*
         * Replaces double underscore with @
         * This is added as a convenience for MS Excel users.
         */
        if (s.startsWith("__")) {
            return "@" + s.substring(2, s.length());
        } else {
            return s;
        }

    }

    static LinkedHashMap<String, ArrayList<Map<String, String>>> readMappingsCsv(String filename) throws Exception {
        /**
         * Reads the csv from the filesystem and puts the rows on a Map of maps
         * entityName -> ArrayList<row>
         * row: columName -> value
         */
        Scanner scanner = new Scanner(new File(filename), "UTF-8");
        String[] columnNames = scanner.nextLine().split(",");
        String csvLine = null;
        ArrayList<Map<String, String>> rows = new ArrayList<>();
        while (scanner.hasNext()) {
            csvLine = scanner.nextLine();
            final Map<String, String> currentRow = new HashMap<String, String>();

            final String[] line = csvLine.split(",");

            for (int i = 0; i < columnNames.length; i++) {
                final String columnName = columnNames[i];
                String value;
                if (i < line.length) {
                    value = line[i];
                } else {
                    value = "";
                }
                if (columnName.equals("targetPropertyName") && value.startsWith("__")) {
                    value = replaceDoubleUnderWithAt(value);
                }
                // if (columnName.equals("value")) {
                // value = fixQuotations(value);
                // }
                currentRow.put(columnName.strip(), value.strip());
            }
            rows.add(currentRow);
        }
        scanner.close();
        return filterRowsByEntity(rows);
    }

}
