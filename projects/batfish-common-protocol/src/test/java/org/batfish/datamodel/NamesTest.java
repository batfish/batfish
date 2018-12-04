package org.batfish.datamodel;

import static org.batfish.datamodel.Names.zoneToZoneFilter;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/** Tests of {@link Names}. */
public class NamesTest {
  @Test
  public void testZoneToZoneFilter() {
    assertThat(zoneToZoneFilter("a", "b"), equalTo("zone~a~to~zone~b"));
  }
}
