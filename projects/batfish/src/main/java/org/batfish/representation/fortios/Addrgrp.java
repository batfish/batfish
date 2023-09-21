package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** FortiOS datamodel component containing addrgrp (address group) configuration */
public class Addrgrp extends AddrgrpMember implements Serializable {
  public enum Type {
    DEFAULT, // from cli description: address may belong to multiple groups
    FOLDER, // from cli description: members may not belong to any other group
  }

  public static final Type DEFAULT_TYPE = Type.DEFAULT;
  public static final boolean DEFAULT_EXCLUDE = false;

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public BatfishUUID getBatfishUUID() {
    return _uuid;
  }

  public @Nullable Boolean getExclude() {
    return _exclude;
  }

  /**
   * Get the effective exclude value of the addrgrp, inferring the value even if not explicitly
   * configured.
   */
  public boolean getExcludeEffective() {
    return firstNonNull(_exclude, DEFAULT_EXCLUDE);
  }

  public @Nullable Type getType() {
    return _type;
  }

  /**
   * Get the effective type of the addrgrp, inferring the value even if not explicitly configured.
   */
  public @Nonnull Type getTypeEffective() {
    return firstNonNull(_type, DEFAULT_TYPE);
  }

  /**
   * Names of addrgrp exclude members associated with this addrgrp. Should be derived from {@link
   * this#getMemberUUIDs} when finishing building the VS model.
   */
  public @Nullable Set<String> getExcludeMember() {
    return _excludeMember;
  }

  /** Set of Batfish-internal UUIDs associated with exclude member references. */
  public @Nonnull Set<BatfishUUID> getExcludeMemberUUIDs() {
    return _excludeMemberUuids;
  }

  /**
   * Names of addrgrp members associated with this addrgrp. Should be derived from {@link
   * this#getMemberUUIDs} when finishing building the VS model.
   */
  public @Nullable Set<String> getMember() {
    return _member;
  }

  /** Set of Batfish-internal UUIDs associated with member references. */
  public @Nonnull Set<BatfishUUID> getMemberUUIDs() {
    return _memberUuids;
  }

  @Override
  public void setName(String name) {
    _name = name;
  }

  public void setExclude(Boolean exclude) {
    _exclude = exclude;
  }

  public void setExcludeMember(Set<String> excludeMember) {
    _excludeMember = ImmutableSet.copyOf(excludeMember);
  }

  public void setMember(Set<String> member) {
    _member = ImmutableSet.copyOf(member);
  }

  public void setType(Type type) {
    _type = type;
  }

  public Addrgrp(String name, BatfishUUID uuid) {
    _name = name;
    _uuid = uuid;

    _excludeMemberUuids = new HashSet<>();
    _memberUuids = new HashSet<>();
  }

  private @Nonnull String _name;
  private final @Nonnull BatfishUUID _uuid;
  private @Nullable Boolean _exclude;
  private @Nullable Set<String> _excludeMember;
  private final @Nonnull Set<BatfishUUID> _excludeMemberUuids;
  private @Nullable Set<String> _member;
  private final @Nonnull Set<BatfishUUID> _memberUuids;
  private @Nullable Type _type;
}
