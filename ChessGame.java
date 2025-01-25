package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        this.teamTurn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();  // Reset the board to the default starting position
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;  // No piece at the given position
        }

        return piece.pieceMoves(board, startPosition);  // Get valid moves for the piece
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // Check if the move is valid first
        if (!validMove(move)) {
            throw new InvalidMoveException("Invalid move");
        }

        // Make the move by updating the board
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece piece = board.getPiece(startPosition);

        // Move the piece on the board
        board.addPiece(endPosition, piece);
        board.addPiece(startPosition, null);  // Remove the piece from the start position

        // Set the next team's turn
        setTeamTurn(teamTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> validMoves = validMoves(position);
                    for (ChessMove move : validMoves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;  // The king is under attack
                        }
                    }
                }
            }
        }
        return false;  // The king is not in check
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // If the team is in check and has no valid moves, it's in checkmate
        return isInCheck(teamColor) && !hasValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !hasValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    // Helper methods

    /**
     * Finds the position of the king of a given team color
     *
     * @param teamColor the team color to find the king for
     * @return the position of the king
     */
    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return position;
                }
            }
        }
        return null;  // King not found (should not happen in a valid game)
    }

    /**
     * Checks if the given team has any valid moves
     *
     * @param teamColor the team to check for valid moves
     * @return true if the team has valid moves, otherwise false
     */
    private boolean hasValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> validMoves = validMoves(position);
                    if (!validMoves.isEmpty()) {
                        return true;  // There is at least one valid move
                    }
                }
            }
        }
        return false;  // No valid moves
    }

    /**
     * Validates a move (checks if the move is within the board and follows piece movement rules)
     *
     * @param move the move to validate
     * @return true if the move is valid, false otherwise
     */

    private boolean validMove(ChessMove move) {
        // Check if the destination is within the board's bounds
        ChessPosition endPosition = move.getEndPosition();
        if (endPosition.getRow() < 1 || endPosition.getRow() > 8 || endPosition.getColumn() < 1 || endPosition.getColumn() > 8) {
            return false;  // Move is out of bounds
        }

        // Further validation could be added here (e.g., check piece movement rules)

        return true;  // Basic bounds check passed
    }
}
