package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> validMoves = new ArrayList<>();

        switch (type) {
            case KING:
                // King moves one square in any direction
                for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
                    for (int colOffset = -1; colOffset <= 1; colOffset++) {
                        ChessPosition newPos = new ChessPosition(myPosition.getRow() + rowOffset, myPosition.getColumn() + colOffset);
                        if (isValidMove(board, newPos)) validMoves.add(new ChessMove(myPosition, newPos, null));
                    }
                }
                break;

            case QUEEN:
                // Queen moves like a rook or bishop (diagonal, horizontal, or vertical)
                validMoves.addAll(getDiagonalMoves(board, myPosition));
                validMoves.addAll(getStraightMoves(board, myPosition));
                break;

            case ROOK:
                // Rook moves horizontally or vertically
                validMoves.addAll(getStraightMoves(board, myPosition));
                break;

            case BISHOP:
                // Bishop moves diagonally
                validMoves.addAll(getDiagonalMoves(board, myPosition));
                break;

            case KNIGHT:
                // Knight moves in an "L" shape (2 squares in one direction, 1 square in another)
                for (int rowOffset : new int[]{-2, -1, 1, 2}) {
                    for (int colOffset : new int[]{-2, -1, 1, 2}) {
                        if (Math.abs(rowOffset) != Math.abs(colOffset)) {
                            ChessPosition newPos = new ChessPosition(myPosition.getRow() + rowOffset, myPosition.getColumn() + colOffset);
                            if (isValidMove(board, newPos)) validMoves.add(new ChessMove(myPosition, newPos, null));
                        }
                    }
                }
                break;

            case PAWN:
                // Pawn moves forward 1 square, or 2 squares from its starting position
                // Pawns can capture diagonally
                int direction = pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1; // White moves up, Black moves down
                ChessPosition oneStep = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
                ChessPosition twoSteps = new ChessPosition(myPosition.getRow() + 2 * direction, myPosition.getColumn());

                if (isValidMove(board, oneStep)) validMoves.add(new ChessMove(myPosition, oneStep, null));
                if (myPosition.getRow() == (pieceColor == ChessGame.TeamColor.WHITE ? 2 : 7) && isValidMove(board, twoSteps))
                    validMoves.add(new ChessMove(myPosition, twoSteps, null));

                // Pawn captures diagonally
                for (int colOffset : new int[]{-1, 1}) {
                    ChessPosition capturePos = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() + colOffset);
                    if (isValidCapture(board, capturePos)) validMoves.add(new ChessMove(myPosition, capturePos, null));
                }
                break;
        }
        return validMoves;
    }


    private Collection<ChessMove> getStraightMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        for (int rowOffset : new int[]{-1, 1}) {
            for (int colOffset : new int[]{0}) {  // Straight moves (left-right, up-down)
                for (int i = 1; i <= 7; i++) {
                    ChessPosition newPos = new ChessPosition(myPosition.getRow() + i * rowOffset, myPosition.getColumn() + i * colOffset);

                    // Check for out-of-bounds
                    if (newPos.getRow() < 0 || newPos.getRow() >= 8 || newPos.getColumn() < 0 || newPos.getColumn() >= 8) {
                        break;  // Break if out of bounds
                    }

                    if (isValidMove(board, newPos)) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                        if (board.getPiece(newPos) != null) break; // Stop if blocked by another piece
                    }
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> getDiagonalMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        // Diagonal directions
        for (int rowOffset : new int[]{-1, 1}) {
            for (int colOffset : new int[]{-1, 1}) {
                for (int i = 1; i <= 7; i++) {
                    int newRow = myPosition.getRow() + i * rowOffset;
                    int newCol = myPosition.getColumn() + i * colOffset;

                    // Check for out-of-bounds
                    if (newRow < 0 || newRow >= 8 || newCol < 0 || newCol >= 8) break;

                    ChessPosition newPos = new ChessPosition(newRow, newCol);
                    ChessPiece targetPiece = board.getPiece(newPos);

                    if (targetPiece != null) {
                        if (targetPiece.getTeamColor() != this.pieceColor) {
                            moves.add(new ChessMove(myPosition, newPos, targetPiece.getPieceType())); // Capture move
                        }
                        break; // Stop if blocked by any piece (ally or enemy)
                    } else {
                        moves.add(new ChessMove(myPosition, newPos, null)); // Empty square
                    }
                }
            }
        }
        return moves;
    }


    private boolean isValidMove(ChessBoard board, ChessPosition position) {
        if (position.getRow() < 0 || position.getRow() >= 8 || position.getColumn() < 0 || position.getColumn() >= 8) {
            return false;
        }
        ChessPiece targetPiece = board.getPiece(position);
        if (targetPiece != null && targetPiece.getTeamColor() == pieceColor) {
            return false;  // Block move if the target piece is friendly
        }
        return true;
    }

    private boolean isValidCapture(ChessBoard board, ChessPosition position) {
        ChessPiece piece = board.getPiece(position);
        return piece != null && piece.getTeamColor() != pieceColor;
    }
}
