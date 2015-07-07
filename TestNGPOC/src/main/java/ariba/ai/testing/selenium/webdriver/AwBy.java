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

  public static AwBy awnameSubstring (String awNameSubstring)
  {
      return new AwBy("//*[contains(@awname, '" + awNameSubstring + "')]");
  }

  public static AwBy parentOf (String awname)
  {
      return new AwBy(PRE + awname + POST + "/..");
  }

  public static AwBy firstDivChildOf (String awname)
  {
      return new AwBy(PRE + awname + POST + "/div");
  }

  public static AwBy awDropDownMenuName (String awname)
  {
      return new AwBy(PRE + awname + POST + "/div/div");
  }

  public static AwBy awDropDownMenuTextInput (String awname)
  {
      return new AwBy(PRE + awname + POST + "/span/div/div[2]/a/div");
  }

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

}
