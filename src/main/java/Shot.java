public class Shot {
    private long createdAt; // a timestamp indicating when it was created at
    private int x;
    private int y;

    public Shot(int x, int y) {
        this.createdAt = System.currentTimeMillis();
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
