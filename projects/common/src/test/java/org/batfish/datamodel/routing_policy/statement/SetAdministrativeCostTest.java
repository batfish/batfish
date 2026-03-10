package org.batfish.datamodel.routing_policy.statement;

import static org.batfish.datamodel.AbstractRoute.MAX_ADMIN_DISTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.routing_policy.expr.DecrementAdministrativeCost;
import org.batfish.datamodel.routing_policy.expr.IncrementAdministrativeCost;
import org.batfish.datamodel.routing_policy.expr.LiteralAdministrativeCost;
import org.junit.Test;

public class SetAdministrativeCostTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new SetAdministrativeCost(new LiteralAdministrativeCost(5)),
            new SetAdministrativeCost(new LiteralAdministrativeCost(5)))
        .addEqualityGroup(new SetAdministrativeCost(new LiteralAdministrativeCost(6)))
        .addEqualityGroup(
            new SetAdministrativeCost(new IncrementAdministrativeCost(10, MAX_ADMIN_DISTANCE)))
        .addEqualityGroup(new SetAdministrativeCost(new DecrementAdministrativeCost(10, 0)))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    SetAdministrativeCost obj = new SetAdministrativeCost(new LiteralAdministrativeCost(123));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
    assertThat(BatfishObjectMapper.clone(obj, Statement.class), equalTo(obj));
  }
}
