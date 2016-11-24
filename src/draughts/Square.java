
package draughts;

public class Square {

   static final int Size = 54;

   private static int[] p_from_50 = {
       0,  1,  2,  3,  4,
       5,  6,  7,  8,  9,
      11, 12, 13, 14, 15,
      16, 17, 18, 19, 20,
      22, 23, 24, 25, 26,
      27, 28, 29, 30, 31,
      33, 34, 35, 36, 37,
      38, 39, 40, 41, 42,
      44, 45, 46, 47, 48,
      49, 50, 51, 52, 53,
   };

   private static int[] p_to_50 = {
       0,  1,  2,  3,  4,
       5,  6,  7,  8,  9, -1,
      10, 11, 12, 13, 14,
      15, 16, 17, 18, 19, -1,
      20, 21, 22, 23, 24,
      25, 26, 27, 28, 29, -1,
      30, 31, 32, 33, 34,
      35, 36, 37, 38, 39, -1,
      40, 41, 42, 43, 44,
      45, 46, 47, 48, 49,
   };

   static int[] inc = {
      -6, -5, +5, +6
   };

   static int[][] side_inc = {
      { -6, -5 },
      { +5, +6 },
   };

   static boolean is_valid(int sq) {
      return sq >= 0 && sq < Size && p_to_50[sq] >= 0;
   }

   public static int from_50(int i) {
      return p_from_50[i];
   }

   public static int to_50(int sq) {
      assert is_valid(sq);
      return p_to_50[sq];
   }

   static boolean is_promotion(int sq, int sd) {

      assert is_valid(sq);

      if (sd == Side.White) {
         return sq <= 4;
      } else {
         return sq >= 49;
      }
   }

   public static String to_string(int sq) {
      return Integer.toString(to_50(sq) + 1);
   }

   public static int from_string(String s) throws Bad_Input {
      int sq = Integer.parseInt(s);
      if (sq < 1 || sq > 50) throw new Bad_Input();
      return from_50(sq - 1);
   }
}

