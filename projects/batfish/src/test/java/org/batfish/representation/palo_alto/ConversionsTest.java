package org.batfish.representation.palo_alto;

import static org.batfish.datamodel.routing_policy.statement.Statements.RemovePrivateAs;
import static org.batfish.representation.palo_alto.Conversions.makeEbgpExportTransformations;
import static org.batfish.representation.palo_alto.Conversions.makeEbgpImportTransformations;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.routing_policy.expr.BgpPeerAddressNextHop;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.expr.UnchangedNextHop;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.representation.palo_alto.EbgpPeerGroupType.ExportNexthopMode;
import org.batfish.representation.palo_alto.EbgpPeerGroupType.ImportNexthopMode;
import org.junit.Test;

/** Tests of {@link Conversions}. */
public class ConversionsTest {
  @Test
  public void testEbgpExportTransformations() {
    EbgpPeerGroupType toTest = new EbgpPeerGroupType();
    // test defaults
    assertThat(
        makeEbgpExportTransformations(toTest),
        containsInAnyOrder(
            RemovePrivateAs.toStaticStatement(), new SetNextHop(SelfNextHop.getInstance())));

    // test explicit override to defaults
    toTest.setExportNexthop(ExportNexthopMode.RESOLVE);
    toTest.setRemovePrivateAs(true);
    assertThat(
        makeEbgpExportTransformations(toTest),
        containsInAnyOrder(
            RemovePrivateAs.toStaticStatement(), new SetNextHop(SelfNextHop.getInstance())));

    // test explicit override to non-defaults
    toTest.setExportNexthop(ExportNexthopMode.USE_SELF);
    toTest.setRemovePrivateAs(false);
    assertThat(
        makeEbgpExportTransformations(toTest),
        containsInAnyOrder(new SetNextHop(SelfNextHop.getInstance())));
  }

  @Test
  public void testIbgpExportTransformations() {
    EbgpPeerGroupType toTest = new EbgpPeerGroupType();
    // test defaults
    assertThat(
        makeEbgpImportTransformations(toTest),
        containsInAnyOrder(new SetNextHop(UnchangedNextHop.getInstance())));

    // test explicit override to defaults
    toTest.setImportNexthop(ImportNexthopMode.ORIGINAL);
    assertThat(
        makeEbgpImportTransformations(toTest),
        containsInAnyOrder(new SetNextHop(UnchangedNextHop.getInstance())));

    // test explicit override to non-defaults
    toTest.setImportNexthop(ImportNexthopMode.USE_PEER);
    assertThat(
        makeEbgpImportTransformations(toTest),
        containsInAnyOrder(new SetNextHop(BgpPeerAddressNextHop.getInstance())));
  }
}
