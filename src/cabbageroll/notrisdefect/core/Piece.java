package cabbageroll.notrisdefect.core;

public class Piece {

    private int color;
    private int x;
    private int y;
    private int rotation;

    public Piece(Piece p) {
        this(p.getColor());
    }

    public Piece(int color) {
        this(color, 3, 17, 0);
    }

    public Piece(int color, int x, int y, int rotation) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
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
