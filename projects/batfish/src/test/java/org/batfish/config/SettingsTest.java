package org.batfish.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Paths;
import org.batfish.common.CleanBatfishException;
import org.batfish.main.Driver.RunMode;
import org.junit.Test;

/** Test for {@link org.batfish.config.Settings} */
public class SettingsTest {

  /** Test default dataplane engine is ibdp */
  @Test
  public void testDefaultDPEngine() {
    Settings settings = new Settings(new String[] {});
    assertThat(settings.getDataPlaneEngineName(), equalTo("ibdp"));
  }

  /** Test that settings copy is deep */
  @Test
  public void testCopy() {
    Settings origSettings = new Settings(new String[] {});
    origSettings.setDataplaneEngineName("NewValue");

    // Check that settings are preserved on copy
    Settings settings = new Settings(origSettings);
    assertThat(settings.getDataPlaneEngineName(), equalTo("NewValue"));

    // But reparsing the command line updates the value
    settings.parseCommandLine(new String[] {"-dataplaneengine=CmdValue"});
    assertThat(settings.getDataPlaneEngineName(), equalTo("CmdValue"));

    // Ensure re-parsing does not modify original config
    assertThat(
        origSettings.getDataPlaneEngineName(), not(equalTo(settings.getDataPlaneEngineName())));
  }

  /** Test that boolean parsing recognizes "true" or "false" */
  @Test
  public void testBooleanParsing() {
    Settings settings = new Settings(new String[] {"-register=true"});
    assertThat(settings.getCoordinatorRegister(), is(true));

    settings = new Settings(new String[] {"-register=false"});
    assertThat(settings.getCoordinatorRegister(), is(false));
  }

  /** Test that boolean parsing fails on garbage values and not defaults to just false. */
  @Test(expected = CleanBatfishException.class)
  public void testBoolenParsingBogusValue() {
    Settings settings = new Settings(new String[] {"-register=blah"});
    assertThat(settings.getCoordinatorRegister(), is(false));
  }

  /** Test that parsing of runmode is case insensitive */
  @Test
  public void testRunModeCaseInsensitive() {
    Settings settings = new Settings(new String[] {"-runmode=workservice"});
    assertThat(settings.getRunMode(), equalTo(RunMode.WORKSERVICE));
  }

  /**
   * Value of question path is allowed to have null and acts as a special value, ensure that's
   * supported.
   */
  @Test
  public void testQuestionPathAllowNull() {
    Settings settings = new Settings(new String[] {});
    settings.setQuestionPath(Paths.get("test"));

    assertThat(settings.getQuestionPath(), equalTo(Paths.get("test")));
    // Update to null
    settings.setQuestionPath(null);
    assertThat(settings.getQuestionPath(), is(nullValue()));
  }
}
