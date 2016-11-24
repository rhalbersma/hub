
package draughts;

public class Side {

   public static final int White = 0;
   public static final int Black = 1;
   public static final int Size  = 2;

   public static int opp(int sd) {
      return sd ^ 1;
   }
}

