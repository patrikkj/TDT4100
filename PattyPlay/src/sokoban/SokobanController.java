package sokoban;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class SokobanController {
	//Instance variables
	private Game game;
	private Scene scene;
	private GridPane gridPane;
	private double tileWidth;
	private double tileHeight;
	private double tileSize;
	private int tilesX;
	private int tilesY;
	private int level;
	private StackPane character;
	private ImageView charImageView;
	
	
	//FXML References
	@FXML StackPane rootPane;
	@FXML AnchorPane subRootPane;
	
	
	//Initialize Game
	public void initialize() {
		//Set default level
		level = 1;
		
		//Initialize GUI
		initializeGrid();
		
		renderCharacter();
		
	}
	
	//Initialize GUI
	private void initializeGrid() {
		//Load specified level
		game = new Game(Levels.getLevel(level));
		
		//Create Grid Pane to encapsulate tiles
		gridPane = new GridPane();
		gridPane.setAlignment(Pos.CENTER);
		
		//Board dimensions
		tilesX = game.getWidth();
		tilesY = game.getHeight();
		
		//Calculate tile dimensions
		tileWidth = subRootPane.getMinWidth() / tilesX;
		tileHeight = subRootPane.getMinHeight() / tilesY;
		tileSize = Math.round(Math.min(tileWidth, tileHeight));


		//Generate tiles
		for (int y = 0; y < tilesY; y++)
			for (int x = 0; x < tilesX; x++) {
				//Initialize new tile
				StackPane tile = new StackPane();
				
				//Set identifier (scene.lookup("#id"))
				tile.setId(String.format("Tile_%s_%s", x, y));

				//Apply size specification
				tile.setPrefSize(tileSize, tileSize);
				
				//Add image to imageView
				updateImage(x, y, tile);

				//Append tile to gridPane
				gridPane.add(tile, x, y);
			}

		//Insert Grid Pane into subRootPane
		subRootPane.getChildren().setAll(gridPane);
		AnchorPane.setBottomAnchor(gridPane, 0d);
		AnchorPane.setTopAnchor(gridPane, 0d);
		AnchorPane.setLeftAnchor(gridPane, 0d);
		AnchorPane.setRightAnchor(gridPane, 0d);
	}
	
	//Set key listener (Note: Do not call prior to scene loading)
	public void postInitialize() {
		//Create reference to current scene
		scene = rootPane.getScene();
		
		//Set key listener
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				String key = event.getCode().getName();
				
				//Check directions
				switch (key) {
				case "Up":
					System.out.println("Up");
					if (game.move(Game.UP))
						moveCharacter(Game.UP);
					break;
				case "Down":
					System.out.println("Down");
					if (game.move(Game.DOWN))
						moveCharacter(Game.DOWN);
					break;
				case "Left":
					System.out.println("Left");
					if (game.move(Game.LEFT))
						moveCharacter(Game.LEFT);
					break;
				case "Right":
					System.out.println("Right");
					if (game.move(Game.RIGHT))
						moveCharacter(Game.RIGHT);
					break;
				case "R":
					System.out.println("Reset");
					resetLevel();
				}
				
				if (game.isFinished())
					System.out.println("Congratulations!!");
					
				//Update GUI after move
				update();				
		}});
	}

	//Update GUI
	private void update() {
		int[] playerCoords = game.getPlayerCoords();
		
		int minX = Math.max(playerCoords[0] - 2, 0);
		int maxX = Math.min(playerCoords[0] + 2, tilesX);
		int minY = Math.max(playerCoords[1] - 2, 0);
		int maxY = Math.min(playerCoords[1] + 2, tilesY);
		
		//Iterate through grid to update images
		for (int y = minY; y < maxY; y++) {
			for (int x = minX; x < maxX; x++) {
				//Reference to ImageView at tile (x, y)
				StackPane stackPane = (StackPane) scene.lookup(String.format("#Tile_%s_%s", x, y));
				
				//Update Image
				updateImage(x, y, stackPane);
			}
		}
	}
	
	//Set image at specified tile
	private void updateImage(int x, int y, StackPane stackPane) {
		//Tile
		Cell tile = game.get(x, y);
		
		//Corresponding StackPane
		ImageView baseImageView = new ImageView(new Image(getClass().getResourceAsStream("../icons/sokoban/Ground_Concrete.png")));
		baseImageView.setFitHeight(tileSize);
		baseImageView.setFitWidth(tileSize);

		stackPane.getChildren().setAll(baseImageView);
		
		ImageView imageView = new ImageView();

		//Set image based on grid values
		switch (tile.toChar()) {
		case Cell.WALL:
			imageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Wall_Black.png")));
			imageView.setFitHeight(tileSize);
			imageView.setFitWidth(tileSize);
			break;
		case Cell.BLOCK_ENDPOINT:
			imageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/CrateDark_Blue.png")));
			imageView.setFitHeight(tileSize);
			imageView.setFitWidth(tileSize);
			break;
		case Cell.BLOCK:
			imageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Crate_Blue.png")));
			imageView.setFitHeight(tileSize);
			imageView.setFitWidth(tileSize);
			break;
		case Cell.ENDPOINT:	
			imageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/EndPoint_Blue.png")));
			imageView.setFitHeight(tileSize/3);
			imageView.setFitWidth(tileSize/3);
			break;
		case Cell.EMPTY:
			stackPane.getChildren().clear();
			imageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Ground_Concrete.png")));
			imageView.setFitHeight(tileSize);
			imageView.setFitWidth(tileSize);
			break;
		case Cell.NONE:
			stackPane.getChildren().clear();
			return;
		}
		
		stackPane.getChildren().add(imageView);
	}
	
	//Load previous level
	@FXML private void previousLevel() {
//		level--;
		System.out.println("...");
	}
	
	//Load next level
	@FXML private void nextLevel() {
//		level++;
		System.out.println("...");
	}
	
	//Reset current level
	@FXML private void resetLevel() {
		initializeGrid();
		
		renderCharacter();
	}
	
	
	//Character rendering
	private void renderCharacter() {
		character = new StackPane();
		character.setAlignment(Pos.CENTER);
		charImageView = new ImageView(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Front0.png")));
		charImageView.setFitHeight(tileSize*0.8);
		charImageView.setFitWidth(tileSize*0.6);
		character.getChildren().setAll(charImageView);
		int[] playerCoords = game.getPlayerCoords();
		gridPane.add(character, 0, 0);
		character.setTranslateX(playerCoords[0] * tileSize);
		character.setTranslateY(playerCoords[1] * tileSize);
	}
	
	//Character movement
	private void moveCharacter(int[] direction) {
		if (direction == Game.UP)
			charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Back0.png")));
		if (direction == Game.DOWN)
			charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Front0.png")));
		if (direction == Game.LEFT)
			charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Left0.png")));
		if (direction == Game.RIGHT)
			charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Right0.png")));
		
		// Player coordinates
		int[] playerCoords = game.getPlayerCoords();
		double playerX = playerCoords[0];
		double playerY = playerCoords[1];
		
		TranslateTransition translateTransition = new TranslateTransition();
		
		//Transition properties
		double duration = 300;
		translateTransition.setNode(character);
		translateTransition.setInterpolator(Interpolator.EASE_BOTH);
		translateTransition.setToX(playerX * tileSize);
		translateTransition.setToY(playerY * tileSize);
		translateTransition.setDuration(Duration.millis(duration));
		
		//Play Transition
		translateTransition.play();
		

		Timeline timeline = new Timeline();
		if (direction == Game.UP) {
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 0.25),
			        ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Back1.png")))));
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 0.5),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Back2.png")))));
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 0.75),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Back1.png")))));
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 1),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Back2.png")))));
		}
		
		else if (direction == Game.DOWN) {
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 0.25),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Front1.png")))));
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 0.5),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Front2.png")))));
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 0.75),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Front1.png")))));
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 1),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Front2.png")))));
		}
		
		else if (direction == Game.LEFT) {
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 0.25),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Left1.png")))));
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 0.5),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Left0.png")))));
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 0.75),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Left1.png")))));
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 1),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Left0.png")))));
		}
		
		else if (direction == Game.RIGHT) {
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 0.25),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Right1.png")))));
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 0.5),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Right0.png")))));
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 0.75),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Right1.png")))));
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration * 1),
					ae -> charImageView.setImage(new Image(getClass().getResourceAsStream("../icons/sokoban/Character_Right0.png")))));
		}
		
		timeline.play();	
		
		System.out.println(String.format("Player coords: %s, %s \n %s, %s", playerX, playerY, character.getTranslateX(), character.getTranslateY()));
	}

	
	//Handlers
    
	
	//
	@FXML
    private void handleLoadClick(ActionEvent event) {
    }

    @FXML
    private void handleSaveClick(ActionEvent event) {
    }

    
}