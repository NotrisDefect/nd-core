package tetrcore;

public class Piece {
    private int pieceNumber;
    private Integer x;
    private Integer y;
    private int rotation;

    public Piece(int pieceNumber) {
        this(pieceNumber, null, null);
    }

    @SuppressWarnings("unused")
    public Piece(int pieceNumber, Integer x, Integer y) {
        this(pieceNumber, x, y, 0);
    }

    @SuppressWarnings("unused")
    public Piece(int pieceNumber, Integer x, Integer y, int rotation) {
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
