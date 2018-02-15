package org.batfish.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.batfish.common.CleanBatfishException;
import org.batfish.main.Driver.RunMode;
import org.junit.Test;

/** Test for {@link org.batfish.config.Settings} */
public class SettingsTest {

  @Test
  public void testDefaultDPEngine() {
    Settings settings = new Settings(new String[] {});
    assertThat(settings.getDataPlaneEngineName(), equalTo("bdp"));
  }

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

  @Test
  public void testBooleanParsing() {
    Settings settings = new Settings(new String[] {"-register=true"});
    assertThat(settings.getCoordinatorRegister(), is(true));

    settings = new Settings(new String[] {"-register=false"});
    assertThat(settings.getCoordinatorRegister(), is(false));
  }

  @Test(expected = CleanBatfishException.class)
  public void testBoolenParsingBogusValue() {
    Settings settings = new Settings(new String[] {"-register=blah"});
    assertThat(settings.getCoordinatorRegister(), is(false));
  }

  @Test
  public void testRunModeCaseInsensitive() {
    Settings settings = new Settings(new String[] {"-runmode=workservice"});
    assertThat(settings.getRunMode(), equalTo(RunMode.WORKSERVICE));
  }
}
