package ariba.ai.testing.selenium.webdriver;

import java.util.List;
import java.util.StringTokenizer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Models a SELECT tag, providing helper methods to select and deselect options.
 * Has the additional isVisibleTextPresent method
 * 
 * @author aoesterholm
 *
 */
public class SelectExtra extends Select {

    private WebElement element2;

    public SelectExtra (WebElement element)
    {
        super(element);
        // Had to copy element reference since it's private in the Select class
        // /aoesterholm
        element2 = element;
    }

    /**
     * Check if the text is the text of any of the <Option> tags in the <Select>
     * menu
     * 
     * @author karthur
     * @param text
     * @return
     */
    public boolean isVisibleTextPresent (String text)
    {
        // try to find the option via XPATH ...
        List<WebElement> options = element2.findElements(By
                .xpath(".//option[normalize-space(.) = " + escapeQuotes(text) + "]"));

        boolean matched = false;
        for (WebElement option : options) {
            // setSelected(option);

            matched = true;
        }

        if (options.isEmpty() && text.contains(" ")) {
            String subStringWithoutSpace = getLongestSubstringWithoutSpace(text);
            List<WebElement> candidates;
            if ("".equals(subStringWithoutSpace)) {
                // hmm, text is either empty or contains only spaces - get all
                // options ...
                candidates = element2.findElements(By.tagName("option"));
            }
            else {
                // get candidates via XPATH ...
                candidates = element2.findElements(By.xpath(".//option[contains(., "
                        + escapeQuotes(subStringWithoutSpace) + ")]"));
            }
            for (WebElement option : candidates) {
                if (text.equals(option.getText())) {
                    // setSelected(option);

                    matched = true;
                }
            }
        }
        return matched;
    }

    /**
     * Had to copy this from the Select class since it's private there
     * /aoesterholm.
     * 
     * @author aoesterholm
     * @param s
     * @return
     */
    private String getLongestSubstringWithoutSpace (String s)
    {
        String result = "";
        StringTokenizer st = new StringTokenizer(s, " ");
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (t.length() > result.length()) {
                result = t;
            }
        }
        return result;
    }
}