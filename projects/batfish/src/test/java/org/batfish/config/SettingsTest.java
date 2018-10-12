package org.batfish.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
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

  @Test
  public void testLogfileWithDeltaTestrig() {
    // Only main testrig
    Settings settings =
        new Settings(new String[] {"-storagebase=/path", "-container=foo", "-testrig=main"});
    settings.setTaskId("tid");

    assertThat(settings.getLogFile(), equalTo("/path/foo/snapshots/main/output/tid.log"));

    // Delta testrig present
    settings =
        new Settings(
            new String[] {
              "-storagebase=/path", "-container=foo", "-testrig=main", "-deltatestrig=delta"
            });
    settings.setTaskId("tid");

    assertThat(settings.getLogFile(), equalTo("/path/foo/snapshots/delta/output/tid.log"));

    // Delta testrig present, but the question is differential
    settings =
        new Settings(
            new String[] {
              "-storagebase=/path",
              "-container=foo",
              "-testrig=main",
              "-deltatestrig=delta",
              "-differential=true"
            });
    settings.setTaskId("tid");

    assertThat(settings.getLogFile(), equalTo("/path/foo/snapshots/main/output/tid.log"));
  }

  @Test
  public void testDebugSettings() {
    Settings settings = new Settings(new String[] {"-debugflags=blah"});
    assertThat(settings.getDebugFlags(), equalTo(ImmutableList.of("blah")));
  }
}
