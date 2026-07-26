

import java.util.Scanner;

// ===================== PIECE (Abstraction + Encapsulation) =====================
// abstract class -> we can never make "new Piece()", only its children (King, Pawn, etc.)
abstract class Piece {
    private boolean isWhite;   // encapsulation: private field
    private boolean hasMoved;

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
        this.hasMoved = false;
    }

    public boolean isWhite() { return isWhite; }
    public boolean hasMoved() { return hasMoved; }
    public void setMoved() { this.hasMoved = true; }

    // Polymorphism: every piece will define canMove in its OWN way
    public abstract boolean canMove(Board board, Spot start, Spot end);

    // Polymorphism: every piece prints a different letter
    public abstract char getSymbol();
}

// ===================== KING =====================
class King extends Piece {
    public King(boolean isWhite) { super(isWhite); }

    @Override
    public boolean canMove(Board board, Spot start, Spot end) {
        int dx = Math.abs(start.getX() - end.getX());
        int dy = Math.abs(start.getY() - end.getY());
        if (dx > 1 || dy > 1) return false;                 // king moves only 1 step
        return Board.destinationOk(this, end);
    }

    @Override
    public char getSymbol() { return isWhite() ? 'K' : 'k'; }
}

// ===================== QUEEN =====================
class Queen extends Piece {
    public Queen(boolean isWhite) { super(isWhite); }

    @Override
    public boolean canMove(Board board, Spot start, Spot end) {
        boolean straight = (start.getX() == end.getX() || start.getY() == end.getY());
        boolean diagonal = Math.abs(start.getX() - end.getX()) == Math.abs(start.getY() - end.getY());
        if (!straight && !diagonal) return false;
        if (!board.isPathClear(start, end)) return false;
        return Board.destinationOk(this, end);
    }

    @Override
    public char getSymbol() { return isWhite() ? 'Q' : 'q'; }
}

// ===================== ROOK =====================
class Rook extends Piece {
    public Rook(boolean isWhite) { super(isWhite); }

    @Override
    public boolean canMove(Board board, Spot start, Spot end) {
        if (start.getX() != end.getX() && start.getY() != end.getY()) return false; // must be straight line
        if (!board.isPathClear(start, end)) return false;
        return Board.destinationOk(this, end);
    }

    @Override
    public char getSymbol() { return isWhite() ? 'R' : 'r'; }
}

// ===================== BISHOP =====================
class Bishop extends Piece {
    public Bishop(boolean isWhite) { super(isWhite); }

    @Override
    public boolean canMove(Board board, Spot start, Spot end) {
        if (Math.abs(start.getX() - end.getX()) != Math.abs(start.getY() - end.getY())) return false; // must be diagonal
        if (!board.isPathClear(start, end)) return false;
        return Board.destinationOk(this, end);
    }

    @Override
    public char getSymbol() { return isWhite() ? 'B' : 'b'; }
}

// ===================== KNIGHT =====================
class Knight extends Piece {
    public Knight(boolean isWhite) { super(isWhite); }

    @Override
    public boolean canMove(Board board, Spot start, Spot end) {
        int dx = Math.abs(start.getX() - end.getX());
        int dy = Math.abs(start.getY() - end.getY());
        boolean lShape = (dx == 1 && dy == 2) || (dx == 2 && dy == 1);   // knight moves in "L" shape
        if (!lShape) return false;
        return Board.destinationOk(this, end);
    }

    @Override
    public char getSymbol() { return isWhite() ? 'N' : 'n'; }
}

// ===================== PAWN =====================
class Pawn extends Piece {
    public Pawn(boolean isWhite) { super(isWhite); }

    @Override
    public boolean canMove(Board board, Spot start, Spot end) {
        int direction = isWhite() ? 1 : -1;     // white moves up (+row), black moves down (-row)
        int startRow = isWhite() ? 1 : 6;
        int dx = end.getX() - start.getX();     // column change
        int dy = end.getY() - start.getY();     // row change

        Piece target = end.getPiece();

        // straight move (no capture allowed straight)
        if (dx == 0 && target == null) {
            if (dy == direction) return true;
            if (dy == 2 * direction && start.getY() == startRow && board.isPathClear(start, end)) return true;
            return false;
        }
        // diagonal capture only
        if (Math.abs(dx) == 1 && dy == direction) {
            return target != null && target.isWhite() != this.isWhite();
        }
        return false;
    }

    @Override
    public char getSymbol() { return isWhite() ? 'P' : 'p'; }
}

// ===================== SPOT (a single square) =====================
class Spot {
    private final int x; // column 0-7 (a-h)
    private final int y; // row 0-7    (rank1-rank8)
    private Piece piece; // null = empty

    public Spot(int x, int y, Piece piece) {
        this.x = x; this.y = y; this.piece = piece;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public Piece getPiece() { return piece; }
    public void setPiece(Piece piece) { this.piece = piece; }
    public boolean isEmpty() { return piece == null; }
}

// ===================== BOARD =====================
class Board {
    private Spot[][] spots; // spots[row][col]  row0=rank1 ... row7=rank8

    public Board() {
        spots = new Spot[8][8];
        for (int y = 0; y < 8; y++)
            for (int x = 0; x < 8; x++)
                spots[y][x] = new Spot(x, y, null);
        setup();
    }

    private void setup() {
        // back rank order: Rook Knight Bishop Queen King Bishop Knight Rook
        Class<?>[] order = { Rook.class, Knight.class, Bishop.class, Queen.class,
                              King.class, Bishop.class, Knight.class, Rook.class };
        for (int x = 0; x < 8; x++) {
            place(x, 0, makePiece(order[x], true));   // white back rank
            place(x, 1, new Pawn(true));              // white pawns
            place(x, 6, new Pawn(false));              // black pawns
            place(x, 7, makePiece(order[x], false));  // black back rank
        }
    }

    private Piece makePiece(Class<?> type, boolean white) {
        if (type == Rook.class) return new Rook(white);
        if (type == Knight.class) return new Knight(white);
        if (type == Bishop.class) return new Bishop(white);
        if (type == Queen.class) return new Queen(white);
        return new King(white);
    }

    private void place(int x, int y, Piece p) { spots[y][x].setPiece(p); }

    public Spot getSpot(int x, int y) { return spots[y][x]; }

    // helper used by all sliding pieces (rook/bishop/queen) to check the road is empty
    public boolean isPathClear(Spot start, Spot end) {
        int dx = Integer.signum(end.getX() - start.getX());
        int dy = Integer.signum(end.getY() - start.getY());
        int x = start.getX() + dx, y = start.getY() + dy;
        while (x != end.getX() || y != end.getY()) {
            if (!spots[y][x].isEmpty()) return false;
            x += dx; y += dy;
        }
        return true;
    }

    // destination must be empty OR hold an enemy piece
    public static boolean destinationOk(Piece mover, Spot end) {
        return end.isEmpty() || end.getPiece().isWhite() != mover.isWhite();
    }

    // find king spot of given color
    public Spot findKing(boolean white) {
        for (int y = 0; y < 8; y++)
            for (int x = 0; x < 8; x++) {
                Piece p = spots[y][x].getPiece();
                if (p instanceof King && p.isWhite() == white) return spots[y][x];
            }
        return null;
    }

    // is the king of this color currently under attack?
    public boolean isInCheck(boolean white) {
        Spot kingSpot = findKing(white);
        for (int y = 0; y < 8; y++)
            for (int x = 0; x < 8; x++) {
                Piece p = spots[y][x].getPiece();
                if (p != null && p.isWhite() != white) {
                    if (p.canMove(this, spots[y][x], kingSpot)) return true;
                }
            }
        return false;
    }

    // try every legal-looking move for a color; if none escapes check -> checkmate
    public boolean isCheckmate(boolean white) {
        if (!isInCheck(white)) return false;
        for (int y1 = 0; y1 < 8; y1++)
            for (int x1 = 0; x1 < 8; x1++) {
                Piece p = spots[y1][x1].getPiece();
                if (p == null || p.isWhite() != white) continue;
                for (int y2 = 0; y2 < 8; y2++)
                    for (int x2 = 0; x2 < 8; x2++) {
                        Spot start = spots[y1][x1], end = spots[y2][x2];
                        if (start == end || !p.canMove(this, start, end)) continue;
                        Piece captured = end.getPiece();
                        end.setPiece(p); start.setPiece(null);          // try the move
                        boolean stillInCheck = isInCheck(white);
                        start.setPiece(p); end.setPiece(captured);      // undo
                        if (!stillInCheck) return false;                // found an escape
                    }
            }
        return true;
    }

    public void print() {
        for (int y = 7; y >= 0; y--) {
            System.out.print((y + 1) + " ");
            for (int x = 0; x < 8; x++) {
                Piece p = spots[y][x].getPiece();
                System.out.print((p == null ? "." : p.getSymbol()) + " ");
            }
            System.out.println();
        }
        System.out.println("  a b c d e f g h");
    }
}

// ===================== PLAYER =====================
class Player {
    private final boolean isWhite;
    public Player(boolean isWhite) { this.isWhite = isWhite; }
    public boolean isWhite() { return isWhite; }
    public String getName() { return isWhite ? "White" : "Black"; }
}

// ===================== MOVE =====================
class Move {
    private final Spot start, end;
    public Move(Spot start, Spot end) { this.start = start; this.end = end; }
    public Spot getStart() { return start; }
    public Spot getEnd() { return end; }
}

// ===================== GAME =====================
class Game {
    private Board board;
    private Player white, black;
    private boolean whiteTurn = true;
    private Scanner sc = new Scanner(System.in);

    public Game() {
        board = new Board();
        white = new Player(true);
        black = new Player(false);
    }

    private Spot parse(String pos) {
        int col = pos.charAt(0) - 'a';
        int row = pos.charAt(1) - '1';
        if (col < 0 || col > 7 || row < 0 || row > 7) return null;
        return board.getSpot(col, row);
    }

    public void play() {
        System.out.println("=== Chess Game (Console Version) ===");
        System.out.println("Initial Board Setup:");
        board.print();

        while (true) {
            Player current = whiteTurn ? white : black;
            System.out.println(current.getName() + "'s turn.");
            System.out.print("Enter move (e.g., e2 e4), or 'quit': ");
            if (!sc.hasNextLine()) { System.out.println("No more input. Ending game."); return; }
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("quit")) { System.out.println("Game ended."); return; }

            String[] parts = line.split("\\s+");
            if (parts.length != 2) { System.out.println("Error: type like 'e2 e4'."); continue; }

            Spot start = parse(parts[0]);
            Spot end = parse(parts[1]);
            if (start == null || end == null) { System.out.println("Error: bad square name."); continue; }

            Piece piece = start.getPiece();
            if (piece == null) { System.out.println("Error: no piece on " + parts[0]); continue; }
            if (piece.isWhite() != current.isWhite()) { System.out.println("Error: that's not your piece."); continue; }
            if (!piece.canMove(board, start, end)) { System.out.println("Error: illegal move for this piece."); continue; }

            // try move, reject if it leaves own king in check
            Piece captured = end.getPiece();
            end.setPiece(piece); start.setPiece(null);
            if (board.isInCheck(current.isWhite())) {
                start.setPiece(piece); end.setPiece(captured);
                System.out.println("Error: move leaves your King in check.");
                continue;
            }
            piece.setMoved();

            System.out.println(piece.getClass().getSimpleName() + " moved from " + parts[0] + " to " + parts[1] + ".");
            board.print();

            boolean opponentWhite = !current.isWhite();
            if (board.isInCheck(opponentWhite)) {
                if (board.isCheckmate(opponentWhite)) {
                    System.out.println("Checkmate! " + current.getName() + " wins the game.");
                    return;
                }
                System.out.println("Check! " + (opponentWhite ? "White" : "Black") + "'s King is in check.");
            }

            whiteTurn = !whiteTurn;
        }
    }
}

// ===================== MAIN =====================
public class Main {
    public static void main(String[] args) {
        new Game().play();
    }
}
