package org.batfish.coordinator.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

/**
 * Tests for {@link Settings}
 */
public class SettingsTest {

   @Test
   public void testDefaultValue() throws Exception {
      Settings settings = new Settings(new String[]{});
      assertThat(settings.getServicePoolHost(), equalTo("0.0.0.0"));
      assertThat(settings.getServiceWorkHost(), equalTo("0.0.0.0"));
   }

   @Test
   public void testGetServicePoolHost() throws Exception {
      String[] args = new String[]{
            "-poolbindhost=10.10.10.10"
      };
      Settings settings = new Settings(args);
      assertThat(settings.getServicePoolHost(), equalTo("10.10.10.10"));
   }

   @Test
   public void testGetServiceWorkHost() throws Exception {
      String[] args = new String[]{
            "-workbindhost=10.10.10.10"
      };
      Settings settings = new Settings(args);
      assertThat(settings.getServiceWorkHost(), equalTo("10.10.10.10"));
   }

   @Test
   public void testGetBothValues() throws Exception {
      String[] args = new String[]{
            "-poolbindhost=10.10.10.10",
            "-workbindhost=20.20.20.20"
      };
      Settings settings = new Settings(args);
      assertThat(settings.getServicePoolHost(), equalTo("10.10.10.10"));
      assertThat(settings.getServiceWorkHost(), equalTo("20.20.20.20"));
   }


}