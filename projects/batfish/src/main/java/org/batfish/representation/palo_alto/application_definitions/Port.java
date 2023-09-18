package org.batfish.representation.palo_alto.application_definitions;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data model class containing information about a Palo Alto application's default port(s) and IP
 * protocol(s).
 */
public final class Port {
  /** Get the list of protocol/port combinations that make up this "port". */
  public @Nonnull List<String> getMember() {
    return _member;
  }

  private static final String PROP_MEMBER = "member";

  @JsonCreator
  private static @Nonnull Port jsonCreator(@JsonProperty(PROP_MEMBER) @Nullable JsonNode member) {
    checkArgument(member != null, "Missing %s", PROP_MEMBER);
    // List of members
    if (member instanceof ArrayNode) {
      ArrayNode arrayNode = (ArrayNode) member;
      ImmutableList.Builder<String> names = ImmutableList.builder();
      arrayNode.forEach(item -> names.add(item.textValue()));
      return new Port(names.build());
    }
    // Single member
    return new Port(ImmutableList.of(member.textValue()));
  }

  @VisibleForTesting
  Port(@Nonnull List<String> member) {
    _member = member;
  }

  private final @Nonnull List<String> _member;
}
