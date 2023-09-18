package org.batfish.question.ipowners;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** Queries the computed IP owners across all nodes/VRFs/Interfaces in the network. */
public class IpOwnersQuestion extends Question {
  private static final String PROP_IPS = "ips";
  private static final String PROP_DUPLICATES_ONLY = "duplicatesOnly";

  private static final String QUESTION_NAME = "ipOwners";

  /** Return results for only these IPs */
  private final @Nullable String _ipSpec;

  /** Whether to return duplicate IPs (owned by multiple nodes) only. */
  private final boolean _duplicatesOnly;

  @JsonCreator
  private static IpOwnersQuestion create(
      @JsonProperty(PROP_IPS) @Nullable String ipSpec,
      @JsonProperty(PROP_DUPLICATES_ONLY) @Nullable Boolean duplicatesOnly) {
    return new IpOwnersQuestion(ipSpec, firstNonNull(duplicatesOnly, false));
  }

  public IpOwnersQuestion(@Nullable String ipSpec, boolean duplicatesOnly) {
    _ipSpec = ipSpec;
    _duplicatesOnly = duplicatesOnly;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return QUESTION_NAME;
  }

  @JsonProperty(PROP_IPS)
  public String getIps() {
    return _ipSpec;
  }

  @JsonProperty(PROP_DUPLICATES_ONLY)
  public boolean getDuplicatesOnly() {
    return _duplicatesOnly;
  }

  /**
   * Returns the IpSpace that the user wants to limit the answer to.
   *
   * <p>If user input is null, there is no limit.
   */
  @JsonIgnore
  public IpSpaceSpecifier getIpSpaceSpecifier() {
    return SpecifierFactories.getIpSpaceSpecifierOrDefault(
        _ipSpec, new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE));
  }
}
