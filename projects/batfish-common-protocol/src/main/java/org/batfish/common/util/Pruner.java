package org.batfish.common.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Prune a set of objects to "maximize" coverage over some ranked set of properties of interest.
 *
 * @param <T> The type of objects being pruned.
 */
public class Pruner<T> {
  private static class Property<T> {
    private final Function<T, Object> _extractor;
    private final Map<Object, List<T>> _objectsByPropertyValue;

    Property(Function<T, Object> extractor, List<T> objects) {
      _extractor = extractor;

      _objectsByPropertyValue = new HashMap<>();
      objects.forEach(
          obj ->
              _objectsByPropertyValue
                  .computeIfAbsent(_extractor.apply(obj), k -> new ArrayList<>())
                  .add(obj));
    }

    void picked(T obj) {
      _objectsByPropertyValue.remove(_extractor.apply(obj));
    }

    @Nullable
    T pick(Set<T> selectedObjects) {
      if (_objectsByPropertyValue.isEmpty()) {
        return null;
      }
      List<Object> valuesToRemove = new ArrayList<>();
      T selected = null;
      Iterator<Entry<Object, List<T>>> iter = _objectsByPropertyValue.entrySet().iterator();
      while (selected == null && iter.hasNext()) {
        Entry<Object, List<T>> entry = iter.next();
        Object propertyValue = entry.getKey();
        for (T obj : entry.getValue()) {
          if (!selectedObjects.contains(obj)) {
            selected = obj;
            break;
          }
        }
        /* Either we selected an object with this property, or all have already
         * been selected. Either way, remove it.
         */
        valuesToRemove.add(propertyValue);
      }
      valuesToRemove.forEach(_objectsByPropertyValue::remove);
      return selected;
    }
  }

  // Properties in order of decreasing importance.
  private final List<Function<T, Object>> _propertyExtractors;

  private Pruner(List<Function<T, Object>> propertyExtractors) {
    Preconditions.checkArgument(!propertyExtractors.isEmpty(), "Must define at least one property");
    _propertyExtractors = ImmutableList.copyOf(propertyExtractors);
  }

  public Collection<T> prune(List<T> objects, int maxSize) {
    if (objects.size() <= maxSize) {
      return objects;
    }

    Set<T> selectedObjects = new HashSet<>();
    List<Property<T>> properties =
        _propertyExtractors.stream()
            .map(propertyExtractor -> new Property<>(propertyExtractor, objects))
            .collect(ImmutableList.toImmutableList());

    boolean done = false;
    while (selectedObjects.size() < maxSize && !done) {
      done = true;
      for (Property<T> property : properties) {
        T picked = property.pick(selectedObjects);
        if (picked != null) {
          selectedObjects.add(picked);
          properties.forEach(prop -> prop.picked(picked));
          done = false;
          break;
        }
      }
    }

    if (selectedObjects.size() < maxSize) {
      // add remaining objects in input order until maxSize is reached
      selectedObjects.addAll(
          objects.stream()
              .filter(obj -> !selectedObjects.contains(obj))
              .limit(maxSize - selectedObjects.size())
              .collect(Collectors.toList()));
    }
    return selectedObjects;
  }

  /** @param <T> The type of objects being pruned. */
  public static class Builder<T> {
    private final ImmutableList.Builder<Function<T, Object>> _propertyExtractors =
        ImmutableList.builder();

    public <U> Builder<T> addProperty(Function<T, U> extractor) {
      _propertyExtractors.add(extractor::apply);
      return this;
    }

    public Pruner<T> build() {
      return new Pruner<T>(_propertyExtractors.build());
    }
  }

  public static <T> Builder<T> builder() {
    return new Builder<T>();
  }
}
