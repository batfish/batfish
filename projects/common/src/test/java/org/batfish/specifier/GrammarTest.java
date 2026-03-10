package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
