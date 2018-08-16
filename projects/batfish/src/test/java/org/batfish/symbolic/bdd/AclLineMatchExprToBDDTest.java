package org.batfish.symbolic.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.oneOf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.common.util.NonRecursiveSupplier;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AclLineMatchExprToBDDTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  private static final String IFACE1 = "iface1";

  private static final String IFACE2 = "iface2";

  private BDDPacket _pkt;

  private BDDSourceManager _sourceMgr;

  private AclLineMatchExprToBDD _toBDD;

  @Before
  public void setup() {
    _pkt = new BDDPacket();
    _sourceMgr = BDDSourceManager.forInterfaces(_pkt, ImmutableSet.of(IFACE1, IFACE2));
    _toBDD =
        new AclLineMatchExprToBDD(
            _pkt.getFactory(), _pkt, ImmutableMap.of(), ImmutableMap.of(), _sourceMgr);
  }

  @Test
  public void testPermittedByAcl() {
    Ip fooIp = new Ip("1.1.1.1");
    BDD fooIpBDD = _pkt.getSrcIp().value(fooIp.asLong());
    PermittedByAcl permittedByAcl = new PermittedByAcl("foo");
    Map<String, Supplier<BDD>> namedAclBDDs = ImmutableMap.of("foo", () -> fooIpBDD);
    AclLineMatchExprToBDD toBDD =
        new AclLineMatchExprToBDD(_pkt.getFactory(), _pkt, namedAclBDDs, ImmutableMap.of());
    assertThat(permittedByAcl.accept(toBDD), equalTo(fooIpBDD));
  }

  @Test
  public void testPermittedByAcl_undefined() {
    PermittedByAcl permittedByAcl = new PermittedByAcl("foo");
    AclLineMatchExprToBDD toBDD =
        new AclLineMatchExprToBDD(_pkt.getFactory(), _pkt, ImmutableMap.of(), ImmutableMap.of());
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Undefined PermittedByAcl reference: foo");
    permittedByAcl.accept(toBDD);
  }

  @Test
  public void testPermittedByAcl_circular() {
    PermittedByAcl permittedByAcl = new PermittedByAcl("foo");
    Map<String, Supplier<BDD>> namedAclBDDs = new HashMap<>();
    namedAclBDDs.put("foo", new NonRecursiveSupplier<>(() -> namedAclBDDs.get("foo").get()));
    AclLineMatchExprToBDD toBDD =
        new AclLineMatchExprToBDD(_pkt.getFactory(), _pkt, namedAclBDDs, ImmutableMap.of());
    exception.expect(BatfishException.class);
    exception.expectMessage("Circular PermittedByAcl reference: foo");
    permittedByAcl.accept(toBDD);
  }

  @Test
  public void testMatchSrcInterface() {
    MatchSrcInterface matchSrcInterface1 = new MatchSrcInterface(ImmutableList.of(IFACE1));
    BDD iface1BDD = _sourceMgr.getSourceInterfaceBDD(IFACE1);
    BDD iface2BDD = _sourceMgr.getSourceInterfaceBDD(IFACE2);

    BDD bdd1 = _toBDD.visit(matchSrcInterface1);
    assertThat(bdd1, equalTo(iface1BDD));
    assertThat(_sourceMgr.getSourceFromAssignment(bdd1), equalTo(Optional.of(IFACE1)));

    MatchSrcInterface matchSrcInterface2 = new MatchSrcInterface(ImmutableList.of(IFACE2));
    assertThat(_toBDD.visit(matchSrcInterface2), equalTo(iface2BDD));

    MatchSrcInterface matchSrcInterface1Or2 =
        new MatchSrcInterface(ImmutableList.of(IFACE1, IFACE2));
    BDD bdd1Or2 = _toBDD.visit(matchSrcInterface1Or2);
    assertThat(bdd1Or2, equalTo(iface1BDD.or(iface2BDD)));
    assertThat(
        _sourceMgr.getSourceFromAssignment(bdd1Or2.fullSatOne()).get(), oneOf(IFACE1, IFACE2));

    AclLineMatchExpr expr =
        new AndMatchExpr(
            ImmutableList.of(matchSrcInterface1Or2, new NotMatchExpr(matchSrcInterface1)));
    assertThat(_toBDD.visit(expr), equalTo(iface2BDD));
  }

  @Test
  public void testOriginateFromInterface() {
    assertThat(
        _toBDD.visit(OriginatingFromDevice.INSTANCE),
        equalTo(_sourceMgr.getOriginatingFromDeviceBDD()));
  }
}
