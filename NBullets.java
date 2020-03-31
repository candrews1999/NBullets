import tester.*;                // The tester library
import javalib.worldimages.*;   // images, like RectangleImage or OverlayImages
import javalib.funworld.*;      // the abstract World class and the big-bang library
import java.awt.Color;          // general colors (as triples of red,green,blue values)
// and predefined colors (Color.RED, Color.GRAY, etc.)
import java.util.Random; // random numbers

//class contains the necessary Constants for the NBullets class and its methods
class Constant{
  static final int SCREEN_WIDTH = 500;
  static final int SCREEN_HEIGHT = 300;
  static final double TICK_RATE = 1.0 / 28.0;
  static final int INTIAL_BULLET_RADIUS = 2;
  static final int BULLET_INCREASE = 2;
  static final int MAX_BULLET_RADIUS = 10;
  static final int BULLET_SPEED = 8;
  static final Color BULLET_COLOR = Color.PINK;
  static final int SHIP_SIZE = SCREEN_HEIGHT / 30;
  static final Color SHIP_COLOR = Color.cyan;
  static final int SHIP_SPEED = BULLET_SPEED / 2;
  static final int SPAWN_FREQUENCY = 28;
  static final int SHIP_SPAWN_RANGE = ((int) (5 * (SCREEN_HEIGHT / 7)));
  static final int MAX_SHIP_SPAWN = 3;
  static final Color TEXT_COLOR = Color.BLACK;
  static final int FONT_SIZE = 13;
  static final WorldScene WORLD_SCENE = new WorldScene(SCREEN_WIDTH, SCREEN_HEIGHT);
  static final int ONE_SEVENTH_HEIGHT = ((int) SCREEN_HEIGHT / 7);
}

//class represents the NBullets Game and the necessary information to run it with big bang
class NBullets extends World {
  int bulletsLeft;
  int numDestroyed;
  ILoGamePiece currentILGP;
  int spawnCount;
  Random rand;

  NBullets(
      int bulletsLeft, int numDestroyed, ILoGamePiece currentILGP, int spawnCount, Random rand) {
    this.bulletsLeft = bulletsLeft;
    this.numDestroyed = numDestroyed;
    this.currentILGP = currentILGP;
    this.spawnCount = spawnCount;
    this.rand = rand;
  }

  NBullets(int bulletsLeft) {
    this(bulletsLeft, 0, new MtLoGamePiece(), Constant.SPAWN_FREQUENCY, new Random());
  }

  NBullets(int bulletsGiven, Random seed) {
    this(bulletsGiven, 0, new MtLoGamePiece(), Constant.SPAWN_FREQUENCY, seed);
  }

  //carries out necessary procedures to the current NBullets each tick
  public NBullets onTick() {
    return new NBullets(this.bulletsLeft, 
        this.numDestroyed + this.currentILGP.offScreen().countShipsDestroyed(new MtLoGamePiece()),
        this.currentILGP.spawnShips(
            this.spawnCount, this.rand).offScreen().moveILGP().removeCollisions(
            new MtLoGamePiece()), 
        this.spawnCount + 1, 
        this.rand); 
  }

  //shoots a bullet upwards when the space key is pressed (if there are Bullets remaining)
  //and removes a subtracts one from the bulletsLeft field
  public NBullets onKeyEvent(String key) {
    if (key.equals(" ")  && this.bulletsLeft > 0) {
      return new NBullets(this.bulletsLeft - 1, this.numDestroyed, this.currentILGP.shootBullet(),
          this.spawnCount, this.rand);
    }
    else {
      return this;
    }
  }

  //determines if the NBullets world has ended(no Bullets left to shoot and no Bullets in ILGP 
  //field) and outputs appropriate WorldScene
  public WorldEnd worldEnds() {
    if ((this.bulletsLeft == 0) && this.currentILGP.areAllShips()) {
      return new WorldEnd(true, this.createLastScene());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  //draws the last Scene using the current NBullets state
  public WorldScene createLastScene() {
    return Constant.WORLD_SCENE.placeImageXY(
        new TextImage("GAME OVER " + "Ships Destroyed: " + this.numDestroyed, Constant.FONT_SIZE,
            Constant.TEXT_COLOR), Constant.SCREEN_WIDTH / 2, Constant.SCREEN_HEIGHT / 2);
  }

  //draws this NBullets as a World Scene
  public WorldScene makeScene() {
    return this.drawCurrGP().placeImageXY(this.drawInfo(), 135, Constant.SCREEN_HEIGHT - 10);
  }

  //draws the current List of GamePieces in the NBullets world
  public WorldScene drawCurrGP() {
    return this.currentILGP.drawILGP(Constant.WORLD_SCENE);
  }

  //draws the required text at the bottom of the WorldScene
  public TextImage drawInfo() {
    return new TextImage("Remaining Bullets: " + this.bulletsLeft +
        "  Ships Destroyed: " + this.numDestroyed, Constant.FONT_SIZE, Constant.TEXT_COLOR);
  }
}

//represents a List of GamePieces (both Ships and Bullets)
interface ILoGamePiece {

  //draws this ILGP as a WorldScene
  public WorldScene drawILGP(WorldScene ws);

  //Spawns random number of Ships at random heights and side if the count has reached 0
  public ILoGamePiece spawnShips(int spawnCount, Random rand);

  //Spawns Ships at random heights and side until count reaches zero
  public ILoGamePiece shipSpawner(int numLefttoSpawn, boolean side, int yCoord, Random rand);

  //shoots a bullet upwards
  public ILoGamePiece shootBullet();

  //moves the ILoGamePiece in their current directions
  public ILoGamePiece moveILGP();

  //filters ILoGamePiece so that elements that are off screen are deleted
  public ILoGamePiece offScreen();

  //Determines if Bullet(s) and Ship(s) have collided and if it has then it is removed and added
  //to the removed ILGP
  public ILoGamePiece removeCollisions(ILoGamePiece removedILGP);

  //Determines if there are any Collisions between Bullet(s) and Ship(s) in this ILoGamePiece
  public boolean anyCollision(IGamePiece check);

  //determines how many Ships were destroyed in Collisions
  public int countShipsDestroyed(ILoGamePiece removedILGP);

  //determines if all the IGamePieces in this list are Ships
  public boolean areAllShips();
  
  //filters out Ships and Bullets from removeILGP list and adds the necessary Bullets
  public ILoGamePiece addBullets();
  
  //appends two lists together
  public ILoGamePiece append(ILoGamePiece accumulator);
}

//Represents a list of Game Pieces abstractly
abstract class ALoGamePiece implements ILoGamePiece {

  //shoots bullet halfway on the x axis of the screen upwards and adds it to previous empty list
  public ILoGamePiece shootBullet() {
    return new ConsLoGamePiece(new Bullet(Constant.INTIAL_BULLET_RADIUS,
        270.0, new MyPosn(Constant.SCREEN_WIDTH / 2, Constant.SCREEN_HEIGHT), 0), this);
  }

  //spawns random number of Ships at random heights and side if the count has reached 0
  public ILoGamePiece spawnShips(int spawnCount, Random rand) {
    if (spawnCount % 28 == 0) {
      return this.shipSpawner(
          rand.nextInt(Constant.MAX_SHIP_SPAWN),
          rand.nextBoolean(),
          Constant.ONE_SEVENTH_HEIGHT + rand.nextInt(Constant.SHIP_SPAWN_RANGE),
          rand);
    }
    else
      return this;
  }

  //spawns Ships at random heights and side until count reaches zero
  public ILoGamePiece shipSpawner(int numLeftToSpawn, boolean side, int yCoord, Random rand) {
    //if count is greater than 0 and side is true (left)
    if (numLeftToSpawn > 0 && side) {
      return new ConsLoGamePiece(
          new Ship(0.0, new MyPosn(0, yCoord)), 
          this.shipSpawner(
              numLeftToSpawn - 1, rand.nextBoolean(),
              Constant.ONE_SEVENTH_HEIGHT + rand.nextInt(Constant.SHIP_SPAWN_RANGE), rand));
    }
    //if count is greater than 0 and side is false (right)
    else if (numLeftToSpawn > 0 && !side) {
      return new ConsLoGamePiece(
          new Ship(180.0, new MyPosn(Constant.SCREEN_WIDTH, yCoord)), 
          this.shipSpawner(
              numLeftToSpawn - 1, rand.nextBoolean(),
              Constant.ONE_SEVENTH_HEIGHT + rand.nextInt(Constant.SHIP_SPAWN_RANGE), rand));
    }
    //if count is 0
    else {
      return this;
    }
  }
}

//represents a piece of a ILoGamePiece with its first value and its rest
class ConsLoGamePiece extends ALoGamePiece {
  IGamePiece first;
  ILoGamePiece rest;

  ConsLoGamePiece(IGamePiece first, ILoGamePiece rest) {
    this.first = first;
    this.rest = rest;
  }

  //draws the ConsLoGamePiece as a WorldScene
  public WorldScene drawILGP(WorldScene ws) {
    return this.rest.drawILGP(this.first.drawGP(ws));
  }


  //moves this ConsLoGamePiece in their current direction
  public ILoGamePiece moveILGP() {
    return new ConsLoGamePiece(this.first.moveGP(), this.rest.moveILGP());
  }

  //Removes ILoGamePieces that are off screen
  public ILoGamePiece offScreen() {
    if (this.first.offScreenGP()) {
      return this.rest.offScreen();
    }
    else {
      return new ConsLoGamePiece(this.first, this.rest.offScreen());
    }
  }

  //Determines if the given IGamePiece is colliding with any of the elements of this ILoGamePiece
  public boolean anyCollision(IGamePiece check) {
    return this.first.isCollision(check) || this.rest.anyCollision(check);
  }

  //if a collision has occurred with, add the first to accumulator, else, add back to this list
  public ILoGamePiece removeCollisions(ILoGamePiece removedILGP) { //remove the collided objects and do adding of the bullets in other method in on tick
    //remove the first because there is a collision
    if (this.rest.anyCollision(this.first) || removedILGP.anyCollision(this.first)) {
      return this.rest.removeCollisions(new ConsLoGamePiece(this.first, removedILGP));
    }
    //add the first to list because there is no collision
    else {
      return new ConsLoGamePiece(this.first, this.rest.removeCollisions(removedILGP));
    }
  }

  //determines how many Ships were destroyed due to collisions
  public int countShipsDestroyed(ILoGamePiece removedILGP) {
    if ((this.rest.anyCollision(this.first) || 
        removedILGP.anyCollision(this.first)) && this.first.isShip()) {
      return 1 + this.rest.countShipsDestroyed(new ConsLoGamePiece(this.first, removedILGP));
    }
    else {
      return this.rest.countShipsDestroyed(removedILGP);
    }
  }

  //determines if all the IGamePieces in this list are Ships
  public boolean areAllShips() {
    return this.first.isShip() && this.rest.areAllShips();
  }
  
  //determines if the first of this list is a bullet and if it is, adds the appropriate amount of
  //Bullets to the list; the first is removed regardless of its type
  public ILoGamePiece addBullets() {
    return this.first.addBullets(this.rest.addBullets(), this.first.findCount());
  }
  
  //appends a list of newBullets to this ILoGamePiece that did not have collisions
  public ILoGamePiece append(ILoGamePiece accumulator) {
    return this.rest.append(new ConsLoGamePiece(this.first, accumulator));
  }
}

//represents the end of a ILoGamePiece
class MtLoGamePiece extends ALoGamePiece {

  //returns the parameterized WorldScene as is
  public WorldScene drawILGP(WorldScene ws) {
    return ws;
  }

  //returns current ILoGamePiece once an empty list has been reached
  public ILoGamePiece moveILGP() {
    return this;
  }

  //returns this MtLoGamePiece when the end of the list is reached
  public ILoGamePiece offScreen() {
    return this;
  }

  //append the list of new Bullets that must be created with this list of GamePieces that did not 
  //collide
  public ILoGamePiece removeCollisions(ILoGamePiece removedILGP) { 
    return this.append(removedILGP.addBullets());
  }

  //when MtLoGamePiece is reached return false because an empty list can not collide
  public boolean anyCollision(IGamePiece check) {
    return false;
  }

  //returns 0 as the end of the list has been reached
  public int countShipsDestroyed(ILoGamePiece removedILGP) {
    return 0;
  }

  //returns true as the end of this list has been reached
  public boolean areAllShips() {
    return true;
  }
  
  //returns this as is if the list is empty as there were no IGamePieces removed due to collision 
  public ILoGamePiece addBullets() {
    return this;
  }
  
  //returns accumulator once end of this list has been reached
  public ILoGamePiece append(ILoGamePiece accumulator) {
    return accumulator;
  }
}

//represents a GamePiece in the NBullets Game
interface IGamePiece {

  //Draws this IGamePiece on the given WorldScene at its MyPosn coordinates
  public WorldScene drawGP(WorldScene ws);

  //Determines if a Bullet and Ship are colliding
  public boolean isCollision(IGamePiece check);

  //Determines if this IGamePiece is off screen based on the myPosn field
  public boolean offScreenGP();

  //moves this individual IGamePiece
  public IGamePiece moveGP();

  //determines if this is a Bullet then checks to see if it overlaps with other if it is
  public boolean isBulletWithOverlap(MyPosn positionOther, int radiusOther);

  //determines if this is a Ship then checks to see if it overlaps with other if it is
  public boolean isShipWithOverlap(MyPosn positionOther, int radiusOther);

  //adds Bullets with the proper fields to the ILoGamePiece list until the count reaches zero
  public ILoGamePiece addBullets(ILoGamePiece recursiveCall, int count);

  //determines if this is a Ship
  public boolean isShip();
  
  // determines whether two game pieces overlap
  public boolean overlap(MyPosn positionOther, int radiusOther);
  
  //determines the the number of Bullets that must be made based on the amount of times a Bullet 
  //has been hit
  public int findCount();
  
}

//represents a GamePiece abstractly
abstract class AGamePiece implements IGamePiece {
  Color color;
  int radius;
  int velocity;
  double angle;
  MyPosn position;

  AGamePiece(Color color, int radius, int velocity, double angle, MyPosn position) {
    this.color = color;
    this.radius = radius;
    this.velocity = velocity;
    this.angle = angle;
    this.position = position;
  }

  //draws a circle to represent the game piece
  public WorldScene drawGP(WorldScene ws) {
    return this.position.placeGP(new CircleImage(this.radius, OutlineMode.SOLID, this.color), ws);
  }

  //Determines if this AGamePiece is off screen by looking at its MyPosn
  public boolean offScreenGP() {
    return this.position.offScreenPosn();
  }
  
  //determines if there is overlap in the two IGamePieces
  public boolean overlap(MyPosn positionOther, int radiusOther) {
    return Math.hypot(this.position.calculateDistance(positionOther).x + 0.0, 
        this.position.calculateDistance(positionOther).y + 0.0) <= this.radius + radiusOther;
  }
}

//Represents a Ship that is to be shot by the player with a Bullet
class Ship extends AGamePiece {

  Ship(double angle, MyPosn position) {
    // may have to define specific hard-coded values for the
    // color, radius, and velocity of the ship in this constructor
    super(Constant.SHIP_COLOR, Constant.SHIP_SIZE, Constant.SHIP_SPEED, angle, position);
  }

  //moves this Ship based on its current direction
  public IGamePiece moveGP() {
    return new Ship(this.angle, this.position.movePosn(this.angle, this.velocity));
  }

  //determines if there is a Collision of a Bullet and Ship
  public boolean isCollision(IGamePiece check) {
    return check.isBulletWithOverlap(this.position, this.radius);
  }

  //since this is a Ship, determine if the two MyPosns are the same
  public boolean isShipWithOverlap(MyPosn positionOther, int radiusOther) {
    return this.overlap(positionOther, radiusOther);
  }

  //since this is also a Ship, return false as there is no collision
  public boolean isBulletWithOverlap(MyPosn positionOther, int radiusOther) {
    return false;
  }

  //calls function again as this is a Ship and thus should not cause new Bullets to be added
  public ILoGamePiece addBullets(ILoGamePiece recursiveCall, int count) {
    return recursiveCall;
  }

  //returns true as this is a Ship
  public boolean isShip() {
    return true;
  }
  
  //returns 0 if a Ship because a Ship has no hitCount field
  public int findCount() {
    return 0;
  }
}

//represents a Bullet that is used to shoot a Ship by the Player
class Bullet extends AGamePiece {
  int timesHit;

  Bullet(int radius, double angle, MyPosn position, int timesHit) {
    super(Constant.BULLET_COLOR, radius, Constant.BULLET_SPEED, angle, position);
    this.timesHit = timesHit;
  }


  //moves this Ship based on its current direction
  public IGamePiece moveGP() {
    return new Bullet(
        this.radius, 
        this.angle,
        this.position.movePosn(this.angle, this.velocity), 
        this.timesHit);
  }

  //determines if there is a Collision of a Bullet and Ship
  public boolean isCollision(IGamePiece check) {
    return check.isShipWithOverlap(this.position, this.radius);
  }

  //since this is also a Bullet, return false as there is no collision
  public boolean isShipWithOverlap(MyPosn positionOther, int radiusOther) {
    return false;
  }

  //since this is a Bullet, determine if the two MyPosns are the same
  public boolean isBulletWithOverlap(MyPosn positionOther, int radiusOther) {
    return this.overlap(positionOther, radiusOther);
  }

  //adds bullets with a larger radius, adjusted angle and increased timesHit field 
  //(until count equals 0)
  public ILoGamePiece addBullets(ILoGamePiece recursiveCall, int count) {
    if (count == 0) {
      return recursiveCall;
    }
    else if (this.radius + 2 <= Constant.MAX_BULLET_RADIUS){
      //if radius is less than Max Bullet Radius, increase radius
      return new ConsLoGamePiece(
          new Bullet(this.radius + Constant.BULLET_INCREASE, ((360 / (this.timesHit + 2)) * (count - 1)),
              this.position, this.timesHit + 1),
          this.addBullets(recursiveCall, count - 1));
    }
    else {
      //if radius is greater than Max Bullet Radius, stop increasing
      return new ConsLoGamePiece(
          new Bullet(this.radius, ((360 / (this.timesHit + 2)) * (count - 1)),
              this.position, this.timesHit + 1),
          this.addBullets(recursiveCall, count - 1));
    }
  }

  //returns false as this is not a Ship
  public boolean isShip() {
    return false;
  }
  
  //returns the amount of Bullets that need to be made based on the times the bullet has been hit
  public int findCount() {
    return this.timesHit + 2;
  }
}

class MyPosn extends Posn {

  MyPosn(int x, int y) {
    super(x, y);
  }

  //determines if this MyPosn is off the screen based on the screen dimensions
  public boolean offScreenPosn() {
    return (this.x > Constant.SCREEN_WIDTH || this.x < 0) ||
        (this.y > Constant.SCREEN_HEIGHT || this.y < 0);
  }

  //creates a new MyPosn based on the given velocity and angle and this MyPosn
  public MyPosn movePosn(double angle, int velocity) {
    return new MyPosn(((int) (this.x + velocity * Math.cos(Math.toRadians(angle)))),
        ((int) (this.y + velocity * Math.sin(Math.toRadians(angle)))));
  }

  //places image at MyPosn coordinates on the current WorldScene
  public WorldScene placeGP(CircleImage circle, WorldScene ws) {
    return ws.placeImageXY(circle, this.x, this.y);
  }
  
  public MyPosn calculateDistance(MyPosn other) {
    return new MyPosn(Math.abs(this.x - other.x), Math.abs(this.y - other.y));
  }
}

class ExamplesNBullets {
  //CircleImage shipCircle = new CircleImage()
  MyPosn shootStartPosn = new MyPosn(250, 300);
  MyPosn offScreenPosn1 = new MyPosn(501, 300);
  MyPosn offScreenPosn2 = new MyPosn(480, 301);
  MyPosn onScreenPosn1 = new MyPosn(420, 200);
  MyPosn onScreenPosn2 = new MyPosn(400, 250);

  CircleImage shipCir = new CircleImage(Constant.SHIP_SIZE, OutlineMode.SOLID, Constant.SHIP_COLOR);
  CircleImage bulletCir = new CircleImage(
      Constant.INTIAL_BULLET_RADIUS, OutlineMode.SOLID, Constant.BULLET_COLOR);

  IGamePiece bulletAtStart = new Bullet(Constant.INTIAL_BULLET_RADIUS,
      270.0, this.shootStartPosn, 0);
  IGamePiece bOffScreen = new Bullet(8, 180.0, this.offScreenPosn1, 2);
  IGamePiece bOnScreen1 = new Bullet(10,180.0, this.onScreenPosn1, 2);
  IGamePiece bOnScreen2 = new Bullet(10,0.0, this.onScreenPosn1, 2);

  IGamePiece shipOffScreen1 = new Ship(180.0, this.offScreenPosn2);
  IGamePiece shipOnScreen1Left = new Ship(180.0, this.onScreenPosn1);
  IGamePiece shipOnScreen1Right = new Ship(0.0, this.onScreenPosn1);
  IGamePiece shipOnScreen2Left = new Ship(180.0, this.onScreenPosn2);
  IGamePiece shipOnScreen2Right = new Ship(0.0, this.onScreenPosn2);

  IGamePiece bulletWithSamePos1 = new Bullet(3, 10.0, new MyPosn(54, 23), 1);
  IGamePiece bulletWithSamePos2 = new Bullet(10, 100.0, new MyPosn(54, 23), 4);
  IGamePiece shipWithSamePos1 = new Ship(180.0, new MyPosn(54, 23));
  IGamePiece shipWithSamePos2 = new Ship(0.0, new MyPosn(54, 23));
  
  IGamePiece bulletOverlap1 = new Bullet(4, 0.0, new MyPosn(1, 1), 3);
  IGamePiece shipOverlap1 = new Ship(0.0, new MyPosn(1, 2));
  IGamePiece bulletNoOverlap1 = new Bullet(4, 180.0, new MyPosn(500, 200), 2);
  IGamePiece shipNoOverlap1 = new Ship(0.0, new MyPosn(10, 20));

  ILoGamePiece emptyILGP = new MtLoGamePiece();
  ILoGamePiece shipsSpawnedNoBullets = new ConsLoGamePiece(shipOnScreen1Left,new ConsLoGamePiece(
      shipOnScreen2Left, new ConsLoGamePiece(
          shipOnScreen1Right,new ConsLoGamePiece(shipOnScreen2Right,
              new MtLoGamePiece()))));
  ILoGamePiece ILGPWithOneBulletShot = new ConsLoGamePiece(this.bulletAtStart, this.emptyILGP);
  ILoGamePiece onlyShipILGP = new ConsLoGamePiece(this.shipOnScreen1Left,this.emptyILGP);
  ILoGamePiece ILGP1 = new ConsLoGamePiece(this.bulletAtStart, this.onlyShipILGP);
  ILoGamePiece ILGP2 = new ConsLoGamePiece(this.bOnScreen1, this.ILGP1);
  ILoGamePiece movedILGP1 = new ConsLoGamePiece(
      new Bullet(2, 270.0, new MyPosn(250, 292), 0), 
      new ConsLoGamePiece(new Ship(180.0, new MyPosn(416, 200)), this.emptyILGP));
  //Collision ILGP's
  ILoGamePiece spILGP1 = new ConsLoGamePiece(this.bulletWithSamePos1, 
      new ConsLoGamePiece(this.shipWithSamePos1, this.emptyILGP));
  ILoGamePiece spILGP2 = new ConsLoGamePiece(this.bulletWithSamePos2, 
      new ConsLoGamePiece(this.shipWithSamePos2, this.emptyILGP));
  ILoGamePiece spILGPWithExtraShip = new ConsLoGamePiece(this.bulletWithSamePos2, 
      new ConsLoGamePiece(this.shipWithSamePos2, this.onlyShipILGP));
  ILoGamePiece spILGPWithCollidingBulletWithRemoved = new ConsLoGamePiece(this.bulletWithSamePos2, 
      new ConsLoGamePiece(this.shipWithSamePos2, new ConsLoGamePiece(this.bulletWithSamePos1, this.emptyILGP)));
  ILoGamePiece oneCollILGP = new ConsLoGamePiece(this.shipWithSamePos1, 
      new ConsLoGamePiece(this.bOnScreen1,
          new ConsLoGamePiece(this.bulletWithSamePos1, this.emptyILGP)));
  ILoGamePiece oneCollILGPBulletFirst = new ConsLoGamePiece(this.bulletWithSamePos1, 
      new ConsLoGamePiece(this.bOnScreen1,
          new ConsLoGamePiece(this.shipWithSamePos1, this.emptyILGP)));
  ILoGamePiece twoBulletsOneColl = new ConsLoGamePiece(this.bulletWithSamePos1, this.oneCollILGP);
  ILoGamePiece twoCollILGP = new ConsLoGamePiece(this.shipWithSamePos1, this.oneCollILGP);
  ILoGamePiece twoSeperateCollILGP = new ConsLoGamePiece(this.shipWithSamePos2,
      new ConsLoGamePiece(this.bulletWithSamePos2, this.twoCollILGP));
  
  Random seededZero = new Random(0);
  Random seededOne = new Random(1);
  Random seededTwo = new Random(2);
  Random seededThree = new Random(3);
  Random seededFour = new Random(4);
  Random seededFive = new Random(5);

  //constants for use in testing DrawILGP()
  CircleImage bulletCirForDrawing = new CircleImage(
      8, OutlineMode.SOLID, Constant.BULLET_COLOR);
  WorldScene emptyWorld = Constant.WORLD_SCENE;
  WorldScene worldWithOneShip = emptyWorld.placeImageXY(shipCir, 50, 100);
  WorldScene worldWithOneBullet = emptyWorld.placeImageXY(bulletCirForDrawing, 100, 200);
  WorldScene worldWithBulletAndShip = worldWithOneShip.placeImageXY(bulletCirForDrawing, 100, 200);
  IGamePiece bulletForDrawing = new Bullet(8, 0.0, new MyPosn(100, 200), 2);
  IGamePiece bulletForDrawing2 = new Bullet(2, 0.0, new MyPosn(50, 300), 5);
  IGamePiece shipForDrawing = new Ship(0.0, new MyPosn(50, 100));
  IGamePiece shipForDrawing2 = new Ship(180.0, new MyPosn(100, 500));
  ILoGamePiece listWithBulletForDrawing = new ConsLoGamePiece(bulletForDrawing, emptyILGP);
  ILoGamePiece listWithShipForDrawing = new ConsLoGamePiece(shipForDrawing, emptyILGP);
  ILoGamePiece listWithBothForDrawing = new ConsLoGamePiece(bulletForDrawing, listWithShipForDrawing);
  ILoGamePiece listWithAllBulletsAndShipsForDrawing = new ConsLoGamePiece(bulletForDrawing2, 
      new ConsLoGamePiece(shipForDrawing2, listWithBothForDrawing));
  WorldScene bullet2OnScene = worldWithBulletAndShip.placeImageXY(bulletCir, 50, 300);
  WorldScene allPiecesOnScene = bullet2OnScene.placeImageXY(shipCir, 100, 500);

  NBullets noBullNBullInILGP = new NBullets(0, 20, this.ILGP1, 0, seededOne);
  NBullets noBullNNOBullInILGP = new NBullets(0, 20, this.onlyShipILGP, 0, seededOne);
  NBullets noBullNEmptyInILGP = new NBullets(0, 20, this.emptyILGP, 0, seededOne);
  NBullets bullLeftNNoBullInILGP = new NBullets(7, 20, this.emptyILGP, 0, seededOne);

  //NBULLETS CLASS TESTS***************************************************************************
  //tests if NBullets world ends when it should
  boolean testWorldEnd(Tester t) {
    return t.checkExpect(this.noBullNBullInILGP.worldEnds().worldEnds, false)
        && t.checkExpect(this.noBullNNOBullInILGP.worldEnds().worldEnds, true)
        && t.checkExpect(this.noBullNEmptyInILGP.worldEnds().worldEnds, true)
        && t.checkExpect(this.bullLeftNNoBullInILGP.worldEnds().worldEnds, false);
  }

  // test createLastScene() method
  NBullets noScore = new NBullets(1, 0, new MtLoGamePiece(), 0, new Random());
  NBullets hasScore = new NBullets(0, 10, new MtLoGamePiece(), 0, new Random());
  boolean testCreateLastScene(Tester t) {
    return t.checkExpect(noScore.createLastScene(), Constant.WORLD_SCENE.placeImageXY(new TextImage("GAME OVER "
        + "Ships Destroyed: " + 0, Constant.FONT_SIZE, Constant.TEXT_COLOR), Constant.SCREEN_WIDTH / 2, Constant.SCREEN_HEIGHT / 2))
        && t.checkExpect(hasScore.createLastScene(), Constant.WORLD_SCENE.placeImageXY(new TextImage("GAME OVER "
            + "Ships Destroyed: " + 10, Constant.FONT_SIZE, Constant.TEXT_COLOR), Constant.SCREEN_WIDTH / 2, Constant.SCREEN_HEIGHT / 2));
  }
 
  //test drawInfo method
  boolean testDrawInfo(Tester t) {
    return t.checkExpect(noScore.drawInfo(), new TextImage("Remaining Bullets: " + 1 + "  Ships Destroyed: " + 0, Constant.FONT_SIZE, Constant.TEXT_COLOR))
        && t.checkExpect(hasScore.drawInfo(), new TextImage("Remaining Bullets: " + 0 + "  Ships Destroyed: " + 10, Constant.FONT_SIZE, Constant.TEXT_COLOR));
  }

  //test onKeyEvent(String key) method
  NBullets bulletsLeftEmpty = new NBullets(10, 20, this.emptyILGP, 0, seededOne);
  NBullets bulletsLeftNonEmpty = new NBullets(10, 20, this.ILGP1, 0, seededOne);
  NBullets noBulletsEmpty = new NBullets(0, 10, this.emptyILGP, 0, seededOne);
  NBullets noBulletsNonEmpty = new NBullets(0, 10, this.ILGP1, 0, seededOne);
  boolean testOnKeyEvent(Tester t) {
    return t.checkExpect(bulletsLeftEmpty.onKeyEvent(" "), new NBullets(9, 20, new ConsLoGamePiece(new Bullet(2, 270.0, new MyPosn(250, 300), 0), this.emptyILGP), 0, seededOne))
        && t.checkExpect(bulletsLeftNonEmpty.onKeyEvent(" "), new NBullets(9, 20, new ConsLoGamePiece(new Bullet(2, 270.0, new MyPosn(250, 300), 0), this.ILGP1), 0, seededOne))
        && t.checkExpect(noBulletsEmpty.onKeyEvent(" "), noBulletsEmpty)
        && t.checkExpect(noBulletsNonEmpty.onKeyEvent(" "), noBulletsNonEmpty)
        && t.checkExpect(bulletsLeftEmpty.onKeyEvent("a"), bulletsLeftEmpty)
        && t.checkExpect(bulletsLeftNonEmpty.onKeyEvent("r"), bulletsLeftNonEmpty)
        && t.checkExpect(noBulletsEmpty.onKeyEvent("q"), noBulletsEmpty)
        && t.checkExpect(noBulletsNonEmpty.onKeyEvent("f"), noBulletsNonEmpty);
  }
  
  //test onTick() method
  boolean testOnTick(Tester t) {
    return t.checkExpect(bulletsLeftEmpty.onTick(), new NBullets(10, 20, new MtLoGamePiece(), 1, seededOne));
  }
  
  // test drawCurrGP() method:
  NBullets shipWorldTestCurr = new NBullets(5, 5, new ConsLoGamePiece(new Ship(0.0, new MyPosn(15, 11)), new MtLoGamePiece()),
      0, new Random());
  WorldScene shipWorldScene = emptyWorld.placeImageXY(shipCir, 15, 11);
  NBullets worldToTestDrawCurr = new NBullets(1, 0, new ConsLoGamePiece(new Bullet(2, 0.0, new MyPosn(1, 1), 4), new MtLoGamePiece()),
      0, new Random());
  NBullets worldToTestDrawCurr2 = new NBullets(5, 5, new ConsLoGamePiece(new Bullet(2, 0.0, new MyPosn(1, 1), 4),
      new ConsLoGamePiece(new Bullet(2, 180.0, new MyPosn(5, 5), 3), new MtLoGamePiece())),
      0, new Random());
  
  WorldScene bulletWorldScene = emptyWorld.placeImageXY(bulletCir, 1, 1);
  WorldScene bulletWorldScene2 = bulletWorldScene.placeImageXY(bulletCir, 5, 5);
      
  boolean testDrawCurrGP(Tester t) {
    return t.checkExpect(shipWorldTestCurr.drawCurrGP(), shipWorldScene)
        && t.checkExpect(worldToTestDrawCurr.drawCurrGP(), bulletWorldScene)
        && t.checkExpect(worldToTestDrawCurr2.drawCurrGP(), bulletWorldScene2);
  }
  
  // test makeScene() method
  TextImage textInfo = new TextImage("Remaining Bullets: " + 1 + "  Ships Destroyed: " + 0, Constant.FONT_SIZE, Constant.TEXT_COLOR);
  TextImage textInfo2 = new TextImage("Remaining Bullets: " + 5 + "  Ships Destroyed: " + 5, Constant.FONT_SIZE, Constant.TEXT_COLOR);
  boolean testMakeScene(Tester t) {
    return t.checkExpect(noScore.makeScene(), emptyWorld.placeImageXY(textInfo, 135, 290))
        && t.checkExpect(shipWorldTestCurr.makeScene(), shipWorldScene.placeImageXY(textInfo2, 135, 290))
        && t.checkExpect(worldToTestDrawCurr.makeScene(), bulletWorldScene.placeImageXY(textInfo, 135, 290))
        && t.checkExpect(worldToTestDrawCurr2.makeScene(), bulletWorldScene2.placeImageXY(textInfo2, 135, 290));
  }

  //ILOGAMEPIECE INTERFACE TESTS*******************************************************************
    
  //test drawILGP method
  boolean testDrawILGP(Tester t) {
    return t.checkExpect(emptyILGP.drawILGP(emptyWorld), emptyWorld)
        && t.checkExpect(emptyILGP.drawILGP(worldWithOneBullet), worldWithOneBullet)
        && t.checkExpect(listWithBulletForDrawing.drawILGP(emptyWorld), worldWithOneBullet)
        && t.checkExpect(listWithShipForDrawing.drawILGP(emptyWorld), worldWithOneShip)
        && t.checkExpect(listWithBothForDrawing.drawILGP(emptyWorld), worldWithBulletAndShip)
        && t.checkExpect(listWithAllBulletsAndShipsForDrawing.drawILGP(emptyWorld), allPiecesOnScene);
  }

  // test shipSpawner() method
  boolean testShipSpawner(Tester t) {
    return t.checkExpect(emptyILGP.shipSpawner(3, true, 45, seededTwo), new ConsLoGamePiece(new Ship(0.0, new MyPosn(0, 45)), 
        new ConsLoGamePiece(new Ship(0.0, new MyPosn(0, 204)), 
            new ConsLoGamePiece(new Ship(0.0, new MyPosn(0, 109)),
                new MtLoGamePiece()))))
        && t.checkExpect(ILGP2.shipSpawner(3, false, 250, seededFour), new ConsLoGamePiece(new Ship(180.0, new MyPosn(500, 250)),
            new ConsLoGamePiece(new Ship(0.0, new MyPosn(0, 184)),
                new ConsLoGamePiece(new Ship(0.0, new MyPosn(0, 100)),
                    ILGP2))))
        && t.checkExpect(ILGP1.shipSpawner(0, true, 100, seededOne), ILGP1)
        && t.checkExpect(ILGP1.shipSpawner(1, true, 100, seededZero), new ConsLoGamePiece(new Ship(0.0, new MyPosn(0, 100)),
            ILGP1))
        && t.checkExpect(shipsSpawnedNoBullets.shipSpawner(2, true, 67, seededZero), new ConsLoGamePiece(new Ship(0.0, new MyPosn(0, 67)),
            new ConsLoGamePiece(new Ship(180.0, new MyPosn(500, 149)), shipsSpawnedNoBullets))); 
  }

  //test offScreen method
  boolean testOffScreen(Tester t) {
    return t.checkExpect((new ConsLoGamePiece(this.bOnScreen1, 
        new ConsLoGamePiece(this.bulletAtStart, 
            new ConsLoGamePiece(this.shipOffScreen1, this.emptyILGP)))).offScreen(),
        new ConsLoGamePiece(this.bOnScreen1, 
            new ConsLoGamePiece(this.bulletAtStart, this.emptyILGP)))
        && t.checkExpect(this.emptyILGP.offScreen(), this.emptyILGP)
        && t.checkExpect((new ConsLoGamePiece(this.bOnScreen1, 
            new ConsLoGamePiece(this.bulletAtStart, 
                new ConsLoGamePiece(this.shipOffScreen1, 
                    new ConsLoGamePiece(this.bOffScreen, 
                        this.emptyILGP))))).offScreen(),
            new ConsLoGamePiece(this.bOnScreen1, 
                new ConsLoGamePiece(this.bulletAtStart, this.emptyILGP)));
  }

  //test removeCollisions method
  boolean testRemoveCollisions(Tester t) {
    return t.checkExpect(this.spILGP1.removeCollisions(this.onlyShipILGP), 
        new ConsLoGamePiece(new Bullet(5, 240.0, new MyPosn(54,23), 2),
            new ConsLoGamePiece(new Bullet(5, 120.0, new MyPosn(54,23), 2),
                new ConsLoGamePiece(new Bullet(5, 0.0, new MyPosn(54,23), 2),   
                    this.emptyILGP))))
        && t.checkExpect(this.emptyILGP.removeCollisions(this.spILGP1), 
            new ConsLoGamePiece(new Bullet(5, 240.0, new MyPosn(54,23), 2),
                new ConsLoGamePiece(new Bullet(5, 120.0, new MyPosn(54,23), 2),
                    new ConsLoGamePiece(new Bullet(5, 0.0, new MyPosn(54,23), 2),   
                        this.emptyILGP))))
        && t.checkExpect((new ConsLoGamePiece(this.shipWithSamePos2, 
            new ConsLoGamePiece(this.bulletWithSamePos2, this.emptyILGP))).removeCollisions(this.spILGP1), 
            new ConsLoGamePiece(new Bullet(10, 300.0, new MyPosn(54,23), 5),
                new ConsLoGamePiece(new Bullet(10, 240.0, new MyPosn(54,23), 5),
                    new ConsLoGamePiece(new Bullet(10, 180.0, new MyPosn(54,23), 5),
                        new ConsLoGamePiece(new Bullet(10, 120.0, new MyPosn(54,23), 5),
                            new ConsLoGamePiece(new Bullet(10, 60.0, new MyPosn(54,23), 5),
                                new ConsLoGamePiece(new Bullet(10, 0.0, new MyPosn(54,23), 5), 
                                    new ConsLoGamePiece(new Bullet(5, 240.0, new MyPosn(54,23), 2),
                                        new ConsLoGamePiece(new Bullet(5, 120.0, new MyPosn(54,23), 2),
                                            new ConsLoGamePiece(new Bullet(5, 0.0, new MyPosn(54,23), 2),   
                                                this.emptyILGP))))))))))
        && t.checkExpect(this.emptyILGP.removeCollisions(this.emptyILGP), this.emptyILGP)
        && t.checkExpect(this.emptyILGP.removeCollisions(this.spILGP1), 
            new ConsLoGamePiece(new Bullet(5, 240.0, new MyPosn(54,23), 2),
                new ConsLoGamePiece(new Bullet(5, 120.0, new MyPosn(54,23), 2),
                    new ConsLoGamePiece(new Bullet(5, 0.0, new MyPosn(54,23), 2),   
                        this.emptyILGP))));
  }
  
  //test addBullets in ILGP class
  boolean testAddBulletsILGP(Tester t) {
    return t.checkExpect((new ConsLoGamePiece(this.bulletWithSamePos1, this.onlyShipILGP)).addBullets(), 
        new ConsLoGamePiece(new Bullet(5, 240.0, new MyPosn(54,23), 2),
          new ConsLoGamePiece(new Bullet(5, 120.0, new MyPosn(54,23), 2),
              new ConsLoGamePiece(new Bullet(5, 0.0, new MyPosn(54,23), 2),   
                  this.emptyILGP))))
        && t.checkExpect(new ConsLoGamePiece(this.shipWithSamePos2, new ConsLoGamePiece(this.bulletWithSamePos2, this.emptyILGP)).addBullets(),
            new ConsLoGamePiece(new Bullet(10, 300.0, new MyPosn(54,23), 5),
              new ConsLoGamePiece(new Bullet(10, 240.0, new MyPosn(54,23), 5),
                  new ConsLoGamePiece(new Bullet(10, 180.0, new MyPosn(54,23), 5),
                      new ConsLoGamePiece(new Bullet(10, 120.0, new MyPosn(54,23), 5),
                          new ConsLoGamePiece(new Bullet(10, 60.0, new MyPosn(54,23), 5),
                              new ConsLoGamePiece(new Bullet(10, 0.0, new MyPosn(54,23), 5), 
                                  this.emptyILGP)))))))
        && t.checkExpect(this.emptyILGP.addBullets(), this.emptyILGP)
        && t.checkExpect((new ConsLoGamePiece(this.shipWithSamePos2, this.emptyILGP)).addBullets(), this.emptyILGP);
  }
  
  ILoGamePiece oneCollILGPBulletSec = new ConsLoGamePiece(this.shipWithSamePos1, 
    new ConsLoGamePiece(this.bOnScreen1,
        new ConsLoGamePiece(this.bulletWithSamePos1, this.emptyILGP)));
  
  //test countShipsDestroyed method
  boolean testCountShipsDestroyed(Tester t) {
    return t.checkExpect(this.oneCollILGP.countShipsDestroyed(this.emptyILGP), 1)
        && t.checkExpect(this.ILGP1.countShipsDestroyed(this.emptyILGP), 0)
        && t.checkExpect(this.emptyILGP.countShipsDestroyed(this.emptyILGP), 0)
        && t.checkExpect(this.oneCollILGPBulletSec.countShipsDestroyed(this.emptyILGP), 1)
        && t.checkExpect(this.twoCollILGP.countShipsDestroyed(this.emptyILGP), 2)
        && t.checkExpect(this.twoSeperateCollILGP.countShipsDestroyed(this.emptyILGP), 3)
        && t.checkExpect(this.twoBulletsOneColl.countShipsDestroyed(this.emptyILGP), 1)
        && t.checkExpect(this.twoCollILGP.countShipsDestroyed(this.twoSeperateCollILGP), 2);
  }
  
//  //Collision ILGP's
//  ILoGamePiece spILGP1 = new ConsLoGamePiece(this.bulletWithSamePos1, 
//      new ConsLoGamePiece(this.shipWithSamePos1, this.emptyILGP));
//  ILoGamePiece spILGP2 = new ConsLoGamePiece(this.bulletWithSamePos2, 
//      new ConsLoGamePiece(this.shipWithSamePos2, this.emptyILGP));
//  ILoGamePiece spILGPWithExtraShip = new ConsLoGamePiece(this.bulletWithSamePos2, 
//      new ConsLoGamePiece(this.shipWithSamePos2, this.onlyShipILGP));
//  ILoGamePiece spILGPWithCollidingBulletWithRemoved = new ConsLoGamePiece(this.bulletWithSamePos2, 
//      new ConsLoGamePiece(this.shipWithSamePos2, new ConsLoGamePiece(this.bulletWithSamePos1, this.emptyILGP)));
//  ILoGamePiece oneCollILGP = new ConsLoGamePiece(this.shipWithSamePos1, 
//      new ConsLoGamePiece(this.bOnScreen1,
//          new ConsLoGamePiece(this.bulletWithSamePos1, this.emptyILGP)));
//  ILoGamePiece oneCollILGPBulletFirst = new ConsLoGamePiece(this.bulletWithSamePos1, 
//      new ConsLoGamePiece(this.bOnScreen1,
//          new ConsLoGamePiece(this.shipWithSamePos1, this.emptyILGP)));
//  ILoGamePiece twoBulletsOneColl = new ConsLoGamePiece(this.bulletWithSamePos1, this.oneCollILGP);
//  ILoGamePiece twoCollILGP = new ConsLoGamePiece(this.shipWithSamePos1, this.oneCollILGP);
//  ILoGamePiece twoSeperateCollILGP = new ConsLoGamePiece(this.shipWithSamePos2,
//      new ConsLoGamePiece(this.bulletWithSamePos2, this.twoCollILGP));

  //test areAllShips method
  boolean testAreAllShips(Tester t) {
    return t.checkExpect(this.ILGP1.areAllShips(), false)
        && t.checkExpect(this.ILGP2.areAllShips(), false)
        && t.checkExpect(this.onlyShipILGP.areAllShips(), true)
        && t.checkExpect(this.emptyILGP.areAllShips(), true);
  }
  
  //test anyCollision method
  boolean testAnyCollision(Tester t) {
    return t.checkExpect(this.ILGP1.anyCollision(this.bulletAtStart), false)
        && t.checkExpect(this.ILGP2.anyCollision(this.shipOnScreen1Left), true)
        && t.checkExpect(this.ILGP2.anyCollision(new Ship(70.0, new MyPosn(250, 300))), true)
        && t.checkExpect(this.ILGP2.anyCollision(this.bOnScreen1), true)
        && t.checkExpect(this.onlyShipILGP.anyCollision(this.shipOnScreen1Right), false)
        && t.checkExpect(this.emptyILGP.anyCollision(this.shipOnScreen1Right), false)
        && t.checkExpect((new ConsLoGamePiece(this.shipOnScreen1Right, this.ILGP2)).anyCollision(this.bOnScreen1), true);
  }

  //test shootBullet() method
  boolean testShootBullet(Tester t) {
    return t.checkExpect(this.emptyILGP.shootBullet(), this.ILGPWithOneBulletShot)
        && t.checkExpect(this.ILGPWithOneBulletShot.shootBullet(), new ConsLoGamePiece(
            this.bulletAtStart, this.ILGPWithOneBulletShot))
        && t.checkExpect(this.shipsSpawnedNoBullets.shootBullet(), new ConsLoGamePiece(
            this.bulletAtStart, this.shipsSpawnedNoBullets));
  }

  //test moveILGP method
  boolean testMoveILGP(Tester t) {
    return t.checkExpect(this.ILGP1.moveILGP(), new ConsLoGamePiece(
        new Bullet(2, 270.0, new MyPosn(250, 292), 0), new ConsLoGamePiece(new Ship(180.0, 
            new MyPosn(416, 200)), this.emptyILGP)))
        && t.checkExpect(this.ILGP2.moveILGP(), new ConsLoGamePiece(new Bullet(
            10, 180.0, new MyPosn(412, 200), 2), this.movedILGP1))
        && t.checkExpect(this.emptyILGP.moveILGP(), this.emptyILGP);
  }

  //test spawnShips method
  boolean testSpawnShips(Tester t) {
    return t.checkExpect(emptyILGP.spawnShips(0, seededThree), new ConsLoGamePiece(
        new Ship(0.0, new MyPosn(0, 132)),
        new ConsLoGamePiece(new Ship(0.0, new MyPosn(0, 210)),
            new MtLoGamePiece())))
        && t.checkExpect(emptyILGP.spawnShips(1, seededThree), new MtLoGamePiece())
        && t.checkExpect(ILGPWithOneBulletShot.spawnShips(10, seededOne), ILGPWithOneBulletShot)
        && t.checkExpect(ILGPWithOneBulletShot.spawnShips(0, seededThree), 
            new ConsLoGamePiece(new Ship(180.0, new MyPosn(500, 223)), ILGPWithOneBulletShot))
        && t.checkExpect(shipsSpawnedNoBullets.spawnShips(0, seededThree), 
            new ConsLoGamePiece(new Ship(180.0, new MyPosn(500, 243)),
                new ConsLoGamePiece(new Ship(180.0, new MyPosn(500, 48)),
                    shipsSpawnedNoBullets))); 
  }
  
  //test append method
  boolean testAppend(Tester t) {
    return t.checkExpect(this.spILGP1.append(this.spILGP2), 
        new ConsLoGamePiece(this.shipWithSamePos1,
            new ConsLoGamePiece(this.bulletWithSamePos1,
                this.spILGP2)))
        && t.checkExpect(this.emptyILGP.append(this.spILGP2), 
            this.spILGP2)
        && t.checkExpect(this.spILGP1.append(this.emptyILGP),
            new ConsLoGamePiece(this.shipWithSamePos1,
                new ConsLoGamePiece(this.bulletWithSamePos1, this.emptyILGP)));
  }
 
  //IGAMEPIECE TESTS*****************************************************************************
  //test drawGP method
  boolean testDrawGP(Tester t) {
    return t.checkExpect(bulletForDrawing.drawGP(emptyWorld), worldWithOneBullet)
        && t.checkExpect(shipForDrawing.drawGP(worldWithOneBullet), worldWithBulletAndShip)
        && t.checkExpect(bulletForDrawing2.drawGP(worldWithBulletAndShip), bullet2OnScene)
        && t.checkExpect(shipForDrawing2.drawGP(bullet2OnScene), allPiecesOnScene);
  }


  //test offScreenGP method
  boolean testOffScreenGP(Tester t) { 
    return t.checkExpect(this.bOnScreen1.offScreenGP(), false)
        && t.checkExpect(this.bOnScreen2.offScreenGP(), false)
        && t.checkExpect(this.shipOnScreen1Left.offScreenGP(), false)
        && t.checkExpect(this.bulletAtStart.offScreenGP(), false)
        && t.checkExpect(this.bOffScreen.offScreenGP(), true)
        && t.checkExpect(this.shipOffScreen1.offScreenGP(), true);
  }
  
  //test moveGP method
  boolean testMoveGP(Tester t) {
    return t.checkExpect(this.bOnScreen1.moveGP(), new Bullet(10, 180.0, new MyPosn(412, 200), 2))
        && t.checkExpect(this.bOnScreen2.moveGP(), new Bullet(10, 0.0, new MyPosn(428, 200), 2))
        && t.checkExpect(this.shipOnScreen1Left.moveGP(), new Ship(180.0, new MyPosn(416, 200)))
        && t.checkExpect(this.bulletAtStart.moveGP(), 
            new Bullet(2, 270.0, new MyPosn(250, 292), 0));
  }

  //test isCollision method
  boolean testIsCollision(Tester t) {
    return t.checkExpect(this.bulletWithSamePos1.isCollision(this.bulletWithSamePos2), false)
        && t.checkExpect(this.bulletWithSamePos2.isCollision(this.bulletWithSamePos1), false)
        && t.checkExpect(this.shipWithSamePos1.isCollision(this.shipWithSamePos2), false)
        && t.checkExpect(this.shipWithSamePos2.isCollision(this.shipWithSamePos1), false)
        //&& t.checkExpect(this.bulletWithSamePos1.isCollision(this.shipWithSamePos1), true)
        && t.checkExpect(this.shipWithSamePos1.isCollision(bulletWithSamePos2), true);
  }

  //test addBullets method in GP class
  boolean testAddBulletsGP(Tester t) {
    return t.checkExpect(this.bulletWithSamePos1.addBullets(this.onlyShipILGP, 3), 
        new ConsLoGamePiece(new Bullet(5, 240.0, new MyPosn(54,23), 2),
            new ConsLoGamePiece(new Bullet(5, 120.0, new MyPosn(54,23), 2),
                new ConsLoGamePiece(new Bullet(5, 0.0, new MyPosn(54,23), 2),   
                    this.onlyShipILGP))))
        && t.checkExpect(this.bulletWithSamePos2.addBullets(this.onlyShipILGP, 6),
            new ConsLoGamePiece(new Bullet(10, 300.0, new MyPosn(54,23), 5),
                new ConsLoGamePiece(new Bullet(10, 240.0, new MyPosn(54,23), 5),
                    new ConsLoGamePiece(new Bullet(10, 180.0, new MyPosn(54,23), 5),
                        new ConsLoGamePiece(new Bullet(10, 120.0, new MyPosn(54,23), 5),
                            new ConsLoGamePiece(new Bullet(10, 60.0, new MyPosn(54,23), 5),
                                new ConsLoGamePiece(new Bullet(10, 0.0, new MyPosn(54,23), 5), 
                                    this.onlyShipILGP)))))))
        && t.checkExpect(this.shipOffScreen1.addBullets(this.onlyShipILGP, 7), this.onlyShipILGP);
  }
  
  //test isBulletWithOverlap() method
  boolean testIsBulletWithOverlap(Tester t) {
    return t.checkExpect(shipOnScreen1Right.isBulletWithOverlap(new MyPosn(0, 0), 1), false)
        && t.checkExpect(bulletOverlap1.isBulletWithOverlap(new MyPosn(1,  2), 2), true)
        && t.checkExpect(bulletNoOverlap1.isBulletWithOverlap(new MyPosn(1,  1), 4), false);
  }

  // test isShipWithOverlap() method
  boolean testIsShipWithOverlap(Tester t) {
    return t.checkExpect(bulletOverlap1.isShipWithOverlap(new MyPosn(0, 0), 1), false)
        && t.checkExpect(shipOverlap1.isShipWithOverlap(new MyPosn(1,  2), 4), true)
        && t.checkExpect(shipNoOverlap1.isShipWithOverlap(new MyPosn(1, 5), 3), false);
  }

  //test overlap(MyPosn positionOther, int radiusOther) method
  boolean testOverlap(Tester t) {
    return t.checkExpect(bulletOverlap1.overlap(new MyPosn(1, 2), 2), true)
        && t.checkExpect(bulletNoOverlap1.overlap(new MyPosn(1,  0),  1), false)
        && t.checkExpect(shipOverlap1.overlap(new MyPosn(1,  1), 1), true)
        && t.checkExpect(shipNoOverlap1.overlap(new MyPosn(0, 0), 3), false);
  }
  
  //test isShip method
  boolean testIsShip(Tester t) {
    return t.checkExpect(this.bulletAtStart.isShip(), false)
        && t.checkExpect(this.shipOnScreen2Left.isShip(), true);     
  }
  
  //test findCount method
  boolean testFindCount(Tester t) {
    return t.checkExpect(this.bulletAtStart.findCount(), 2)
        && t.checkExpect(this.bOnScreen2.findCount(), 4)
        && t.checkExpect(this.shipWithSamePos1.findCount(), 0);
  }

  //POSN CLASS TESTS*****************************************************************************
  MyPosn movePosnTester1 = new MyPosn(10, 10);
  MyPosn movePosnTester2 = new MyPosn(0, 0);
  boolean testMovePosn(Tester t) {
    return t.checkExpect(movePosnTester1.movePosn(0.0, 10), new MyPosn(20, 10))
        && t.checkExpect(movePosnTester1.movePosn(180.0, 10), new MyPosn(0, 10))
        && t.checkExpect(movePosnTester1.movePosn(45.0, 10), new MyPosn(17, 17))
        && t.checkExpect(movePosnTester1.movePosn(135.0, 10), new MyPosn(2, 17))
        && t.checkExpect(movePosnTester2.movePosn(135.0, 10), new MyPosn(-7, 7))
        && t.checkExpect(movePosnTester2.movePosn(0.0, 1), new MyPosn(1, 0))
        && t.checkExpect(movePosnTester2.movePosn(180, 10), new MyPosn(-10, 0))
        && t.checkExpect(movePosnTester2.movePosn(135.0, 10), new MyPosn(-7, 7))
        && t.checkExpect(movePosnTester2.movePosn(225.0, 10), new MyPosn(-7, -7))
        && t.checkExpect(movePosnTester2.movePosn(315.0, 10), new MyPosn(7, -7))
        && t.checkExpect(movePosnTester2.movePosn(45.0, 10), new MyPosn(7, 7))
        && t.checkExpect(this.onScreenPosn1.movePosn(180.0, 8), new MyPosn(412, 200));
  }

  //tests placeGP method
  boolean testPlaceGP(Tester t) {
    return t.checkExpect(this.shootStartPosn.placeGP(
        this.bulletCir, Constant.WORLD_SCENE), Constant.WORLD_SCENE.placeImageXY(
            this.bulletCir, Constant.SCREEN_WIDTH / 2, Constant.SCREEN_HEIGHT))
        && t.checkExpect(this.shootStartPosn.placeGP(
            this.shipCir, Constant.WORLD_SCENE), Constant.WORLD_SCENE.placeImageXY(
                this.shipCir, Constant.SCREEN_WIDTH / 2, Constant.SCREEN_HEIGHT));
  }

  //tests offScreen method
  boolean testOffScreenPosn(Tester t) {
    return t.checkExpect(this.offScreenPosn1.offScreenPosn(), true)
        && t.checkExpect(this.offScreenPosn2.offScreenPosn(), true)
        && t.checkExpect(this.onScreenPosn1.offScreenPosn(), false)
        && t.checkExpect(this.onScreenPosn2.offScreenPosn(), false)
        && t.checkExpect((new MyPosn(-1,0)).offScreenPosn(), true)
        && t.checkExpect((new MyPosn(0,0)).offScreenPosn(), false)
        && t.checkExpect((new MyPosn(0,-1)).offScreenPosn(), true)
        && t.checkExpect((new MyPosn(500,300)).offScreenPosn(), false)
        && t.checkExpect((new MyPosn(501,301)).offScreenPosn(), true);
  }
  
  boolean testBigBang(Tester t) {
    NBullets w = new NBullets(10);
    int worldWidth = Constant.SCREEN_WIDTH;
    int worldHeight = Constant.SCREEN_HEIGHT;
    double tickRate = Constant.TICK_RATE;
    return w.bigBang(worldWidth, worldHeight, tickRate);
  }
}