package page;

import base.PageBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import util.AppLogger;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HomePage extends PageBase {
    public HomePage(WebDriver driver) {
        super(driver);
        AppLogger.info("HomePage instance created.");
        driver.get("valid url ");
    }

    //Web Elements
    @FindBy (id = "selsearch")
    private WebElement searchBar;
    @FindBy (className = "tt-menu")
    private WebElement searchBarResult;
    @FindBy (className = "tt-dataset")
    private WebElement divResults;
    @FindBy (xpath = "//a[@class='anav']//img")
    private WebElement logoElement;
    @FindBy (xpath = "//div[@class='breadcrumbs']//li[5]")
    private WebElement gameNameWE;
    @FindBy (xpath = "//div[@class='slotAttrReview']//table")
    private WebElement slotGameAttributes;
    @FindBy (xpath = "//div[@id='casino-attributes']//table")
    private WebElement casinoGameAttributes;
    @FindBy (id = "game-review")
    private WebElement gameDescription;

    JsonFileReadAndWrite jsonFileReadAndWrite = new JsonFileReadAndWrite();

    //Methods
    public void verifyHomePage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        String actualUrl = driver.getCurrentUrl();
        String expectedUrl = "valid url";
        Assert.assertEquals(actualUrl, expectedUrl, "Url doesnt match");

        wait.until(ExpectedConditions.elementToBeClickable(searchBar));
        Assert.assertEquals(searchBar.getAttribute("placeholder"),
                "Search for a Casino, Game or Developer",
                "Placeholder text doesnt match");

        AppLogger.info("Home page verification completed.");
    }

    public void searchAndSubmitGameName(String gameName) throws InterruptedException {
        scrollToElement(searchBar);
        searchBar.sendKeys(Keys.CONTROL + "a");
        searchBar.sendKeys(Keys.DELETE);
        searchBar.sendKeys(gameName);
        Thread.sleep(1000);
    }

    public List<WebElement> getAElements() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOf(divResults));

        return divResults.findElements(By.tagName("p"));
    }

    public void loadJsonAndIterate() {
        String filePath = "src/resources/json/for_ripping.json";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonData = objectMapper.readTree(new File(filePath));

            Iterator<JsonNode> elements = jsonData.elements();
            while (elements.hasNext()) {
                JsonNode item = elements.next();
                String gameName = item.get("game").asText();
                String provider = item.get("provider").asText();
                String formattedString = (gameName + " (" + provider + ")").toLowerCase();
                searchAndSubmitGameName(gameName);

                if (searchWithOneResult() || performSearchWithMoreGameName(formattedString) ||
                        searchWithoutApostrophes(gameName, formattedString) || searchGameBySection(gameName, formattedString)) {
                    processGame(gameName, provider);
                } else {
                    jsonFileReadAndWrite.writeNoGameFoundIntoJson(gameName, provider);
                }
            }
        } catch (IOException | InterruptedException e) {
            AppLogger.severe("Error during JSON data loading and iteration", e);
        }
    }

    public void processGame(String gameName, String provider) throws InterruptedException {
        String slotFilePath = "src/resources/json/casino_game_info.json";
        String casinoFilePath = "src/resources/json/slot_game_info.json";
        List<String> gameData = getBasicInfoAboutGame();
        GameAttributesResult result = checkAndGetGameAttributes();
        String gameType = result.getGameType();
        List<String> attributesValues = result.getAttributes();
        String gameDescription = getGameDescription();

        if (gameType.equals("Slot")){
            jsonFileReadAndWrite.writeGameDataIntoJson(slotFilePath, gameName, provider, gameData, attributesValues, gameType, gameDescription);
        } else if (gameType.equals("Casino")){
            jsonFileReadAndWrite.writeGameDataIntoJson(casinoFilePath, gameName, provider, gameData, attributesValues, gameType, gameDescription);
        }
    }

    public boolean searchWithOneResult() {
        List<WebElement> aElements = getAElements();
        if (aElements.size() == 1) {
            aElements.get(0).click();
            return true;
        }
        return false;
    }

    public boolean performSearchWithMoreGameName(String formattedString) {
        List<WebElement> aElements = getAElements();

        if (aElements.size() > 1) {
            double maxSimilarity = 0.0;
            WebElement mostSimilarResult = null;

            for (WebElement aElement : aElements) {
                String modifiedResultText = getElementsText(aElement);
                double similarity = calculateLevenshteinSimilarity(modifiedResultText, formattedString);

                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    mostSimilarResult = aElement;
                }
            }
            if (mostSimilarResult != null && maxSimilarity >= 0.9) {
                mostSimilarResult.click();
                return true;
            }
        }
        return false;
    }

    public boolean searchWithoutApostrophes(String gameName, String formattedString) throws InterruptedException {
        String modifiedGameWithoutApostrophes = adjustSearchQuery(gameName);
        searchAndSubmitGameName(modifiedGameWithoutApostrophes);
        List<WebElement> aElements = getAElements();

        if (aElements.size() > 1) {
            double maxSimilarity = 0.0;
            WebElement mostSimilarResult = null;

            for (WebElement aElement : aElements) {
                String modifiedResultText = getElementsText(aElement);
                double similarity = calculateLevenshteinSimilarity(modifiedResultText, formattedString);

                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    mostSimilarResult = aElement;
                }
            }
            if (mostSimilarResult != null && maxSimilarity >= 0.9) {
                mostSimilarResult.click();
                return true;
            }
        }
        return false;
    }

    public boolean searchGameBySection(String gameName, String formattedString) throws InterruptedException {
        String modifiedGameBySection = countAndReturnFirstWord(gameName);
        searchAndSubmitGameName(modifiedGameBySection);
        List<WebElement> aElements = getAElements();

        double maxSimilarity = 0.0;
        WebElement mostSimilarResult = null;

        for (WebElement aElement : aElements) {
            String modifiedResultText = getElementsText(aElement);
            double similarity = calculateLevenshteinSimilarity(modifiedResultText, formattedString);

            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                mostSimilarResult = aElement;
            }
        }
        if (mostSimilarResult != null && maxSimilarity >= 0.9) {
            mostSimilarResult.click();
            return true;
        }

        return false;
    }

    public String getElementsText(WebElement element) {
        String fullElementText = element.getText();
        String spanText = element.findElement(By.tagName("span")).getText();
        String textWithoutSpan = fullElementText.replace(spanText, "");

        if (textWithoutSpan.contains("(") || textWithoutSpan.contains(")")) {
            String textWithoutBrackets =textWithoutSpan.replaceAll("\\s*\\(.*?\\)\\s*", "");
            return (textWithoutBrackets + " (" + spanText + ")").toLowerCase();
        }
        else {
            return (textWithoutSpan + " (" + spanText + ")").toLowerCase();
        }
    }

    public static double calculateLevenshteinSimilarity(String string1, String string2) {
        LevenshteinDistance levenshteinDistance = LevenshteinDistance.getDefaultInstance();
        int distance = levenshteinDistance.apply(string1, string2);

        return 1.0 - (double) distance / Math.max(string1.length(), string2.length());
    }

    public String adjustSearchQuery(String originalQuery) {
        return originalQuery.replace("'", "");
    }

    public static String countAndReturnFirstWord(String gameName) {
        if (gameName == null || gameName.isEmpty()) {
            return null;
        }
        String[] words = gameName.split("\\s+");

        if (words.length == 1) {
            return words[0];
        } else if (words.length >= 2){
            return words[0] + " " + words[1];
        } else {
           return null;
        }
    }

    public List<String> getBasicInfoAboutGame() {
        List<String> gameData = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.urlContains("valid url"));
        String gameUrl = driver.getCurrentUrl();

        wait.until(ExpectedConditions.visibilityOf(gameNameWE));
        String nameGame = gameNameWE.getText();

        wait.until(ExpectedConditions.visibilityOf(logoElement));
        String gameLogoUrl = logoElement.getAttribute("src");

        gameData.add(nameGame);
        gameData.add(gameUrl);
        gameData.add(gameLogoUrl);

        AppLogger.info("Basic information retrieved successfully.");
        return gameData;
    }

    public GameAttributesResult checkAndGetGameAttributes() {
        List<String> gameAttributes;
        String gameType;
        try{
            gameAttributes = slotGameAttributes();
            gameType = "Slot";
        } catch (NoSuchElementException e) {
            gameAttributes = casinoGameAttributes();
            gameType = "Casino";
            AppLogger.warning("Error while retrieving game attributes. Falling back to casino attributes.", e);
        }

        AppLogger.info("Game attributes checked successfully.");
        return new GameAttributesResult(gameType, gameAttributes);
    }

    public List<String> slotGameAttributes() {
        scrollToElement(slotGameAttributes);
        List<WebElement> rows = slotGameAttributes.findElements(By.tagName("tr"));
        List<String> gameAttributes = new ArrayList<>();

        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            StringBuilder rowAttributesData = new StringBuilder();
            for (WebElement cell : cells) {
                rowAttributesData.append(cell.getText()).append("\t");
            }
            gameAttributes.add(rowAttributesData.toString());
        }
        AppLogger.info("Slot game attributes retrieved successfully.");
        return gameAttributes;
    }

    public List<String> casinoGameAttributes() {
        scrollToElement(casinoGameAttributes);
        WebElement attrTableLeft = casinoGameAttributes.findElement(By.cssSelector(".attrTableLeft"));
        WebElement attrTableRight = casinoGameAttributes.findElement(By.cssSelector(".attrTableRight"));
        StringBuilder rowAttributes = new StringBuilder();
        List<String> gameAttributes = new ArrayList<>();

        List<WebElement> leftTableAttributes = attrTableLeft.findElements(By.cssSelector("td.propLeft"));
        List<WebElement> rightTableAttributes = attrTableRight.findElements(By.cssSelector("td.propLeft"));

        for (WebElement attribute : leftTableAttributes) {
            rowAttributes.append(attribute.getText()).append("\t");
        }
        for (WebElement attribute : rightTableAttributes) {
            rowAttributes.append(attribute.getText()).append("\t");
        }
        gameAttributes.add(rowAttributes.toString());
        AppLogger.info("Casino game attributes retrieved successfully.");
        return gameAttributes;
    }

    public String getGameDescription() {
        try {
            if (gameDescription != null) {
                scrollToElement(gameDescription);
                List<WebElement> paragraphs = gameDescription.findElements(By.tagName("p"));
                StringBuilder description = new StringBuilder();

                for (WebElement paragraph : paragraphs) {
                    description.append(paragraph.getText()).append(" ");
                }
                AppLogger.info("Game description retrieved successfully.");
                return description.toString();
            }
        } catch (Exception e) {
            AppLogger.severe("Error while fetching game description: " + e.getMessage(), e);
        }
        return "";
    }

    private void scrollToElement(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView();", element);
    }

}
