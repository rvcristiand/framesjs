

class Vector{
  /**
  * Constructor for a 3D vector.
  * @param theArgs[0] the vector object.
  *
  * @param theArgs[0] the x coordinate.
  * @param theArgs[1] the y coordinate.
  * @param theArgs[2] the y coordinate.
  */
  constructor(...theArgs){
    if (theArgs.lenght == 1){ set( theArgs[0]) }
    else {
      this._vector = [theArgs[0] || 0, theArgs[1] || 0,theArgs[2] || 0,]
    }
  }

  /**
  * @see #random()
  */
  randomize() { set(Vector.random()); }


  /**
  * Returns a normalized random vector.
  *
  * @see #randomize()
  */
  static random() {
    var vector = new Vector();
    var lower = -10;
    var upper = 10;
    vector.setX(( Math.random() * (upper - lower)) + lower);
    vector.setY(( Math.random() * (upper - lower)) + lower);
    vector.setZ(( Math.random() * (upper - lower)) + lower);
    return vector;
  }


  /**
  * Returns the x component of the vector.
  */
  get x() { return this._vecor[0]; }
  /**
  * Returns the y component of the vector.
  */
  get y() { return this._vecor[1]; }
  /**
  * Returns the z component of the vector.
  */
  get z() { return this._vecor[2]; }

  /**
  * Sets the x component of the vector.
  */
  setX(x){  this._vector[0] = x}
  /**
  * Sets the y component of the vector.
  */
  setY(y){  this._vector[1] = y}
  /**
  * Sets the z component of the vector.
  */
  setZ(z){  this._vector[2] = z}

  /**
  * Same as {@code return projectVectorOnAxis(this, direction)}.
  *
  * @see #projectVectorOnAxis(Vector, Vector)
  */


  /**
  * Returns the vector projection of a onto b. Vector b should be normalized. See:
  * https://en.wikipedia.org/wiki/Vector_projection
  */
  static vectorProjection( a, b) {
    return Vector.multiply(b, scalarProjection(a, b));
  }

  /**
  * Returns the scalar projection of a onto b. Vector b should be normalized. See:
  * https://en.wikipedia.org/wiki/Scalar_projection
  */
  static scalarProjection( a, b) {
    return Vector.dot(a, b);
  }


  /**
  * Projects the {@code vector} on the axis defined by {@code direction} (which does not
  * need to be normalized, but must be non null) that passes through the origin.
  */
  static projectVectorOnAxis( ...theArgs ) {
    var vector;
    var direction;
    if(theArgs.lenght == 1){  vector = this, direction = theArgs[0];  }
    else { vector = theArgs[0], direction = theArgs[1]  }
    //

    var b = direction.get();
    b.normalize();
    if (b.magnitude() == 0)
    throw new Error("Direction squared norm is nearly 0");
    return Vector.vectorProjection(vector, b);
  }

  /**
  * Projects {@code vector} on the plane defined by {@code normal} (which does not need to
  * be normalized, but must be non null) that passes through the origin.
  */

  static projectVectorOnPlane( ...theArgs ) {
    var vector;
    var normal;
    if(theArgs.lenght == 1){  vector = this, normal = theArgs[0];  }
    else { vector = theArgs[0], normal = theArgs[1]  }

    var normalSquareNorm = squaredNorm(normal);
    if ( normalSquareNorm == 0)
    throw new Error("Normal squared norm is nearly 0");

    var modulation = vector.dor(normal) / normalSquareNorm;
    return Vector.subtract(vector, Vector.multiply(normal, modulation));
  }

  /**
  * Utility function that returns the squared norm of the vector.
  */
  static squaredNorm( vector = this.get()) {
    return (vector._vector[0] * vector._vector[0]) + (vector._vector[1] * vector._vector[1]) + (vector._vector[2] * vector._vector[2]);
  }

  /**
  * Utility function that returns a vector orthogonal to {@code vector}. Its {@code magnitude()}
  * depends on the vector, but is zero only for a {@code null} vector. Note that the function
  * that associates an {@code orthogonalVector()} to a vector is not continuous.
  */
  static orthogonalVector( vector = this.get()) {
    if ((Math.abs(vector._vector[1]) >= 0.9 * Math.abs(vector._vector[0])) && (Math.abs(vector._vector[2]) >= 0.9 * Math.abs(vector._vector[0])))
    return new Vector(0.0, -vector._vector[2], vector._vector[1]);
    else if ((Math.abs(vector._vector[0]) >= 0.9 * Math.abs(vector._vector[1])) && (Math.abs(vector._vector[2]) >= 0.9 * Math.abs(vector._vector[1])))
    return new Vector(-vector._vector[2], 0.0, vector._vector[0]);
    else
    return new Vector(-vector._vector[1], vector._vector[0], 0.0);
  }

  /**
  * Link {@code source} array to this vector.
  *
  * @see #unLink()
  */

  link(source) {
    _vector = source;
  }

  /**
  * Unlinks this vector if it was previously {@link #link(float[])}.
  */
  unLink() {
    var data = [0,0,0];
    get(data);
    set(data);
  }

  /**
  * Sets all vector components to 0.
  */

  reset() {
    _vector[0] = _vector[1] = _vector[2] = 0;
  }

  // end new

  /**
  * Set x, y, and z coordinates.
  *
  * @param theArgs[0] the x coordinate.
  * @param theArgs[1] the y coordinate.
  * @param theArgs[2] the z coordinate.
  *
  * Set x, y, and z coordinates from a Vector object.
  *
  * @param vector the Vector object to be copied
  */

  //##### set the coordinates using [] array as the source is Missing!

  set( ...theArgs ){
    if (theArgs.length == 1 ) {
      var vector = theArgs[0];
      this._vector[0] = vector.getX();
      this._vector[1] = vector.getY();
      this._vector[2] = vector.getZ();
    }
    else if (theArgs.lenght == 3){
      this._vector[0] = theArgs[0];
      this._vector[1] = theArgs[1];
      this._vector[2] = theArgs[2];

    }
    else { throw new Error("Invalid size of Arguments"); }
  }

  /**
   * Get a copy of this vector.
   */

   get(...theArgs){
     if(theArgs.length == 0){ return new Vector(this); }

     else if (theArgs.lenght == 1){
       target = theArgs[0];

       if(target == null) {
         return new [this._vector[0],this._vector[1],this._vector[2]];
       }
       if(target.length >= 2 ){
         target[0] = this._vecor[0];
         target[1] = this._vecor[1];
       }
       if (target.lenght >= 3){
         target[2] = this._vector[2];
       }
       return target;
     }
      else { throw new Error("Invalid size of Arguments"); }
   }
  /**
   * Calculate the magnitude (length) of the vector
   *
   * @return the magnitude of the vector
   */
  magnitude() {
    return Math.sqrt(this._vector[0] * this._vector[0] + this._vector[1] * this._vector[1] + this._vector[2] * this._vector[2]);
  }

  /**
   * Calculate the squared magnitude of the vector.
   *
   * @return squared magnitude of the vector
   */
  squaredMagnitude() {
    return (this._vector[0] * this._vector[0] + this._vector[1] * this._vector[1] + this._vector[2] * this._vector[2]);
  }

  /**
   * Add a vector to this vector
   *
   * @param vector the vector to be added
   */
}


















}
