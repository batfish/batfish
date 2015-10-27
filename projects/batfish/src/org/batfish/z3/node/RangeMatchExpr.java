package org.batfish.z3.node;

import java.util.Set;

import org.batfish.util.SubRange;
import org.batfish.z3.NodProgram;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;

public class RangeMatchExpr extends BooleanExpr {

   public static BooleanExpr bitvectorGEExpr(String bv, long lb, int numBits) {
      // these masks refer to nested conditions, not to bitwise and, or
      int numBitsLeft = numBits;

      BooleanExpr finalExpr = null;
      BooleanExpr currentExpr = null;
      OrExpr currentOrExpr = null;
      AndExpr currentAndExpr = null;
      while (numBitsLeft > 0) {
         // find largest remaining 'subnet mask' not overlapping with lowerbound
         int orSpread = -1;
         long orMask = 0;
         int orStartPos;
         int orEndPos;
         orEndPos = numBitsLeft - 1;
         for (int i = orEndPos; i >= 0; i--) {
            orMask |= (1L << i);
            if ((lb & orMask) != 0) {
               orMask ^= (1L << i);
               break;
            }
            else {
               orSpread++;
               numBitsLeft--;
            }
         }
         if (orSpread >= 0) {
            orStartPos = orEndPos - orSpread;
            LitIntExpr zeroExpr = new LitIntExpr(0L, orStartPos, orEndPos);
            IntExpr extractExpr = newExtractExpr(bv, numBits, orStartPos,
                  orEndPos);
            EqExpr eqExpr = new EqExpr(extractExpr, zeroExpr);
            NotExpr notExpr = new NotExpr(eqExpr);
            OrExpr oldOrExpr = currentOrExpr;
            currentOrExpr = new OrExpr();
            currentOrExpr.addDisjunct(notExpr);
            if (currentExpr != null) {
               if (currentExpr == currentAndExpr) {
                  currentAndExpr.addConjunct(currentOrExpr);
               }
               else if (currentExpr == oldOrExpr) {
                  oldOrExpr.addDisjunct(currentOrExpr);
               }
            }
            else {
               finalExpr = currentOrExpr;
            }
            currentExpr = currentOrExpr;
         }

         // find largest remaining 'subnet mask' not overlapping with lowerbound
         int andSpread = -1;
         long andMask = 0;
         int andStartPos;
         int andEndPos;
         andEndPos = numBitsLeft - 1;
         for (int i = andEndPos; i >= 0; i--) {
            andMask |= (1L << i);
            if ((lb & andMask) != andMask) {
               andMask ^= (1L << i);
               break;
            }
            else {
               andSpread++;
               numBitsLeft--;
            }
         }
         if (andSpread >= 0) {
            andStartPos = andEndPos - andSpread;
            LitIntExpr andMaskExpr = new LitIntExpr(andMask, andStartPos,
                  andEndPos);
            IntExpr extractExpr = newExtractExpr(bv, numBits, andStartPos,
                  andEndPos);
            EqExpr eqExpr = new EqExpr(extractExpr, andMaskExpr);

            AndExpr oldAndExpr = currentAndExpr;
            currentAndExpr = new AndExpr();
            currentAndExpr.addConjunct(eqExpr);
            if (currentExpr != null) {
               if (currentExpr == currentOrExpr) {
                  currentOrExpr.addDisjunct(currentAndExpr);
               }
               else if (currentExpr == oldAndExpr) {
                  oldAndExpr.addConjunct(currentAndExpr);
               }
            }
            else {
               finalExpr = currentAndExpr;
            }
            currentExpr = currentAndExpr;
         }
      }
      return finalExpr;
   }

   public static BooleanExpr bitvectorLEExpr(String bv, long lb, int numBits) {
      OrExpr leExpr = new OrExpr();
      LitIntExpr upperBound = new LitIntExpr(lb, numBits);
      VarIntExpr var = new VarIntExpr(bv);
      EqExpr exactMatch = new EqExpr(var, upperBound);
      BooleanExpr ge = bitvectorGEExpr(bv, lb, numBits);
      NotExpr lessThan = new NotExpr(ge);
      leExpr.addDisjunct(exactMatch);
      leExpr.addDisjunct(lessThan);
      return leExpr;
   }

   private static IntExpr newExtractExpr(String var, int varSize, int low,
         int high) {
      if (low == 0 && high == varSize - 1) {
         return new VarIntExpr(var);
      }
      else {
         return new ExtractExpr(var, low, high);
      }
   }

   private BooleanExpr _expr;

   public RangeMatchExpr(String var, int bits, Set<SubRange> range) {
      long max = (1l << bits) - 1;
      OrExpr or = new OrExpr();
      for (SubRange subRange : range) {
         long low = subRange.getStart();
         long high = subRange.getEnd();
         if (low == high) {
            VarIntExpr portVarExpr = new VarIntExpr(var);
            LitIntExpr portLitExpr = new LitIntExpr(low, bits);
            EqExpr exactMatch = new EqExpr(portVarExpr, portLitExpr);
            or.addDisjunct(exactMatch);
         }
         else {
            boolean doLE = (high < max);
            boolean doGE = (low > 0);
            AndExpr and = new AndExpr();
            if (doGE) {
               BooleanExpr geExpr = bitvectorGEExpr(var, low, bits);
               and.addConjunct(geExpr);
            }
            if (doLE) {
               BooleanExpr leExpr = bitvectorLEExpr(var, high, bits);
               and.addConjunct(leExpr);
            }
            if (!doGE && !doLE) {
               // any value in range matches
               _expr = TrueExpr.INSTANCE;
            }
            and.addConjunct(TrueExpr.INSTANCE);
            or.addDisjunct(and);
         }
      }
      or.addDisjunct(FalseExpr.INSTANCE);
      _expr = or;

   }

   @Override
   public void print(StringBuilder sb, int indent) {
      _expr.print(sb, indent);
   }

   @Override
   public BoolExpr toBoolExpr(NodProgram nodProgram) throws Z3Exception {
      return _expr.toBoolExpr(nodProgram);
   }

   @Override
   public String toString() {
      return _expr.toString();
   }

}
