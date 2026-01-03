package org.batfish.datamodel;

import static junit.framework.TestCase.assertEquals;
// import static junit.framework.TestCase.assertFalse;
// import static junit.framework.TestCase.assertTrue;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class Ip6SpaceReferenceTest {

  @Test
  public void testCompareSameClass() {
    Ip6SpaceReference ip6SR1 = new Ip6SpaceReference("s1", "d1");
    Ip6SpaceReference ip6SR2 = new Ip6SpaceReference("s1", "d1");
    Ip6SpaceReference ip6SR3 = new Ip6SpaceReference("s2", "d1");

    assertEquals(0, ip6SR1.compareSameClass(ip6SR2));
    assertEquals(-1, ip6SR1.compareSameClass(ip6SR3));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Ip6SpaceReference("s1", "d1"), new Ip6SpaceReference("s1", "d1"))
        .addEqualityGroup(new Ip6SpaceReference("s1", "d2"))
        .addEqualityGroup(new Ip6SpaceReference("s2", "d1"))
        .addEqualityGroup(new Ip6SpaceReference("s2", null))
        .testEquals();
  }
}
