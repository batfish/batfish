package org.batfish.bddreachability.transition;

import static org.batfish.common.bdd.BDDUtils.bddFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.BDDPairingFactory;
import org.batfish.common.bdd.PrimedBDDInteger;
import org.junit.Before;
import org.junit.Test;

public class TransformTest {
  private BDDFactory _factory;
  private PrimedBDDInteger _xPrimedInt;
  private PrimedBDDInteger _yPrimedInt;
  private BDDInteger _x;
  private BDDInteger _y;

  private BDD _zero;
  private BDD _one;

  private BDD _x0;
  private BDD _x1;
  private BDD _x2;
  private BDD _y2;
  private BDD _y3;
  private BDD _yPrime3;

  private Transform _transformX0;
  private Transform _transformX1;
  private Transform _transformY;

  @Before
  public void setup() {
    _factory = bddFactory(8);
    _zero = _factory.zero();
    _one = _factory.one();

    _xPrimedInt =
        new PrimedBDDInteger(
            _factory,
            new BDD[] {_factory.ithVar(0), _factory.ithVar(2)},
            new BDD[] {_factory.ithVar(1), _factory.ithVar(3)});
    _yPrimedInt =
        new PrimedBDDInteger(
            _factory,
            new BDD[] {_factory.ithVar(4), _factory.ithVar(6)},
            new BDD[] {_factory.ithVar(5), _factory.ithVar(7)});

    _x = _xPrimedInt.getVar();
    BDDInteger xPrime = _xPrimedInt.getPrimeVar();

    _y = _yPrimedInt.getVar();
    BDDInteger yPrime = _yPrimedInt.getPrimeVar();

    _x0 = _x.value(0);
    _x1 = _x.value(1);
    _x2 = _x.value(2);
    BDD xPrime0 = xPrime.value(0);
    BDD xPrime1 = xPrime.value(1);
    BDD xPrime2 = xPrime.value(2);

    _transformX0 = new Transform(_x0.and(xPrime1.or(xPrime2)), _xPrimedInt.getPairingFactory());
    _transformX1 = new Transform(_x1.and(xPrime0), _xPrimedInt.getPairingFactory());

    _y2 = _y.value(2);
    _y3 = _y.value(3);
    _yPrime3 = yPrime.value(3);

    _transformY = new Transform(_y2.and(_yPrime3), _yPrimedInt.getPairingFactory());
  }

  @Test
  public void testTransitions() {
    // forward
    assertEquals(_x1.or(_x2), _transformX0.transitForward(_x0));
    assertEquals(_zero, _transformX0.transitForward(_x1));

    // backward
    assertEquals(_x0, _transformX0.transitBackward(_x1));
    assertEquals(_x0, _transformX0.transitBackward(_x2));
    assertEquals(_zero, _transformX0.transitBackward(_x0));
  }

  @Test
  public void testOr() {
    BDD x01 = _x0.or(_x1);
    BDD x12 = _x1.or(_x2);
    BDD x012 = _x0.or(x12);

    // _transformX0 is only defined on x=0, but we can apply it to x=0 or x=1
    assertEquals(x12, _transformX0.transitForward(_x0));
    assertEquals(x12, _transformX0.transitForward(x01));

    // _transformX1 is only defined on x=1, but we can apply it to x=0 or x=1
    assertEquals(_x0, _transformX1.transitForward(_x1));
    assertEquals(_x0, _transformX1.transitForward(x01));

    // merging unions outputs
    Transform merged = _transformX0.or(_transformX1);
    assertEquals(x12, merged.transitForward(_x0));
    assertEquals(_x0, merged.transitForward(_x1));
    assertEquals(x012, merged.transitForward(x01));

    // transitBackwards works as expected
    assertEquals(_zero, _transformX0.transitBackward(_x0));
    assertEquals(_x1, _transformX1.transitBackward(_x0));
    assertEquals(_x1, merged.transitBackward(_x0));

    assertEquals(_x0, _transformX0.transitBackward(_x1));
    assertEquals(_zero, _transformX1.transitBackward(_x1));
    assertEquals(_x0, merged.transitBackward(_x1));

    assertEquals(_x0, _transformX0.transitBackward(_x2));
    assertEquals(_zero, _transformX1.transitBackward(_x2));
    assertEquals(_x0, merged.transitBackward(_x2));

    assertEquals(_x0, _transformX0.transitBackward(x012));
    assertEquals(_x1, _transformX1.transitBackward(x012));
    assertEquals(x01, merged.transitBackward(x012));
  }

  @Test
  public void testCompose() {
    BDD x12 = _x1.or(_x2);
    assertEquals(x12, _transformX0.transitForward(_x0));
    assertEquals(x12, _transformX0.transitForward(_one));

    assertEquals(_y3, _transformY.transitForward(_y2));
    assertEquals(_y3, _transformY.transitForward(_one));

    assertEquals(_y2, _transformY.transitBackward(_y3));
    assertEquals(_y2, _transformY.transitBackward(_one));

    Transform composite = _transformX0.tryCompose(_transformY).get();
    assertEquals(x12.and(_y3), composite.transitForward(_x0.and(_y2)));
    assertEquals(x12.and(_y3), composite.transitForward(_one));
    assertEquals(_x0.and(_y2), composite.transitBackward(x12.and(_y3)));

    // Transiting the composite edge is equivalent to transiting both edges
    assertEquals(
        _transformY.transitForward(_transformX0.transitForward(_x0.and(_y2))),
        composite.transitForward(_x0.and(_y2)));
    assertEquals(
        _transformY.transitForward(_transformX0.transitForward(_one)),
        composite.transitForward(_one));

    // Transiting backward is equivalent to transiting the edges backward in reverse order
    assertEquals(
        _transformX0.transitBackward(_transformY.transitBackward(_x0.and(_y2))),
        composite.transitBackward(_x0.and(_y2)));
    assertEquals(
        _transformX0.transitBackward(_transformY.transitBackward(_one)),
        composite.transitBackward(_one));

    // test all pairs of single x,y values
    for (int xVal = 0; xVal < 4; xVal++) {
      for (int yVal = 0; yVal < 4; yVal++) {
        BDD bdd = _x.value(xVal).and(_y.value(yVal));
        assertEquals(
            _transformY.transitForward(_transformX0.transitForward(bdd)),
            composite.transitForward(bdd));
        assertEquals(
            _transformX0.transitBackward(_transformY.transitBackward(bdd)),
            composite.transitBackward(bdd));
      }
    }
  }

  /**
   * If the second Transform constrains a variable transformed by the first, that constrain is
   * applied after the first transformation.
   */
  @Test
  public void testComposeSequence() {
    BDD x12 = _x1.or(_x2);

    // x=2 is in the codomain of the first transform (on x)
    assertEquals(x12, _transformX0.transitForward(_x0));
    assertEquals(x12, _transformX0.transitForward(_one));

    // map y=2 to y=3, but require x=1
    Transform transformY =
        new Transform(_x1.and(_y2.and(_yPrime3)), _yPrimedInt.getPairingFactory());

    BDD x0y2 = _x0.and(_y2);
    BDD x1y3 = _x1.and(_y3);

    assertEquals(x1y3, transformY.transitForward(_y2));
    assertEquals(x1y3, transformY.transitForward(_one));
    assertEquals(_zero, transformY.transitForward(_x2));

    assertEquals(_y2, _transformY.transitBackward(_y3));
    assertEquals(_y2, _transformY.transitBackward(_one));

    // x=2 is not in the codomain of the composite transform
    Transform composite = _transformX0.tryCompose(transformY).get();
    assertEquals(x1y3, composite.transitForward(x0y2));
    assertEquals(x0y2, composite.transitBackward(x1y3));

    assertEquals(x1y3, composite.transitForward(_one));
    assertEquals(x0y2, composite.transitBackward(_one));
  }

  @Test
  public void testReduceWithOr() {
    BDDPairingFactory xPairingFactory = _xPrimedInt.getPairingFactory();
    BDDPairingFactory yPairingFactory = _yPrimedInt.getPairingFactory();
    BDD yIdRel = yPairingFactory.identityRelation();
    BDD xIdRel = xPairingFactory.identityRelation();

    assertThat(
        Transform.reduceWithOr(ImmutableList.of(_transformX0, _transformX1, _transformY)),
        contains(
            new Transform(
                _factory.orAll(
                    _transformX0.getForwardRelation().and(yIdRel),
                    _transformX1.getForwardRelation().and(yIdRel),
                    _transformY.getForwardRelation().and(xIdRel)),
                BDDPairingFactory.union(ImmutableList.of(xPairingFactory, yPairingFactory)))));
  }

  @Test
  public void testEquals() {
    // transiting initializes _reverseRelation and _swapPairing, but does not affect equality
    Transform transited = new Transform(_x0, _xPrimedInt.getPairingFactory());
    transited.transitForward(_one);

    new EqualsTester()
        .addEqualityGroup(transited, new Transform(_x0, _xPrimedInt.getPairingFactory()))
        .addEqualityGroup(new Transform(_x1, _xPrimedInt.getPairingFactory()))
        .addEqualityGroup(new Transform(_x0, _yPrimedInt.getPairingFactory()))
        .testEquals();
  }
}
