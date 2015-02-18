package ariba.ai.testing.selenium.webdriver;

import org.openqa.selenium.By.ByXPath;

public class AwBy extends ByXPath {

  /**
   * Default ID for now
   */
  private static final long serialVersionUID = 1L;

  public AwBy (String xpathExpression)
  {
      super(xpathExpression);
  }

  //private static String PRE = "//*[@awname=\"";
  //private static String POST = "\"]";
  private static String PRE = "//*";
  private static String POST = "";
  
  public enum Weekdays {
      Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
  };

  public static AwBy awname (String Awname)
  {
      return new AwBy(PRE + Awname + POST);
  }

  /**
   * 
   * @author aoesterholm
   * @param awNameSubstring
   * @return
   */
  public static AwBy awnameSubstring (String awNameSubstring)
  {
      return new AwBy("//*[contains(@awname, '" + awNameSubstring + "')]");
  }

  /**
   * get the AwBy for the parent element of the element with awname
   * 
   * @param awname
   * @author gavijay, aoesterholm
   */
  public static AwBy parentOf (String awname)
  {
      return new AwBy(PRE + awname + POST + "/..");
  }

  /**
   * Get the AwBy for the first div child of the awname
   * 
   * @author aoesterholm
   * @param awname
   * @return
   */
  public static AwBy firstDivChildOf (String awname)
  {
      return new AwBy(PRE + awname + POST + "/div");
  }

  /**
   * Use this only for the NextGen fancy looking AribaWeb drop down menus
   * where the user can't type text.
   * 
   * @param awname
   * @return
   */
  public static AwBy awDropDownMenuName (String awname)
  {
      return new AwBy(PRE + awname + POST + "/div/div");
  }

  /**
   * Use this for the Next Gen UI widget that is a text input field with a
   * drop down menu where the user can either enter text or select values from
   * the drop-down menu.
   * 
   * @param awname
   * @return
   */
  public static AwBy awDropDownMenuTextInput (String awname)
  {
      return new AwBy(PRE + awname + POST + "/span/div/div[2]/a/div");
  }

  /**
   * Use this only for the NextGen fancy looking AribaWeb drop down menus.
   * 
   * @param awname
   * @return
   */
  public static AwBy awWeekdayCheckbox (String awname, Weekdays weekday)
  {
      String suffix = "";
      switch (weekday) {
      case Sunday:
          suffix = "/div/div";
          break;
      case Monday:
          suffix = "/div[3]/div";
          break;
      case Tuesday:
          suffix = "/div[5]/div";
          break;
      case Wednesday:
          suffix = "/div[7]/div";
          break;
      case Thursday:
          suffix = "/div[9]/div";
          break;
      case Friday:
          suffix = "/div[11]/div";
          break;
      case Saturday:
          suffix = "/div[13]/div";
          break;
      default:
          break;
      }
      return new AwBy(PRE + awname + POST + suffix);
  }

  /**
   * Try to use AwBy.awname instead. I know it's not always possible, but the
   * deprecated annotation is here to remind you to try.
   * 
   * @param linkText
   * @return
   */
  @Deprecated
  public static AwBy linkText (String linkText)
  {
      // return new AwBy("//a[contains(text(),'" + linkText + "')]");
      return new AwBy("//a[normalize-space(text())='" + linkText + "']");
  }

  /**
   * Returns the AwBy for the first matched element for the provided link text
   * substring.
   * 
   * @author aoesterholm
   * @deprecated Try to use AwBy.awname instead. I know it's not always
   *             possible, but the deprecated annotation is here to remind you
   *             to try.
   * @param linkTextSubstring
   * @return
   */
  @Deprecated
  public static AwBy linkTextSubstring (String linkTextSubstring)
  {
      return new AwBy("//a[normalize-space(text())[contains(.,\"" + linkTextSubstring
              + "\")]]");
  }

  /**
   * Try to use AwBy.awname instead. I know it's not always possible, but the
   * deprecated annotation is here to remind you to try.
   * 
   * @param linkText
   * @return
   */
  @Deprecated
  public static AwBy xpath (String xpathExpression)
  {
      return new AwBy(xpathExpression);
  }

  /**
   * NOT SUPPORTED in LAF. Throws UnsupportedOperationException. The reason is
   * that awname and xpath are more robust for locators and work in all cases
   * that have been found so far If you think this method should be supported
   * then you must present the case to the LAF team.
   * 
   * @param cssPath
   * @return
   * @throws UnsupportedOperationException
   */
  @Deprecated
  public static AwBy cssSelector (String cssPath)
  {
      throw new UnsupportedOperationException(
              "LAF does not support cssSelector(). You should use awname");
  }

  /**
   * NOT SUPPORTED in LAF. Throws UnsupportedOperationException. The reason is
   * that awname and xpath are more robust for locators and work in all cases
   * that have been found so far If you think this method should be supported
   * then you must present the case to the LAF team.
   * 
   * @param classNameString
   * @return
   * @throws UnsupportedOperationException
   */
  @Deprecated
  public static AwBy className (String classNameString)
  {

      throw new UnsupportedOperationException(
              "LAF does not support className(). You should use awname");
  }

  /**
   * NOT SUPPORTED in LAF. Throws UnsupportedOperationException. The reason is
   * that awname and xpath are more robust as locators and work in all cases
   * that have been found so far If you think this method should be supported
   * then you must present the case to the LAF team.
   * 
   * @param id
   * @return
   * @throws UnsupportedOperationException
   */
  @Deprecated
  public static AwBy id (String id)
  {
      throw new UnsupportedOperationException(
              "LAF does not support id(). You should use awname");
  }

  /**
   * NOT SUPPORTED in LAF. Throws UnsupportedOperationException. The reason is
   * that awname and xpath are more robust as locators and work in all cases
   * that have been found so far If you think this method should be supported
   * then you must present the case to the LAF team.
   * 
   * @param name
   * @return
   * @throws UnsupportedOperationException
   */
  @Deprecated
  public static AwBy name (String name)
  {
      throw new UnsupportedOperationException(
              "LAF does not support name(). You should use awname");
  }

  /**
   * NOT SUPPORTED in LAF. Throws UnsupportedOperationException. The reason is
   * that awname and xpath are more robust as locators and work in all cases
   * that have been found so far If you think this method should be supported
   * then you must present the case to the LAF team.
   * 
   * @param id
   * @return
   * @throws UnsupportedOperationException
   */
  @Deprecated
  public static AwBy partialLinkText (String partialLinkText)
  {
      throw new UnsupportedOperationException(
              "LAF does not support partialLinktext(). You should use awname");
  }

}
