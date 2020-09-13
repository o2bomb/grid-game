public class PlayerSquare extends GridSquare {
    private Thread playerThread;

    public PlayerSquare(int x, int y) {
        super(x, y);
        playerThread = new Thread(new Player(), "Player thread");
        playerThread.start();
    }

    public void end() {
        playerThread.interrupt();
    }
}
