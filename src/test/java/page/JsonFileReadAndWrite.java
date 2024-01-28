package page;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonFileReadAndWrite {

    public void writeNoGameFoundIntoJson(String gameName, String providerName) throws InterruptedException {
        String filePath = "src/resources/json/no_game_found.json";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            JsonNode jsonData = objectMapper.readTree(new File(filePath));
            boolean dataExists = false;
            if (jsonData.isArray()) {
                ArrayNode jsonArray = (ArrayNode) jsonData;
                for (JsonNode node : jsonArray) {
                    if (node.isObject() && node.has("game") && node.has("provider")) {
                        if (node.get("game").asText().equals(gameName) && node.get("provider").asText().equals(providerName)) {
                            dataExists = true;
                            break;
                        }
                    }
                }
            }
            if (!dataExists) {
                ObjectNode noGameDataEntry = objectMapper.createObjectNode();
                noGameDataEntry.put("game", gameName);
                noGameDataEntry.put("provider", providerName);
                if (jsonData.isArray()) {
                    ((ArrayNode) jsonData).add(noGameDataEntry);
                }
                else {
                    ArrayNode newArray = objectMapper.createArrayNode();
                    newArray.add(noGameDataEntry);
                    jsonData = newArray;
                }
                objectMapper.writeValue(new File(filePath), jsonData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread.sleep(1000);
    }

public void writeGameDataIntoJson(String filePath, String nameGame, String nameProvider, List<String> dataGame, List<String> valuesAttribute, String gameType, String descriptionGame) throws InterruptedException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    try {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            ObjectNode initialData = objectMapper.createObjectNode();
            initialData.putArray("found");
            objectMapper.writeValue(file, initialData);
        }

        JsonNode rootNode = objectMapper.readTree(file);

        if (rootNode != null && rootNode.isObject() && rootNode.has("found") && rootNode.get("found").isArray()) {
            ArrayNode foundArray = (ArrayNode) rootNode.get("found");
            for (int i = 0; i < foundArray.size(); i++) {
                JsonNode existingEntry = foundArray.get(i);
                if (existingEntry.isObject() && existingEntry.has("name")) {
                    JsonNode nameNode = existingEntry.get("name");
                    String existingGameName = nameNode.get("game").asText();
                    String existingProviderName = nameNode.get("provider").asText();
                    if (existingGameName.equals(nameGame) && existingProviderName.equals(nameProvider)) {
                        foundArray.remove(i);
                        break;
                    }
                }
            }
            ObjectNode gameData = createGameObject(nameGame, nameProvider);
            ObjectNode data = createDataObject(dataGame);

            if (gameType.equals("Slot")) {
                ObjectNode attributeValues = createAttributesObject(valuesAttribute);
                data.set("attributes", attributeValues);
            }
            else if (gameType.equals("Casino")) {
                ObjectNode attributeValues = createCasinoAttributesObject(valuesAttribute);
                data.set("attributes", attributeValues);
            }

            ObjectNode description = createDescriptionObject(descriptionGame);
            data.set("description", description);

            ObjectNode newEntry = objectMapper.createObjectNode();
            newEntry.set("name", gameData);
            newEntry.set("data", data);
            foundArray.add(newEntry);

            objectMapper.writeValue(file, rootNode);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    Thread.sleep(1000);
}

    public ObjectNode createGameObject(String gameName, String providerName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode gameData = objectMapper.createObjectNode();

        gameData.put("game", gameName);
        gameData.put("provider", providerName);

        return gameData;
    }

    public ObjectNode createDataObject(List<String> gameData) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode data = objectMapper.createObjectNode();

        data.put("name", gameData.get(0));
        data.put("link", gameData.get(1));
        data.put("icon", gameData.get(2));

        return  data;
    }

    public ObjectNode createAttributesObject(List<String> attributesValues) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode attributes = objectMapper.createObjectNode();

        attributes.put("Provider", attributesValues.get(0));
        attributes.put("Release Date", attributesValues.get(1));
        attributes.put("Type", attributesValues.get(2));
        attributes.put("RTP", attributesValues.get(3));
        attributes.put("Variance", attributesValues.get(4));
        attributes.put("Hit Frequency", attributesValues.get(5));
        attributes.put("Max Win", attributesValues.get(6));
        attributes.put("Min bet $, €, £", attributesValues.get(7));
        attributes.put("Max bet $, €, £", attributesValues.get(8));
        attributes.put("Layout", attributesValues.get(9));
        attributes.put("Betways", attributesValues.get(10));
        attributes.put("Features", attributesValues.get(11));
        attributes.put("Theme", attributesValues.get(12));
        attributes.put("Objects", attributesValues.get(13));
        attributes.put("Genre", attributesValues.get(14));
        attributes.put("Other tags", attributesValues.get(15));
        attributes.put("Technology", attributesValues.get(16));
        attributes.put("Game Size", attributesValues.get(17));
        attributes.put("Last Update", attributesValues.get(18));

        return attributes;
    }

    public ObjectNode createCasinoAttributesObject(List<String> attributesValues) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode attributes = objectMapper.createObjectNode();

        attributes.put("Company", attributesValues.get(0));
        attributes.put("Launched", attributesValues.get(1));
        attributes.put("Jurisdiction", attributesValues.get(2));
        attributes.put("Live Chat", attributesValues.get(3));
        attributes.put("Site Sections", attributesValues.get(4));
        attributes.put("Games Q-ty", attributesValues.get(5));
        attributes.put("Providers Quantity", attributesValues.get(6));
        attributes.put("Bonus q-ty", attributesValues.get(7));
        attributes.put("Scanned Countries Q-ty", attributesValues.get(8));
        attributes.put("Min. deposit", attributesValues.get(9));
        attributes.put("Min. withdrawal", attributesValues.get(10));
        attributes.put("Currency", attributesValues.get(11));
        attributes.put("Languages on site", attributesValues.get(12));
        attributes.put("Support Chat Languages", attributesValues.get(13));
        attributes.put("Allowed Countries", attributesValues.get(14));
        return attributes;
    }

    public ObjectNode createDescriptionObject(String gameDescription) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode description = objectMapper.createObjectNode();

        description.put("description", gameDescription);

        return description;
    }
}
