package org.batfish.datamodel.references;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import com.google.common.collect.TreeMultiset;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.common.util.CollectionUtil;
import org.batfish.datamodel.DefinedStructureInfo;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.vendor.StructureType;
import org.batfish.vendor.StructureUsage;

/** Manages information about structure definition, reference, and usage. */
public final class StructureManager implements Serializable {

  /**
   * Delete the definition of and any references to the specified structure. Returns {@code false}
   * and warns if the delete request is unsuccessful (e.g. no corresponding structure), otherwise
   * returns {@code true}.
   */
  public boolean deleteStructure(String name, StructureType type, Warnings warnings) {
    boolean succeeded = deleteDefinition(name, type, warnings);
    if (succeeded) {
      deleteReferences(name, type);
    }
    return succeeded;
  }

  /**
   * Delete the definition for the specified structure. Returns {@code true} if the delete was
   * successful, otherwise returns {@code false}.
   */
  private boolean deleteDefinition(String name, StructureType type, Warnings warnings) {
    // TODO we're only deleting structures with self-refs currently and need to determine how to
    // handle other references (e.g. are they undefined references now? do they disappear?)
    DefinedStructureInfo info = _definitions.get(type.getDescription(), name);
    if (info == null) {
      warnings.redFlagf(
          "Cannot delete structure %s (%s): %s is undefined.", name, type.getDescription(), name);
      return false;
    }

    _definitions.remove(type.getDescription(), name);
    return true;
  }

  /** Delete any existing references to the specified structure. */
  private void deleteReferences(String name, StructureType type) {
    Map<String, Map<StructureUsage, Multiset<Integer>>> refsByName = _references.get(type);
    if (refsByName != null) {
      refsByName.remove(name);
    }
  }

  /**
   * Gets the {@link DefinedStructureInfo} for the specified structure {@code name} and {@code
   * structureType}, initializing if necessary.
   */
  public @Nonnull DefinedStructureInfo getOrDefine(StructureType structureType, String name) {
    DefinedStructureInfo info = _definitions.get(structureType.getDescription(), name);
    if (info == null) {
      info = new DefinedStructureInfo();
      _definitions.put(structureType.getDescription(), name, info);
    }
    return info;
  }

  /**
   * Gets the {@link DefinedStructureInfo} for the specified structure {@code name} and {@code
   * structureType}, initializing if necessary.
   */
  public @Nonnull Optional<DefinedStructureInfo> getDefinition(
      StructureType structureType, String name) {
    return Optional.ofNullable(_definitions.get(structureType.getDescription(), name));
  }

  /** Returns a map of all structure references to the given type. */
  public @Nonnull Map<String, Map<StructureUsage, Multiset<Integer>>> getStructureReferences(
      StructureType type) {
    return _references.getOrDefault(type, ImmutableMap.of());
  }

  /** Returns {@code true} iff there is a defined structure with the given type and name. */
  public boolean hasDefinition(String type, String name) {
    return _definitions.contains(type, name);
  }

  /**
   * Mark all references to a structure of the given type.
   *
   * <p>Do not use if {@code type} is used as an abstract structure type; instead use {@link
   * #markAbstractStructure(StructureType, StructureUsage, Collection)}.
   */
  public void markConcreteStructure(StructureType type) {
    Map<String, Map<StructureUsage, Multiset<Integer>>> references =
        _references.getOrDefault(type, Collections.emptyMap());
    references.forEach(
        (name, byUsage) -> {
          DefinedStructureInfo def = _definitions.get(type.getDescription(), name);
          if (def == null) {
            byUsage.forEach(
                (usage, lines) -> lines.forEach(line -> undefined(type, name, usage, line)));
          } else {
            int count = byUsage.values().stream().mapToInt(Multiset::size).sum();
            def.setNumReferrers(def.getNumReferrers() + count);
          }
        });
  }

  /**
   * Updates referrers and/or warns for undefined structures based on references to an abstract
   * {@link StructureType}: a reference type that may refer to one of a number of defined structure
   * types passed in {@code structureTypesToCheck}.
   *
   * <p>For example using Cisco devices, see {@code CiscoStructureType.ACCESS_LIST} and how it
   * expands to a list containing many types of IPv4 and IPv6 access lists.
   */
  public void markAbstractStructure(
      StructureType type,
      StructureUsage usage,
      Collection<? extends StructureType> structureTypesToCheck) {
    Map<String, Map<StructureUsage, Multiset<Integer>>> references =
        firstNonNull(_references.get(type), Collections.emptyMap());
    references.forEach(
        (name, byUsage) -> {
          Multiset<Integer> lines = firstNonNull(byUsage.get(usage), ImmutableMultiset.of());
          List<DefinedStructureInfo> matchingStructures =
              structureTypesToCheck.stream()
                  .map(t -> _definitions.get(t.getDescription(), name))
                  .filter(Objects::nonNull)
                  .collect(ImmutableList.toImmutableList());
          if (matchingStructures.isEmpty()) {
            for (int line : lines) {
              undefined(type, name, usage, line);
            }
          } else {
            matchingStructures.forEach(
                info -> info.setNumReferrers(info.getNumReferrers() + lines.size()));
          }
        });
  }

  /**
   * Updates referrers and/or warns for undefined structures based on references to an abstract
   * {@link StructureType}: a reference type that may refer to one of a number of defined structure
   * types passed in {@code structureTypesToCheck}.
   */
  public void markAbstractStructureAllUsages(
      StructureType type, Collection<? extends StructureType> structureTypesToCheck) {
    Map<String, Map<StructureUsage, Multiset<Integer>>> references =
        firstNonNull(_references.get(type), Collections.emptyMap());
    references.forEach(
        (name, byUsage) ->
            byUsage.forEach(
                (usage, lines) -> {
                  List<DefinedStructureInfo> matchingStructures =
                      structureTypesToCheck.stream()
                          .map(t -> _definitions.get(t.getDescription(), name))
                          .filter(Objects::nonNull)
                          .collect(ImmutableList.toImmutableList());
                  if (matchingStructures.isEmpty()) {
                    for (int line : lines) {
                      undefined(type, name, usage, line);
                    }
                  } else {
                    matchingStructures.forEach(
                        info -> info.setNumReferrers(info.getNumReferrers() + lines.size()));
                  }
                }));
  }

  /** Record a reference on the given line. */
  public void referenceStructure(
      @Nonnull StructureType type, @Nonnull String name, @Nonnull StructureUsage usage, int line) {
    _references
        .computeIfAbsent(type, t -> new HashMap<>())
        .computeIfAbsent(name, n -> new HashMap<>())
        .computeIfAbsent(usage, u -> TreeMultiset.create())
        .add(line);
  }

  /**
   * Rename the specified structure in its definition as well as references. Returns {@code false}
   * and warns if the rename request is unsuccessful (e.g. no corresponding structure or name
   * conflict), otherwise returns {@code true}.
   *
   * <p>The specified {@link Collection} of {@link StructureType} should contain all structure types
   * that share the same namespace (including the specified structure {@code type}); i.e. a rename
   * will only succeed if the new name is not already in use by any structures of the specified
   * types.
   */
  public boolean renameStructure(
      String origName,
      String newName,
      StructureType type,
      Collection<StructureType> sameNamespaceTypes,
      Warnings warnings) {
    assert sameNamespaceTypes.contains(type);
    boolean succeeded =
        renameStructureDefinition(origName, newName, type, sameNamespaceTypes, warnings);
    if (succeeded) {
      renameStructureReferences(origName, newName, type);
    }
    return succeeded;
  }

  /**
   * Update the definition for the specified structure to use the new name. Returns {@code true} if
   * the rename was successful, otherwise returns {@code false}.
   *
   * <p>Update is only successful if the specified structure is defined and the new name is not
   * already taken. Checks for a name conflict with any of the specified structure types.
   */
  private boolean renameStructureDefinition(
      String origName,
      String newName,
      StructureType type,
      Collection<StructureType> sameNamespaceTypes,
      Warnings warnings) {
    if (!_definitions.contains(type.getDescription(), origName)) {
      warnings.redFlagf(
          "Cannot rename structure %s (%s) to %s: %s is undefined.",
          origName, type.getDescription(), newName, origName);
      return false;
    }

    for (StructureType otherType : sameNamespaceTypes) {
      if (_definitions.contains(otherType.getDescription(), newName)) {
        warnings.redFlag(
            String.format(
                "Cannot rename structure %s (%s) to %s: %s is already in use as %s.",
                origName, type.getDescription(), newName, newName, otherType.getDescription()));
        return false;
      }
    }

    DefinedStructureInfo def = _definitions.remove(type.getDescription(), origName);
    _definitions.put(type.getDescription(), newName, def);
    return true;
  }

  /** If any references exist to the specified structure, update them to use the new name. */
  private void renameStructureReferences(String origName, String newName, StructureType type) {
    Map<String, Map<StructureUsage, Multiset<Integer>>> refsByName = _references.get(type);
    if (refsByName != null && refsByName.containsKey(origName)) {
      Map<StructureUsage, Multiset<Integer>> refs = refsByName.remove(origName);
      refsByName.put(newName, refs);
    }
  }

  public void undefined(StructureType structureType, String name, StructureUsage usage, int line) {
    _undefinedReferences
        .computeIfAbsent(structureType.getDescription(), t -> new HashMap<>())
        .computeIfAbsent(name, n -> new HashMap<>())
        .computeIfAbsent(usage.getDescription(), u -> new HashSet<>())
        .add(line);
  }

  private void addReference(
      Map<String, Map<String, Map<String, Set<Integer>>>> referenceMapByType,
      StructureType structureType,
      String name,
      StructureUsage usage,
      int line) {
    referenceMapByType
        .computeIfAbsent(structureType.getDescription(), t -> new HashMap<>())
        .computeIfAbsent(name, n -> new HashMap<>())
        .computeIfAbsent(usage.getDescription(), u -> new HashSet<>())
        .add(line);
  }

  /** Returns a new, empty {@link StructureManager}. */
  public static StructureManager create() {
    return new StructureManager();
  }

  /**
   * Saves the structure definition and reference information into the given {@link
   * ConvertConfigurationAnswerElement}.
   */
  public void saveInto(ConvertConfigurationAnswerElement ccae, String filename) {
    SortedMap<String, SortedMap<String, DefinedStructureInfo>> sortedDefs =
        CollectionUtil.toImmutableSortedMap(
            _definitions.rowMap(),
            Entry::getKey,
            typeEntry ->
                CollectionUtil.toImmutableSortedMap(
                    typeEntry.getValue(), Entry::getKey, Entry::getValue));
    ccae.getDefinedStructures().put(filename, sortedDefs);

    SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> sortedRefs =
        CollectionUtil.toImmutableSortedMap(
            _references,
            typeEntry -> typeEntry.getKey().getDescription(),
            typeEntry ->
                CollectionUtil.toImmutableSortedMap(
                    typeEntry.getValue(),
                    Entry::getKey,
                    nameEntry ->
                        CollectionUtil.toImmutableSortedMap(
                            nameEntry.getValue(),
                            usageEntry -> usageEntry.getKey().getDescription(),
                            usageEntry -> ImmutableSortedSet.copyOf(usageEntry.getValue()))));
    if (!sortedRefs.isEmpty()) {
      ccae.getReferencedStructures().put(filename, sortedRefs);
    }

    SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> sortedUndefined =
        CollectionUtil.toImmutableSortedMap(
            _undefinedReferences,
            Entry::getKey,
            typeEntry ->
                CollectionUtil.toImmutableSortedMap(
                    typeEntry.getValue(),
                    Entry::getKey,
                    nameEntry ->
                        CollectionUtil.toImmutableSortedMap(
                            nameEntry.getValue(),
                            Entry::getKey,
                            usageEntry -> ImmutableSortedSet.copyOf(usageEntry.getValue()))));
    ccae.getUndefinedReferences().put(filename, sortedUndefined);
  }

  /** Type description -> Name -> DefinedStructureInfo */
  private final @Nonnull Table<String, String, DefinedStructureInfo> _definitions;

  /** StructureType -> Name -> StructureUsage */
  private final @Nonnull Map<StructureType, Map<String, Map<StructureUsage, Multiset<Integer>>>>
      _references;

  /** structType -> structName -> usage -> lines */
  private final @Nonnull Map<String, Map<String, Map<String, Set<Integer>>>> _undefinedReferences;

  private StructureManager() {
    _definitions = HashBasedTable.create();
    _references = new HashMap<>();
    _undefinedReferences = new HashMap<>();
  }
}
