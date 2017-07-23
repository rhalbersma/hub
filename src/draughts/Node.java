
package draughts;

public class Node {

   private static final boolean Draw = false;
   private static final int Draw_Ply = 50;

   private final Pos p_pos;
   private final int p_ply;
   private final Node p_parent;

   public Node(Pos pos) {
      this(pos, 0, null);
   }

   private Node(Pos pos, int ply, Node parent) {
      p_pos = pos;
      p_ply = ply;
      p_parent = parent;
   }

   public Node succ(long mv) {

      Pos new_pos = p_pos.succ(mv);

      if (Move.is_conversion(mv, p_pos)) {
         return new Node(new_pos);
      } else {
         return new Node(new_pos, p_ply + 1, this);
      }
   }

   public Pos pos() {
      return p_pos;
   }

   public int ply() {
      return p_ply;
   }

   public boolean is_end() {
      return p_pos.is_end() || is_draw();
   }

   public boolean is_draw() {

      if (Draw && p_ply >= Draw_Ply) {
         return !p_pos.is_end();
      } else if (p_ply >= 4) {
         return is_rep();
      } else {
         return false;
      }
   }

   private boolean is_rep() {

      Node node = this;

      for (int i = 0; i < p_ply / 2; i++) {
         node = node.p_parent;
         node = node.p_parent;
         if (node.p_pos.equals(p_pos)) return true;
      }

      return false;
   }
}

