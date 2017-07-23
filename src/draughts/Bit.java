
package draughts;

import java.io.*;

public class Bit {

   static long Squares;

   private static long[] p_king_moves;

   private static long[][] p_between;
   private static byte[][] p_inc;

   static void init() {

      // all squares

      Squares = 0;

      for (int dense = 0; dense < Square.Dense_Size; dense++) {
         int sq = Square.sparse(dense);
         Squares = Bit.set(Squares, sq);
      }

      // king moves

      p_king_moves = new long[Square.Sparse_Size];

      for (int dense = 0; dense < Square.Dense_Size; dense++) {

         int from = Square.sparse(dense);
         long b = 0;

         for (int dir = 0; dir < 4; dir++) {

            int inc = Square.inc[dir];

            int to = from + inc;

            while (Square.is_valid(to)) {
               b = Bit.set(b, to);
               to += inc;
            }
         }

         p_king_moves[from] = b;
      }

      // between & inc // TODO: merge with p_king_moves? #

      p_between = new long[Square.Sparse_Size][Square.Sparse_Size];
      p_inc     = new byte[Square.Sparse_Size][Square.Sparse_Size];

      for (int dense = 0; dense < Square.Dense_Size; dense++) {

         int from = Square.sparse(dense);

         for (int dir = 0; dir < 4; dir++) {

            int inc = Square.inc[dir];
            long b = 0;

            int to = from + inc;

            while (Square.is_valid(to)) {

               p_between[from][to] = b;
               p_inc[from][to] = (byte) inc;

               b = Bit.set(b, to);
               to += inc;
            }
         }
      }
   }

   static long king_moves(int sq) {
      assert Square.is_valid(sq);
      return p_king_moves[sq];
   }

   static long between(int from, int to) {
      assert Square.is_valid(from);
      assert Square.is_valid(to);
      return p_between[from][to];
   }

   static int inc(int from, int to) {

      assert Square.is_valid(from);
      assert Square.is_valid(to);

      int inc = p_inc[from][to];
      assert inc != 0;

      return inc;
   }

   static boolean is_valid(long b) {
      return is_incl(b, Squares);
   }

   public static long bit(int n) {
      assert Square.is_valid(n); // HACK?
      return 1L << n;
   }

   public static boolean has(long b, int n) {
      return (b & bit(n)) != 0;
   }

   public static long set(long b, int n) {
      assert !Bit.has(b, n);
      return b | bit(n);
   }

   public static long clear(long b, int n) {
      assert Bit.has(b, n);
      return b & ~bit(n);
   }

   public static long flip(long b, int n) {
      return b ^ bit(n);
   }

   static long shift(long b, int n) {
      if (n < 0) {
         return b >> -n;
      } else {
         return b << +n;
      }
   }

   static boolean is_incl(long b0, long b1) {
      return (b0 & ~b1) == 0;
   }

   static boolean is_disjoint(long b0, long b1) { // not used
      return (b0 & b1) == 0;
   }

   static boolean has_common(long b0, long b1) {
      return (b0 & b1) != 0;
   }

   static int first(long b) {
      assert b != 0;
      return Long.numberOfTrailingZeros(b);
   }

   static long rest(long b) {
      assert b != 0;
      return b & (b - 1);
   }

   static int count(long b) {
      return Long.bitCount(b);
   }

   static void disp(long b) {

      for (int rk = 0; rk < Square.Rank_Size; rk++) {

         for (int fl = 0; fl < Square.File_Size; fl++) {

            if (Square.is_light(fl, rk)) {

               System.out.print("  ");

            } else {

               int sq = Square.make(fl, rk);

               if (Bit.has(b, sq)) {
                  System.out.print("# ");
               } else {
                  System.out.print("- ");
               }
            }
         }

         System.out.println();
      }

      System.out.println();

/*
      System.out.println("0x" + Long.toHexString(b) + "L");
      System.out.println();
*/
   }
}

