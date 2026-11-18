package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

/** Tests of {@link CompositeFirewallFilter}. */
class CompositeFirewallFilterTest {

  @Test
  void testGetters() {
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
  void testVerifiesFamily() {
    ConcreteFirewallFilter v4 = new ConcreteFirewallFilter("v4", Family.INET);
    ConcreteFirewallFilter v6 = new ConcreteFirewallFilter("v6", Family.INET6);

    Throwable exception =
        assertThrows(
            Exception.class,
            () -> {
              new CompositeFirewallFilter("bad", ImmutableList.of(v4, v6));
            });

    assertThat(
        exception.getMessage(),
        containsString(
            "All member lists in a composite firewall-filter must have the same family"));
  }
}
