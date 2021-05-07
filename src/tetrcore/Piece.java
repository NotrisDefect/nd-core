package tetrcore;

import java.awt.*;

public class Piece {
    private int pieceNumber;
    private int x;
    private int y;
    private int rotation;

    public Piece(int pieceNumber) {
        this(pieceNumber, null);
    }

    @SuppressWarnings("unused")
    public Piece(int pieceNumber, Point position) {
        this(pieceNumber, position, 0);
    }

    @SuppressWarnings("unused")
    public Piece(int pieceNumber, Point position, int rotation) {
        this.pieceNumber = pieceNumber;
        this.x = position.x;
        this.y = position.y;
        this.rotation = rotation;
    }

    public int getPieceNumber() {
        return pieceNumber;
    }

    public void setPieceNumber(int pieceNumber) {
        this.pieceNumber = pieceNumber;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
