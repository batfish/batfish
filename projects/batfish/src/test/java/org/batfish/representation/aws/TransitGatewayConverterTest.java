package org.batfish.representation.aws;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.representation.aws.TransitGatewayConverter.TransitGatewayWithMetadata;
import org.junit.Test;

public class TransitGatewayConverterTest {

  @Test
  public void testFindOriginalGateway() {
    TransitGateway tgw1 = mock(TransitGateway.class);
    when(tgw1.getId()).thenReturn("id1");
    when(tgw1.getOwnerId()).thenReturn("account1");
    TransitGateway tgw2 = mock(TransitGateway.class);
    when(tgw2.getId()).thenReturn("id1");
    when(tgw2.getOwnerId()).thenReturn("account1");
    TransitGatewayWithMetadata t1 =
        new TransitGatewayWithMetadata(tgw1, new Region("r1"), "account1");
    TransitGatewayWithMetadata t2 =
        new TransitGatewayWithMetadata(tgw2, new Region("r1"), "account2");
    assertThat(
        TransitGatewayConverter.findOriginalGateway(ImmutableList.of(t1, t2)), sameInstance(t1));
  }

  @Test
  public void testFindOriginalGatewayNonAmbiguous() {
    TransitGateway tgw1 = mock(TransitGateway.class);
    when(tgw1.getId()).thenReturn("id1");
    when(tgw1.getOwnerId()).thenReturn("account1");
    // Note account mismatch
    TransitGatewayWithMetadata t1 =
        new TransitGatewayWithMetadata(tgw1, new Region("r1"), "wrong_account");
    assertThat(TransitGatewayConverter.findOriginalGateway(ImmutableList.of(t1)), sameInstance(t1));
  }

  @Test
  public void testFindOriginalGatewayFailure() {
    TransitGateway tgw1 = mock(TransitGateway.class);
    when(tgw1.getId()).thenReturn("id1");
    when(tgw1.getOwnerId()).thenReturn("account1");
    TransitGateway tgw2 = mock(TransitGateway.class);
    when(tgw2.getId()).thenReturn("id1");
    when(tgw2.getOwnerId()).thenReturn("account1");
    // Note account mismatch
    TransitGatewayWithMetadata t1 =
        new TransitGatewayWithMetadata(tgw1, new Region("r1"), "wrong_account");
    TransitGatewayWithMetadata t2 =
        new TransitGatewayWithMetadata(tgw1, new Region("r1"), "wrong_account_2");
    assertThat(TransitGatewayConverter.findOriginalGateway(ImmutableList.of(t1, t2)), nullValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFindOriginalGatewayEmpty() {
    TransitGatewayConverter.findOriginalGateway(ImmutableList.of());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFindOriginalGatewayNotAllSameId() {
    TransitGateway tgw1 = mock(TransitGateway.class);
    when(tgw1.getId()).thenReturn("id1");
    TransitGateway tgw2 = mock(TransitGateway.class);
    when(tgw2.getId()).thenReturn("id2");
    TransitGatewayWithMetadata t1 =
        new TransitGatewayWithMetadata(tgw1, new Region("r1"), "account1");
    TransitGatewayWithMetadata t2 =
        new TransitGatewayWithMetadata(tgw2, new Region("r1"), "account1");
    TransitGatewayConverter.findOriginalGateway(ImmutableList.of(t1, t2));
  }

  @Test
  public void testGetUniqueTransitGateways() {
    TransitGateway tgw1 = mock(TransitGateway.class);
    when(tgw1.getId()).thenReturn("id1");
    when(tgw1.getOwnerId()).thenReturn("account1");
    TransitGateway tgw2 = mock(TransitGateway.class);
    when(tgw2.getId()).thenReturn("id1");
    when(tgw2.getOwnerId()).thenReturn("account1");

    TransitGateway tgw3 = mock(TransitGateway.class);
    when(tgw3.getId()).thenReturn("id2");
    when(tgw3.getOwnerId()).thenReturn("account3");

    TransitGatewayWithMetadata t1 =
        new TransitGatewayWithMetadata(tgw1, new Region("r1"), "account1");
    TransitGatewayWithMetadata t2 =
        new TransitGatewayWithMetadata(tgw2, new Region("r1"), "account2");
    TransitGatewayWithMetadata t3 =
        new TransitGatewayWithMetadata(tgw3, new Region("r1"), "account3");
    assertThat(
        TransitGatewayConverter.getUniqueTransitGateways(
            ImmutableList.of(t1, t2, t3), new Warnings()),
        containsInAnyOrder(t1, t3));
  }

  @Test
  public void testGetUniqueTransitGatewaysLogFailure() {
    TransitGateway tgw1 = mock(TransitGateway.class);
    when(tgw1.getId()).thenReturn("id1");
    when(tgw1.getOwnerId()).thenReturn("account1");
    TransitGateway tgw2 = mock(TransitGateway.class);
    when(tgw2.getId()).thenReturn("id1");
    when(tgw2.getOwnerId()).thenReturn("account1");

    TransitGateway tgw3 = mock(TransitGateway.class);
    when(tgw3.getId()).thenReturn("id2");
    when(tgw3.getOwnerId()).thenReturn("account3");

    TransitGatewayWithMetadata t1 =
        new TransitGatewayWithMetadata(tgw1, new Region("r1"), "wrong_account");
    TransitGatewayWithMetadata t2 =
        new TransitGatewayWithMetadata(tgw2, new Region("r1"), "wrong_account_2");
    TransitGatewayWithMetadata t3 =
        new TransitGatewayWithMetadata(tgw3, new Region("r1"), "account3");
    Warnings w = new Warnings(true, true, true);
    assertThat(
        TransitGatewayConverter.getUniqueTransitGateways(ImmutableList.of(t1, t2, t3), w),
        contains(t3));
    assertThat(
        w.getRedFlagWarnings(),
        contains(
            new Warning(
                "Could not find authoritative representation for transit gateways: id1",
                Warnings.TAG_RED_FLAG)));
  }
}
