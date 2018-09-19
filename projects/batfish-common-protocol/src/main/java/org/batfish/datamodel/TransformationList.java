package org.batfish.datamodel;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.transformation.DynamicNatRule;
import org.batfish.datamodel.transformation.StaticNatRule;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.Transformation.Direction;
import org.batfish.datamodel.transformation.Transformation.RuleAction;
import org.batfish.datamodel.transformation.TransformationEvaluator;

public class TransformationList implements Serializable {
  private static final String PROP_TRANSFORMATIONS = "transformations";

  private static final long serialVersionUID = 1L;

  @Nonnull private final ImmutableList<Transformation> _transformations;
  @Nonnull private final Map<Direction, Map<Transformed, List<Transformation>>> _transformTypes;

  public TransformationList(@Nonnull List<Transformation> transformations) {
    _transformations =
        transformations
            .stream()
            .sorted(TransformationList::transformationOrdering)
            .collect(ImmutableList.toImmutableList());

    _transformTypes =
        Arrays.stream(Direction.values())
            .collect(
                Collectors.toMap(
                    Function.identity(), direction -> partition(_transformations, direction)));
  }

  public TransformationList() {
    _transformations = ImmutableList.of();
    _transformTypes = new TreeMap<>();
  }

  @JsonCreator
  private static TransformationList create(
      @JsonProperty(PROP_TRANSFORMATIONS) @Nullable List<Transformation> transformations) {
    // TODO why is this necessary
    if (transformations == null) {
      return new TransformationList(ImmutableList.of());
    }
    return new TransformationList(requireNonNull(transformations));
  }

  /**
   * Apply the first matching {@link Transformation} of each {@link RuleAction}.
   *
   * @param flow Flow to br transformed.
   * @param direction Direction the flow is traveling through the node.
   * @param srcInterface Interface that sourced the flow.
   * @param aclDefinitions ACLs that maybe referenced by a NAT rule.
   * @param namedIpSpaces Named IpSpace that maybe referenced by a NAT rul.
   * @return The resultant flow after applying each matching transform. If no transforms match the
   *     given flow, the original flow will be returned.
   */
  public Flow apply(
      Flow flow,
      Direction direction,
      @Nullable String srcInterface,
      Map<String, IpAccessList> aclDefinitions,
      Map<String, IpSpace> namedIpSpaces) {
    Flow transformedFlow = flow;
    for (List<Transformation> transforms : _transformTypes.get(direction).values()) {
      transformedFlow =
          apply(
              transforms,
              transformedFlow,
              new TransformationEvaluator(
                  transformedFlow, direction, srcInterface, aclDefinitions, namedIpSpaces));
    }
    return transformedFlow;
  }

  @JsonProperty(PROP_TRANSFORMATIONS)
  public List<Transformation> getTransformations() {
    return _transformations;
  }

  @Override
  public String toString() {
    StringBuilder output = new StringBuilder().append(getClass().getSimpleName());
    for (Transformation transformation : _transformations) {
      output.append("\n");
      output.append(transformation);
    }
    return output.toString();
  }

  public List<DynamicNatRule> getDynamicTransformations() {
    return _transformations
        .stream()
        .filter(DynamicNatRule.class::isInstance)
        .map(DynamicNatRule.class::cast)
        .collect(toImmutableList());
  }

  public List<StaticNatRule> getStaticTransformations() {
    return _transformations
        .stream()
        .filter(StaticNatRule.class::isInstance)
        .map(StaticNatRule.class::cast)
        .collect(toImmutableList());
  }

  public Map<Transformed, List<Transformation>> getTransformsByType(Direction direction) {
    return _transformTypes.get(direction);
  }

  private static Flow apply(
      List<Transformation> rules, Flow flow, TransformationEvaluator evaluator) {
    return rules
        .stream()
        .map(rule -> rule.accept(evaluator))
        .filter(Objects::nonNull)
        .filter(newFlow -> !flow.equals(newFlow))
        .findFirst()
        .orElse(flow);
  }

  private static Map<Transformed, List<Transformation>> partition(
      List<Transformation> transformations, Direction direction) {
    return transformations
        .stream()
        .collect(Collectors.groupingBy(t -> whatChanges(t.getAction(), direction)));
  }

  private static Transformed whatChanges(RuleAction action, Direction direction) {
    switch (action) {
      case SOURCE_INSIDE:
        return direction == Direction.EGRESS
            ? Transformed.SOURCE_ADDR
            : Transformed.DESTINATION_ADDR;
      case SOURCE_OUTSIDE:
      case DESTINATION_INSIDE:
        return direction == Direction.EGRESS
            ? Transformed.DESTINATION_ADDR
            : Transformed.SOURCE_ADDR;
      default:
        throw new BatfishException("Unexpected action: " + action);
    }
  }

  private static int transformationOrdering(Transformation t1, Transformation t2) {
    // These sorting rules are known.
    // - static goes before dynamic
    // - static with a longer prefix length goes before static with a shorter prefix
    // TODO: verify the following
    // - NATs with route map are sorted route map name and then by static/dynamic
    // - Not sure about route map NATs vs non-route map NATs
    // - Not sure if dynamic NATs are ordered

    boolean t1Static = t1 instanceof StaticNatRule;
    boolean t2Static = t2 instanceof StaticNatRule;
    if (!t1Static) {
      return t2Static ? 1 : 0;
    } else if (!t2Static) {
      return 1;
    }
    // reverse order for network compare so bigger prefixes come first
    return Integer.compare(
        ((StaticNatRule) t2).getLocalNetwork().getPrefixLength(),
        ((StaticNatRule) t1).getLocalNetwork().getPrefixLength());
  }

  public enum Transformed {
    SOURCE_ADDR,
    DESTINATION_ADDR
  }
}
