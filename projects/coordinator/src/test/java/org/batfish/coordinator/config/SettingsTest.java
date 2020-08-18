package org.batfish.coordinator.config;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

/** Tests for {@link Settings}. */
public class SettingsTest {

  @Test
  public void testDefaultValue() {
    Settings settings = new Settings(new String[] {});
    assertThat(settings.getPoolBindHost(), equalTo("localhost"));
    assertThat(settings.getWorkBindHost(), equalTo("0.0.0.0"));
  }

  @Test
  public void testGetBothValues() {
    String[] args = new String[] {"-poolbindhost=10.10.10.10", "-workbindhost=20.20.20.20"};
    Settings settings = new Settings(args);
    assertThat(settings.getPoolBindHost(), equalTo("10.10.10.10"));
    assertThat(settings.getWorkBindHost(), equalTo("20.20.20.20"));
  }

  /** Ensure {@link Path} objects are stored/returned properly (with default values) */
  @Test
  public void testGetPathDefault() {
    Settings settings = new Settings(new String[] {});
    assertThat(settings.getSslPoolKeystoreFile(), is(nullValue()));
    Path keyfile = Paths.get("keyfile");
    settings.setSslPoolKeystoreFile(keyfile);
    assertThat(settings.getSslPoolKeystoreFile(), equalTo(keyfile));
  }

  /** Ensure lists of {@link Path} objects are stored/returned properly */
  @Test
  public void testGetPathList() {
    String[] args = new String[] {"-templatedirs=path1,path2"};
    Settings settings = new Settings(args);
    assertThat(
        settings.getQuestionTemplateDirs(), contains(Paths.get("path1"), Paths.get("path2")));
  }
}
