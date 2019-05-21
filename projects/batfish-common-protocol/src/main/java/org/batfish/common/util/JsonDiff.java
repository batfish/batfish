package org.batfish.common.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;
import org.batfish.common.BatfishException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class JsonDiff {

  public static final String ADDED_ITEM_CODE = "ADDED :: ";
  public static final String CHANGED_ITEM_BASE = "BASE";
  public static final String CHANGED_ITEM_CODE = "CHANGED :: ";
  public static final String CHANGED_ITEM_DELTA = "DELTA";
  public static final String COMMON_ITEM_CODE = "COMMON :: ";
  public static final String REMOVED_ITEM_CODE = "REMOVED :: ";

  private final SortedMap<String, Object> _data;

  public JsonDiff(JSONArray lhsArray, JSONArray rhsArray) throws JSONException {
    _data = new TreeMap<>();
    int lhsLength = lhsArray.length();
    int rhsLength = rhsArray.length();
    int minLength = Math.min(lhsLength, rhsLength);
    for (int i = 0; i < minLength; i++) {
      Object lhsCurrentElem = lhsArray.get(i);
      Object rhsCurrentElem = rhsArray.get(i);
      if (lhsCurrentElem instanceof JSONObject) {
        if (rhsCurrentElem instanceof JSONObject) {
          // Both are JSONObjects
          JSONObject lhsJ = (JSONObject) lhsCurrentElem;
          JSONObject rhsJ = (JSONObject) rhsCurrentElem;
          JsonDiff diff = new JsonDiff(lhsJ, rhsJ);
          if (!diff._data.isEmpty()) {
            String indexAsString = COMMON_ITEM_CODE + i;
            _data.put(indexAsString, diff);
          }
        } else {
          throw new BatfishException("Cannot compare JSONObject to non-JSONObject");
        }
      } else if (lhsCurrentElem instanceof JSONArray) {
        if (rhsCurrentElem instanceof JSONArray) {
          // Both are JSONArrays
          JSONArray lhsA = (JSONArray) lhsCurrentElem;
          JSONArray rhsA = (JSONArray) rhsCurrentElem;
          JsonDiff diff = new JsonDiff(lhsA, rhsA);
          if (!diff._data.isEmpty()) {
            String indexAsString = COMMON_ITEM_CODE + i;
            _data.put(indexAsString, diff);
          }
        } else {
          throw new BatfishException("Cannot compare JSONArray to non-JSONArray");
        }
      } else if (rhsCurrentElem instanceof JSONObject) {
        throw new BatfishException("Cannot compare non-JSONObject to JSONObject");
      } else if (rhsCurrentElem instanceof JSONArray) {
        throw new BatfishException("Cannot compare non-JSONArray to JSONArray");
      } else {
        // Neither is a JSONObject nor JSONArray
        Class<?> lhsClass = lhsCurrentElem.getClass();
        Class<?> rhsClass = rhsCurrentElem.getClass();
        if (!lhsClass.equals(rhsClass)) {
          throw new BatfishException(
              "lhs class: '"
                  + lhsClass.getCanonicalName()
                  + "' differs from rhs class: '"
                  + rhsClass.getCanonicalName()
                  + "'");
        }
        if (!lhsCurrentElem.equals(rhsCurrentElem)) {
          // String removedIndex = CHANGED_ITEM_BASE_CODE + i;
          // String addedIndex = CHANGED_ITEM_DELTA_CODE + i;
          // _data.put(removedIndex, lhsCurrentElem);
          // _data.put(addedIndex, rhsCurrentElem);
          String key = CHANGED_ITEM_CODE + i;
          SortedMap<String, Object> value = new TreeMap<>();
          value.put(CHANGED_ITEM_BASE, lhsCurrentElem);
          value.put(CHANGED_ITEM_DELTA, rhsCurrentElem);
          _data.put(key, value);
        }
      }
    }
    for (int i = minLength; i < lhsLength; i++) {
      String removedIndex = REMOVED_ITEM_CODE + i;
      Object lhsCurrentElem = lhsArray.get(i);
      Object jdValue = getValue(lhsCurrentElem);
      _data.put(removedIndex, jdValue);
    }
    for (int i = minLength; i < rhsLength; i++) {
      String addedIndex = ADDED_ITEM_CODE + i;
      Object rhsCurrentElem = rhsArray.get(i);
      Object jdValue = getValue(rhsCurrentElem);
      _data.put(addedIndex, jdValue);
    }
  }

  public JsonDiff(JSONObject lhs, JSONObject rhs) {
    _data = new TreeMap<>();
    try {
      SortedSet<String> lhsKeys = new TreeSet<>();
      for (Iterator<?> i = lhs.keys(); i.hasNext(); ) {
        String key = (String) i.next();
        lhsKeys.add(key);
      }
      SortedSet<String> rhsKeys = new TreeSet<>();
      for (Iterator<?> i = rhs.keys(); i.hasNext(); ) {
        String key = (String) i.next();
        rhsKeys.add(key);
      }
      SortedSet<String> commonKeys = intersection(lhsKeys, rhsKeys, TreeSet::new);
      SortedSet<String> lhsOnlyKeys = difference(lhsKeys, rhsKeys, TreeSet::new);
      SortedSet<String> rhsOnlyKeys = difference(rhsKeys, lhsKeys, TreeSet::new);
      for (String lhsOnlyKey : lhsOnlyKeys) {
        String removedKeyName = REMOVED_ITEM_CODE + lhsOnlyKey;
        _data.put(removedKeyName, getValue(lhs.get(lhsOnlyKey)));
      }
      for (String rhsOnlyKey : rhsOnlyKeys) {
        String addedKeyName = ADDED_ITEM_CODE + rhsOnlyKey;
        _data.put(addedKeyName, getValue(rhs.get(rhsOnlyKey)));
      }
      for (String commonKey : commonKeys) {
        Object lhsValue = lhs.get(commonKey);
        Object rhsValue = rhs.get(commonKey);
        if (lhsValue instanceof JSONObject) {
          if (rhsValue instanceof JSONObject) {
            // Both are JSONObjects
            JSONObject lhsValueJson = (JSONObject) lhsValue;
            JSONObject rhsValueJson = (JSONObject) rhsValue;
            JsonDiff value = new JsonDiff(lhsValueJson, rhsValueJson);
            if (!value._data.isEmpty()) {
              _data.put(commonKey, value);
            }
          } else {
            throw new BatfishException("Cannot compare JSONObject to non-JSONObject");
          }
        } else if (lhsValue instanceof JSONArray) {
          if (rhsValue instanceof JSONArray) {
            // Both are JSONArrays
            JSONArray lhsArray = (JSONArray) lhsValue;
            JSONArray rhsArray = (JSONArray) rhsValue;
            JsonDiff arrayDiff = new JsonDiff(lhsArray, rhsArray);
            if (!arrayDiff._data.isEmpty()) {
              _data.put(commonKey, arrayDiff);
            }
          } else {
            throw new BatfishException("Cannot compare JSONArray to non-JSONArray");
          }
        } else if (rhsValue instanceof JSONArray) {
          throw new BatfishException("Cannot compare non-JSONArray to JSONArray");
        } else if (rhsValue instanceof JSONObject) {
          throw new BatfishException("Cannot compare non-JSONObject to JSONObject");
        } else {
          // Neither is a JSONObject nor a JSONArray
          Class<?> lhsClass = lhsValue.getClass();
          Class<?> rhsClass = rhsValue.getClass();
          if (!lhsClass.equals(rhsClass)) {
            throw new BatfishException(
                "lhs class: '"
                    + lhsClass.getCanonicalName()
                    + "' differs from rhs class: '"
                    + rhsClass.getCanonicalName()
                    + "'");
          }
          if (!lhsValue.equals(rhsValue)) {
            // String removedKey = CHANGED_ITEM_BASE_CODE + commonKey;
            // String addedKey = CHANGED_ITEM_DELTA_CODE + commonKey;
            // _data.put(removedKey, lhsValue);
            // _data.put(addedKey, rhsValue);
            String key = CHANGED_ITEM_CODE + commonKey;
            SortedMap<String, Object> value = new TreeMap<>();
            value.put(CHANGED_ITEM_BASE, lhsValue);
            value.put(CHANGED_ITEM_DELTA, rhsValue);
            _data.put(key, value);
          }
        }
      }
    } catch (JSONException e) {
      throw new BatfishException("Could not create diff of JSON objects", e);
    }
  }

  @JsonCreator
  private JsonDiff(SortedMap<String, Object> data) {
    _data = data;
  }

  private static <S extends Set<T>, T> S difference(
      Set<T> minuendSet, Set<T> subtrahendSet, Supplier<S> setConstructor) {
    S differenceSet = setConstructor.get();
    differenceSet.addAll(minuendSet);
    differenceSet.removeAll(subtrahendSet);
    return differenceSet;
  }

  private static <S extends Set<T>, T> S intersection(
      Set<T> set1, Collection<T> set2, Supplier<S> setConstructor) {
    S intersectionSet = setConstructor.get();
    intersectionSet.addAll(set1);
    intersectionSet.retainAll(set2);
    return intersectionSet;
  }

  @JsonValue
  public SortedMap<String, Object> getData() {
    return _data;
  }

  private static Object getValue(Object object) throws JSONException {
    if (object instanceof JSONObject) {
      JSONObject j = (JSONObject) object;
      Map<String, Object> map = new TreeMap<>();
      for (Iterator<?> i = j.keys(); i.hasNext(); ) {
        String key = (String) i.next();
        Object value = j.get(key);
        Object jdValue = getValue(value);
        map.put(key, jdValue);
      }
      return map;
    } else if (object instanceof JSONArray) {
      List<Object> list = new LinkedList<>();
      JSONArray array = (JSONArray) object;
      for (int i = 0; i < array.length(); i++) {
        Object value = array.get(i);
        Object jdValue = getValue(value);
        list.add(jdValue);
      }
      return list;
    } else {
      return object;
    }
  }
}
