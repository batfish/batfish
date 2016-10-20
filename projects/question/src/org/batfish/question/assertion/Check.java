package org.batfish.question.assertion;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.batfish.common.BatfishException;
import org.batfish.question.assertion.matchers.AssertionMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Check {
   ABSENT("absent"),
   EQ("eq"),
   EXISTS("exists"),
   GE("ge"),
   GT("gt"),
   LE("le"),
   LT("lt"),
   SIZE_EQ("sizeeq"),
   SIZE_GE("sizege"),
   SIZE_GT("sizegt"),
   SIZE_LE("sizele"),
   SIZE_LT("sizelt");

   private final static Map<String, Check> _map = buildMap();

   private synchronized static Map<String, Check> buildMap() {
      Map<String, Check> map = new HashMap<>();
      for (Check value : Check.values()) {
         String name = value._name;
         map.put(name, value);
      }
      return Collections.unmodifiableMap(map);
   }

   @JsonCreator
   public static Check fromName(String name) {
      Check instance = _map.get(name.toLowerCase());
      if (instance == null) {
         throw new BatfishException("No " + Check.class.getSimpleName()
               + " with name: '" + name + "'");
      }
      return instance;
   }

   private final String _name;

   private Check(String name) {
      _name = name;
   }

   @JsonValue
   public String checkName() {
      return _name;
   }

   public Matcher<?> matcher(List<Object> args) {
      switch (this) {
      case ABSENT: {
         return Matchers.equalTo(PathResult.EMPTY);
      }

      case EQ: {
         if (args.size() != 1) {
            throw new BatfishException("Expected only 1 arg");
         }
         Object arg = args.get(0);
         return Matchers.equalTo(arg);
      }

      case EXISTS: {
         return Matchers.not(Matchers.equalTo(PathResult.EMPTY));
      }

      case GE: {
         if (args.size() != 1) {
            throw new BatfishException("Expected only 1 arg");
         }
         Object arg = args.get(0);
         return AssertionMatchers.compare(arg, Matchers::greaterThanOrEqualTo);
      }

      case GT: {
         if (args.size() != 1) {
            throw new BatfishException("Expected only 1 arg");
         }
         Object arg = args.get(0);
         return AssertionMatchers.compare(arg, Matchers::greaterThan);
      }

      case LE: {
         if (args.size() != 1) {
            throw new BatfishException("Expected only 1 arg");
         }
         Object arg = args.get(0);
         return AssertionMatchers.compare(arg, Matchers::lessThanOrEqualTo);
      }

      case LT: {
         if (args.size() != 1) {
            throw new BatfishException("Expected only 1 arg");
         }
         Object arg = args.get(0);
         return AssertionMatchers.compare(arg, Matchers::lessThan);
      }

      case SIZE_EQ: {
         if (args.size() != 1) {
            throw new BatfishException("Expected only 1 arg");
         }
         Object arg = args.get(0);
         return AssertionMatchers.hasSize(Matchers.equalTo(arg));
      }

      case SIZE_GE: {
         if (args.size() != 1) {
            throw new BatfishException("Expected only 1 arg");
         }
         Object arg = args.get(0);
         if (!(arg instanceof Comparable<?>)) {
            throw new BatfishException("Expected Comparable arg");
         }
         Integer i = (Integer) arg;
         return AssertionMatchers.hasSize(Matchers.greaterThanOrEqualTo(i));
      }

      case SIZE_GT: {
         if (args.size() != 1) {
            throw new BatfishException("Expected only 1 arg");
         }
         Object arg = args.get(0);
         Integer i = (Integer) arg;
         return AssertionMatchers.hasSize(Matchers.greaterThan(i));
      }

      case SIZE_LE: {
         if (args.size() != 1) {
            throw new BatfishException("Expected only 1 arg");
         }
         Object arg = args.get(0);
         Integer i = (Integer) arg;
         return AssertionMatchers.hasSize(Matchers.lessThanOrEqualTo(i));
      }

      case SIZE_LT: {
         if (args.size() != 1) {
            throw new BatfishException("Expected only 1 arg");
         }
         Object arg = args.get(0);
         Integer i = (Integer) arg;
         return AssertionMatchers.hasSize(Matchers.lessThan(i));
      }

      default:
         break;
      }
      throw new BatfishException("Unimplemented check: '" + name() + "'");
   }

}
