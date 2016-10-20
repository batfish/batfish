package org.batfish.question.assertion;

import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.question.assertion.matchers.AssertionMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public enum Check {
   ABSENT,
   EQ,
   EXISTS,
   GE,
   GT,
   LE,
   LT,
   SIZE_EQ,
   SIZE_GE,
   SIZE_GT,
   SIZE_LE,
   SIZE_LT;

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
