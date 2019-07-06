
package draughts;

import java.io.*;

public class Bit {

   static long Squares;

   private static long[] p_king_moves;
   private static long[] p_king_captures;

   private static long[][] p_between;
   private static byte[][] p_inc;

   static void init() {

      // all squares

      Squares = 0;

      for (int rk = 0; rk < Square.Rank_Size; rk++) {
         for (int fl = 0; fl < Square.File_Size; fl++) {
            if (Square.is_dark(fl, rk)) {
               int sq = Square.make(fl, rk);
               Squares = set(Squares, sq);
            }
         }
      }

      assert(count(Squares) == Square.Dense_Size);

      // bitboard tables

      p_king_moves    = new long[Square.Sparse_Size];
      p_king_captures = new long[Square.Sparse_Size];

      p_between = new long[Square.Sparse_Size][Square.Sparse_Size];
      p_inc     = new byte[Square.Sparse_Size][Square.Sparse_Size];

      for (long froms = Squares; froms != 0; froms = rest(froms)) {

         int from = first(froms);

         for (int dir = 0; dir < Square.dir_size; dir++) {

            int inc = Square.dir_inc[dir];

            long ray = 0;

            for (int to = from + inc; Square.is_valid(to); to += inc) {

               p_between[from][to] = ray; // partial ray
               p_inc[from][to] = (byte) inc;

               ray = set(ray, to);
            }

            if (dir < 4) p_king_moves[from] |= ray; // only diagonals, even in Frisian draughts
            p_king_captures[from] |= ray; // captures are different in Frisian draughts
         }
      }
   }

   static long king_moves(int sq) {
      assert Square.is_valid(sq);
      return p_king_moves[sq];
   }

   static long king_captures(int sq) {
      assert Square.is_valid(sq);
      return p_king_captures[sq];
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
      assert !has(b, n);
      return b | bit(n);
   }

   public static long clear(long b, int n) {
      assert has(b, n);
      return b & ~bit(n);
   }

   public static long flip(long b, int n) {
      return b ^ bit(n);
   }

   static long shift(long b, int n) {
      return (n < 0) ? b >>> -n : b << +n;
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

               if (has(b, sq)) {
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

