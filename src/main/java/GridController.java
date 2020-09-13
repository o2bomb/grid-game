/**
 * A thread-safe singleton class that initializes a thread
 * containing the Grid object
 */
public class GridController {
    // SINGLETON INSTANCE
    private static GridController instance = null;

    // GRID OBJECT
    private Grid grid;
    
    // THREADING STUFF
    private Thread gridThread;

    private GridController() {
        this.grid = new Grid(9, 9, 2000);
        this.gridThread = new Thread(grid, "Grid thread");
        gridThread.start();
    }

    public static GridController getInstance() {
        if (instance == null) {
            synchronized (GridController.class) {
                if (instance == null) {
                    instance = new GridController();
                }
            }
        }
        return instance;
    }

    public Grid getGrid() {
        return grid;
    }

    public void end() {
        gridThread.interrupt();
    }
}
