package org.batfish.specifier.parboiled;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.junit.Test;

public class GrammarTest {

  /** Check that the base URL is valid. This test will fail without Internet connectivity */
  @Test
  public void testBaseUrl() {
    try (Response r = ClientBuilder.newClient().target(Grammar.BASE_URL).request().get()) {
      assertThat(r.getStatus(), equalTo(200));
    }
  }
}
