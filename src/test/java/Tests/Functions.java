package Tests;

import Framework.BaseTestCase;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.List;

public class Functions extends BaseTestCase {

    String url = xmlReader.getValueByName("URL");
    String tableId = xmlReader.getValueByName("TABLE_XPATH");
    int searchColumnInt = Integer.parseInt(xmlReader.getValueByName("SEARCH_COLUMN_INT"));
    String searchColumnText = xmlReader.getValueByName("SEARCH_COLUMN_TEXT");
    String searchText = xmlReader.getValueByName("SEARCH_TEXT");
    int returnColumnText = Integer.parseInt(xmlReader.getValueByName("RETURN_COLUMN_TEXT"));

    WebElement table;

    //Constructor
    public Functions() {
        super(getChromeDriverWithMaximizedWindow());
    }

    private static WebDriver getChromeDriverWithMaximizedWindow() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        return new ChromeDriver(options);
    }

    private Boolean areParametersEmpty(String url, String tableId, Integer searchColumnInt, String searchColumnText, String searchText, Integer returnColumnText) {
        // Check if any of the parameters is null or empty
        return (url != null) && (tableId != null) && (searchColumnInt > 0) && (searchColumnText != null) && (searchText != null) && (!returnColumnText.equals(searchColumnInt) && returnColumnText != 0);
    }

    private Boolean checkIfSearchTextOnCorrectColumn(int numberOfColumns, int indexOfSearchColumn, int indexOfSearchText) {
        int SearchRow = (indexOfSearchText / numberOfColumns) + 1;
        int Diff;
        int res = (numberOfColumns * SearchRow) - indexOfSearchText;

        switch (indexOfSearchColumn)
        {
            case 1:
                Diff = 3;
                return res == Diff;
            case 2:
                Diff = 2;
                return res == Diff;
            case 3:
                Diff = 1;
                return res == Diff;
            default:
                System.out.println("Number Of Columns Out Of The Range");
                return false;
        }
    }

    private String returnColumnText(List<WebElement> tableCells, int indexCellOfSearchText, int searchColumn, int returnColumn) {
        int Diff = returnColumn - searchColumn;
        WebElement el = tableCells.get(indexCellOfSearchText + Diff);
        return el.getText();
    }

    @BeforeClass
    public void SetUp() {
        // ENV Params
        this.url = xmlReader.getValueByName("URL");
        this.tableId = xmlReader.getValueByName("TABLE_XPATH");
        this.searchColumnInt = Integer.parseInt(xmlReader.getValueByName("SEARCH_COLUMN_INT"));
        this.searchColumnText = xmlReader.getValueByName("SEARCH_COLUMN_TEXT");
        this.searchText = xmlReader.getValueByName("SEARCH_TEXT");
        this.returnColumnText = Integer.parseInt(xmlReader.getValueByName("RETURN_COLUMN_TEXT"));

        //Check ENV Params
        Boolean isEmpty = areParametersEmpty(url, tableId, searchColumnInt, searchColumnText, searchText, returnColumnText);
        if (!isEmpty)
        {
            Assert.fail("ONE OF THE PARAMS IS NULL OR EMPTY, PLEASE CHECK 'Resources.xml'");
        } else
        {
            try
            {
                //Open BROWSER
                guiHandler.openBrowser(url);
                this.table = guiHandler.findElementByXPath(tableId);
                if (this.table == null)
                {
                    Assert.fail("TABLE NOT FOUND");
                }
            } catch (Exception e)
            {
                System.out.println("ERROR WILL OPENING BROWSER");
                e.printStackTrace();
            }
        }
    }

    @Test
    //Using 'SearchColumnIndex'
    public void Ex1_v1_1() {
        try
        {
            WebElement table = guiHandler.findElementByXPath(tableId);
            if (table != null)
            {
                System.out.println("Table Found");
                String Result = getTableCellText(table, searchColumnInt, searchText, returnColumnText);
                System.out.println("RESULT =\t" + Result);
            } else System.out.println("Not Found");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    //Using 'SearchColumnText'
    public void Ex1_v1_2() {
        try
        {
            String Result = getTableCellText(table, searchColumnText, searchText, returnColumnText);
            System.out.println("RESULT =\t" + Result);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    //Verify Cell Text By 'Correct' Value, - SearchColumn Index
    public void Ex1_v2_2() {
        try
        {
            String expected = xmlReader.getValueByName("EXPECTED_TEXT_PASS");
            Boolean flag = verifyTableCellText(table, searchColumnInt, searchText, returnColumnText, expected);
            System.out.println("RESULT =\t" + flag);
            Assert.assertTrue(flag);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    //Verify Cell Text By 'Wrong' Value - SearchColumn Text
    public void Ex1_v2_2_1() {
        try
        {
            String expected = xmlReader.getValueByName("EXPECTED_TEXT_FAIL");
            Boolean flag = verifyTableCellText(table, searchColumnText, searchText, returnColumnText, expected);
            System.out.println("RESULT =\t" + flag);
            Assert.assertFalse(flag);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getTableCellText(WebElement table, String searchColumn, String searchText, int returnColumnText) {
        int sc;
        switch (searchColumn)
        {
            case "Company":
                sc = 1;
                break;
            case "Contact":
                sc = 2;
                break;
            case "Country":
                sc = 3;
                break;
            default:
                return null;
        }
        if (sc == returnColumnText)
        {
            Assert.fail("Can't Be 'Search Column' and 'Return Column' Same Value");
        }
        return getTableCellText(table, sc, searchText, returnColumnText);
    }

    public String getTableCellText(WebElement table, int searchColumn, String searchText, int returnColumnText) {
        try
        {
            if (table != null)
            {
                String tableHeaderId = xmlReader.getValueByName("TABLE_HEADER_XPATH");
                String tableCellsId = xmlReader.getValueByName("TABLE_CELLS_IN_ROWS_XPATH");

                int numberOfColumns = guiHandler.countVisibleElementsByXPath(tableHeaderId); //3
                int numberOfCells = guiHandler.countVisibleElementsByXPath(tableCellsId); //18

                if (searchColumn <= numberOfColumns && returnColumnText <= numberOfColumns && numberOfCells != 0)
                {
                    String searchTextId = xmlReader.getValueByName("SEARCH_TEXT_XPATH");
                    String xPathString = String.format(searchTextId, searchText);

                    WebElement cell = guiHandler.findElementByXPath(xPathString);
                    if (cell == null)
                    {
                        Assert.fail(String.format("Search Text = '%s', Wasn't Found in the Table", searchText));
                    } else
                    {
                        List<WebElement> tableCells = guiHandler.findElementsByXPath(tableCellsId);

                        int index = tableCells.indexOf(cell);

                        // Checking if the 'SearchText' into the 'SearchColumn'
                        if (!checkIfSearchTextOnCorrectColumn(numberOfColumns, searchColumn, index))
                        {
                            Assert.fail(String.format("'%s' NOT FOUND ON COLUMN [%d]", searchText, searchColumn));
                        }

                        return returnColumnText(tableCells, index, searchColumn, returnColumnText);
                    }
                } else
                {
                    System.out.println("THE SEARCH COLUMN OUT OF THE CURRENT TABLE");
                }
            } else System.out.println("Not Found");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean verifyTableCellText(WebElement table, int searchColumn, String searchText, int returnColumnText, String expectedText) {
        String check = getTableCellText(table, searchColumn, searchText, returnColumnText);
        return check.equals(expectedText);
    }

    public Boolean verifyTableCellText(WebElement table, String searchColumn, String searchText, int returnColumnText, String expectedText) {
        String check = getTableCellText(table, searchColumn, searchText, returnColumnText);
        return check.equals(expectedText);
    }

    public String getTableCellTextByXpath(WebElement table, int searchColumn, String searchText, int returnColumnText) throws Exception {

        return null;
    }
}
