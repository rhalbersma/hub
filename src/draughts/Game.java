
package draughts;

import java.io.*;

import hub.*;

public class Game {

   private static final boolean Draw = false;
   private static final int Draw_Ply = 50;

   private static final int Size = 1024;

   private Pos p_start_pos;

   private int p_moves;
   private int p_time;
   private int p_inc;

   private long[] p_move;
   private int[] p_move_time;
   private int p_size;
   private int p_i;

   private Pos p_pos;
   private int p_ply;

   private int[] p_clock;
   private boolean[] p_flag;

   public Game() {
      init(new Pos());
   }

   public Game(String fen) {
      try {
         init(FEN.from_fen(fen));
      } catch (Bad_Input e) {
         e.printStackTrace();
      }
   }

   private void init(Pos pos) {

      p_start_pos = pos;

      p_moves = 0;
      p_time = 0;
      p_inc = 0;

      p_move = new long[Size];
      p_move_time = new int[Size];

      p_clock = new int[Side.Size];
      p_flag = new boolean[Side.Size];

      p_size = 0;
      reset();
   }

   public void set_time_control(int moves, int time, int inc) {

      p_moves = moves;
      p_time = time;
      p_inc = inc;

      go_to(p_i);
   }

   private void reset() {

      p_i = 0;

      p_pos = p_start_pos;
      p_ply = 0;

      p_clock[Side.White] = p_time;
      p_clock[Side.Black] = p_time;
      p_flag[Side.White] = false;
      p_flag[Side.Black] = false;
   }

   public void add_move(long mv) {
      add_move(mv, 0);
   }

   public void add_move(long mv, int time) {

      p_size = p_i; // truncate move list

      if (!Move.is_legal(mv, p_pos)) {
         p_pos.disp();
         System.out.println("ILLEGAL MOVE: " + Move.to_hub(mv) + "###");
         System.out.println();
         return;
      }

      p_move[p_size] = mv;
      p_move_time[p_size] = time;
      p_size++;

      play_move();
   }

   private void play_move() {

      assert p_i < p_size;
      long mv = p_move[p_i];
      int time = p_move_time[p_i];
      p_i++;

      if (Move.is_conversion(mv, p_pos)) {
         p_ply = 0;
      } else {
         p_ply++;
      }

      int turn = p_pos.turn();

      p_clock[turn] += p_inc; // pre-increment #
      p_clock[turn] -= time;

      if (p_clock[turn] < 0 && !p_flag[turn]) {
         Hub.log("LOSS ON TIME ###");
         p_flag[turn] = true;
      }

      if (p_moves != 0 && p_i % (p_moves * 2) == 0) {
         p_clock[Side.White] += p_time;
         p_clock[Side.Black] += p_time;
      }

      p_pos = new Pos(p_pos, mv);
   }

   public void go_to(int n) {

      assert n >= 0 && n <= p_size;

      reset();

      for (int i = 0; i < n; i++) {
         play_move();
      }

      assert p_i == n;
   }

   public boolean is_end() { // TODO: repetition
      return p_pos.is_end() || (Draw && p_ply >= Draw_Ply);
   }

   public int ply() {
      return p_ply;
   }

   public int size() {
      return p_size;
   }

   public int i() {
      return p_i;
   }

   public long move(int i) {
      assert i < p_size;
      return p_move[i];
   }

   public Pos start_pos() {
      return p_start_pos;
   }

   public Pos pos() {
      return p_pos;
   }

   public Pos pos(int n) {

      assert n >= 0 && n <= p_size;

      Pos pos = p_start_pos;

      for (int i = 0; i < n; i++) {
         long mv = p_move[i];
         pos = new Pos(pos, mv);
      }

      return pos;
   }

   public int moves(int sd) {
      assert sd == p_pos.turn();
      return (p_moves == 0) ? 0 : p_moves - (p_i / 2) % p_moves;
   }

   public int time(int sd) {
      return p_clock[sd];
   }

   public int inc() {
      return p_inc;
   }
}

