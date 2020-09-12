import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Grid implements Runnable {
    // GRID DATA
    private int length;
    private int height;
    private long spawnDelay;
    private GridSquare[][] grid;

    // THREADING STUFF
    private static int robotCounter = 1;
    private ExecutorService es = new ThreadPoolExecutor(4, 8, 10, TimeUnit.SECONDS, new SynchronousQueue<>());

    public Grid() {
        this.length = 9;
        this.height = 9;
        this.spawnDelay = 2000;
        this.grid = new GridSquare[length][height];

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = new GridSquare(i, j);
            }
        }
    }

    public Grid(int length, int height, int spawnDelay) {
        this.length = length;
        this.height = height;
        this.spawnDelay = spawnDelay;
        this.grid = new GridSquare[height][length];

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = new GridSquare(i, j);
            }
        }
    }

    @Override
    public void run() {
        try {
            while(true) {
                // attempt to spawn robot
                attemptSpawn();
                Thread.sleep(spawnDelay);
            }
        } catch (InterruptedException e) {
            System.out.println("Grid thread has exited");
        }
    }
    
    public GridSquare getGridSquare(int x, int y) {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > length - 1) x = length - 1;
        if (y > height - 1) y = height - 1;

        return grid[x][y];
    }

    /**
     * Attemps to place a new Robot at one of the four corners of the grid.
     * If no corners are free during the execution of this method, the grid 
     * will remain unchanged (i.e. no new robot is spawned). A new thread will
     * be created for the successfully spawned robot via a thread-pool
     */
    private void attemptSpawn() {
        // what happens when gridsquare.isOccupied() then gridsquare.setRobot()?
        for (int i = 1; i <= 4; i++) {
            GridSquare corner = getCorner(i);
            Robot newRobot = new Robot(robotCounter, corner.getX(), corner.getY(), this);
            if (!corner.isOccupied()) {
                try {
                    // attempt to set the robot on the square
                    corner.setRobot(newRobot);
                    // create a new thread for the robot to run in
                    es.submit(newRobot);
                    // update robotCounter
                    robotCounter++;
                    // exit the loop (the robot has been successfully spawned)
                    return;
                } catch (AlreadyOccupiedException e) {
                    // the square is already occupied; try a different square
                    continue;
                } catch (RejectedExecutionException e) {
                    // the robot cannot be spawned due to constraints set for thread
                    // spawning for the ExecutorService object; do nothing
                    break;
                }
            }
        }
    }

    /**
     * Returns a GridSquare object corresponding to the one of the four 
     * corners in the grid (as indicated by cornerId):
     * 1: Top-left corner
     * 2: Bottom-left corner
     * 3: Bottom-right corner
     * 4: Top-right corner
     * If the value of cornerId is invalid, this method returns the
     * top-left GridSquare.
     */
    private GridSquare getCorner(int cornerId) {
        switch (cornerId) {
            case 1:
                return grid[0][0];
            case 2:
                return grid[0][height-1];
            case 3:
                return grid[length-1][height-1];
            case 4:
                return grid[length-1][0];
            default:
                return grid[0][0];
        }
    }
}
