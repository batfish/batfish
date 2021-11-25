package org.batfish.vendor.sonic.representation;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.vendor.sonic.representation.ConfigDb.Data;
import org.junit.Test;

public class SonicConfigDbsTest extends TestCase {

  @Test
  public void testJavaSerialization() {
    SonicConfigDbs obj = new SonicConfigDbs(ImmutableMap.of());
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new SonicConfigDbs(ImmutableMap.of()), new SonicConfigDbs(ImmutableMap.of()))
        .addEqualityGroup(
            new SonicConfigDbs(
                ImmutableMap.of("h1", new ConfigDb("file", new Data(ImmutableMap.of())))))
        .testEquals();
  }
}
