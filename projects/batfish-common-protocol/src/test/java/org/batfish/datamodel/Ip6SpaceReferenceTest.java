package org.batfish.datamodel;

import static junit.framework.TestCase.assertEquals;
// import static junit.framework.TestCase.assertFalse;
// import static junit.framework.TestCase.assertTrue;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class Ip6SpaceReferenceTest {
  private static final String IP_SPACE_NAME1 = "ip6Space1";
  private static final String IP_SPACE_NAME2 = "ip6Space2";
  private static final String DESCRIPTION1 = "description1";
  private static final String DESCRIPTION2 = "description2";

  @Test
  public void testCompareSameClass() {
    Ip6SpaceReference ip6SR1 = new Ip6SpaceReference(IP_SPACE_NAME1, DESCRIPTION1);
    Ip6SpaceReference ip6SR2 = new Ip6SpaceReference(IP_SPACE_NAME1, DESCRIPTION1);
    Ip6SpaceReference ip6SR3 = new Ip6SpaceReference(IP_SPACE_NAME2, DESCRIPTION1);

    assertEquals(0, ip6SR1.compareSameClass(ip6SR2));
    assertEquals(-1, ip6SR1.compareSameClass(ip6SR3));
  }

  @Test
  public void testExprEquals() {
    Ip6SpaceReference ip6SR1 = new Ip6SpaceReference(IP_SPACE_NAME1, DESCRIPTION1);
    Ip6SpaceReference ip6SR2 = new Ip6SpaceReference(IP_SPACE_NAME1, DESCRIPTION1);
    Ip6SpaceReference ip6SR3 = new Ip6SpaceReference(IP_SPACE_NAME1, DESCRIPTION2);
    Ip6SpaceReference ip6SR4 = new Ip6SpaceReference(IP_SPACE_NAME2, DESCRIPTION1);
    Ip6SpaceReference ip6SR5 = new Ip6SpaceReference(IP_SPACE_NAME2, DESCRIPTION2);

    new EqualsTester()
        .addEqualityGroup(ip6SR1, ip6SR2)
        .addEqualityGroup(ip6SR3)
        .addEqualityGroup(ip6SR4)
        .addEqualityGroup(ip6SR5)
        .testEquals();
  }
}
