
package draughts;

import hub.*;

public class Square {

   public  static final int File_Size   = 10;
   public  static final int Rank_Size   = 10;
   private static final int Corner_Dark = 0; // 0: light, 1: dark

   private static final int Ghost_Size = 3; // additional internal files

   private static final int File_Both = File_Size + Ghost_Size;
   private static final int Dark_Even = File_Both / 2 + Corner_Dark;
   private static final int Ghost_Odd = (Ghost_Size + (1 - Corner_Dark)) / 2;

   static final int Dense_Size  = (File_Size * Rank_Size + Corner_Dark) / 2;
   static final int Sparse_Size = (File_Both * Rank_Size + Corner_Dark) / 2 - Ghost_Odd;

   private static final int I = File_Both / 2;
   private static final int J = File_Both / 2 + 1;
   private static final int K = 1;
   private static final int L = File_Both;

   static int dir_size;

   static int[] dir_inc = {
      +I, +J, -I, -J, // diagonals for all draughts variants
      +L, +K, -L, -K, // orthogonal directions for Frisian draughts (optional)
   };

   static int[][] side_dir_inc = {
      { -I, -J },
      { +I, +J },
   };

   static void init() {

      assert File_Size % 2 == 0;
      assert Rank_Size % 2 == 0;
      assert Ghost_Size >= 2; // for Frisian draughts
      assert Ghost_Size % 2 != 0;

      assert Sparse_Size <= 64;

      dir_size = (Hub.vars.get("game-variant").equals("frisian")) ? 8 : 4;
   }

   public static boolean is_valid(int fl, int rk) {
      return (fl >= 0 && fl < File_Size)
          && (rk >= 0 && rk < Rank_Size)
          && is_dark(fl, rk);
   }

   public static boolean is_light(int fl, int rk) {
      return !is_dark(fl, rk);
   }

   public static boolean is_dark(int fl, int rk) {
      return (fl + rk) % 2 != Corner_Dark;
   }

   public static int make(int fl, int rk) {

      assert is_valid(fl, rk);

      int sq = (rk / 2) * File_Both;
      if (rk % 2 != 0) sq += Dark_Even; // odd rank
      sq += fl / 2;

      assert(sq < Sparse_Size);
      return sq;
   }

   static boolean is_valid(int sq) {
      return sq >= 0
          && file(sq) < File_Size
          && rank(sq) < Rank_Size;
   }

   static int file(int sq) {

      assert sq >= 0;

      int files = sq % File_Both;
      // int ranks = sq / File_Both;

      if (files >= Dark_Even) { // odd rank
         return (files - Dark_Even) * 2 + Corner_Dark;
      } else {
         return files * 2 + (1 - Corner_Dark);
      }
   }

   static int rank(int sq) {

      assert sq >= 0;

      int files = sq % File_Both;
      int ranks = sq / File_Both;

      int rk = ranks * 2;
      if (files >= Dark_Even) rk += 1; // odd rank

      return rk;
   }

   static int rank(int sq, int sd) {
      int rk = rank(sq);
      return (sd != Side.Black) ? (Rank_Size - 1) - rk : rk;
   }

   public static int opp(int sq) {
      assert is_valid(sq);
      return (Sparse_Size - 1) - sq;
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

      int fl = ((std - 1) * 2) % File_Size;
      int rk = ((std - 1) * 2) / File_Size;
      if (!is_dark(fl, rk)) fl += 1;

      return make(fl, rk);
   }

   static int to_std(int sq) {
      return (rank(sq) * File_Size + file(sq)) / 2 + 1;
   }
}

