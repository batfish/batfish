package org.batfish.representation.palo_alto.application_definitions;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data model class containing information about a Palo Alto application's "{@code
 * use-applications}". These correspond to applications used by / related to others (details TBD).
 */
public final class UseApplications {
  public @Nonnull List<String> getMember() {
    return _member;
  }

  private static final String PROP_MEMBER = "member";

  @JsonCreator
  private static @Nonnull UseApplications create(
      @JsonProperty(PROP_MEMBER) @Nullable JsonNode member) {
    checkArgument(member != null, "Missing %s", PROP_MEMBER);
    // List of members
    if (member instanceof ArrayNode) {
      ArrayNode arrayNode = (ArrayNode) member;
      ImmutableList.Builder<String> names = ImmutableList.builder();
      arrayNode.forEach(item -> names.add(memberToName(item)));
      return new UseApplications(names.build());
    }
    // Single member
    return new UseApplications(ImmutableList.of(memberToName(member)));
  }

  /**
   * Extract the {@code name} from a member.
   *
   * <p>Members will either be a string containing its name, or an object with a {@code #text} field
   * containing the name.
   */
  private static @Nonnull String memberToName(@Nullable JsonNode singleMember) {
    if (singleMember instanceof TextNode) {
      return singleMember.textValue();
    }
    assert singleMember instanceof ObjectNode; // Map/Object
    ObjectNode objectNode = (ObjectNode) singleMember;
    JsonNode text = objectNode.findValue("#text");
    assert text instanceof TextNode;
    return text.textValue();
  }

  @VisibleForTesting
  UseApplications(@Nonnull List<String> member) {
    _member = ImmutableList.copyOf(member);
  }

  private final @Nonnull List<String> _member;
}
