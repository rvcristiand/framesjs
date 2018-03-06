

 class Rectangle{


  /**
   * Constructs a new Rectangle whose upper-left corner is specified as (x,y) and whose
   * width and height are specified by the arguments of the same name.
   */
  constructor( x=0, y=0, width=0, height=0){
    this._x = x;
    this._y = y;
    this._width = width;
    this._height = height;
  }

  /**
   * Get a copy of this rectangle.
   */
  get Rectangle(){ return new Rectangle(this); }
  /**
   * @return x coordinate
   */
  get x(){ return this._x; }

  /**
   * @return y coordinate
   */
  get y(){ return this._y; }

  /**
   * @return width
   */
  get width(){ return this._width; }

  /**
   * @return height
   */
  get height(){ return this._height; }

  /**
   * Sets the x coordinate
   *
   * @param x
   */
  setX(x){ this._x = x ; }

  /**
   * Sets the y coordinate
   *
   * @param y
   */
  setY(y){ this._y = y ; }

  /**
   * @return width
   */
  setWidth(width){ this._width = width ; }

  /**
   * @return height
   */
  setHeight(height){ this._height = height ; }

  /**
   * Returns the X coordinate of the center of the rectangle.
   */
  centerX(){
    return (( this._x + this.width) / 2);
  }

  /**
   * Returns the Y coordinate of the center of the rectangle.
   */
  centerY(){
    return (( this._y + this.height) / 2);
  }

}
