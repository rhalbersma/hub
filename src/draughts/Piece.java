
package draughts;

public class Piece {

   public static final int WM = 0;
   public static final int BM = 1;
   public static final int WK = 2;
   public static final int BK = 3;
   public static final int Empty = 4;

   public static boolean is_man(int pc) {
      return pc == WM || pc == BM;
   }

   public static boolean is_king(int pc) {
      return pc == WK || pc == BK;
   }
}

