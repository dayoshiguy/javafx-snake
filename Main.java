package application;
	
import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import java.util.Random;

public class Main extends Application {
	private static final int GRID_SIZE = 15;
	private static final int SQUARE_SIZE = 50;
	
    private GridPane gridPane = new GridPane();

    @Override
    public void start(Stage primaryStage) {
    	Grid grid = new Grid(GRID_SIZE, SQUARE_SIZE, gridPane);
        Scene scene = new Scene(gridPane, GRID_SIZE * SQUARE_SIZE + 15, GRID_SIZE * SQUARE_SIZE + 15);
        Player player = new Player(GRID_SIZE/2-2, GRID_SIZE/2-1, grid, 1);
        KeysPressed keys = new KeysPressed(false, false, false, true);
        
        player.draw();
        
        //spawn the first apple
        grid.spawnApple(player.getPrevious());
        
        
        
        // events
        scene.setOnKeyPressed(event ->{
        	KeyCode keyCode = event.getCode();
        	if (keyCode == KeyCode.UP) {
        		if(!keys.down && player.getVel().y != 1) { // cant go up if going down
        				keys.up = true;
        				keys.left = false;
        				keys.right = false;
        		}
        	}
        	if (keyCode == KeyCode.DOWN) {
        		if(!keys.up && player.getVel().y != -1) { // cant go down if going up
        			keys.down = true;
        			keys.left = false;
        			keys.right = false;
        		}
        	}
        	if (keyCode == KeyCode.LEFT) {
        		if(!keys.right && player.getVel().x != 1) { // cant go left if going right
        			keys.left = true;
        			keys.up = false;
        			keys.down = false;
        		}
        	}
        	if (keyCode == KeyCode.RIGHT) {
        		if(!keys.left && player.getVel().x != -1) { // cant go right if going left
        			keys.right = true;
        			keys.up = false;
        			keys.down = false;
        		}
        	}
        });
        // "game loop" runs every 0.25 seconds: (https://stackoverflow.com/a/58776226/14216469)
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.25), e -> {
        	// will run every "frame"
        	if (player.getActive()) {
        		if(keys.up) {
        			player.setVel(0, -1);
        		}
        		if(keys.down) {
        			player.setVel(0, 1);
        		}
        		if(keys.left) {
        			player.setVel(-1, 0);
        		}
        		if(keys.right) {
        			player.setVel(1, 0);
        		}
        		
            	player.update();
            	player.draw();
        	} else { // if dead, close the window
        		// https://stackoverflow.com/a/18362656/14216469
        		System.out.println("GAME OVER. SCORE: " + player.getLength());
        		Platform.exit();
        	}
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.playFromStart();


        
        // boilerplate
        primaryStage.setScene(scene);
        primaryStage.setTitle("Snake in JavaFX by Luke Cardoza 2024");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
class Player {
	private int x;
	private int y;
	private V2d vel;
	private Grid grid;
	private ArrayList<V2d> previous;
	private boolean active;
	private GridPane gridPane;
	private int length;
	public Player(int x, int y, Grid grid, int length) {
		this.x = x;
		this.y = y;
		this.vel = new V2d(0, 0);
		this.grid = grid;
		this.previous = new ArrayList<V2d>();
		this.active = true;
		this.gridPane = this.grid.getGridPane();
		this.length = length;
	}
	// 'draw's current position to grid array
	public void draw() {
		this.grid.update(this.gridPane);
	}
	// update the current position and kill snake if necessary
	public void update() {
		this.previous.add(new V2d(this.x, this.y));
		
		this.x = (this.x+this.grid.getSize()+this.vel.x) % this.grid.getSize();
		this.y = (this.y+this.grid.getSize()+this.vel.y) % this.grid.getSize();
		for(int i = 0; i<this.previous.size(); i++) {
			// if the player crashed into themselves
			if(this.previous.get(i).equals(this.x, this.y)) {
				this.active = false;
			}
		}
		// if "eating" apple, increase length and spawn a new one
		if(this.grid.getGrid()[this.y][this.x] == 2) {
			// spawn a new apple
			grid.spawnApple(this.previous);
			// increase the length of the player
			this.length++;
		}
		if(this.previous.size() > this.length-1) {
			// pop leftmost
			V2d tail = this.previous.get(0);
			this.previous.remove(0);
			
			this.grid.setGrid(tail.x, tail.y, 0);
		}
		
		this.grid.setGrid(x, y, 1);
	}
	//getters
	public boolean getActive() {
		return this.active;
	}
	public V2d getVel() {
		return this.vel;
	}
	public int getLength() {
		return this.length;
	}
	public V2d getPos() {
		return new V2d(this.x, this.y);
	}
	//setters
	public void setVel(V2d vel) {
		this.vel = vel;
	}
	public void setVel(int x, int y) {
		this.vel = new V2d(x, y);
	}
	public ArrayList<V2d> getPrevious(){
		return this.previous;
	}
}
class Grid {
	private int size;
	private int square_size;
	private int[][] grid;
	private GridPane gridPane;
	
	public Grid(int size, int square_size, GridPane gridPane) {
		this.size = size;
		this.square_size = square_size;
		this.gridPane = gridPane;
		
		grid = new int[size][size];
		
		for(int row = 0; row < this.size; row++) {
			for(int col = 0; col < this.size; col++) {
				grid[row][col] = 0;
				Rectangle square = new Rectangle(this.square_size, this.square_size);
				square.setFill(grid[row][col] == 0 ? Color.WHITE : Color.BLACK);
				square.setStroke(Color.BLACK);
				this.gridPane.add(square, col, row);
			}
		}
	}
	//getters
	public int[][] getGrid(){
		return this.grid;
	}
	public GridPane getGridPane() {
		return this.gridPane;
	}
	public int getSize() {
		return this.size;
	}
	//setters
	public void setGrid(int[][] grid) {
		this.grid = grid;
	}
	public void setGrid(int x, int y, int val) {
		this.grid[y][x] = val;
	}
	public void update(GridPane gridPane) {
		for(int row = 0; row < this.size; row++) {
			for(int col = 0; col < this.size; col++) {
				// https://stackoverflow.com/a/41348291/14216469
				Rectangle square = (Rectangle) gridPane.getChildren().get(row * size + col);
				// update each cell based on grid[][] array
				if(grid[row][col] == 0) { // Empty
					square.setFill(Color.WHITE);
				}else if(grid[row][col] == 1) { // Player
					square.setFill(Color.BLACK);
				}else if(grid[row][col] == 2) { // Apple
					square.setFill(Color.RED);
				}
			}
		}
	}
	public void spawnApple(ArrayList<V2d> playerTiles) {
		Random random = new Random();
		int randomX = 0;
		int randomY = 0;
		boolean going = true;
		while(going) {
			randomX = random.nextInt(this.size);
			randomY = random.nextInt(this.size);
			going = false;
			for(int i = 0; i<playerTiles.size(); i++) {
				// if the apple spawned inside player, spawn it somewhere else
				if(playerTiles.get(i).equals(randomX, randomY)) {
					going = true;
				}
			}
		}
		this.grid[randomY][randomX] = 2;
	}
}
class V2d{ // basic vector implementation
	public int x;
	public int y;
	public V2d(int x, int y){
		this.x = x;
		this.y = y;
	}
	public boolean equals(int x, int y) {
		if(this.x == x && this.y == y) {
			return true;
		}
		return false;
	}
}
class KeysPressed{
	public boolean up;
	public boolean left;
	public boolean down;
	public boolean right;
	public KeysPressed(boolean up, boolean down, boolean left, boolean right) {
		this.up = up;
		this.left = left;
		this.down = down;
		this.right = right;
	}
	public String toString() {
		return "up: " + this.up + "\ndown: " + this.down + "\nleft: " + this.left + "\nright: " + this.right;
	}
}
