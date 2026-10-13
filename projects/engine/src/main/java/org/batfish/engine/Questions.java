package org.batfish.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.Question;

/**
 * The questions the engine answers, by the names pybatfish uses. Each maps to a Batfish question
 * class; the caller's parameters are that class's own JSON properties. Only listed questions can be
 * asked: the engine is not a general Batfish front end.
 */
public final class Questions {

  private record Spec(String className, Map<String, Object> fixed, boolean differentialOnly) {}

  private static final Map<String, Spec> SPECS =
      ImmutableMap.<String, Spec>builder()
          .put(
              "vxlanEdges",
              new Spec(
                  "org.batfish.question.edges.EdgesQuestion",
                  ImmutableMap.of("edgeType", "vxlan", "initial", false),
                  false))
          .put(
              "evpnRib",
              new Spec(
                  "org.batfish.question.routes.RoutesQuestion",
                  ImmutableMap.of("rib", "evpn"),
                  false))
          .put(
              "evpnL3VniProperties",
              spec("org.batfish.question.evpnl3vniproperties.EvpnL3VniPropertiesQuestion"))
          .put("routes", spec("org.batfish.question.routes.RoutesQuestion"))
          .put(
              "bgpSessionStatus",
              spec("org.batfish.question.bgpsessionstatus.BgpSessionStatusQuestion"))
          .put(
              "bgpSessionCompatibility",
              spec("org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityQuestion"))
          .put(
              "ospfSessionCompatibility",
              spec("org.batfish.question.ospfsession.OspfSessionCompatibilityQuestion"))
          .put("traceroute", spec("org.batfish.question.traceroute.TracerouteQuestion"))
          .put("testFilters", spec("org.batfish.question.testfilters.TestFiltersQuestion"))
          .put(
              "reachability",
              spec("org.batfish.question.specifiers.SpecifiersReachabilityQuestion"))
          .put(
              "differentialReachability",
              new Spec(
                  "org.batfish.question.differentialreachability.DifferentialReachabilityQuestion",
                  ImmutableMap.of(),
                  true))
          .put(
              "layer3Edges",
              new Spec(
                  "org.batfish.question.edges.EdgesQuestion",
                  ImmutableMap.of("edgeType", "layer3"),
                  false))
          .buildOrThrow();

  private static Spec spec(String className) {
    return new Spec(className, ImmutableMap.of(), false);
  }

  private Questions() {}

  public static @Nonnull Set<String> names() {
    return SPECS.keySet();
  }

  public static boolean known(@Nonnull String name) {
    return SPECS.containsKey(name);
  }

  /** Whether the question only has a differential form. */
  public static boolean differentialOnly(@Nonnull String name) {
    Spec spec = SPECS.get(name);
    return spec != null && spec.differentialOnly();
  }

  /**
   * Builds the named question from the caller's parameters. Throws {@link IllegalArgumentException}
   * for an unknown question or parameters the question does not take.
   */
  public static @Nonnull Question create(@Nonnull String name, @Nullable JsonNode parameters) {
    Spec spec = SPECS.get(name);
    if (spec == null) {
      throw new IllegalArgumentException("unknown question " + name);
    }
    ObjectNode json = BatfishObjectMapper.mapper().createObjectNode();
    if (parameters != null && !parameters.isNull()) {
      if (!parameters.isObject()) {
        throw new IllegalArgumentException("question parameters must be an object");
      }
      parameters
          .fields()
          .forEachRemaining(
              e -> {
                if (e.getKey().equals("class")) {
                  throw new IllegalArgumentException("parameter 'class' is not allowed");
                }
                json.set(e.getKey(), e.getValue());
              });
    }
    spec.fixed().forEach((k, v) -> json.set(k, BatfishObjectMapper.mapper().valueToTree(v)));
    json.put("class", spec.className());
    try {
      return BatfishObjectMapper.mapper().treeToValue(json, Question.class);
    } catch (IOException e) {
      throw new IllegalArgumentException("bad parameters for " + name + ": " + e.getMessage(), e);
    }
  }
}
