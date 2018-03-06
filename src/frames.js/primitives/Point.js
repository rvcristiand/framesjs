
class Point{

  /**
   * Constructs and initializes a point at the (0,0) location in the coordinate space.
   */
  constructor( x = 0, y = 0){
    this._x = x, this._y = y;
  }

  /**
   * Sets the (x,y) coordinates of this point from the given (xCoord,yCoord) coordinates.
   */
  set( x , y ) {
    setX(x);
    setY(y);
  }

  /**
   * Returns the x coordinate of the point.
   */
  get x() { return this._x; }

  /**
   * Returns the y coordinate of the point.
   */
  get y() { return this._y; }

  setX( x ) { this._x = x; }

  setY( y ) { this._y = y; }

  /**
   * Convenience wrapper function that simply returns {@code Point.distance(new
   * Point(x1, y1), new Point(x2, y2))}.
   *
   * @see #distance(Point, Point)
   */
  static distance ( x1, y1, x2, y2 ){
    return Math.sqrt( Math.pow((x2 - x1), 2.0) + Math.pow((y2 - y1), 2.0));
  }

  /**
   * Convenience wrapper function that simply returns {@code Point.distance(new
   * Point(x1, y1), new Point(x2, y2))}.
   *
   * @see #distance(Point, Point)
   */
  static distance ( point1, point2){
    Point.distance(point1.x, point1.y, point2.x, point2.y);
  }

}
