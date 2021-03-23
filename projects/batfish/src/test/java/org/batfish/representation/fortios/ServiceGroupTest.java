package org.batfish.representation.fortios;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ServiceGroupTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testContainsDirectly() {
    ServiceGroup parent = new ServiceGroup("parent", new BatfishUUID(0));

    BatfishUUID childId1 = new BatfishUUID(1);
    parent.getMemberUUIDs().add(childId1);

    BatfishUUID childId2 = new BatfishUUID(2);
    parent.getMemberUUIDs().add(childId2);

    assertTrue(parent.contains(childId1, ImmutableSet.of()));
    assertTrue(parent.contains(childId2, ImmutableSet.of()));
  }

  @Test
  public void testContainsIndirectly() {
    ServiceGroup parent = new ServiceGroup("parent", new BatfishUUID(0));

    BatfishUUID childId1 = new BatfishUUID(1);
    parent.getMemberUUIDs().add(childId1);

    BatfishUUID childId2 = new BatfishUUID(2);
    ServiceGroup child2 = new ServiceGroup("child2", childId2);
    parent.getMemberUUIDs().add(childId2);

    BatfishUUID grandChildId1 = new BatfishUUID(3);
    ServiceGroup grandChild1 = new ServiceGroup("grandChild1", grandChildId1);
    child2.getMemberUUIDs().add(grandChildId1);

    BatfishUUID greatGrandChildId1 = new BatfishUUID(4);
    grandChild1.getMemberUUIDs().add(greatGrandChildId1);

    assertTrue(parent.contains(grandChildId1, ImmutableSet.of(child2, grandChild1)));
    assertTrue(parent.contains(greatGrandChildId1, ImmutableSet.of(child2, grandChild1)));
  }

  @Test
  public void testContainsNoMatch() {
    ServiceGroup parent = new ServiceGroup("name", new BatfishUUID(0));

    BatfishUUID childId1 = new BatfishUUID(1);
    parent.getMemberUUIDs().add(childId1);

    BatfishUUID childId2 = new BatfishUUID(2);
    ServiceGroup child2 = new ServiceGroup("child2", childId2);
    parent.getMemberUUIDs().add(childId2);

    BatfishUUID grandChildId1 = new BatfishUUID(3);
    child2.getMemberUUIDs().add(grandChildId1);

    BatfishUUID orphan = new BatfishUUID(4);

    assertFalse(parent.contains(orphan, ImmutableSet.of(child2)));
  }
}
