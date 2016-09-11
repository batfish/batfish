package org.batfish.datamodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum State {
   ESTABLISHED(1),
   INVALID(3),
   NEW(0),
   RELATED(2);

   private final static Map<Integer, State> _map = buildMap();

   private static Map<Integer, State> buildMap() {
      Map<Integer, State> map = new HashMap<>();
      for (State value : State.values()) {
         int num = value._num;
         map.put(num, value);
      }
      return Collections.unmodifiableMap(map);
   }

   @JsonCreator
   public static State fromNum(int num) {
      State instance = _map.get(num);
      if (instance == null) {
         throw new BatfishException(
               "Not a valid state number: \"" + num + "\"");
      }
      return instance;
   }

   private final int _num;

   private State(int num) {
      _num = num;
   }

   public int number() {
      return _num;
   }
}
