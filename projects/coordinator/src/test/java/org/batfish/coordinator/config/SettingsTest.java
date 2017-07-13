package org.batfish.coordinator.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

/**
 * Tests for {@link Settings}.
 */
public class SettingsTest {

   @Test
   public void testDefaultValue() throws Exception {
      Settings settings = new Settings(new String[]{});
      assertThat(settings.getPoolBindHost(), equalTo("0.0.0.0"));
      assertThat(settings.getWorkBindHost(), equalTo("0.0.0.0"));
   }

   @Test
   public void testGetBothValues() throws Exception {
      String[] args = new String[]{
            "-poolbindhost=10.10.10.10",
            "-workbindhost=20.20.20.20"
      };
      Settings settings = new Settings(args);
      assertThat(settings.getPoolBindHost(), equalTo("10.10.10.10"));
      assertThat(settings.getWorkBindHost(), equalTo("20.20.20.20"));
   }

}