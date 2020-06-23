package org.batfish.representation.aws;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AwsPrefixesTest {

  @Test
  public void testGetPrefixes() {
    assertTrue(AwsPrefixes.getPrefixes().size() > 0);
  }

  @Test
  public void testGetPrefixes_service() {
    assertTrue(AwsPrefixes.getPrefixes(AwsPrefixes.SERVICE_AMAZON).size() > 0);
    assertTrue(
        AwsPrefixes.getPrefixes(AwsPrefixes.SERVICE_AMAZON).size()
            < AwsPrefixes.getPrefixes().size());
  }
}
