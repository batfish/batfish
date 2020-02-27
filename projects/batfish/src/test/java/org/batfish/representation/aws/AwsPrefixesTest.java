package org.batfish.representation.aws;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AwsPrefixesTest {

  @Test
  public void testGetPrefixes() {
    assertTrue(AwsPrefixes.getPrefixes().size() > 0);
  }
}
