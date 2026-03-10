package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

/** Test of {@link ForwardOutInterface}. */
@ParametersAreNonnullByDefault
public final class ForwardOutInterfaceTest {

  @Test
  public void testEquals() {
    ForwardOutInterface f = new ForwardOutInterface("a", null);
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(f, f, new ForwardOutInterface("a", null))
        .addEqualityGroup(new ForwardOutInterface("b", null))
        .addEqualityGroup(new ForwardOutInterface("a", NodeInterfacePair.of("a", "a")))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    ForwardOutInterface f = new ForwardOutInterface("a", null);
    SessionAction clone = BatfishObjectMapper.clone(f, SessionAction.class);
    assertThat(clone, equalTo(f));

    f = new ForwardOutInterface("b", NodeInterfacePair.of("a", "b"));
    clone = BatfishObjectMapper.clone(f, ForwardOutInterface.class);
    assertThat(clone, equalTo(f));
  }
}
