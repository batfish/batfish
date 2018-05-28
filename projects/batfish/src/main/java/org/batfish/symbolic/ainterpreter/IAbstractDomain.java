package org.batfish.symbolic.ainterpreter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.bdd.BDDAcl;

public interface IAbstractDomain<T> {

  T bot();

  T value(String router, RoutingProtocol proto, @Nullable Set<Prefix> prefixes);

  T transform(T input, EdgeTransformer f);

  T merge(T x, T y);

  T selectBest(T x);

  List<RibEntry> toRoutes(AbstractRib<T> value);

  BDD toFib(Map<String, AbstractRib<T>> ribs, FiniteIndexMap<BDDAcl> acls);

  boolean reachable(
      Map<String, AbstractRib<T>> ribs,
      FiniteIndexMap<BDDAcl> acls,
      String src,
      String dst,
      Flow flow);

  String debug(T x);
}
