

class Vector {
  /**
	 * Constructor for a 3D vector.
	 * 
	 * @param theArgs[0]
	 *            the vector object.
	 * 
	 * @param theArgs[0]
	 *            the x coordinate.
	 * @param theArgs[1]
	 *            the y coordinate.
	 * @param theArgs[2]
	 *            the y coordinate.
	 */
  constructor(...theArgs){
    if (theArgs.lenght == 1){ set( theArgs[0]) }
    else {
      this._vector = [theArgs[0] || 0, theArgs[1] || 0,theArgs[2] || 0]
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
  getX() { return this._vector[0]; }
  /**
	 * Returns the y component of the vector.
	 */
  getY() { return this._vector[1]; }
  /**
	 * Returns the z component of the vector.
	 */
  getZ() { return this._vector[2]; }

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
	 * Returns the vector projection of a onto b. Vector b should be normalized.
	 * See: https://en.wikipedia.org/wiki/Vector_projection
	 */
  static vectorProjection( a, b) {
    return Vector.multiply(b, scalarProjection(a, b));
  }

  /**
	 * Returns the scalar projection of a onto b. Vector b should be normalized.
	 * See: https://en.wikipedia.org/wiki/Scalar_projection
	 */
  static scalarProjection( a, b) {
    return Vector.dot(a, b);
  }


  /**
	 * Projects the {@code vector} on the axis defined by {@code direction}
	 * (which does not need to be normalized, but must be non null) that passes
	 * through the origin.
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
	 * Projects {@code vector} on the plane defined by {@code normal} (which
	 * does not need to be normalized, but must be non null) that passes through
	 * the origin.
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
	 * Utility function that returns a vector orthogonal to {@code vector}. Its
	 * {@code magnitude()} depends on the vector, but is zero only for a
	 * {@code null} vector. Note that the function that associates an
	 * {@code orthogonalVector()} to a vector is not continuous.
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
	 * @param theArgs[0]
	 *            the x coordinate.
	 * @param theArgs[1]
	 *            the y coordinate.
	 * @param theArgs[2]
	 *            the z coordinate.
	 * 
	 * Set x, y, and z coordinates from a Vector object.
	 * 
	 * @param vector
	 *            the Vector object to be copied
	 */

  // ##### set the coordinates using [] array as the source is Missing!

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
	 * Get a copy of this vector. // P5.Vector() implements copy instead of
	 * get()
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

  /*
	 * Add a vector to this vector
	 * 
	 * @param vector the vector to be added
	 */

  add(x, y, z){
    if(x instanceof Vector){
      this._vector[0] += x._vector[0] || 0;
      this._vector[1] += x._vector[1] || 0;
      this._vector[2] += x._vector[2] || 0;
      return this;
    }
    if(x instanceof Array){
      this._vector[0] += x[0] || 0;
      this._vector[1] += x[1] || 0;
      this._vector[2] += x[2] || 0;
      return this;
    }
    this._vecor[0] +=x || 0;
    this._vecor[1] +=y || 0;
    this._vecor[2] +=z || 0;
    return this;
  }
  /**
	 * Add two vectors.
	 * 
	 * @param vector1
	 *            a vector
	 * @param vector2
	 *            another vector
	 * @return 3a new vector that is the sum of vector1 and vector2
	 */
  /**
	 * Add two vectors.
	 * 
	 * @param vector1
	 *            a vector
	 * @param vector2
	 *            another vector
	 * @return a new vector that is the sum of vector1 and vector2
	 * 
	 * Add two vectors into a target vector.
	 * 
	 * @param vector1
	 *            a vector
	 * @param vector2
	 *            another vector
	 * @param target
	 *            copy target vector (if null, a new vector will be created)
	 * @return a new vector that is the sum of vector1 and vector2
	 */

  static add(vector1 , vector2, target){
    if(!target){ targer = vertor1.get(); }
    else{
      target.set(v1);
    }
    target.add(v2);
    return target;
  };


  /**
	 * Subtract a vector from this vector.
	 * 
	 * @param vector
	 *            the vector to be subtracted
	 */
  sub(x, y, z){
    if(x instanceof Vector){
      this._vector[0] -= x._vector[0] || 0;
      this._vector[1] -= x._vector[1] || 0;
      this._vector[2] -= x._vector[2] || 0;
      return this;
    }
    if(x instanceof Array){
      this._vector[0] -= x[0] || 0;
      this._vector[1] -= x[1] || 0;
      this._vector[2] -= x[2] || 0;
      return this;
    }
    this._vecor[0] -= x || 0;
    this._vecor[1] -= y || 0;
    this._vecor[2] -= z || 0;
    return this;
  }

  /**
	 * Subtract one vector from another.
	 * 
	 * @param vector1
	 *            a vector
	 * @param vector2
	 *            another vector
	 * @return a new vector that is vector1 - vector2
	 * 
	 * Subtract one vector from another and store in another vector.
	 * 
	 * @param vector1
	 *            the x, y, and z components of a Vector object
	 * @param vector2
	 *            the x, y, and z components of a Vector object
	 * @param target
	 *            Vector in which to store the result
	 */

  static sub(vector1, vector2, target=null) {
    if (!target) {
      target = vector1.get();
    } else {
      target.set(v1);
    }
    target.sub(v2);
    return target;
  };

  /**
	 * Multiply this vector by a scalar.
	 * 
	 * @param n
	 *            the value to multiply by
	 */
  
 multiply( n ){
	 this._vector[0] *= n;
	 this._vector[1] *= n;
	 this._vector[2] *= n;
	 
 }; 
 
 
 /**
	 * Multiply a vector by a scalar, and write the result into a target vector.
	 * 
	 * @param vector
	 *            a vector
	 * @param n
	 *            scalar
	 * @param target
	 *            Vector to store the result
	 * @return the target vector, now set to vector * n
	 */
 
 static multiply(vector, n, target=null){
	 if(!target)  {
		 target = new Vector(vector._vector[0] * n, vector._vector[1] * n, vector._vector[2] * n);
	 }else {
	      target.set(vector._vector[0] * n, vector._vector[1] * n, vector._vector[2] * n);
	 }
	 return target; 
	 
 }
 
 /**
	 * Divide this vector by a scalar.
	 * 
	 * @param n
	 *            the value to divide by
	 */
 
 divide (n ){ 
	 this._vector[0] /= n; 
	 this._vector[1] /= n; 
	 this._vector[2] /= n; 
 }
 
 /**
	 * Divide a vector by a scalar and return the result in a new vector.
	 * 
	 * @param vector
	 *            a vector
	 * @param n
	 *            scalar
	 * @return a new vector that is vector / n
	 * @param target
	 *            Vector in which to store the result
	 */
 
 static divide(vector, float, target = null) {
	 if (target == null) {
	      target = new Vector(vector._vector[0] / n, vector._vector[1] / n, vector._vector[2] / n);
	    } else {
	      target.set(vector._vector[0] / n, vector._vector[1] / n, vector._vector[2] / n);
	    }
	    return target;
	 
 }
 
 
 /**
	 * Calculate the Euclidean distance between two points (considering a point
	 * as a vector object).
	 * 
	 * @param vector
	 *            another vector
	 * @return the Euclidean distance between
	 */
 
 distance (vector) {
	 var dx = this._vector[0] - vector._vector[0]; 
	 var dy = this._vector[1] - vector._vector[1]; 
	 var dz = this._vector[2] - vector._vector[2];
	 return Math.sqrt( dx*dx + dy*dy + dz*dz ); 
	 
 }
 
 /**
	 * Calculate the Euclidean distance between two points (considering a point
	 * as a vector object).
	 * 
	 * @param vector1
	 *            a vector
	 * @param vector2
	 *            another vector
	 * @return the Euclidean distance between vector1 and vector2
	 */
 
 static distance( vector1, vector2){
	 var dx = vector1._vector[0] - vector2._vector[0]; 
	 var dy = vector1._vector[1] - vector2._vector[1]; 
	 var dz = vector1._vector[2] - vector2._vector[2];
	 return Math.sqrt( dx*dx + dy*dy + dz*dz ); 
 }
	 
 /**
	 * Calculate the dot product with another vector.
	 * 
	 * @return the dot product
	 * 
	 * @param vector
	 *            x component of the vector
	 * @param y
	 *            y component of the vector
	 * @param z
	 *            z component of the vector
	 */
 
 dot(vector, y, z) {
	 if (vector instanceof Vector) {  
		 return this._vector[0] * vector._vector[0] + this._vector[1] * vector._vector[1] + this._vector[2] * vector._vector[2];
	 }
	 else{
		 return this._vector[0] * x + this._vector[1] * y + this._vector[2] * z;
	 }
 }
 
 /**
	 * @param vector1
	 *            any variable of type Vector
	 * @param vector2
	 *            any variable of type Vector
	 */
 
 static dot( vector1, vector2){
	 return vector1._vector[0] * vector2._vector[0] + vector1._vector[1] * vector2._vector[1] + vector1._vector[2] * vector2._vector[2];	  
 }
 
 /**
	 * Perform cross product between this and another vector, and store the
	 * result in 'target'. If target is null, a new vector is created.
	 */
 
 cross( vector, target = null ){
 	var crossX = this._vector[1] * vector._vector[2] - vector._vector[1] * this._vector[2];
 	var crossY = this._vector[2] * vector._vector[0] - vector._vector[2] * this._vector[0];
 	var crossZ = this._vector[0] * vector._vector[1] - vector._vector[0] * this._vector[1];
 	
 	if (target == null){
 		target = new Vector(crossX, crossY, crossZ);
 	} else {
 		target.set(CrossX, crossY, crossZ);
 	}
	
 	return target;
 }
 
 /**
	 * Cross product: target = vector1 * vector2.
	 * 
	 * @param vector1
	 *            any variable of type Vector
	 * @param vector2
	 *            any variable of type Vector
	 * @param target
	 *            Vector to store the result
	 */
 
 static cross ( vector1, vector2, target) {
	 var crossX = vector1._vector[1] * vector2._vector[2] - vector2._vector[1] * vector1._vector[2];
	 var crossY = vector1._vector[2] * vector2._vector[0] - vector2._vector[2] * vector1._vector[0];
	 var crossZ = vector1._vector[0] * vector2._vector[1] - vector2._vector[0] * vector1._vector[1];

	 if(target = null) {
		 target = new Vector(crossX, crossY, crossZ); 
	 } else {
		 target.set(crossX, crossY, crossZ);
	 }
	 return target; 
 }
 
 /**
	 * Normalize the vector to length 1 (make it a unit vector).
	 */
 
 normalize(...theArgs){
	 if (theArgs.lenght == 0) {
		 var m = magnitude(); 
		 if ( m != 0 && m != 1 ){
			 divide(m);
		 }
	 }
	 if (theArgs.lenght == 1){
		 var target = theArgs[0]; 
		 if (target == null) { target = new Vector(); } 
		 
		 var m = magnitude(); 
		 if( m > 0) { 
			 target.set(this._vector[0] / m, this._vector[1] / m, this._vector[2] / m);
		 } else {
			 target.set(this._vector[0], this._vector[1], this._vector[2]);
		 }
		 
		 return target;  
	 }
 }
 
 /**
	 * Limit the magnitude of this vector.
	 * 
	 * @param maximum
	 *            the maximum length to limit this vector
	 */
 
 limit ( maximum ){
	 if ( magnitude() > maximum) {
		 normalize(); 
		 multiply(maximum);
	 }
 }
 
 /**
	 * if target == null Sets the magnitude of the vector to an arbitrary
	 * amount.
	 * 
	 * @param magnitude
	 *            the new length for this vector
	 */
 
 /**
	 * target Sets the magnitude of this vector, storing the result in another
	 * vector.
	 * 
	 * @param target
	 *            Set to null to create a new vector
	 * @param magnitude
	 *            the new length for the new vector
	 * @return a new vector (if target was null), or target
	 */
 
 setMagnitude (magnitude, target = null){
	 
	 if(target == null){
		 normalize();
		 multiply(magnitude);
	 }
	 else {
		 target = normalize(target);
		 target.multiply(magnitude);
		 return target; 
	 }
 }
 
 /**
	 * Calculate the angle of rotation for this vector (only 2D vectors).
	 * 
	 * @return the angle of rotation
	 */
 
 heading() {
	 var angle =  Math.atan2( -this._vector[1], this._vector[0]);
	 return -1 * angle;
 }
 
 /**
	 * Rotate the vector by an angle (only 2D vectors), magnitude remains the
	 * same.
	 * 
	 * @param theta
	 *            the angle of rotation
	 * 
	 */
 
 rotate (theta) {
	 var xTemp = this._vector[0]; 
	 // Might need to check for rounding errors like with angleBetween
		// function?
	 this._vector[0] = this._vector[0] * Math.cos(theta) - this._vector[1] * Math.sin(theta);
	 this._vector[1] = xTemp * Math.sin(theta) + this._vector[1] * Math.cos(theta);
 }

 /**
	 * Calculates a number between two numbers at a specific increment. The
	 * {@code amount} parameter is the amount to interpolate between the two
	 * values where 0.0 equal to the first point, 0.1 is very near the first
	 * point, 0.5 is half-way in between, etc.
	 */
 
 static lerp ( start, stop, amount){
	 return start + (stop - start) * amount;
 }
 
 /**
	 * if vector2 == null therArgs.lenght = 2 Linear interpolate the vector to
	 * another vector.
	 * 
	 * @param vector
	 *            the vector to lerp to
	 * @param amount
	 *            The amt parameter is the amount to interpolate between the two
	 *            vectors where 1.0 equal to the new vector 0.1 is very near the
	 *            new vector, 0.5 is half-way in between.
	 */
 

 /**
	 * Linear interpolate between two vectors (returns a new Vector object).
	 * 
	 * @param vector1
	 *            the vector to start from
	 * @param vector2
	 *            the vector to lerp to
	 */
 
 
 /**
	 * Linear interpolate the vector to x,y,z values.
	 * 
	 * @param x
	 *            the x component to lerp to
	 * @param y
	 *            the y component to lerp to
	 * @param z
	 *            the z component to lerp to
	 */
 lerp (...theArgs) {  // (vector, amount ) || (vector1, vector2, amount) || (x
						// ,y , z ,amount)
	
	 if(theArgs.lenght == 2){
		 var vector1 = theArgs[0];
		 var amount  = theArgs[1]; 
		 this._vector[0] = Vector1.lerp(this._vector[0], vector1._vector[0], amount);
		 this._vector[1] = Vector1.lerp(this._vector[1], vector1._vector[1], amount);
		 this._vector[2] = Vector1.lerp(this._vector[2], vector1._vector[2], amount);
	 }
	 else if(theArgs.lenght == 3) {
		 var vector1 = theArgs[0];
		 var vector2 = theArgs[1];
		 var amount  = theArgs[2];
		 var v = vector1.get();
		 v.lerp(vector2, amount);
		 return v; 
	 } else {
		 var x = theArgs[0];
		 var y = theArgs[1];
		 var z = theArgs[2];
		 var amount = theArgs[3];
		 
		 this._vector[0] = Vector.lerp(this.x(), x, amount);
		 this._vector[1] = Vector.lerp(this.y(), y, amount);
		 this._vector[2] = Vector.lerp(this.z(), z, amount); 
	 }
 }
 
 /**
	 * Calculate the angle between two vectors, using the dot product.
	 * 
	 * @param vector1
	 *            a vector
	 * @param vector2
	 *            another vector
	 * @return the angle between the vectors
	 */
 
 static angleBetween(vector1, vector2){
	 // We get NaN if we pass in a zero vector which can cause problems
	 // Zero seems like a reasonable angle between a 0 length vector and
	 // something else
	 
	 if ( vector1.magnitude() == 0) 
		 return 0.0; 
	 if ( vector2.magnitude() == 0)
		 return 0.0;
	 
	 var s = cross(vector1, vector2, null).magnitude();
	 var c = dot(vector1, vector2);
	 return Math.atan2(s, c);
 } 
 
 /**
	 * Make a new 2D unit vector from an angle.
	 * 
	 * @param angle
	 *            the angle
	 * @return the new unit PVec
	 */
 
 static fromAngle( angle, target = null ){
	 if(target == null){
		 target = new Vector( Math.cos(angle), Math.sin(angle), 0); 
	 } else {
		 target.set( Math.cos(angle), Math.sin(angle), 0 ); 
	 }
	 return target;
 }
 

 print(){
	 console.log( this.getX() + " " +  this.getY() + " " +  this.getZ() + "\n" ); 
 }
 
 toString() {
	 return "[ " + this._vetor[0] + ", " + this._vetor[1] + ", " + this._vetor[2] + " ]" ;  
 }
 
 
 

 
 
	 
}
