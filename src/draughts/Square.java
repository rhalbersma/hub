
package draughts;

public class Square {

   public static final int File_Size = 10; // must be even
   public static final int Rank_Size = 10; // must be even
   private static final int Dark_Shift = 1;

   private static final int Ghost_Size = 1; // must be odd

   private static final int File_Even  = File_Size  / 2;
   private static final int Ghost_Even = Ghost_Size / 2 + (1 - Dark_Shift);

   public static final int Dense_Size  =        File_Size         * (Rank_Size / 2);
   public static final int Sparse_Size = (File_Size + Ghost_Size) * (Rank_Size / 2);

   private static final int I = (File_Size + Ghost_Size) / 2;
   private static final int J = (File_Size + Ghost_Size) / 2 + 1;

   static int[] inc = {
      -J, -I, +I, +J
   };

   static int[][] side_inc = {
      { -J, -I },
      { +I, +J },
   };

   public static boolean is_valid(int fl, int rk) {
      return (fl >= 0 && fl < File_Size)
          && (rk >= 0 && rk < Rank_Size)
          && is_dark(fl, rk);
   }

   static boolean is_valid(int sq) {

      if (sq < 0 || sq >= Sparse_Size) return false;

      int rest = sq % (File_Size + Ghost_Size);

      if (rest >= File_Even + Ghost_Even) { // odd rank
         rest -= File_Even + Ghost_Even;
      }

      return rest < File_Size / 2;
   }

   public static boolean is_light(int fl, int rk) {
      return !is_dark(fl, rk);
   }

   public static boolean is_dark(int fl, int rk) {
      return (fl + rk + Dark_Shift) % 2 == 0;
   }

   public static int make(int fl, int rk) {
      assert is_valid(fl, rk);
      int dense = (rk * File_Size + fl) / 2;
      return sparse(dense);
   }

   public static int sparse(int dense) {

      assert dense >= 0 && dense < Dense_Size;

      int ranks = dense / File_Size;
      int rest  = dense % File_Size;

      int sparse = dense + Ghost_Size * ranks;

      if (rest >= File_Even) { // odd rank
         sparse += Ghost_Even;
      }

      return sparse;
   }

   public static int dense(int sq) {

      assert is_valid(sq);

      int ranks = sq / (File_Size + Ghost_Size);
      int rest  = sq % (File_Size + Ghost_Size);

      int dense = sq - Ghost_Size * ranks;

      if (rest >= File_Even + Ghost_Even) { // odd rank
         dense -= Ghost_Even;
      }

      return dense;
   }

   static int rank(int sq) {

      assert is_valid(sq);

      int ranks = sq / (File_Size + Ghost_Size);
      int rest  = sq % (File_Size + Ghost_Size);

      int rk = ranks * 2;

      if (rest >= File_Even + Ghost_Even) { // odd rank
         rk++;
      }

      return rk;
   }

   static int rank(int sq, int sd) {

      int rk = rank(sq);

      if (sd == Side.White) {
         return (Rank_Size - 1) - rk;
      } else {
         return rk;
      }
   }

   public static int opp(int sq) {
      return sparse((Dense_Size - 1) - dense(sq));
   }

   static boolean is_promotion(int sq, int sd) {
      return rank(sq, sd) == Rank_Size - 1;
   }

   static int from_string(String s) throws Bad_Input {
      return from_std(Integer.parseInt(s));
   }

   static String to_string(int sq) {
      return Integer.toString(to_std(sq));
   }

   static int from_std(int std) throws Bad_Input {
      if (std < 1 || std > Dense_Size) throw new Bad_Input();
      return sparse(std - 1);
   }

   static int to_std(int sq) {
      return dense(sq) + 1;
   }
}

