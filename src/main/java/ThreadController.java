/**
 * A thread-safe singleton class that initializes a thread
 * containing the Grid and Player object
 */
public class ThreadController {
    // SINGLETON INSTANCE
    private static ThreadController instance = null;

    // GRID OBJECT
    private Grid grid;

    // PLAYER OBJECT
    private Player player;
    
    // THREADING STUFF
    private Thread gridThread = null;
    private Thread playerThread = null;

    private ThreadController() {
        this.grid = new Grid(9, 9, 2000);
        this.player = new Player();
    }

    public static ThreadController getInstance() {
        if (instance == null) {
            synchronized (ThreadController.class) {
                if (instance == null) {
                    instance = new ThreadController();
                }
            }
        }
        return instance;
    }

    public Grid getGrid() {
        return grid;
    }

    public Player getPlayer() {
        return player;
    }

    public void start() {
        if (gridThread != null || playerThread != null) {
            throw new IllegalStateException("Either Player or Grid thread already exists");
        }

        gridThread = new Thread(grid, "Grid thread");
        gridThread.start();
        playerThread = new Thread(new Player(), "Player thread");
        playerThread.start();
    }

    public void end() {
        gridThread.interrupt();
        gridThread = null;

        playerThread.interrupt();
        playerThread = null;
    }
}
