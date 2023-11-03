package org.batfish.question.lpmroutes;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.routes.RoutesQuestion.RibProtocol;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/**
 * Retrieves a longest prefix match for a given {@link Ip} address. Can be done is a
 * protocol-specific RIB supported by {@link RibProtocol}.
 */
@ParametersAreNonnullByDefault
public class LpmRoutesQuestion extends Question {
  private static final String PROP_IP = "ip";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_RIB = "rib";
  private static final String PROP_VRFS = "vrfs";
  private static final String VRFS_ALL = ".*";

  private final @Nonnull Ip _ip;
  private final @Nullable String _nodes;
  private final @Nonnull String _vrfs;
  private final @Nonnull RibProtocol _ribProtocol;

  /**
   * Create a new question.
   *
   * @param ip the IP address to do longest prefix match on
   * @param nodes input to a {@link NodeSpecifier} for filtering nodes
   * @param vrfs regex for VRF filtering
   * @param ribProtocol the {@link RibProtocol} for which to retrieve the RIBs.
   */
  public LpmRoutesQuestion(Ip ip, @Nullable String nodes, String vrfs, RibProtocol ribProtocol) {
    checkArgument(
        ribProtocol == RibProtocol.MAIN,
        "Unsupported RIB %s. Only %s is supported at this time",
        ribProtocol,
        RibProtocol.MAIN);
    _ip = ip;
    _nodes = nodes;
    _vrfs = vrfs;
    _ribProtocol = ribProtocol;
  }

  LpmRoutesQuestion() {
    // Choose an arbitrary public IP
    this(Ip.parse("8.8.8.8"), null, VRFS_ALL, RibProtocol.MAIN);
  }

  @JsonCreator
  private static LpmRoutesQuestion jsonCreator(
      @JsonProperty(PROP_IP) @Nullable Ip ip,
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_VRFS) @Nullable String vrfs,
      @JsonProperty(PROP_RIB) @Nullable RibProtocol protocol) {
    checkArgument(ip != null, "Missing %s", PROP_IP);
    return new LpmRoutesQuestion(
        ip, nodes, firstNonNull(vrfs, VRFS_ALL), firstNonNull(protocol, RibProtocol.MAIN));
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @JsonProperty(PROP_IP)
  public @Nonnull Ip getIp() {
    return _ip;
  }

  @JsonProperty(PROP_NODES)
  private @Nullable String getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_VRFS)
  public @Nonnull String getVrfs() {
    return _vrfs;
  }

  @JsonProperty(PROP_RIB)
  public @Nonnull RibProtocol getRibProtocol() {
    return _ribProtocol;
  }

  @Override
  public String getName() {
    return "lpmRoutes";
  }

  @JsonIgnore
  public @Nonnull NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }
}
