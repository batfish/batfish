package org.batfish.specifier.parboiled;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.Test;

public class GrammarTest {

  /** Check that the base URL is valid. This test will fail without Internet connectivity */
  @Test
  public void testBaseUrl() throws IOException {
    HttpURLConnection huc = (HttpURLConnection) new URL(Grammar.BASE_URL).openConnection();
    huc.setRequestMethod("GET");
    huc.connect();
    assertThat(huc.getResponseCode(), equalTo(HttpURLConnection.HTTP_OK));
  }
}
