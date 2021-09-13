package cabbageroll.notrisdefect.core;

public class Piece {
    private int pieceNumber;
    private int x;
    private int y;
    private int rotation;

    public Piece(int pieceNumber) {
        this(pieceNumber, 0, 0);
    }

    public Piece(int pieceNumber, int x, int y) {
        this(pieceNumber, x, y, 0);
    }

    public Piece(int pieceNumber, int x, int y, int rotation) {
        this.pieceNumber = pieceNumber;
        this.x = x;
        this.y = y;
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
