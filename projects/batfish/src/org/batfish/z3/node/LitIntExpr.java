package org.batfish.z3.node;

import org.batfish.representation.Ip;
import org.batfish.z3.NodProgram;

import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;

public class LitIntExpr extends IntExpr {

   private int _bits;
   private long _num;
   private String _numString;

   public LitIntExpr(Ip ip) {
      _num = ip.asLong();
      _bits = 32;
      init();
   }

   public LitIntExpr(long num, int bits) {
      _num = num;
      _bits = bits;
      init();
   }

   public LitIntExpr(long num, int low, int high) {
      _bits = high - low + 1;
      _num = num >> low;
      init();
   }

   @Override
   public boolean equals(Object o) {
      if (o instanceof LitIntExpr) {
         LitIntExpr rhs = (LitIntExpr) o;
         return _bits == rhs._bits && _num == rhs._num;
      }
      return false;
   }

   @Override
   public int hashCode() {
      return _numString.hashCode();
   }

   private void init() {
      if (_bits % 4 == 0) {
         // hex
         int numNibbles = _bits / 4;
         _numString = "#x" + String.format("%0" + numNibbles + "x", _num);
      }
      else {
         // bin
         _numString = "#b";
         for (int pos = _bits - 1; pos >= 0; pos--) {
            long mask = 1L << pos;
            long bit = _num & mask;
            _numString += (bit != 0) ? 1 : 0;
         }
      }
      _printer = new SimpleExprPrinter(_numString);
   }

   @Override
   public BitVecExpr toBitVecExpr(NodProgram nodProgram) throws Z3Exception {
      Context ctx = nodProgram.getContext();
      BitVecExpr result = ctx.mkBV(_num, _bits);
      return result;
   }

}
