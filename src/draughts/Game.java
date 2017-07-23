
package draughts;

import java.io.*;

import hub.*;

public class Game implements Cloneable {

   private static final int Size = 1024;

   private final Pos p_start_pos;

   private int p_moves;
   private double p_time;
   private double p_inc;

   private Move_Time[] p_move_time;
   private int p_size;
   private int p_i;

   private Node p_node;

   private double[] p_clock;
   private boolean[] p_flag;

   public Game() {
      this(Pos.Start);
   }

   public Game(Pos pos) {

      p_start_pos = pos;

      p_moves = 0;
      p_time = 0.0;
      p_inc = 0.0;

      p_move_time = new Move_Time[Size];

      p_clock = new double[Side.Size];
      p_flag = new boolean[Side.Size];

      p_size = 0;
      reset();
   }

   private Game(Game game) { // for clone()

      p_start_pos = game.p_start_pos;

      p_moves = game.p_moves;
      p_time  = game.p_time;
      p_inc   = game.p_inc;

      p_move_time = new Move_Time[Size];

      for (int i = 0; i < game.p_size; i++) {
         p_move_time[i] = game.p_move_time[i];
      }

      p_size = game.p_size;
      p_i    = game.p_i;

      p_node = game.p_node;

      p_clock = game.p_clock.clone();
      p_flag  = game.p_flag.clone();
   }

   public synchronized Game clone() {
      return new Game(this);
   }

   public synchronized void set_time_control(int moves, double time, double inc) {

      p_moves = moves;
      p_time = time;
      p_inc = inc;

      go_to(p_i);
   }

   private void reset() {

      p_i = 0;

      p_node = new Node(p_start_pos);

      p_clock[Side.White] = p_time;
      p_clock[Side.Black] = p_time;
      p_flag[Side.White] = false;
      p_flag[Side.Black] = false;
   }

   public synchronized void add_move(long mv) {
      add_move(mv, 0.0);
   }

   public synchronized void add_move(long mv, double time) {

      if (!Move.is_legal(mv, pos())) {
         pos().disp();
         Bit.disp(mv);
         // System.out.println("ILLEGAL MOVE: " + Move.to_hub(mv, pos()) + " ###");
         System.out.println("ILLEGAL MOVE ###");
         System.out.println();
         System.exit(1);
      }

      p_size = p_i; // truncate
      p_move_time[p_size++] = Move_Time.make(mv, time);

      play_move();
   }

   private void play_move() {

      assert p_i < p_size;
      Move_Time move_time = p_move_time[p_i++];

      long   mv   = move_time.move();
      double time = move_time.time();

      int turn = pos().turn();

      p_clock[turn] += p_inc; // pre-increment #
      p_clock[turn] -= time;

      if (p_clock[turn] < 0.0 && !p_flag[turn]) {
         Hub.log("LOSS ON TIME ###");
         p_flag[turn] = true;
      }

      if (p_moves != 0 && p_i % (p_moves * 2) == 0) {
         p_clock[Side.White] += p_time;
         p_clock[Side.Black] += p_time;
      }

      p_node = p_node.succ(mv);
   }

   public synchronized void go_to(int n) {

      assert n >= 0 && n <= p_size;

      reset();

      for (int i = 0; i < n; i++) {
         play_move();
      }

      assert p_i == n;
   }

   public int turn() {
      return pos().turn();
   }

   public boolean is_end() {
      return p_node.is_end();
   }

   public int ply() {
      return p_node.ply();
   }

   public int size() {
      return p_size;
   }

   public int i() {
      return p_i;
   }

   public long move(int i) {
      assert i < p_size;
      return p_move_time[i].move();
   }

   public Pos start_pos() {
      return p_start_pos;
   }

   public Pos pos() {
      return p_node.pos();
   }

   public Pos pos(int n) {

      assert n >= 0 && n <= p_size;

      Pos pos = p_start_pos;

      for (int i = 0; i < n; i++) {
         long mv = move(i);
         pos = pos.succ(mv);
      }

      return pos;
   }

   public Node node() {
      return p_node;
   }

   public long last_move() {
      return (p_i == 0) ? Move.None : move(p_i - 1);
   }

   public void set_time(int sd, double time) {
      p_clock[sd] = time;
   }

   public int moves(int sd) {
      assert sd == pos().turn();
      return (p_moves == 0) ? 0 : p_moves - (p_i / 2) % p_moves;
   }

   public double time(int sd) {
      return p_clock[sd];
   }

   public double inc() {
      return p_inc;
   }
}

class Move_Time {

   private final long p_move;
   private final double p_time;

   public static Move_Time make(long move, double time) {
      return new Move_Time(move, time);
   }

   public Move_Time(long move, double time) {
      p_move = move;
      p_time = time;
   }

   public long move() {
      return p_move;
   }

   public double time() {
      return p_time;
   }
}

