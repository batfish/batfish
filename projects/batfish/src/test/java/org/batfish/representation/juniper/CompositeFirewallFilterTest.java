package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link CompositeFirewallFilter}. */
public class CompositeFirewallFilterTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testGetters() {
    ConcreteFirewallFilter foo = new ConcreteFirewallFilter("foo", Family.INET);
    foo.setUsedForFBF(true);
    ConcreteFirewallFilter bar = new ConcreteFirewallFilter("bar", Family.INET);
    bar.setUsedForFBF(false);

    CompositeFirewallFilter fooOnly = new CompositeFirewallFilter("comp", ImmutableList.of(foo));
    assertThat(fooOnly.getFamily(), equalTo(Family.INET));
    assertThat(fooOnly.isUsedForFBF(), equalTo(true));

    CompositeFirewallFilter barOnly = new CompositeFirewallFilter("comp", ImmutableList.of(bar));
    assertThat(barOnly.getFamily(), equalTo(Family.INET));
    assertThat(barOnly.isUsedForFBF(), equalTo(false));

    CompositeFirewallFilter foobar =
        new CompositeFirewallFilter("comp", ImmutableList.of(foo, bar));
    assertThat(foobar.getFamily(), equalTo(Family.INET));
    assertThat(foobar.isUsedForFBF(), equalTo(true));

    CompositeFirewallFilter barfoo =
        new CompositeFirewallFilter("comp", ImmutableList.of(bar, foo));
    assertThat(barfoo.getFamily(), equalTo(Family.INET));
    assertThat(barfoo.isUsedForFBF(), equalTo(true));

    CompositeFirewallFilter barbarOnly =
        new CompositeFirewallFilter("comp", ImmutableList.of(bar, barOnly));
    assertThat(barbarOnly.getFamily(), equalTo(Family.INET));
    assertThat(barbarOnly.isUsedForFBF(), equalTo(false));
  }

  @Test
  public void testVerifiesFamily() {
    ConcreteFirewallFilter v4 = new ConcreteFirewallFilter("v4", Family.INET);
    ConcreteFirewallFilter v6 = new ConcreteFirewallFilter("v6", Family.INET6);

    _thrown.expectMessage(
        "All member lists in a composite firewall-filter must have the same family");
    new CompositeFirewallFilter("bad", ImmutableList.of(v4, v6));
  }
}
