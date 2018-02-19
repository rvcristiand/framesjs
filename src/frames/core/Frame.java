/**************************************************************************************
 * dandelion_tree
 * Copyright (c) 2014-2017 National University of Colombia, https://github.com/remixlab
 * @author Jean Pierre Charalambos, http://otrolado.info/
 *
 * All rights reserved. Library that eases the creation of interactive
 * scenes, released under the terms of the GNU Public License v3.0
 * which is available at http://www.gnu.org/licenses/gpl.html
 **************************************************************************************/

package frames.core;

import frames.core.constraint.Constraint;
import frames.core.constraint.WorldConstraint;
import frames.input.Agent;
import frames.input.Event;
import frames.input.Grabber;
import frames.input.InputHandler;
import frames.input.event.*;
import frames.primitives.*;
import frames.timing.TimingHandler;
import frames.timing.TimingTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A frame is a 2D or 3D coordinate system, represented by a {@link #position()}, an
 * {@link #orientation()} and {@link #magnitude()}. The order of these transformations is
 * important: the frame is first translated, then rotated around the new translated origin
 * and then scaled. This class API aims to conform that of the great
 * <a href="http://libqglviewer.com/refManual/classqglviewer_1_1Frame.html">libQGLViewer
 * Frame</a>, but it adds {@link #magnitude()} to it.
 * <h2>Geometry transformations</h2>
 * A frame is useful to define the position, orientation and magnitude of an object, using
 * its {@link #matrix()} method, as shown below:
 * <p>
 * {@code // Builds a frame at position (0.5,0,0) and oriented such that its Y axis is
 * along the (1,1,1)} direction<br>
 * {@code Frame frame = new Frame(new Vector(0.5,0,0), new Quaternion(new Vector(0,1,0),
 * new Vector(1,1,1)));} <br>
 * {@code graph.pushModelView();} <br>
 * {@code graph.applyModelView(frame.matrix());} <br>
 * {@code // Draw your object here, in the local frame coordinate system.} <br>
 * {@code graph.popModelView();} <br>
 * <p>
 * Many functions are provided to transform a point from one frame to another, see
 * {@link #coordinatesOf(Vector)}, {@link #inverseCoordinatesOf(Vector)},
 * {@link #coordinatesOfIn(Vector, Frame)}, {@link #coordinatesOfFrom(Vector, Frame)}...
 * <p>
 * You may also want to transform a vector (such as a normal), which corresponds to
 * applying only the rotational part of the frame transformation: see
 * {@link #transformOf(Vector)} and {@link #inverseTransformOf(Vector)}.
 * <p>
 * The {@link #translation()}, {@link #rotation()} and uniform positive {@link #scaling()}
 * that are encapsulated in a frame can also be used to represent an angle preserving
 * transformation of space. Such a transformation can also be interpreted as a change of
 * coordinate system, and the coordinate system conversion functions actually allow you to
 * use a frame as an angle preserving transformation. Use
 * {@link #inverseCoordinatesOf(Vector)} (resp. {@link #coordinatesOf(Vector)}) to apply
 * the transformation (resp. its inverse). Note the inversion.
 * <h2>Hierarchy of frames</h2>
 * The frame position, orientation and magnitude are actually defined with respect to
 * a {@link #reference()} frame. The default {@link #reference()} is the world
 * coordinate system (represented by a {@code null} {@link #reference()}). If you
 * {@link #setReference(Frame)} to a different frame, you must then differentiate:
 * <p>
 * <ul>
 * <li>The <b>local</b> {@link #translation()}, {@link #rotation()} and {@link #scaling()},
 * defined with respect to the {@link #reference()}.</li>
 * <li>the <b>global</b> {@link #position()}, {@link #orientation()} and
 * {@link #magnitude()}, always defined with respect to the world coordinate system.</li>
 * </ul>
 * <p>
 * A frame is actually defined by its {@link #translation()} with respect to its
 * {@link #reference()}, then by {@link #rotation()} of the coordinate system around
 * the new translated origin and then by a uniform positive {@link #scaling()} along its
 * rotated axes.
 * <p>
 * This terminology for <b>local</b> ({@link #translation()}, {@link #rotation()} and
 * {@link #scaling()}) and <b>global</b> ({@link #position()}, {@link #orientation()} and
 * {@link #magnitude()}) definitions is used in all the methods' names and should be
 * enough to prevent ambiguities. These notions are obviously identical when the
 * {@link #reference()} is {@code null}, i.e., when the frame is defined in the world
 * coordinate system (the one you are left with after calling a graph preDraw() method).
 * <p>
 * Frames can hence easily be organized in a tree hierarchy, which root is the world
 * coordinate system. A loop in the hierarchy would result in an inconsistent (multiple)
 * frame definition. Therefore {@link #settingAsReferenceWillCreateALoop(Frame)}
 * checks this and prevents {@link #reference()} from creating such a loop.
 * <p>
 * This frame hierarchy is used in methods like {@link #coordinatesOfIn(Vector, Frame)},
 * {@link #coordinatesOfFrom(Vector, Frame)} ... which allow coordinates (or vector)
 * conversions from a frame to any other one (including the world coordinate system).
 * <h2>Constraints</h2>
 * One interesting feature of a frame is that its displacements can be constrained. When a
 * {@link frames.core.constraint.Constraint} is attached to a frame, it filters
 * the input of {@link #translate(Vector)} and {@link #rotate(Quaternion)}, and only the
 * resulting filtered motion is applied to the frame. The default {@link #constraint()}
 * is {@code null} resulting in no filtering. Use {@link #setConstraint(Constraint)} to
 * attach a constraint to a frame.
 * <p>
 * Classical constraints are provided for convenience (see
 * {@link frames.core.constraint.LocalConstraint},
 * {@link frames.core.constraint.WorldConstraint} and
 * {@link frames.core.constraint.EyeConstraint}) and new constraints can very
 * easily be implemented.
 */

/**
 * A Frame is a {@link Frame} element on a {@link Graph} hierarchy, which converts user gestures
 * into translation, rotation and scaling updates (see {@link #translationSensitivity()},
 * {@link #rotationSensitivity()} and {@link #scalingSensitivity()}). A node may be attached
 * to some of your visual objects to control their behavior using an {@link Agent}.
 * <h2>Geometry transformations</h2>
 * <p>
 * To define the position, orientation and magnitude of a visual object, use {@link #matrix()}
 * (see the {@link Frame} class documentation for details) or
 * {@link #applyTransformation()} (or {@link #applyWorldTransformation()}), as shown below:
 * <p>
 * {@code // Builds a node located at (0,0,0) with an identity orientation (node and
 * world axes match)} <br>
 * {@code Frame node = new Frame(graph);} <br>
 * {@code graph.pushModelView();} <br>
 * {@code node.applyWorldTransformation(); //same as graph.applyModelView(node.matrix());} <br>
 * {@code // Draw your object here, in the local coordinate system.} <br>
 * {@code graph.popModelView();} <br>
 * <p>
 * Alternatively, the node geometry transformation may be automatically handled by the graph
 * traversal algorithm (see {@link frames.core.Graph#traverse()}), provided
 * that the node {@link #visit()} method is overridden, as shown below:
 * <p>
 * <pre>
 * {@code
 * node = new Frame(graph) {
 *   public void visit() {
 *     //hierarchical culling is optional and disabled by default
 *     cull(cullingCondition);
 *     if(!isCulled())
 *       // Draw your object here, in the local coordinate system.
 *   }
 * }
 * }
 * </pre>
 * <p>
 * Implement a {@code cullingCondition} to perform hierarchical culling on the node
 * (culling of the node and its descendants by the {@link frames.core.Graph#traverse()}
 * algorithm). The {@link #isCulled()} flag is {@code false} by default, see
 * {@link #cull(boolean)}.
 * <p>
 * A node may also be defined as the {@link Graph#eye()} (see {@link #isEye()}
 * and {@link Graph#setEye(Frame)}). Some user gestures are then interpreted in a negated way,
 * respect to non-eye nodes. For instance, with a move-to-the-right user gesture the
 * {@link Graph#eye()} has to go to the <i>left</i>, so that the scene seems to move
 * to the right.
 * <h2>Behaviors</h2>
 * To implement a node behavior derive from this class and override {@code interact()}.
 * For example, with the following code:
 * <p>
 * <pre>
 * {@code
 * Shortcut left = new Shortcut(PApplet.LEFT);
 * Shortcut right = new Shortcut(PApplet.RIGHT);
 * node = new Frame(graph) {
 *   public void interact(Event event) {
 *     if(left.matches(event.shortcut()))
 *       rotate(event);
 *     if(right.matches(event.shortcut()))
 *       translate(event);
 *   }
 * }
 * }
 * </pre>
 * <p>
 * your custom node will then accordingly react to the LEFT and RIGHT mouse buttons,
 * provided it's added to the mouse-agent first (see {@link Agent#addGrabber(Grabber)}.
 * <p>
 * Note that a node implements by default several gesture-to-motion converting methods,
 * such as: {@link #rotate(Event)}, {@link #moveForward(Event)},
 * {@link #translateXPos()}, etc.
 * <h2>Picking</h2>
 * Picking a node is done accordingly to a {@link #precision()}. Refer to
 * {@link #setPrecision(Precision)} for details.
 * <h2>Syncing</h2>
 * Two nodes can be synced together ({@link #sync(Frame, Frame)}), meaning that they will
 * share their global parameters (position, orientation and magnitude) taken the one
 * that hasGrabber been most recently updated. Syncing can be useful to share nodes
 * among different off-screen graphs.
 */
public class Frame implements Grabber {
  /**
   * Returns whether or not this frame matches other taking into account the {@link #translation()},
   * {@link #rotation()} and {@link #scaling()} frame parameters, but not its {@link #reference()}.
   *
   * @param other frame
   */
  public boolean matches(Frame other) {
    return translation().matches(other.translation()) && rotation().matches(other.rotation()) && scaling() == other.scaling();
  }

  //core attributes
  protected Vector _translation;
  protected float _scaling;
  protected Quaternion _rotation;
  protected Frame _reference;
  protected Constraint _constraint;
  protected long _lastUpdate;

  // according to space-nav fine tuning it turned out that the space-nav is
  // right handed
  // we thus define our gesture physical space as right-handed as follows:
  // hid.sens should be non-negative for the space-nav to behave as expected
  // from the physical interface
  // TODO: really need to check the second part above. For a fact it's known
  // 1. from the space-bav pov LH vs RH works the same way
  // 2. all space-nav sens are positive
  // Sens
  protected float _rotationSensitivity;
  protected float _translationSensitivity;
  protected float _scalingSensitivity;
  protected float _wheelSensitivity;
  protected float _keySensitivity;

  // spinning stuff:
  protected float _spinningSensitivity;
  protected TimingTask _spinningTask;
  protected Quaternion _spinningQuaternion;
  protected float _dampFriction; // new
  // toss and _spin share the damp var:
  protected float _spiningFriction; // new

  // Whether the SCREEN_TRANS direction (horizontal or vertical) is fixed or not
  public boolean _directionIsFixed;
  protected boolean _horizontal = true; // Two simultaneous nodes require two mice!

  protected float _eventSpeed; // spnning and tossing
  protected long _eventDelay;

  // _fly
  protected Vector _flyDirection;
  protected float _flySpeed;
  protected TimingTask _flyTask;
  protected Vector _fly;
  protected long _flyUpdatePeriod = 20;
  //TODO move to Frame? see Graph.setUpVector
  protected Vector _upVector;
  protected Graph _graph;

  protected float _threshold;

  protected boolean _culled;

  // id
  protected int _id;

  /**
   * Enumerates the Picking precision modes.
   */
  public enum Precision {
    FIXED, ADAPTIVE, EXACT
  }

  protected Precision _Precision;

  protected MotionEvent2 _initEvent;
  protected float _flySpeedCache;

  protected List<Frame> _children;

  /**
   * Same as {@code this(graph, null, new Vector(), new Quaternion(), 1)}.
   *
   * @see #Frame(Graph, Frame, Vector, Quaternion, float)
   */
  public Frame(Graph graph) {
    this(graph, null, new Vector(), new Quaternion(), 1);
  }

  /**
   * Same as {@code this(reference.graph(), reference, new Vector(), new Quaternion(), 1)}.
   *
   * @see #Frame(Graph, Frame, Vector, Quaternion, float)
   */
  public Frame(Frame reference) {
    this(reference.graph(), reference, new Vector(), new Quaternion(), 1);
  }

  /**
   * Creates a graph node with {@code reference} as {@link #reference()}, and
   * {@code translation}, {@code rotation} and {@code scaling} as the frame
   * {@link #translation()}, {@link #rotation()} and {@link #scaling()}, respectively.
   * <p>
   * The {@link Graph#inputHandler()} will attempt to addGrabber the node to all its
   * {@link InputHandler#agents()}.
   * <p>
   * The node sensitivities are set to their default values, see
   * {@link #spinningSensitivity()}, {@link #wheelSensitivity()},
   * {@link #keySensitivity()}, {@link #rotationSensitivity()},
   * {@link #translationSensitivity()} and {@link #scalingSensitivity()}.
   * <p>
   * Sets the {@link #precision()} to {@link Precision#FIXED}.
   * <p>
   * After object creation a call to {@link #isEye()} will return {@code false}.
   */
  protected Frame(Graph graph, Frame reference, Vector translation, Quaternion rotation, float scaling) {
    setTranslation(translation);
    setRotation(rotation);
    setScaling(scaling);
    setReference(reference);
    _graph = graph;

    if (graph() == null)
      return;

    _id = ++graph()._nodeCount;
    // unlikely but theoretically possible
    if (_id == 16777216)
      throw new RuntimeException("Maximum node instances reached. Exiting now!");

    if (graph().is2D()) {
      if (position().z() != 0)
        throw new RuntimeException("2D frame z-position should be 0. Set it as: setPosition(x, y)");
      if (orientation().axis().x() != 0 || orientation().axis().y() != 0)
        throw new RuntimeException("2D frame rotation axis should (0,0,1). Set it as: setOrientation(new Quaternion(orientation().angle()))");
      WorldConstraint constraint2D = new WorldConstraint();
      constraint2D.setTranslationConstraint(WorldConstraint.Type.PLANE, new Vector(0, 0, 1));
      constraint2D.setRotationConstraint(WorldConstraint.Type.AXIS, new Vector(0, 0, 1));
      setConstraint(constraint2D);
    }

    setFlySpeed(0.01f * graph().radius());
    _upVector = new Vector(0.0f, 1.0f, 0.0f);
    _culled = false;
    _children = new ArrayList<Frame>();
    // graph()._addLeadingNode(this);
    setReference(reference());// _restorePath seems more robust
    setRotationSensitivity(1.0f);
    setScalingSensitivity(1.0f);
    setTranslationSensitivity(1.0f);
    setWheelSensitivity(15f);
    setKeySensitivity(10f);
    setSpinningSensitivity(0.3f);
    setDamping(0.5f);

    _spinningTask = new TimingTask() {
      public void execute() {
        _spinExecution();
      }
    };
    graph().registerTask(_spinningTask);

    _fly = new Vector(0.0f, 0.0f, 0.0f);
    _flyTask = new TimingTask() {
      public void execute() {
        _fly();
      }
    };
    graph().registerTask(_flyTask);
    graph().inputHandler().addGrabber(this);
    _Precision = Precision.FIXED;
    setPrecisionThreshold(20);
    setFlySpeed(0.01f * graph().radius());
  }

  protected Frame(Graph graph, Frame other) {
    _translation = other.translation().get();
    _rotation = other.rotation().get();
    _scaling = other.scaling();
    _reference = other.reference();
    _constraint = other.constraint();
    this._graph = graph;

    if (graph() == null)
      return;

    if (this.graph() == other.graph()) {
      this._id = ++graph()._nodeCount;
      // unlikely but theoretically possible
      if (this._id == 16777216)
        throw new RuntimeException("Maximum iFrame instances reached. Exiting now!");
    } else {
      this._id = other._id();
      this.setWorldMatrix(other);
    }

    this._upVector = other._upVector.get();
    this._culled = other._culled;

    this._children = new ArrayList<Frame>();
    if (this.graph() == other.graph()) {
      this.setReference(reference());// _restorePath
    }

    this._spinningTask = new TimingTask() {
      public void execute() {
        _spinExecution();
      }
    };

    this._graph.registerTask(_spinningTask);

    this._fly = new Vector();
    this._fly.set(other._fly.get());
    this._flyTask = new TimingTask() {
      public void execute() {
        _fly();
      }
    };
    this._graph.registerTask(_flyTask);
    _lastUpdate = other.lastUpdate();
    // end
    // this.isInCamPath = otherFrame.isInCamPath;
    //
    // this.setPrecisionThreshold(otherFrame.precisionThreshold(),
    // otherFrame.adaptiveGrabsInputThreshold());
    this._Precision = other._Precision;
    this._threshold = other._threshold;

    this.setRotationSensitivity(other.rotationSensitivity());
    this.setScalingSensitivity(other.scalingSensitivity());
    this.setTranslationSensitivity(other.translationSensitivity());
    this.setWheelSensitivity(other.wheelSensitivity());
    this.setKeySensitivity(other.keySensitivity());
    //
    this.setSpinningSensitivity(other.spinningSensitivity());
    this.setDamping(other.damping());
    //
    this.setFlySpeed(other.flySpeed());

    if (this.graph() == other.graph()) {
      for (Agent agent : this._graph.inputHandler().agents())
        if (agent.hasGrabber(other))
          agent.addGrabber(this);
    } else {
      this.graph().inputHandler().addGrabber(this);
    }
  }

  /**
   * Perform a deep, non-recursive copy of this node.
   * <p>
   * The copied node will keep this node {@link #reference()}, but its children aren't copied.
   *
   * @return node copy
   */
  public Frame get() {
    return new Frame(this.graph(), this);
  }

  // detached frames

  public static Frame detach(Graph graph, Vector position, Quaternion orientation, float magnitude) {
    Frame frame = new Frame(graph);
    graph.pruneBranch(frame);
    frame.setPosition(position);
    frame.setOrientation(orientation);
    frame.setMagnitude(magnitude);
    return frame;
  }

  public Frame detach() {
    Frame frame = new Frame(this.graph());
    graph().pruneBranch(frame);
    frame.setWorldMatrix(this);
    return frame;
  }

  //_id

  /**
   * Internal use. Frame graphics color to be used for picking with a color buffer.
   */
  protected int _id() {
    // see here:
    // http://stackoverflow.com/questions/2262100/rgb-int-to-rgb-python
    return (255 << 24) | ((_id & 255) << 16) | (((_id >> 8) & 255) << 8) | (_id >> 16) & 255;
  }

  /**
   * Randomized this frame. The frame is randomly re-positioned inside the ball
   * defined by {@code center} and {@code radius} (see {@link Vector#random()}). The
   * {@link #orientation()} is randomized by {@link Quaternion#randomize()}. The new
   * magnitude is a random in oldMagnitude * [0,5...2].
   *
   * @see #randomize()
   * @see #random(Graph, Vector, float)
   */
  public void randomize(Vector center, float radius) {
    Vector displacement = Vector.random();
    displacement.setMagnitude(radius);
    setPosition(Vector.add(center, displacement));
    setOrientation(Quaternion.random());
    float lower = 0.5f;
    float upper = 2;
    float magnitude = magnitude() * ((float) Math.random() * (upper - lower)) + lower;
    setMagnitude(magnitude);
  }

  /**
   * Same as {@code randomize(graph().center(), graph().radius())}.
   *
   * @see #randomize(Vector, float)
   * @see #random(Graph)
   */
  public void randomize() {
    randomize(graph().center(), graph().radius());
  }

  /**
   * Returns a random frame. The frame is randomly positioned inside the ball defined
   * by {@code center} and {@code radius} (see {@link Vector#random()}). The
   * {@link #orientation()} is set by {@link Quaternion#random()}. The magnitude
   * is a random in [0,5...2].
   *
   * @see #randomize(Vector, float)
   */
  public static Frame random(Graph graph, Vector center, float radius) {
    Frame frame = new Frame(graph);
    Vector displacement = Vector.random();
    displacement.setMagnitude(radius);
    frame.setPosition(Vector.add(center, displacement));
    frame.setOrientation(Quaternion.random());
    float lower = 0.5f;
    float upper = 2;
    frame.setMagnitude(((float) Math.random() * (upper - lower)) + lower);
    return frame;
  }

  /**
   * Same as {@code return random(graph, graph.center(), graph.radius())}.
   *
   * @see #randomize()
   */
  public static Frame random(Graph graph) {
    return random(graph, graph.center(), graph.radius());
  }

  // core attributes

  // CONSTRAINT

  /**
   * Returns the current {@link frames.core.constraint.Constraint} applied to the
   * frame.
   * <p>
   * A {@code null} value (default) means that no constraint is used to filter the frame
   * translation and rotation.
   * <p>
   * See the Constraint class documentation for details.
   */
  public Constraint constraint() {
    return _constraint;
  }

  /**
   * Sets the {@link #constraint()} attached to the frame.
   * <p>
   * A {@code null} value means set no constraint (also reset it if there was one).
   */
  public void setConstraint(Constraint constraint) {
    _constraint = constraint;
  }

  // TRANSLATION

  /**
   * Returns the frame translation, defined with respect to the {@link #reference()}.
   * <p>
   * Use {@link #position()} to get the result in world coordinates. These two values are
   * identical when the {@link #reference()} is {@code null} (default).
   *
   * @see #setTranslation(Vector)
   */
  public Vector translation() {
    return _translation;
  }

  /**
   * Sets the {@link #translation()} of the frame, locally defined with respect to the
   * {@link #reference()}.
   * <p>
   * Note that if there's a {@link #constraint()} it is satisfied, i.e., to
   * bypass a frame constraint simply reset it (see {@link #setConstraint(Constraint)}).
   * <p>
   * Use {@link #setPosition(Vector)} to define the world coordinates {@link #position()}.
   *
   * @see #setConstraint(Constraint)
   */
  public void setTranslation(Vector translation) {
    if (constraint() == null)
      _translation = translation;
    else
      translation().add(constraint().constrainTranslation(Vector.subtract(translation, this.translation()), this));
    _modified();
  }

  /**
   * Same as {@link #setTranslation(Vector)}, but with {@code float} parameters.
   */
  public void setTranslation(float x, float y) {
    setTranslation(new Vector(x, y));
  }

  /**
   * Same as {@link #setTranslation(Vector)}, but with {@code float} parameters.
   */
  public void setTranslation(float x, float y, float z) {
    setTranslation(new Vector(x, y, z));
  }

  /**
   * Same as {@link #translate(Vector)} but with {@code float} parameters.
   */
  public void translate(float x, float y, float z) {
    translate(new Vector(x, y, z));
  }

  /**
   * Same as {@link #translate(Vector)} but with {@code float} parameters.
   */
  public void translate(float x, float y) {
    translate(new Vector(x, y));
  }

  /**
   * Translates the frame according to {@code vector}, locally defined with respect to the
   * {@link #reference()}.
   * <p>
   * If there's a {@link #constraint()} it is satisfied. Hence the translation actually
   * applied to the frame may differ from {@code vector} (since it can be filtered by the
   * {@link #constraint()}).
   *
   * @see #rotate(Quaternion)
   * @see #scale(float)
   */
  public void translate(Vector vector) {
    translation().add(constraint() != null ? constraint().constrainTranslation(vector, this) : vector);
    _modified();
  }

  // POSITION

  /**
   * Returns the frame position defined in the world coordinate system.
   *
   * @see #orientation()
   * @see #magnitude()
   * @see #setPosition(Vector)
   * @see #translation()
   */
  public Vector position() {
    return inverseCoordinatesOf(new Vector(0, 0, 0));
  }

  /**
   * Sets the frame {@link #position()}, defined in the world coordinate system.
   * <p>
   * Use {@link #setTranslation(Vector)} to define the local frame translation (with respect
   * to the {@link #reference()}).
   * <p>
   * Note that the potential {@link #constraint()} of the frame is taken into account, i.e.,
   * to bypass a frame constraint simply reset it (see {@link #setConstraint(Constraint)}).
   *
   * @see #setConstraint(Constraint)
   */
  public void setPosition(Vector position) {
    setTranslation(reference() != null ? reference().coordinatesOf(position) : position);
  }

  /**
   * Same as {@link #setPosition(Vector)}, but with {@code float} parameters.
   */
  public void setPosition(float x, float y) {
    setPosition(new Vector(x, y));
  }

  /**
   * Same as {@link #setPosition(Vector)}, but with {@code float} parameters.
   */
  public void setPosition(float x, float y, float z) {
    setPosition(new Vector(x, y, z));
  }

  // ROTATION

  /**
   * Returns the frame rotation, defined with respect to the {@link #reference()}
   * (i.e, the current Quaternion orientation).
   * <p>
   * Use {@link #orientation()} to get the result in world coordinates. These two values
   * are identical when the {@link #reference()} is {@code null} (default).
   *
   * @see #setRotation(Quaternion)
   */
  public Quaternion rotation() {
    return _rotation;
  }

  /**
   * Same as {@link #setRotation(Quaternion)} but with {@code float} Quaternion parameters.
   */
  public void setRotation(float x, float y, float z, float w) {
    setRotation(new Quaternion(x, y, z, w));
  }

  /**
   * Set the current rotation. See the different {@link Quaternion} constructors.
   * <p>
   * Sets the frame {@link #rotation()}, locally defined with respect to the
   * {@link #reference()}. Use {@link #setOrientation(Quaternion)} to define the
   * world coordinates {@link #orientation()}.
   * <p>
   * Note that if there's a {@link #constraint()} it is satisfied, i.e., to
   * bypass a frame constraint simply reset it (see {@link #setConstraint(Constraint)}).
   *
   * @see #setConstraint(Constraint)
   * @see #rotation()
   * @see #setTranslation(Vector)
   */
  public void setRotation(Quaternion rotation) {
    if (constraint() == null)
      _rotation = rotation;
    else {
      rotation().compose(constraint().constrainRotation(Quaternion.compose(rotation().inverse(), rotation), this));
      rotation().normalize(); // Prevents numerical drift
    }
    _modified();
  }

  /**
   * Rotates the frame by {@code quaternion} (defined in the frame coordinate system):
   * {@code rotation().compose(quaternion)}.
   * <p>
   * Note that if there's a {@link #constraint()} it is satisfied, i.e., to
   * bypass a frame constraint simply reset it (see {@link #setConstraint(Constraint)}).
   *
   * @see #setConstraint(Constraint)
   * @see #translate(Vector)
   */
  public void rotate(Quaternion quaternion) {
    rotation().compose(constraint() != null ? constraint().constrainRotation(quaternion, this) : quaternion);
    rotation().normalize(); // Prevents numerical drift
    _modified();
  }

  /**
   * Same as {@link #rotate(Quaternion)} but with {@code float} rotation parameters.
   */
  public void rotate(float x, float y, float z, float w) {
    rotate(new Quaternion(x, y, z, w));
  }

  /**
   * Makes the frame {@link #rotate(Quaternion)} by {@code rotation} around {@code point}.
   * The {@code point} is defined in the world coordinate system while the {@code rotation}
   * axis is defined in the frame coordinate system.
   * <p>
   * Note that if there's a {@link #constraint()} it is satisfied, i.e., to
   * bypass a frame constraint simply reset it (see {@link #setConstraint(Constraint)}).
   *
   * @see #setConstraint(Constraint)
   */
  public void rotateAroundPoint(Quaternion rotation, Vector point) {
    if (constraint() != null)
      rotation = constraint().constrainRotation(rotation, this);

    this.rotation().compose(rotation);
    this.rotation().normalize(); // Prevents numerical drift

    Quaternion q = new Quaternion(orientation().rotate(rotation.axis()), rotation.angle());

    Vector t = Vector.add(point, q.rotate(Vector.subtract(position(), point)));
    t.subtract(translation());
    if (constraint() != null)
      translate(constraint().constrainTranslation(t, this));
    else
      translate(t);
  }

  /**
   * Applies a {@code rotation} (to this frame) around the {@code frame} param.
   */
  public void rotateAroundFrame(Quaternion rotation, Frame frame) {
    Vector euler = rotation.eulerAngles();
    rotateAroundFrame(euler.x(), euler.y(), euler.z(), frame);
  }

  /*
  public void rotateAroundFrame(float roll, float pitch, float yaw, Frame frame) {
    //TODO check with other version!!!
    if (frame != null) {
      Frame rotateAroundFrameCopy = frame.get();
      Frame thisFrameCopy = get();
      thisFrameCopy.setReference(rotateAroundFrameCopy);
      thisFrameCopy.setWorldMatrix(this);
      rotateAroundFrameCopy.rotate(new Quaternion(roll, pitch, yaw));
      setWorldMatrix(thisFrameCopy);
      return;
    }
  }
  */

  /**
   * Applies the rotation (to this frame) defined by the Euler angles
   * around the {@code frame} param.
   */
  public void rotateAroundFrame(float roll, float pitch, float yaw, Frame frame) {
    if (frame != null) {
      Frame axis = frame.detach();
      Frame copy = detach();
      copy.setReference(axis);
      //copy.setWorldMatrix(this);
      axis.rotate(new Quaternion(_graph.isLeftHanded() ? -roll : roll, pitch, _graph.isLeftHanded() ? -yaw : yaw));
      setWorldMatrix(copy);
    }
  }

  // ORIENTATION

  /**
   * Returns the orientation of the frame, defined in the world coordinate system.
   *
   * @see #position()
   * @see #magnitude()
   * @see #setOrientation(Quaternion)
   * @see #rotation()
   */
  public Quaternion orientation() {
    Quaternion quaternion = rotation().get();
    Frame reference = reference();
    while (reference != null) {
      quaternion = Quaternion.compose(reference.rotation(), quaternion);
      reference = reference.reference();
    }
    return quaternion;
  }

  /**
   * Sets the {@link #orientation()} of the frame, defined in the world coordinate system.
   * <p>
   * Use {@link #setRotation(Quaternion)} to define the local frame rotation (with respect
   * to the {@link #reference()}).
   * <p>
   * Note that the potential {@link #constraint()} of the frame is taken into account, i.e.,
   * to bypass a frame constraint simply reset it (see {@link #setConstraint(Constraint)}).
   */
  public void setOrientation(Quaternion quaternion) {
    setRotation(reference() != null ? Quaternion.compose(reference().orientation().inverse(), quaternion) : quaternion);
  }

  /**
   * Same as {@link #setOrientation(Quaternion)}, but with {@code float} parameters.
   */
  public void setOrientation(float x, float y, float z, float w) {
    setOrientation(new Quaternion(x, y, z, w));
  }

  // SCALING

  /**
   * Returns the frame scaling, defined with respect to the {@link #reference()}.
   * <p>
   * Use {@link #magnitude()} to get the result in world coordinates. These two values are
   * identical when the {@link #reference()} is {@code null} (default).
   *
   * @see #setScaling(float)
   */
  public float scaling() {
    return _scaling;
  }

  /**
   * Sets the {@link #scaling()} of the frame, locally defined with respect to the
   * {@link #reference()}.
   * <p>
   * Use {@link #setMagnitude(float)} to define the world coordinates {@link #magnitude()}.
   */
  public void setScaling(float scaling) {
    if (scaling > 0) {
      _scaling = scaling;
      _modified();
    } else
      System.out.println("Warning. Scaling should be positive. Nothing done");
  }

  /**
   * Scales the frame according to {@code scaling}, locally defined with respect to the
   * {@link #reference()}.
   *
   * @see #rotate(Quaternion)
   * @see #translate(Vector)
   */
  public void scale(float scaling) {
    setScaling(scaling() * scaling);
  }

  // MAGNITUDE

  /**
   * Returns the magnitude of the frame, defined in the world coordinate system.
   *
   * @see #orientation()
   * @see #position()
   * @see #setPosition(Vector)
   * @see #translation()
   */
  public float magnitude() {
    if (reference() != null)
      return reference().magnitude() * scaling();
    else
      return scaling();
  }

  /**
   * Sets the {@link #magnitude()} of the frame, defined in the world coordinate system.
   * <p>
   * Use {@link #setScaling(float)} to define the local frame scaling (with respect to the
   * {@link #reference()}).
   */
  public void setMagnitude(float magnitude) {
    Frame reference = reference();
    if (reference != null)
      setScaling(magnitude / reference.magnitude());
    else
      setScaling(magnitude);
  }

  // ALIGNMENT

  /**
   * Convenience function that simply calls {@code alignWithFrame(frame, false, 0.85f)}
   */
  public void alignWithFrame(Frame frame) {
    alignWithFrame(frame, false, 0.85f);
  }

  /**
   * Convenience function that simply calls {@code alignWithFrame(frame, move, 0.85f)}
   */
  public void alignWithFrame(Frame frame, boolean move) {
    alignWithFrame(frame, move, 0.85f);
  }

  /**
   * Convenience function that simply calls
   * {@code alignWithFrame(frame, false, threshold)}
   */
  public void alignWithFrame(Frame frame, float threshold) {
    alignWithFrame(frame, false, threshold);
  }

  /**
   * Aligns the frame with {@code frame}, so that two of their axis are parallel.
   * <p>
   * If one of the X, Y and Z axis of the Frame is almost parallel to any of the X, Y, or
   * Z axis of {@code frame}, the Frame is rotated so that these two axis actually become
   * parallel.
   * <p>
   * If, after this first rotation, two other axis are also almost parallel, a second
   * alignment is performed. The two nodes then have identical orientations, up to 90
   * degrees rotations.
   * <p>
   * {@code threshold} measures how close two axis must be to be considered parallel. It
   * is compared with the absolute values of the dot product of the normalized axis.
   * <p>
   * When {@code move} is set to {@code true}, the Frame {@link #position()} is also
   * affected by the alignment. The new Frame {@link #position()} is such that the
   * {@code frame} frame position (computed with {@link #coordinatesOf(Vector)}, in the Frame
   * coordinates system) does not change.
   * <p>
   * {@code frame} may be {@code null} and then represents the world coordinate system
   * (same convention than for the {@link #reference()}).
   */
  public void alignWithFrame(Frame frame, boolean move, float threshold) {
    Vector[][] directions = new Vector[2][3];

    for (int d = 0; d < 3; ++d) {
      Vector dir = new Vector((d == 0) ? 1.0f : 0.0f, (d == 1) ? 1.0f : 0.0f, (d == 2) ? 1.0f : 0.0f);
      if (frame != null)
        directions[0][d] = frame.orientation().rotate(dir);
      else
        directions[0][d] = dir;
      directions[1][d] = orientation().rotate(dir);
    }

    float maxProj = 0.0f;
    float proj;
    short[] index = new short[2];
    index[0] = index[1] = 0;

    Vector vector = new Vector(0.0f, 0.0f, 0.0f);
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        vector.set(directions[0][i]);
        proj = Math.abs(vector.dot(directions[1][j]));
        if ((proj) >= maxProj) {
          index[0] = (short) i;
          index[1] = (short) j;
          maxProj = proj;
        }
      }
    }
    Frame old = detach(); // correct line
    // VFrame old = this.get();// this call the get overloaded method and
    // hence addGrabber the frame to the mouse _grabber

    vector.set(directions[0][index[0]]);
    float coef = vector.dot(directions[1][index[1]]);

    if (Math.abs(coef) >= threshold) {
      vector.set(directions[0][index[0]]);
      Vector axis = vector.cross(directions[1][index[1]]);
      float angle = (float) Math.asin(axis.magnitude());
      if (coef >= 0.0)
        angle = -angle;
      // setOrientation(Quaternion(axis, angle) * orientation());
      Quaternion q = new Quaternion(axis, angle);
      q = Quaternion.multiply(rotation().inverse(), q);
      q = Quaternion.multiply(q, orientation());
      rotate(q);

      // Try to align an other axis direction
      short d = (short) ((index[1] + 1) % 3);
      Vector dir = new Vector((d == 0) ? 1.0f : 0.0f, (d == 1) ? 1.0f : 0.0f, (d == 2) ? 1.0f : 0.0f);
      dir = orientation().rotate(dir);

      float max = 0.0f;
      for (int i = 0; i < 3; ++i) {
        vector.set(directions[0][i]);
        proj = Math.abs(vector.dot(dir));
        if (proj > max) {
          index[0] = (short) i;
          max = proj;
        }
      }

      if (max >= threshold) {
        vector.set(directions[0][index[0]]);
        axis = vector.cross(dir);
        angle = (float) Math.asin(axis.magnitude());
        vector.set(directions[0][index[0]]);
        if (vector.dot(dir) >= 0.0)
          angle = -angle;
        // setOrientation(Quaternion(axis, angle) * orientation());
        q.fromAxisAngle(axis, angle);
        q = Quaternion.multiply(rotation().inverse(), q);
        q = Quaternion.multiply(q, orientation());
        rotate(q);
      }
    }
    if (move) {
      Vector center = new Vector(0.0f, 0.0f, 0.0f);
      if (frame != null)
        center = frame.position();

      vector = Vector.subtract(center, inverseTransformOf(old.coordinatesOf(center)));
      vector.subtract(translation());
      translate(vector);
    }
  }

  /**
   * Translates the frame so that its {@link #position()} lies on the line defined by
   * {@code origin} and {@code direction} (defined in the world coordinate system).
   * <p>
   * Simply uses an orthogonal projection. {@code direction} does not need to be
   * normalized.
   */
  public void projectOnLine(Vector origin, Vector direction) {
    Vector position = position();
    Vector shift = Vector.subtract(origin, position);
    Vector proj = shift;
    proj = Vector.projectVectorOnAxis(proj, direction);
    setPosition(Vector.add(position, Vector.subtract(shift, proj)));
  }

  /**
   * Rotates the frame so that its {@link #xAxis()} becomes {@code axis} defined in the
   * world coordinate system.
   * <p>
   * <b>Attention:</b> this rotation is not uniquely defined. See
   * {@link Quaternion#fromTo(Vector, Vector)}.
   *
   * @see #xAxis()
   * @see #setYAxis(Vector)
   * @see #setZAxis(Vector)
   */
  public void setXAxis(Vector axis) {
    rotate(new Quaternion(new Vector(1.0f, 0.0f, 0.0f), transformOf(axis)));
  }

  /**
   * Rotates the frame so that its {@link #yAxis()} becomes {@code axis} defined in the
   * world coordinate system.
   * <p>
   * <b>Attention:</b> this rotation is not uniquely defined. See
   * {@link Quaternion#fromTo(Vector, Vector)}.
   *
   * @see #yAxis()
   * @see #setYAxis(Vector)
   * @see #setZAxis(Vector)
   */
  public void setYAxis(Vector axis) {
    rotate(new Quaternion(new Vector(0.0f, 1.0f, 0.0f), transformOf(axis)));
  }

  /**
   * Rotates the frame so that its {@link #zAxis()} becomes {@code axis} defined in the
   * world coordinate system.
   * <p>
   * <b>Attention:</b> this rotation is not uniquely defined. See
   * {@link Quaternion#fromTo(Vector, Vector)}.
   *
   * @see #zAxis()
   * @see #setYAxis(Vector)
   * @see #setZAxis(Vector)
   */
  public void setZAxis(Vector axis) {
    rotate(new Quaternion(new Vector(0.0f, 0.0f, 1.0f), transformOf(axis)));
  }

  /**
   * Same as {@code return xAxis(true)}
   *
   * @see #xAxis(boolean)
   */
  public Vector xAxis() {
    return xAxis(true);
  }

  /**
   * Returns the x-axis of the frame, represented as a normalized vector defined in the
   * world coordinate system.
   *
   * @see #setXAxis(Vector)
   * @see #yAxis()
   * @see #zAxis()
   */
  public Vector xAxis(boolean positive) {
    Vector axis = inverseTransformOf(new Vector(positive ? 1.0f : -1.0f, 0.0f, 0.0f));
    if (magnitude() != 1)
      axis.normalize();
    return axis;
  }

  /**
   * Same as {@code return yAxis(true)}
   *
   * @see #yAxis(boolean)
   */
  public Vector yAxis() {
    return yAxis(true);
  }

  /**
   * Returns the y-axis of the frame, represented as a normalized vector defined in the
   * world coordinate system.
   *
   * @see #setYAxis(Vector)
   * @see #xAxis()
   * @see #zAxis()
   */
  public Vector yAxis(boolean positive) {
    Vector axis = inverseTransformOf(new Vector(0.0f, positive ? 1.0f : -1.0f, 0.0f));
    if (magnitude() != 1)
      axis.normalize();
    return axis;
  }

  /**
   * Same as {@code return zAxis(true)}
   *
   * @see #zAxis(boolean)
   */
  public Vector zAxis() {
    return zAxis(true);
  }

  /**
   * Returns the z-axis of the frame, represented as a normalized vector defined in the
   * world coordinate system.
   *
   * @see #setZAxis(Vector)
   * @see #xAxis()
   * @see #yAxis()
   */
  public Vector zAxis(boolean positive) {
    Vector axis = inverseTransformOf(new Vector(0.0f, 0.0f, positive ? 1.0f : -1.0f));
    if (magnitude() != 1)
      axis.normalize();
    return axis;
  }

  // CONVERSION

  /**
   * Returns the local transformation matrix represented by the frame.
   * <p>
   * This method could be used in conjunction with {@code applyMatrix()} to modify a graph
   * modelView() matrix from a frame hierarchy. For example, with this frame hierarchy:
   * <p>
   * {@code Frame body = new Frame();} <br>
   * {@code Frame leftArm = new Frame();} <br>
   * {@code Frame rightArm = new Frame();} <br>
   * {@code leftArm.setReference(body);} <br>
   * {@code rightArm.setReference(body);} <br>
   * <p>
   * The associated drawing code should look like:
   * <p>
   * {@code graph.pushModelView();}<br>
   * {@code graph.applyMatrix(body.matrix());} <br>
   * {@code drawBody();} <br>
   * {@code graph.pushModelView();} <br>
   * {@code graph.applyMatrix(leftArm.matrix());} <br>
   * {@code drawArm();} <br>
   * {@code graph.popModelView();} <br>
   * {@code graph.pushModelView();} <br>
   * {@code graph.applyMatrix(rightArm.matrix());} <br>
   * {@code drawArm();} <br>
   * {@code graph.popModelView();} <br>
   * {@code graph.popModelView();} <br>
   * <p>
   * Note the use of nested {@code pushModelView()} and {@code popModelView()} blocks to
   * represent the frame hierarchy: {@code leftArm} and {@code rightArm} are both
   * correctly drawn with respect to the {@code body} coordinate system.
   * <p>
   * This matrix only represents the local frame transformation (i.e., with respect to the
   * {@link #reference()}). Use {@link #worldMatrix()} to get the full Frame
   * transformation matrix (i.e., from the world to the Frame coordinate system). These
   * two match when the {@link #reference()} is {@code null}.
   * <p>
   * <b>Attention:</b> In Processing this technique is inefficient because
   * {@code papplet.applyMatrix} will try to calculate the inverse of the transform.
   * Use {@link frames.core.Graph#applyTransformation(Frame)} instead.
   *
   * @see #setMatrix(Frame)
   * @see #worldMatrix()
   * @see #view()
   */
  public Matrix matrix() {
    Matrix matrix = rotation().matrix();

    matrix._matrix[12] = translation()._vector[0];
    matrix._matrix[13] = translation()._vector[1];
    matrix._matrix[14] = translation()._vector[2];

    if (scaling() != 1) {
      matrix.setM00(matrix.m00() * scaling());
      matrix.setM10(matrix.m10() * scaling());
      matrix.setM20(matrix.m20() * scaling());

      matrix.setM01(matrix.m01() * scaling());
      matrix.setM11(matrix.m11() * scaling());
      matrix.setM21(matrix.m21() * scaling());

      matrix.setM02(matrix.m02() * scaling());
      matrix.setM12(matrix.m12() * scaling());
      matrix.setM22(matrix.m22() * scaling());
    }

    return matrix;
  }

  /**
   * Returns the global transformation matrix represented by the frame.
   * <p>
   * This method should be used in conjunction with {@code applyMatrix()} to modify a
   * graph modelView() matrix from a frame:
   * <p>
   * {@code // Here the modelview matrix corresponds to the world coordinate system.} <br>
   * {@code Frame frame = new Frame(translation, new Rotation(from, to));} <br>
   * {@code graph.pushModelView();} <br>
   * {@code graph.applyModelView(frame.worldMatrix());} <br>
   * {@code // draw object in the frame coordinate system.} <br>
   * {@code graph.popModelView();} <br>
   * <p>
   * This matrix represents the global frame transformation: the entire
   * {@link #reference()} hierarchy is taken into account to define the frame
   * transformation from the world coordinate system. Use {@link #matrix()} to get the
   * local frame transformation matrix (i.e. defined with respect to the
   * {@link #reference()}). These two match when the {@link #reference()} is
   * {@code null}.
   *
   * @see #setWorldMatrix(Frame)
   * @see #matrix()
   * @see #view()
   */
  public Matrix worldMatrix() {
    if (reference() != null)
      return detach().matrix();
    else
      return matrix();
  }

  /**
   * Same as {@link #worldMatrix()}, but the view matrix is computing with the frame magnitude
   * set to 1, i.e., returns the matrix associated with the frame position and orientation.
   * To be used when the frame represents an eye.
   * <p>
   * The view matrix converts from the world coordinates system to the eye coordinates system,
   * so that coordinates can then be projected on screen using a projection matrix.
   *
   * @see #matrix()
   * @see #worldMatrix()
   * @see #setMatrix(Frame)
   * @see #setWorldMatrix(Frame)
   */
  public Matrix view() {
    Matrix view = new Matrix();

    Quaternion q = orientation();

    float q00 = 2.0f * q._quaternion[0] * q._quaternion[0];
    float q11 = 2.0f * q._quaternion[1] * q._quaternion[1];
    float q22 = 2.0f * q._quaternion[2] * q._quaternion[2];

    float q01 = 2.0f * q._quaternion[0] * q._quaternion[1];
    float q02 = 2.0f * q._quaternion[0] * q._quaternion[2];
    float q03 = 2.0f * q._quaternion[0] * q._quaternion[3];

    float q12 = 2.0f * q._quaternion[1] * q._quaternion[2];
    float q13 = 2.0f * q._quaternion[1] * q._quaternion[3];
    float q23 = 2.0f * q._quaternion[2] * q._quaternion[3];

    view._matrix[0] = 1.0f - q11 - q22;
    view._matrix[1] = q01 - q23;
    view._matrix[2] = q02 + q13;
    view._matrix[3] = 0.0f;

    view._matrix[4] = q01 + q23;
    view._matrix[5] = 1.0f - q22 - q00;
    view._matrix[6] = q12 - q03;
    view._matrix[7] = 0.0f;

    view._matrix[8] = q02 - q13;
    view._matrix[9] = q12 + q03;
    view._matrix[10] = 1.0f - q11 - q00;
    view._matrix[11] = 0.0f;

    Vector t = q.inverseRotate(position());

    view._matrix[12] = -t._vector[0];
    view._matrix[13] = -t._vector[1];
    view._matrix[14] = -t._vector[2];
    view._matrix[15] = 1.0f;

    return view;
  }

  /**
   * Convenience function that simply calls {@code fromMatrix(matrix, 1))}.
   *
   * @see #fromMatrix(Matrix, float)
   */
  public void fromMatrix(Matrix matrix) {
    fromMatrix(matrix, 1);
  }

  /**
   * Sets the frame from a Matrix representation: rotation in the upper left 3x3 matrix and
   * translation on the last column. Scaling is defined separately in {@code scaling}.
   * <p>
   * Hence, if your openGL code fragment looks like:
   * <p>
   * {@code float [] m = new float [16]; m[0]=...;} <br>
   * {@code gl.glMultMatrixf(m);} <br>
   * <p>
   * It is equivalent to write:
   * <p>
   * {@code Frame frame = new Frame();} <br>
   * {@code frame.fromMatrix(m);} <br>
   * {@code graph.applyModelView(frame.matrix());} <br>
   * <p>
   * Using this conversion, you can benefit from the powerful frame transformation methods
   * to translate points and vectors to and from the frame coordinate system to any other
   * frame coordinate system (including the world coordinate system). See
   * {@link #coordinatesOf(Vector)} and {@link #transformOf(Vector)}.
   */
  public void fromMatrix(Matrix matrix, float scaling) {
    if (matrix._matrix[15] == 0) {
      System.out.println("Doing nothing: pM.mat[15] should be non-zero!");
      return;
    }

    translation()._vector[0] = matrix._matrix[12] / matrix._matrix[15];
    translation()._vector[1] = matrix._matrix[13] / matrix._matrix[15];
    translation()._vector[2] = matrix._matrix[14] / matrix._matrix[15];

    float[][] r = new float[3][3];

    r[0][0] = matrix._matrix[0] / matrix._matrix[15];
    r[0][1] = matrix._matrix[4] / matrix._matrix[15];
    r[0][2] = matrix._matrix[8] / matrix._matrix[15];
    r[1][0] = matrix._matrix[1] / matrix._matrix[15];
    r[1][1] = matrix._matrix[5] / matrix._matrix[15];
    r[1][2] = matrix._matrix[9] / matrix._matrix[15];
    r[2][0] = matrix._matrix[2] / matrix._matrix[15];
    r[2][1] = matrix._matrix[6] / matrix._matrix[15];
    r[2][2] = matrix._matrix[10] / matrix._matrix[15];

    setScaling(scaling);// calls _modified() :P

    if (scaling() != 1) {
      r[0][0] = r[0][0] / scaling();
      r[1][0] = r[1][0] / scaling();
      r[2][0] = r[2][0] / scaling();

      r[0][1] = r[0][1] / scaling();
      r[1][1] = r[1][1] / scaling();
      r[2][1] = r[2][1] / scaling();

      r[0][2] = r[0][2] / scaling();
      r[1][2] = r[1][2] / scaling();
      r[2][2] = r[2][2] / scaling();
    }

    Vector x = new Vector(r[0][0], r[1][0], r[2][0]);
    Vector y = new Vector(r[0][1], r[1][1], r[2][1]);
    Vector z = new Vector(r[0][2], r[1][2], r[2][2]);

    rotation().fromRotatedBasis(x, y, z);
  }

  /**
   * Sets {@link #position()}, {@link #orientation()} and {@link #magnitude()} values from
   * those of the {@code other} frame.
   * <p>
   * After calling {@code setWorldMatrix(other)} a call to {@code this.matches(other)} should
   * return {@code true}.
   *
   * @see #worldMatrix()
   * @see #setMatrix(Frame)
   */
  public void setWorldMatrix(Frame other) {
    if (other == null)
      return;
    setPosition(other.position());
    setOrientation(other.orientation());
    setMagnitude(other.magnitude());
  }

  /**
   * Sets {@link #translation()}, {@link #rotation()} and {@link #scaling()} values from
   * those of the {@code other} frame.
   *
   * @see #matrix()
   * @see #setWorldMatrix(Frame)
   */
  public void setMatrix(Frame other) {
    if (other == null)
      return;
    setTranslation(other.translation());
    setRotation(other.rotation());
    setScaling(other.scaling());
  }

  /**
   * Returns a frame representing the inverse of this frame space transformation.
   * <p>
   * The the new frame {@link #rotation()} is the
   * {@link Quaternion#inverse()} of the original rotation. Its
   * {@link #translation()} is the negated inverse rotated image of the original
   * translation. Its {@link #scaling()} is 1 / original scaling.
   * <p>
   * If a frame is considered as a space rigid transformation, i.e., translation and
   * rotation, but no scaling (scaling=1), the inverse() frame performs the inverse
   * transformation.
   * <p>
   * Only the local frame transformation (i.e., defined with respect to the
   * {@link #reference()}) is inverted. Use {@link #worldInverse()} for a global
   * inverse.
   * <p>
   * The resulting frame has the same {@link #reference()} as the this frame and a
   * {@code null} {@link #constraint()}.
   *
   * @see #worldInverse()
   */
  public Frame inverse() {
    Frame frame = new Frame(graph());
    frame.setTranslation(Vector.multiply(rotation().inverseRotate(translation()), -1));
    frame.setRotation(rotation().inverse());
    frame.setScaling(1 / scaling());
    frame.setReference(reference());
    return frame;
  }

  /**
   * Returns the {@link #inverse()} of the frame world transformation.
   * <p>
   * The {@link #orientation()} of the new frame is the
   * {@link Quaternion#inverse()} of the original orientation. Its
   * {@link #position()} is the negated and inverse rotated image of the original
   * position. The {@link #magnitude()} is the the original magnitude multiplicative
   * inverse.
   * <p>
   * The result frame has a {@code null} {@link #reference()} and a {@code null}
   * {@link #constraint()}.
   * <p>
   * Use {@link #inverse()} for a local (i.e., with respect to {@link #reference()})
   * transformation inverse.
   *
   * @see #inverse()
   */
  public Frame worldInverse() {
    Frame frame = new Frame(graph());
    frame.setTranslation(Vector.multiply(orientation().inverseRotate(position()), -1));
    frame.setRotation(orientation().inverse());
    frame.setScaling(1 / magnitude());
    return frame;
  }

  // POINT CONVERSION

  /**
   * Returns the frame coordinates of the point whose position in the {@code from}
   * coordinate system is {@code src} (converts from {@code from} to this frame).
   * <p>
   * {@link #coordinatesOfIn(Vector, Frame)} performs the inverse transformation.
   */
  public Vector coordinatesOfFrom(Vector src, Frame from) {
    if (this == from)
      return src;
    else if (reference() != null)
      return localCoordinatesOf(reference().coordinatesOfFrom(src, from));
    else
      return localCoordinatesOf(from.inverseCoordinatesOf(src));
  }

  /**
   * Returns the {@code in} coordinates of the point whose position in the frame
   * coordinate system is {@code src} (converts from this frame to {@code in}).
   * <p>
   * {@link #coordinatesOfFrom(Vector, Frame)} performs the inverse transformation.
   */
  public Vector coordinatesOfIn(Vector vector, Frame in) {
    Frame fr = this;
    Vector res = vector;
    while ((fr != null) && (fr != in)) {
      res = fr.localInverseCoordinatesOf(res);
      fr = fr.reference();
    }

    if (fr != in)
      // in was not found in the branch of this, res is now expressed in the
      // world
      // coordinate system. Simply convert to in coordinate system.
      res = in.coordinatesOf(res);

    return res;
  }

  /**
   * Returns the frame coordinates of a point {@code src} defined in the
   * {@link #reference()} coordinate system (converts from {@link #reference()}
   * to this frame).
   * <p>
   * {@link #localInverseCoordinatesOf(Vector)} performs the inverse conversion.
   *
   * @see #localTransformOf(Vector)
   */
  public Vector localCoordinatesOf(Vector vector) {
    return Vector.divide(rotation().inverseRotate(Vector.subtract(vector, translation())), scaling());
  }

  /**
   * Returns the frame coordinates of a point {@code src} defined in the world coordinate
   * system (converts from world to this frame).
   * <p>
   * {@link #inverseCoordinatesOf(Vector)} performs the inverse conversion.
   * {@link #transformOf(Vector)} converts vectors instead of coordinates.
   */
  public Vector coordinatesOf(Vector vector) {
    if (reference() != null)
      return localCoordinatesOf(reference().coordinatesOf(vector));
    else
      return localCoordinatesOf(vector);
  }

  // VECTOR CONVERSION

  /**
   * Returns the frame transform of the vector whose coordinates in the {@code from}
   * coordinate system is {@code src} (converts vectors from {@code from} to this frame).
   * <p>
   * {@link #transformOfIn(Vector, Frame)} performs the inverse transformation.
   */
  public Vector transformOfFrom(Vector vector, Frame from) {
    if (this == from)
      return vector;
    else if (reference() != null)
      return localTransformOf(reference().transformOfFrom(vector, from));
    else
      return localTransformOf(from.inverseTransformOf(vector));
  }

  /**
   * Returns the {@code in} transform of the vector whose coordinates in the frame
   * coordinate system is {@code src} (converts vectors from this frame to {@code in}).
   * <p>
   * {@link #transformOfFrom(Vector, Frame)} performs the inverse transformation.
   */
  public Vector transformOfIn(Vector vector, Frame in) {
    Frame fr = this;
    Vector res = vector;
    while ((fr != null) && (fr != in)) {
      res = fr.localInverseTransformOf(res);
      fr = fr.reference();
    }

    if (fr != in)
      // in was not found in the branch of this, res is now expressed in
      // the world coordinate system. Simply convert to in coordinate system.
      res = in.transformOf(res);

    return res;
  }

  /**
   * Returns the {@link #reference()} coordinates of a point {@code src} defined in
   * the frame coordinate system (converts from this frame to {@link #reference()}).
   * <p>
   * {@link #localCoordinatesOf(Vector)} performs the inverse conversion.
   *
   * @see #localInverseTransformOf(Vector)
   */
  public Vector localInverseCoordinatesOf(Vector vector) {
    return Vector.add(rotation().rotate(Vector.multiply(vector, scaling())), translation());
  }

  /**
   * Returns the world coordinates of the point whose position in the frame coordinate
   * system is {@code src} (converts from this frame to world).
   * <p>
   * {@link #coordinatesOf(Vector)} performs the inverse conversion. Use
   * {@link #inverseTransformOf(Vector)} to transform vectors instead of coordinates.
   */
  public Vector inverseCoordinatesOf(Vector vector) {
    Frame fr = this;
    Vector res = vector;
    while (fr != null) {
      res = fr.localInverseCoordinatesOf(res);
      fr = fr.reference();
    }
    return res;
  }

  /**
   * Returns the frame transform of a vector {@code src} defined in the world coordinate
   * system (converts vectors from world to this frame).
   * <p>
   * {@link #inverseTransformOf(Vector)} performs the inverse transformation.
   * {@link #coordinatesOf(Vector)} converts coordinates instead of vectors (here only the
   * rotational part of the transformation is taken into account).
   */
  public Vector transformOf(Vector vector) {
    if (reference() != null)
      return localTransformOf(reference().transformOf(vector));
    else
      return localTransformOf(vector);
  }

  /**
   * Returns the world transform of the vector whose coordinates in the fFrame coordinate
   * system is {@code src} (converts vectors from this frame to world).
   * <p>
   * {@link #transformOf(Vector)} performs the inverse transformation. Use
   * {@link #inverseCoordinatesOf(Vector)} to transform coordinates instead of vectors.
   */
  public Vector inverseTransformOf(Vector vector) {
    Frame fr = this;
    Vector res = vector;
    while (fr != null) {
      res = fr.localInverseTransformOf(res);
      fr = fr.reference();
    }
    return res;
  }

  /**
   * Returns the frame transform of a vector {@code src} defined in the
   * {@link #reference()} coordinate system (converts vectors from
   * {@link #reference()} to this frame).
   * <p>
   * {@link #localInverseTransformOf(Vector)} performs the inverse transformation.
   *
   * @see #localCoordinatesOf(Vector)
   */
  public Vector localTransformOf(Vector vector) {
    return Vector.divide(rotation().inverseRotate(vector), scaling());
  }

  /**
   * Returns the {@link #reference()} transform of a vector {@code src} defined in
   * the Frame coordinate system (converts vectors from this frame to {@link #reference()}).
   * <p>
   * {@link #localTransformOf(Vector)} performs the inverse transformation.
   *
   * @see #localInverseCoordinatesOf(Vector)
   */
  public Vector localInverseTransformOf(Vector vector) {
    return rotation().rotate(Vector.multiply(vector, scaling()));
  }

  // MODIFIED

  /**
   * Internal use. Automatically call by all methods which change the node state.
   */
  protected void _modified() {
    _lastUpdate = TimingHandler.frameCount;
    if (children() != null)
      for (Frame child : children())
        child._modified();
  }

  // SYNC

  /**
   * Same as {@code sync(this, other)}.
   *
   * @see #sync(Frame, Frame)
   */
  public void sync(Frame other) {
    sync(this, other);
  }

  /**
   * If {@code frame1} has been more recently updated than {@code frame2}, calls
   * {@code frame2.setWorldMatrix(frame1)}, otherwise calls {@code frame1.setWorldMatrix(frame2)}.
   * Does nothing if both objects were updated at the same frame.
   * <p>
   * This method syncs only the global geometry attributes ({@link #position()},
   * {@link #orientation()} and {@link #magnitude()}) among the two nodes. The
   * {@link #reference()} and {@link #constraint()} (if any) of each node are kept
   * separately.
   *
   * @see #setWorldMatrix(Frame)
   */
  public static void sync(Frame frame1, Frame frame2) {
    if (frame1 == null || frame2 == null)
      return;
    if (frame1.lastUpdate() == frame2.lastUpdate())
      return;
    Frame source = (frame1.lastUpdate() > frame2.lastUpdate()) ? frame1 : frame2;
    Frame target = (frame1.lastUpdate() > frame2.lastUpdate()) ? frame2 : frame1;
    target.setWorldMatrix(source);
  }

  /**
   * @return the last frame the this obect was updated.
   */
  public long lastUpdate() {
    return _lastUpdate;
  }

  // GRAPH

  /**
   * Returns the reference frame, in which this frame is defined.
   * <p>
   * The frame {@link #translation()}, {@link #rotation()} and {@link #scaling()} are
   * defined with respect to the {@link #reference()} coordinate system. A
   * {@code null} reference frame (default value) means that the frame is defined in the
   * world coordinate system.
   * <p>
   * Use {@link #position()}, {@link #orientation()} and {@link #magnitude()} to
   * recursively convert values along the reference frame chain and to get values
   * expressed in the world coordinate system. The values match when the reference frame
   * is {@code null}.
   * <p>
   * Use {@link #setReference(Frame)} to set this value and create a frame hierarchy.
   * Convenient functions allow you to convert coordinates from one frame to another: see
   * {@link #coordinatesOf(Vector)}, {@link #localCoordinatesOf(Vector)} ,
   * {@link #coordinatesOfIn(Vector, Frame)} and their inverse functions.
   * <p>
   * Vectors can also be converted using {@link #transformOf(Vector)},
   * {@link #transformOfIn(Vector, Frame)}, {@link #localTransformOf(Vector)} and their inverse
   * functions.
   */
  public Frame reference() {
    return _reference;
  }

  /**
   * Sets the {@link #reference()} of the frame.
   * <p>
   * The frame {@link #translation()}, {@link #rotation()} and {@link #scaling()} are then
   * defined in the {@link #reference()} coordinate system.
   * <p>
   * Use {@link #position()}, {@link #orientation()} and {@link #magnitude()} to express
   * the frame global transformation in the world coordinate system.
   * <p>
   * Using this method, you can create a hierarchy of frames. This hierarchy needs to be a
   * tree, which root is the world coordinate system (i.e., {@code null}
   * {@link #reference()}). No action is performed if setting {@code reference} as the
   * {@link #reference()} would create a loop in the hierarchy.
   */
  public void setReference(Frame frame) {
    if (settingAsReferenceWillCreateALoop(frame)) {
      System.out.println("Frame.setReference would create a loop in Frame hierarchy. Nothing done.");
      return;
    }
    // 1. no need to re-parent, just check this needs to be added as leadingFrame
    if (reference() == frame) {
      _restorePath(reference(), this);
      return;
    }
    // 2. else re-parenting
    // 2a. before assigning new reference frame
    if (reference() != null) // old
      reference()._removeChild(this);
    else if (graph() != null)
      graph()._removeLeadingNode(this);
    // finally assign the reference frame
    _reference = frame;// reference() returns now the new value
    // 2b. after assigning new reference frame
    _restorePath(reference(), this);
    _modified();
  }

  protected void _restorePath(Frame parent, Frame child) {
    if (parent == null) {
      if (graph() != null)
        graph()._addLeadingNode(child);
    } else {
      if (!parent._hasChild(child)) {
        parent._addChild(child);
        _restorePath(parent.reference(), parent);
      }
    }
  }

  /**
   * Returns {@code true} if setting {@code frame} as the frame's
   * {@link #reference()} would create a loop in the frame hierarchy.
   */
  public boolean settingAsReferenceWillCreateALoop(Frame frame) {
    Frame f = frame;
    while (f != null) {
      if (f == this)
        return true;
      f = f.reference();
    }
    return false;
  }

  /**
   * Returns a list of the node children, i.e., nodes which {@link #reference()} is this.
   */
  public List<Frame> children() {
    return _children;
  }

  protected boolean _addChild(Frame frame) {
    if (frame == null)
      return false;
    if (_hasChild(frame))
      return false;
    return children().add(frame);
  }

  /**
   * Removes the leading frame if present. Typically used when re-parenting the frame.
   */
  protected boolean _removeChild(Frame frame) {
    boolean result = false;
    Iterator<Frame> it = children().iterator();
    while (it.hasNext()) {
      if (it.next() == frame) {
        it.remove();
        result = true;
        break;
      }
    }
    return result;
  }

  protected boolean _hasChild(Frame node) {
    for (Frame frame : children())
      if (frame == node)
        return true;
    return false;
  }

  /**
   * Procedure called on the node by the graph traversal algorithm. Default implementation is
   * empty, i.e., it is meant to be implemented by derived classes.
   * <p>
   * Hierarchical culling, i.e., culling of the node and its children, should be decided here.
   * Set the culling flag with {@link #cull(boolean)} according to your culling condition:
   * <p>
   * <pre>
   * {@code
   * node = new Frame(graph) {
   *   public void visit() {
   *     //hierarchical culling is optional and disabled by default
   *     cull(cullingCondition);
   *     if(!isCulled())
   *       // Draw your object here, in the local coordinate system.
   *   }
   * }
   * }
   * </pre>
   *
   * @see Graph#traverse()
   * @see #cull(boolean)
   * @see #isCulled()
   */
  public void visit() {
  }

  /**
   * Same as {@code cull(true)}.
   *
   * @see #cull(boolean)
   * @see #isCulled()
   */

  public void cull() {
    cull(true);
  }

  /**
   * Enables or disables {@link #visit()} of this node and its children during
   * {@link Graph#traverse()}. Culling should be decided within {@link #visit()}.
   *
   * @see #isCulled()
   */
  public void cull(boolean cull) {
    _culled = cull;
  }

  /**
   * Returns whether or not the node culled or not. Culled nodes (and their children)
   * will not be visited by the {@link Graph#traverse()} algoruthm.
   *
   * @see #cull(boolean)
   */
  public boolean isCulled() {
    return _culled;
  }

  /**
   * Returns the graph this node belongs to.
   *
   * @see Graph#eye()
   */
  public Graph graph() {
    return _graph;
  }

  /**
   * Returns true if this node is the {@link Graph#eye()}, and false otherwise.
   *
   * @see Graph#setEye(Frame)
   * @see Graph#eye()
   */
  public boolean isEye() {
    return graph().eye() == this;
  }

  @Override
  public boolean track(Event event) {
    if (event instanceof KeyEvent)
      return track((KeyEvent) event);
    if (event instanceof TapEvent)
      return track((TapEvent) event);
    if (event instanceof MotionEvent)
      return track((MotionEvent) event);
    return false;
  }

  /**
   * Internal use. You don't need to call this. Automatically called by agents handling this node.
   */
  public boolean track(MotionEvent motionEvent) {
    if (isEye())
      return false;
    if (motionEvent instanceof MotionEvent1)
      return track((MotionEvent1) motionEvent);
    if (motionEvent instanceof MotionEvent2)
      return track((MotionEvent2) motionEvent);
    if (motionEvent instanceof MotionEvent3)
      return track((MotionEvent3) motionEvent);
    if (motionEvent instanceof MotionEvent6)
      return track((MotionEvent6) motionEvent);
    return false;
  }

  /**
   * Internal use. You don't need to call this. Automatically called by agents handling this node.
   */
  public boolean track(TapEvent tapEvent) {
    if (isEye())
      return false;
    return track(tapEvent.x(), tapEvent.y());
  }

  /**
   * Internal use. You don't need to call this. Automatically called by agents handling this node.
   * <p>
   * Override this method when you want the object to be picked from a {@link KeyEvent}.
   */
  public boolean track(KeyEvent keyEvent) {
    return false;
  }

  /**
   * Internal use. You don't need to call this. Automatically called by agents handling this node.
   * <p>
   * Override this method when you want the object to be picked from a {@link MotionEvent1}.
   */
  public boolean track(MotionEvent1 motionEvent1) {
    return false;
  }

  /**
   * Internal use. You don't need to call this. Automatically called by agents handling this node.
   */
  public boolean track(MotionEvent2 motionEvent2) {
    if (isEye())
      return false;
    if (motionEvent2.isAbsolute()) {
      System.out.println("track(Event) requires a relative motion-event");
      return false;
    }
    return track(motionEvent2.x(), motionEvent2.y());
  }

  /**
   * Picks the node according to the {@link #precision()}.
   *
   * @see #precision()
   * @see #setPrecision(Precision)
   */
  public boolean track(float x, float y) {
    Vector proj = _graph.projectedCoordinatesOf(position());
    float halfThreshold = precisionThreshold() / 2;
    return ((Math.abs(x - proj._vector[0]) < halfThreshold) && (Math.abs(y - proj._vector[1]) < halfThreshold));
  }

  /**
   * Internal use. You don't need to call this. Automatically called by agents handling this node.
   */
  public boolean track(MotionEvent3 motionEvent3) {
    return track(motionEvent3.event2());
  }

  /**
   * Internal use. You don't need to call this. Automatically called by agents handling this node.
   */
  public boolean track(MotionEvent6 motionEvent6) {
    return track(motionEvent6.event3().event2());
  }

  @Override
  public void interact(Event event) {
    if (event instanceof TapEvent)
      interact((TapEvent) event);
    if (event instanceof MotionEvent)
      interact((MotionEvent) event);
    if (event instanceof KeyEvent)
      interact((KeyEvent) event);
  }

  /**
   * Calls interact() on the proper motion event:
   * {@link MotionEvent1}, {@link MotionEvent2},
   * {@link MotionEvent3} or {@link MotionEvent6}.
   * <p>
   * Override this method when you want the object to perform an interaction from a
   * {@link frames.input.event.MotionEvent}.
   */
  protected void interact(MotionEvent motionEvent) {
    if (motionEvent instanceof MotionEvent1)
      interact((MotionEvent1) motionEvent);
    if (motionEvent instanceof MotionEvent2)
      interact((MotionEvent2) motionEvent);
    if (motionEvent instanceof MotionEvent3)
      interact((MotionEvent3) motionEvent);
    if (motionEvent instanceof MotionEvent6)
      interact((MotionEvent6) motionEvent);
  }

  /**
   * Override this method when you want the object to perform an interaction from a
   * {@link MotionEvent1}.
   */
  protected void interact(MotionEvent1 motionEvent1) {
  }

  /**
   * Override this method when you want the object to perform an interaction from a
   * {@link MotionEvent2}.
   */
  protected void interact(MotionEvent2 motionEvent2) {
  }

  /**
   * Override this method when you want the object to perform an interaction from a
   * {@link MotionEvent3}.
   */
  protected void interact(MotionEvent3 motionEvent3) {
  }

  /**
   * Override this method when you want the object to perform an interaction from a
   * {@link MotionEvent6}.
   */
  protected void interact(MotionEvent6 motionEvent6) {
  }

  /**
   * Override this method when you want the object to perform an interaction from a
   * {@link TapEvent}.
   */
  protected void interact(TapEvent tapEvent) {
  }

  /**
   * Override this method when you want the object to perform an interaction from a
   * {@link KeyEvent}.
   */
  protected void interact(KeyEvent keyEvent) {
  }

  // APPLY TRANSFORMATION

  /**
   * Convenience function that simply calls {@code applyTransformation(graph())}. It applies
   * the transformation defined by this node to {@link #graph()}.
   *
   * @see #applyTransformation(Graph)
   * @see #matrix()
   * @see #graph()
   */
  public void applyTransformation() {
    applyTransformation(graph());
  }

  /**
   * Convenience function that simply calls {@code applyWorldTransformation(graph())}. It
   * applies the world transformation defined by this node to {@link #graph()}.
   *
   * @see #applyWorldTransformation(Graph)
   * @see #worldMatrix()
   * @see #graph()
   */
  public void applyWorldTransformation() {
    applyWorldTransformation(graph());
  }

  /**
   * Convenience function that simply calls {@code graph.applyTransformation(this)}. You may
   * apply the transformation represented by this node to any graph you want using this
   * method.
   * <p>
   * Very efficient prefer always this than
   *
   * @see #applyTransformation()
   * @see #matrix()
   * @see Graph#applyTransformation(Frame)
   */
  public void applyTransformation(Graph graph) {
    graph.applyTransformation(this);
  }

  /**
   * Convenience function that simply calls {@code graph.applyWorldTransformation(this)}.
   * You may apply the world transformation represented by this node to any graph you
   * want using this method.
   *
   * @see #applyWorldTransformation()
   * @see #worldMatrix()
   * @see Graph#applyWorldTransformation(Frame)
   */
  public void applyWorldTransformation(Graph graph) {
    graph.applyWorldTransformation(this);
  }

  // Fx

  /**
   * Internal use.
   * <p>
   * Returns the cached value of the spinning friction used in
   * {@link #_recomputeSpinningQuaternion()}.
   */
  protected float _dampingFx() {
    return _spiningFriction;
  }

  /**
   * Defines the spinning deceleration.
   * <p>
   * Default value is 0.5. Use {@link #setDamping(float)} to tune this value. A higher
   * value will make damping more difficult (a value of 1 forbids damping).
   */
  public float damping() {
    return _dampFriction;
  }

  /**
   * Defines the {@link #damping()}. Values must be in the range [0..1].
   */
  public void setDamping(float damping) {
    if (damping < 0 || damping > 1)
      return;
    _dampFriction = damping;
    _setDampingFx(_dampFriction);
  }

  /**
   * Internal use.
   * <p>
   * Computes and caches the value of the spinning friction used in
   * {@link #_recomputeSpinningQuaternion()}.
   */
  protected void _setDampingFx(float friction) {
    _spiningFriction = friction * friction * friction;
  }

  /**
   * Defines the {@link #rotationSensitivity()}.
   */
  public void setRotationSensitivity(float sensitivity) {
    _rotationSensitivity = sensitivity;
  }

  /**
   * Defines the {@link #scalingSensitivity()}.
   */
  public void setScalingSensitivity(float sensitivity) {
    _scalingSensitivity = sensitivity;
  }

  /**
   * Defines the {@link #translationSensitivity()}.
   */
  public void setTranslationSensitivity(float sensitivity) {
    _translationSensitivity = sensitivity;
  }

  /**
   * Defines the {@link #spinningSensitivity()}.
   */
  public void setSpinningSensitivity(float sensitivity) {
    _spinningSensitivity = sensitivity;
  }

  /**
   * Defines the {@link #wheelSensitivity()}.
   */
  public void setWheelSensitivity(float sensitivity) {
    _wheelSensitivity = sensitivity;
  }

  /**
   * Defines the {@link #keySensitivity()}.
   */
  public void setKeySensitivity(float sensitivity) {
    _keySensitivity = sensitivity;
  }

  /**
   * Returns the influence of a gesture displacement on the node rotation.
   * <p>
   * Default value is 1 (for instance matching an identical mouse displacement), a higher
   * value will generate a larger rotation (and inversely for lower values). A 0 value will
   * forbid rotation (see also {@link #constraint()}).
   *
   * @see #setRotationSensitivity(float)
   * @see #translationSensitivity()
   * @see #scalingSensitivity()
   * @see #keySensitivity()
   * @see #spinningSensitivity()
   * @see #wheelSensitivity()
   */
  public float rotationSensitivity() {
    return _rotationSensitivity;
  }

  /**
   * Returns the influence of a gesture displacement on the node scaling.
   * <p>
   * Default value is 1, a higher value will generate a larger scaling (and inversely
   * for lower values). A 0 value will forbid scaling (see also {@link #constraint()}).
   *
   * @see #setScalingSensitivity(float)
   * @see #setRotationSensitivity(float)
   * @see #translationSensitivity()
   * @see #keySensitivity()
   * @see #spinningSensitivity()
   * @see #wheelSensitivity()
   */
  public float scalingSensitivity() {
    return _scalingSensitivity;
  }

  /**
   * Returns the influence of a gesture displacement on the node translation.
   * <p>
   * Default value is 1 which in the case of a mouse interaction makes the node
   * precisely stays under the mouse cursor.
   * <p>
   * With an identical gesture displacement, a higher value will generate a larger
   * translation (and inversely for lower values). A 0 value will forbid translation
   * (see also {@link #constraint()}).
   *
   * @see #setTranslationSensitivity(float)
   * @see #rotationSensitivity()
   * @see #scalingSensitivity()
   * @see #keySensitivity()
   * @see #spinningSensitivity()
   * @see #wheelSensitivity()
   */
  public float translationSensitivity() {
    return _translationSensitivity;
  }

  /**
   * Returns the minimum gesture speed required to make the node spin.
   * Spinning requires to set {@link #damping()} to 0.
   * <p>
   * See {@link #_spin()}, {@link #spinningQuaternion()} and
   * {@link #_startSpinning(MotionEvent, Quaternion)} for details.
   * <p>
   * Gesture speed is expressed in pixels per milliseconds. Default value is 0.3 (300
   * pixels per second). Use {@link #setSpinningSensitivity(float)} to tune this value. A
   * higher value will make spinning more difficult (a value of 100 forbids spinning in
   * practice).
   *
   * @see #setSpinningSensitivity(float)
   * @see #translationSensitivity()
   * @see #rotationSensitivity()
   * @see #scalingSensitivity()
   * @see #keySensitivity()
   * @see #wheelSensitivity()
   * @see #setDamping(float)
   */
  public float spinningSensitivity() {
    return _spinningSensitivity;
  }

  /**
   * Returns the wheel sensitivity.
   * <p>
   * Default value is 15. A higher value will make the wheel action more efficient
   * (usually meaning faster motion). Use a negative value to invert the operation
   * direction.
   *
   * @see #setWheelSensitivity(float)
   * @see #translationSensitivity()
   * @see #rotationSensitivity()
   * @see #scalingSensitivity()
   * @see #keySensitivity()
   * @see #spinningSensitivity()
   */
  public float wheelSensitivity() {
    return _wheelSensitivity;
  }

  /**
   * Returns the keyboard sensitivity.
   * <p>
   * Default value is 10. A higher value will make the keyboard more efficient (usually
   * meaning faster motion).
   *
   * @see #setKeySensitivity(float)
   * @see #translationSensitivity()
   * @see #rotationSensitivity()
   * @see #scalingSensitivity()
   * @see #wheelSensitivity()
   * @see #setDamping(float)
   */
  public float keySensitivity() {
    return _keySensitivity;
  }

  /**
   * Returns {@code true} when the node is spinning.
   * <p>
   * During spinning, {@link #_spin()} rotates the node by its
   * {@link #spinningQuaternion()} at a frequency defined when the node
   * {@link #_startSpinning(MotionEvent, Quaternion)}.
   * <p>
   * Use {@link #_startSpinning(MotionEvent, Quaternion)} and {@link #stopSpinning()} to
   * change this state. Default value is {@code false}.
   *
   * @see #isFlying()
   */
  public boolean isSpinning() {
    return _spinningTask.isActive();
  }

  /**
   * Returns the incremental rotation that is applied by {@link #_spin()} to the
   * node orientation when it {@link #isSpinning()}.
   * <p>
   * Default value is a {@code null} quaternion. Use {@link #setSpinningQuaternion(Quaternion)}
   * to change this value.
   * <p>
   * The {@link #spinningQuaternion()} axis is defined in the node coordinate
   * system. You can use {@link #transformOfFrom(Vector, Frame)}
   * to convert this axis from another coordinate system.
   * <p>
   * <b>Attention: </b>Spinning may be decelerated according to {@link #damping()} till it
   * stops completely.
   *
   * @see #flyDirection()
   */
  public Quaternion spinningQuaternion() {
    return _spinningQuaternion;
  }

  /**
   * Defines the {@link #spinningQuaternion()}. Its axis is defined in the node
   * coordinate system.
   *
   * @see #setFlyDirection(Vector)
   */
  public void setSpinningQuaternion(Quaternion spinningQuaternion) {
    _spinningQuaternion = spinningQuaternion;
  }

  /**
   * Stops the spinning motion _started using {@link #_startSpinning(MotionEvent, Quaternion)}.
   * Note that {@link #isSpinning()} will return {@code false} after this call.
   * <p>
   * <b>Attention: </b>This method may be called by {@link #_spin()}, since spinning may be
   * decelerated according to {@link #damping()} till it stops completely.
   *
   * @see #damping()
   */
  public void stopSpinning() {
    _spinningTask.stop();
  }

  /**
   * Internal use. Same as {@code _startSpinning(rt, _event._speed(), _event._delay())}.
   *
   * @see #_startFlying(MotionEvent, Vector)
   * @see #startSpinning(Quaternion, float, long)
   */
  protected void _startSpinning(MotionEvent event, Quaternion quaternion) {
    startSpinning(quaternion, event.speed(), event.delay());
  }

  /**
   * Starts the spinning of the node.
   * <p>
   * This method starts a timer that will call {@link #_spin()} every
   * {@code updateInterval} milliseconds. The node {@link #isSpinning()} until
   * you call {@link #stopSpinning()}.
   * <p>
   * <b>Attention: </b>Spinning may be decelerated according to {@link #damping()} till it
   * stops completely.
   *
   * @see #damping()
   * @see #startFlying(Vector, float)
   */
  public void startSpinning(Quaternion quaternion, float speed, long delay) {
    setSpinningQuaternion(quaternion);
    _eventSpeed = speed;
    _eventDelay = delay;
    if (damping() == 0 && _eventSpeed < spinningSensitivity())
      return;
    int updateInterval = (int) delay;
    if (updateInterval > 0)
      _spinningTask.run(updateInterval);
  }

  /**
   * Cache version. Used by rotate methods when damping is 0.
   */
  protected void _startSpinning() {
    startSpinning(spinningQuaternion(), _eventSpeed, _eventDelay);
  }

  protected void _spinExecution() {
    if (damping() == 0)
      _spin();
    else {
      if (_eventSpeed == 0) {
        stopSpinning();
        return;
      }
      _spin();
      _recomputeSpinningQuaternion();
    }
  }

  protected void _spin(Quaternion quaternion, float speed, long delay) {
    if (damping() == 0) {
      _spin(quaternion);
      _eventSpeed = speed;
      _eventDelay = delay;
    } else
      startSpinning(quaternion, speed, delay);
  }

  protected void _spin(Quaternion quaternion) {
    setSpinningQuaternion(quaternion);
    _spin();
  }

  /**
   * Rotates the node by its {@link #spinningQuaternion()} or around the {@link Graph#anchor()}
   * when this node is the {@link Graph#eye()}. Called by a timer when the node {@link #isSpinning()}.
   * <p>
   * <b>Attention: </b>Spinning may be decelerated according to {@link #damping()} till it
   * stops completely.
   *
   * @see #damping()
   */
  protected void _spin() {
    if (isEye())
      rotateAroundPoint(spinningQuaternion(), graph().anchor());
    else
      rotate(spinningQuaternion());
  }

  /**
   * Internal method. Recomputes the {@link #spinningQuaternion()} according to {@link #damping()}.
   */
  protected void _recomputeSpinningQuaternion() {
    float prevSpeed = _eventSpeed;
    float damping = 1.0f - _dampingFx();
    _eventSpeed *= damping;
    if (Math.abs(_eventSpeed) < .001f)
      _eventSpeed = 0;
    // float currSpeed = eventSpeed;
    spinningQuaternion().fromAxisAngle((spinningQuaternion()).axis(), spinningQuaternion().angle() * (_eventSpeed / prevSpeed));
  }

  protected int _originalDirection(MotionEvent event) {
    return _originalDirection(event, true);
  }

  protected int _originalDirection(MotionEvent event, boolean fromX) {
    MotionEvent2 motionEvent2 = MotionEvent.event2(event, fromX);
    if (motionEvent2 != null)
      return _originalDirection(motionEvent2);
    else
      return 0;
  }

  /**
   * Return 1 if mouse motion was started horizontally and -1 if it was more vertical.
   * Returns 0 if this could not be determined yet (perfect diagonal motion, rare).
   */
  protected int _originalDirection(MotionEvent2 event) {
    if (!_directionIsFixed) {
      Point delta = new Point(event.dx(), event.dy());
      _directionIsFixed = Math.abs(delta.x()) != Math.abs(delta.y());
      _horizontal = Math.abs(delta.x()) > Math.abs(delta.y());
    }

    if (_directionIsFixed)
      if (_horizontal)
        return 1;
      else
        return -1;
    else
      return 0;
  }

  /**
   * Returns a quaternion computed according to the 2-DOF gesture motion, such as those gathered
   * from mice (mouse positions are projected on a deformed ball, centered on ({@code center.x()},
   * {@code center.y()})).
   */
  protected Quaternion _deformedBallQuaternion(MotionEvent2 event, Vector center) {
    if (event.isAbsolute()) {
      System.out.println("deformedBallQuaternion(Event) requires a relative motion-event");
      return null;
    }
    float cx = center.x();
    float cy = center.y();
    float x = event.x();
    float y = event.y();
    float prevX = event.previousX();
    float prevY = event.previousY();
    // Points on the deformed ball
    float px = rotationSensitivity() * ((int) prevX - cx) / _graph.width();
    float py =
        rotationSensitivity() * (_graph.isLeftHanded() ? ((int) prevY - cy) : (cy - (int) prevY)) / _graph.height();
    float dx = rotationSensitivity() * (x - cx) / _graph.width();
    float dy = rotationSensitivity() * (_graph.isLeftHanded() ? (y - cy) : (cy - y)) / _graph.height();

    Vector p1 = new Vector(px, py, _projectOnBall(px, py));
    Vector p2 = new Vector(dx, dy, _projectOnBall(dx, dy));
    // Approximation of rotation angle Should be divided by the _projectOnBall
    // size, but it is 1.0
    Vector axis = p2.cross(p1);
    float angle = 2.0f * (float) Math.asin((float) Math.sqrt(axis.squaredNorm() / p1.squaredNorm() / p2.squaredNorm()));
    return new Quaternion(axis, angle);
  }

  /**
   * Returns "pseudo-_distance" from (x,y) to ball of radius size. For a point inside the
   * ball, it is proportional to the euclidean distance to the ball. For a point outside
   * the ball, it is proportional to the inverse of this distance (tends to zero) on the
   * ball, the function is continuous.
   */
  protected float _projectOnBall(float x, float y) {
    // If you change the size value, change angle computation in
    // deformedBallQuaternion().
    float size = 1.0f;
    float size2 = size * size;
    float size_limit = size2 * 0.5f;

    float d = x * x + y * y;
    return d < size_limit ? (float) Math.sqrt(size2 - d) : size_limit / (float) Math.sqrt(d);
  }

  // macro's

  protected float _computeAngle(MotionEvent1 event) {
    return _computeAngle(event.dx());
  }

  protected float _computeAngle() {
    return _computeAngle(1);
  }

  protected float _computeAngle(float dx) {
    return dx * (float) Math.PI / _graph.width();
  }

  protected boolean _wheel(MotionEvent event) {
    return event instanceof MotionEvent1;
  }

  /**
   * Wrapper method for {@link #alignWithFrame(Frame, boolean, float)} that discriminates
   * between eye and non-eye nodes.
   *
   * @see #isEye()
   */
  public void align() {
    if (isEye())
      alignWithFrame(null, true);
    else
      alignWithFrame(_graph.eye());
  }

  /**
   * Centers the node into the graph.
   */
  public void center() {
    if (isEye())
      projectOnLine(graph().center(), graph().viewDirection());
    else
      projectOnLine(_graph.eye().position(), _graph.eye().zAxis(false));
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void translateX(Event event) {
    if (event instanceof MotionEvent)
      translateX((MotionEvent) event);
    else
      System.out.println("translateX(Event) requires a motion event");
  }

  /**
   * User gesture into x-translation conversion routine.
   */
  public void translateX(MotionEvent event) {
    translateX(event, true);
  }

  /**
   * User gesture into x-translation conversion routine.
   */
  public void translateX(MotionEvent event, boolean fromX) {
    MotionEvent1 motionEvent1 = MotionEvent.event1(event, fromX);
    if (motionEvent1 != null)
      _translateX(motionEvent1, _wheel(event) ? this.wheelSensitivity() : this.translationSensitivity());
  }

  /**
   * User gesture into x-translation conversion routine.
   */
  public void translateX(MotionEvent1 event) {
    _translateX(event, _wheel(event) ? this.wheelSensitivity() : this.translationSensitivity());
  }

  /**
   * User gesture into x-translation conversion routine.
   */
  protected void _translateX(MotionEvent1 event, float sensitivity) {
    translate(screenToVector(Vector.multiply(new Vector(isEye() ? -event.dx() : event.dx(), 0, 0), sensitivity)));
  }

  /**
   * User gesture into x-translation conversion routine.
   */
  public void translateXPos() {
    _translateX(true);
  }

  /**
   * User gesture into x-translation conversion routine.
   */
  public void translateXNeg() {
    _translateX(false);
  }

  /**
   * User gesture into x-translation conversion routine.
   */
  protected void _translateX(boolean right) {
    translate(screenToVector(
        Vector.multiply(new Vector(1, 0), (right ^ this.isEye()) ? keySensitivity() : -keySensitivity())));
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void translateY(Event event) {
    if (event instanceof MotionEvent)
      translateY((MotionEvent) event);
    else
      System.out.println("translateY(Event) requires a motion event");
  }

  /**
   * User gesture into x-translation conversion routine.
   */
  public void translateY(MotionEvent event) {
    translateY(event, false);
  }

  /**
   * User gesture into y-translation conversion routine.
   */
  public void translateY(MotionEvent event, boolean fromX) {
    MotionEvent1 motionEvent1 = MotionEvent.event1(event, fromX);
    if (motionEvent1 != null)
      _translateY(motionEvent1, _wheel(event) ? this.wheelSensitivity() : this.translationSensitivity());
  }

  /**
   * User gesture into y-translation conversion routine.
   */
  public void translateY(MotionEvent1 event) {
    _translateY(event, _wheel(event) ? this.wheelSensitivity() : this.translationSensitivity());
  }

  /**
   * User gesture into y-translation conversion routine.
   */
  protected void _translateY(MotionEvent1 event, float sensitivity) {
    translate(
        screenToVector(Vector.multiply(new Vector(0, isEye() ^ _graph.isRightHanded() ? -event.dx() : event.dx()), sensitivity)));
  }

  /**
   * User gesture into y-translation conversion routine.
   */
  public void translateYPos() {
    _translateY(true);
  }

  /**
   * User gesture into y-translation conversion routine.
   */
  public void translateYNeg() {
    _translateY(false);
  }

  /**
   * User gesture into y-translation conversion routine.
   */
  protected void _translateY(boolean up) {
    translate(screenToVector(
        Vector.multiply(new Vector(0, (up ^ this.isEye() ^ _graph.isLeftHanded()) ? 1 : -1), this.keySensitivity())));
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void translateZ(Event event) {
    if (event instanceof MotionEvent)
      translateZ((MotionEvent) event);
    else
      System.out.println("translateZ(Event) requires a motion event");
  }

  /**
   * User gesture into z-translation conversion routine.
   */
  public void translateZ(MotionEvent event) {
    translateZ(event, true);
  }

  /**
   * User gesture into z-translation conversion routine.
   */
  public void translateZ(MotionEvent event, boolean fromX) {
    MotionEvent1 motionEvent1 = MotionEvent.event1(event, fromX);
    if (motionEvent1 != null)
      _translateZ(motionEvent1, _wheel(event) ? this.wheelSensitivity() : this.translationSensitivity());
  }

  /**
   * User gesture into z-translation conversion routine.
   */
  public void translateZ(MotionEvent1 event) {
    _translateZ(event, _wheel(event) ? this.wheelSensitivity() : this.translationSensitivity());
  }

  /**
   * User gesture into z-translation conversion routine.
   */
  protected void _translateZ(MotionEvent1 event, float sensitivity) {
    translate(screenToVector(Vector.multiply(new Vector(0.0f, 0.0f, isEye() ? -event.dx() : event.dx()), sensitivity)));
  }

  /**
   * User gesture into z-translation conversion routine.
   */
  public void translateZPos() {
    _translateZ(true);
  }

  /**
   * User gesture into z-translation conversion routine.
   */
  public void translateZNeg() {
    _translateZ(false);
  }

  /**
   * User gesture into z-translation conversion routine.
   */
  protected void _translateZ(boolean up) {
    translate(screenToVector(
        Vector.multiply(new Vector(0.0f, 0.0f, 1), (up ^ this.isEye()) ? -keySensitivity() : keySensitivity())));
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void translate(Event event) {
    if (event instanceof MotionEvent)
      translate((MotionEvent) event, true);
    else
      System.out.println("translate(Event) requires a motion event");
  }

  /**
   * User gesture into xy-translation conversion routine.
   */
  public void translate(MotionEvent event) {
    translate(event, true);
  }

  /**
   * User gesture into xy-translation conversion routine.
   */
  public void translate(MotionEvent event, boolean fromX) {
    MotionEvent2 motionEvent2 = MotionEvent.event2(event, fromX);
    if (motionEvent2 != null)
      translate(motionEvent2);
    else
      System.out.println("translate(Event) requires a motion event of at least 2 DOFs");
  }

  /**
   * User gesture into xy-translation conversion routine.
   */
  public void translate(MotionEvent2 event) {
    translate(screenToVector(Vector.multiply(new Vector(isEye() ? -event.dx() : event.dx(),
        (_graph.isRightHanded() ^ isEye()) ? -event.dy() : event.dy(), 0.0f), this.translationSensitivity())));
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void translateXYZ(Event event) {
    if (event instanceof MotionEvent)
      translateXYZ((MotionEvent) event);
    else
      System.out.println("translateXYZ(Event) requires a motion event");
  }

  /**
   * User gesture into xyz-translation conversion routine.
   */
  public void translateXYZ(MotionEvent event) {
    MotionEvent3 motionEvent3 = MotionEvent.event3(event, true);
    if (motionEvent3 != null)
      translateXYZ(motionEvent3);
    else
      System.out.println("translateXYZ(Event) requires a motion event of at least 3 DOFs");
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void translateRotateXYZ(Event event) {
    if (event instanceof MotionEvent)
      translateRotateXYZ((MotionEvent) event);
    else
      System.out.println("translateY(Event) requires a motion event");
  }

  /**
   * User gesture into xyz-translation and rotation conversion routine.
   */
  public void translateRotateXYZ(MotionEvent event) {
    translateXYZ(event);
    // B. Rotate the iFrame
    rotateXYZ(event);
  }

  /**
   * User gesture into xyz-translation conversion routine.
   */
  public void translateXYZ(MotionEvent3 event) {
    translate(screenToVector(
        Vector.multiply(new Vector(event.dx(), _graph.isRightHanded() ? -event.dy() : event.dy(), -event.dz()),
            this.translationSensitivity())));
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void zoomOnAnchor(Event event) {
    if (event instanceof MotionEvent)
      zoomOnAnchor((MotionEvent) event);
    else
      System.out.println("zoomOnAnchor(Event) requires a motion event");
  }

  /**
   * User gesture into zoom-on-anchor conversion routine.
   *
   * @see Graph#anchor()
   */
  public void zoomOnAnchor(MotionEvent event) {
    zoomOnAnchor(event, true);
  }

  /**
   * User gesture into zoom-on-anchor conversion routine.
   *
   * @see Graph#anchor()
   */
  public void zoomOnAnchor(MotionEvent event, boolean fromX) {
    MotionEvent1 motionEvent1 = MotionEvent.event1(event, fromX);
    if (motionEvent1 != null)
      _zoomOnAnchor(motionEvent1, _wheel(event) ? this.wheelSensitivity() : this.translationSensitivity());
  }

  /**
   * User gesture into zoom-on-anchor conversion routine.
   *
   * @see Graph#anchor()
   */
  protected void _zoomOnAnchor(MotionEvent1 event, float sensitivity) {
    Vector direction = Vector.subtract(_graph.anchor(), position());
    if (reference() != null)
      direction = reference().transformOf(direction);
    float delta = event.dx() * sensitivity / _graph.height();
    if (direction.magnitude() > 0.02f * _graph.radius() || delta > 0.0f)
      translate(Vector.multiply(direction, delta));
  }

  /**
   * User gesture into zoom-on-anchor conversion routine.
   *
   * @see Graph#anchor()
   */
  public void zoomOnAnchorPos() {
    _zoomOnAnchor(true);
  }

  /**
   * User gesture into zoom-on-anchor conversion routine.
   *
   * @see Graph#anchor()
   */
  public void zoomOnAnchorNeg() {
    _zoomOnAnchor(false);
  }

  /**
   * User gesture into zoom-on-anchor conversion routine.
   *
   * @see Graph#anchor()
   */
  protected void _zoomOnAnchor(boolean in) {
    Vector direction = Vector.subtract(_graph.anchor(), position());
    if (reference() != null)
      direction = reference().transformOf(direction);
    float delta = (in ? keySensitivity() : -keySensitivity()) / _graph.height();
    if (direction.magnitude() > 0.02f * _graph.radius() || delta > 0.0f)
      translate(Vector.multiply(direction, delta));
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void zoomOnRegion(Event event) {
    if (event instanceof MotionEvent)
      zoomOnRegion((MotionEvent) event);
    else
      System.out.println("zoomOnRegion(Event) requires a motion event");
  }

  /**
   * User gesture into zoom-on-region conversion routine.
   */
  public void zoomOnRegion(MotionEvent event) {
    MotionEvent2 dof2 = MotionEvent.event2(event);
    if (dof2 == null) {
      System.out.println("zoomOnRegion(Event) requires a motion event of at least 2 DOFs");
      return;
    }
    zoomOnRegion(dof2);
  }

  /**
   * User gesture into zoom-on-region conversion routine.
   */
  public void zoomOnRegion(MotionEvent2 event) {
    if (!isEye()) {
      return;
    }
    if (event.isAbsolute()) {
      System.out.println("zoomOnRegion(Event) requires a relative motion-event");
      return;
    }
    if (event.fired()) {
      _initEvent = event.get();
      //TODO handle me
      //graph.setZoomVisualHint(true);
    } else if (event.flushed()) {
      MotionEvent2 e = new MotionEvent2(_initEvent.get(), event.x(), event.y(), event.modifiers(), event.id());
      //TODO handle me
      //graph.setZoomVisualHint(false);
      int w = (int) Math.abs(e.dx());
      int tlX = (int) e.previousX() < (int) e.x() ? (int) e.previousX() : (int) e.x();
      int h = (int) Math.abs(e.dy());
      int tlY = (int) e.previousY() < (int) e.y() ? (int) e.previousY() : (int) e.y();
      graph().fitScreenRegionInterpolation(new Rectangle(tlX, tlY, w, h));
      //graph().fitScreenRegion(new Rectangle(tlX, tlY, w, h));
    }
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void rotateX(Event event) {
    if (event instanceof MotionEvent)
      rotateX((MotionEvent) event);
    else
      System.out.println("rotateX(Event) requires a motion event");
  }

  /**
   * User gesture into x-rotation conversion routine.
   */
  public void rotateX(MotionEvent event) {
    rotateX(event, false);
  }

  /**
   * User gesture into x-rotation conversion routine.
   */
  public void rotateX(MotionEvent event, boolean fromX) {
    MotionEvent1 motionEvent1 = MotionEvent.event1(event, fromX);
    if (motionEvent1 != null)
      _rotateX(motionEvent1, _wheel(event) ? this.wheelSensitivity() : this.rotationSensitivity());
  }

  /**
   * User gesture into x-rotation conversion routine.
   */
  public void rotateX(MotionEvent1 event) {
    _rotateX(event, _wheel(event) ? this.wheelSensitivity() : this.translationSensitivity());
  }

  /**
   * User gesture into x-rotation conversion routine.
   */
  protected void _rotateX(MotionEvent1 event, float sensitivity) {
    _spin(screenToQuaternion(_computeAngle(event) * (isEye() ? -sensitivity : sensitivity), 0, 0), event.speed(), event.delay());
  }

  /**
   * User gesture into x-rotation conversion routine.
   */
  public void rotateXPos() {
    _rotateX(true);
  }

  /**
   * User gesture into x-rotation conversion routine.
   */
  public void rotateXNeg() {
    _rotateX(false);
  }

  /**
   * User gesture into x-rotation conversion routine.
   */
  protected void _rotateX(boolean up) {
    rotate(screenToQuaternion(_computeAngle() * (up ? keySensitivity() : -keySensitivity()), 0, 0));
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void rotateY(Event event) {
    if (event instanceof MotionEvent)
      rotateY((MotionEvent) event);
    else
      System.out.println("rotateY(Event) requires a motion event");
  }

  /**
   * User gesture into y-rotation conversion routine.
   */
  public void rotateY(MotionEvent event) {
    rotateY(event, true);
  }

  /**
   * User gesture into y-rotation conversion routine.
   */
  public void rotateY(MotionEvent event, boolean fromX) {
    MotionEvent1 motionEvent1 = MotionEvent.event1(event, fromX);
    if (motionEvent1 != null)
      _rotateY(motionEvent1, _wheel(event) ? this.wheelSensitivity() : this.rotationSensitivity());
  }

  /**
   * User gesture into y-rotation conversion routine.
   */
  public void rotateY(MotionEvent1 event) {
    _rotateY(event, _wheel(event) ? this.wheelSensitivity() : this.translationSensitivity());
  }

  /**
   * User gesture into y-rotation conversion routine.
   */
  protected void _rotateY(MotionEvent1 event, float sensitivity) {
    _spin(screenToQuaternion(0, _computeAngle(event) * (isEye() ? -sensitivity : sensitivity), 0), event.speed(), event.delay());
  }

  /**
   * User gesture into y-rotation conversion routine.
   */
  public void rotateYPos() {
    _rotateY(true);
  }

  /**
   * User gesture into y-rotation conversion routine.
   */
  public void rotateYNeg() {
    _rotateY(false);
  }

  /**
   * User gesture into y-rotation conversion routine.
   */
  protected void _rotateY(boolean up) {
    Quaternion rt = screenToQuaternion(0, _computeAngle() * (up ? keySensitivity() : -keySensitivity()), 0);
    rotate(rt);
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void rotateZ(Event event) {
    if (event instanceof MotionEvent)
      rotateZ((MotionEvent) event);
    else
      System.out.println("rotateZ(Event) requires a motion event");
  }

  /**
   * User gesture into z-rotation conversion routine.
   */
  public void rotateZ(MotionEvent event) {
    rotateZ(event, false);
  }

  /**
   * User gesture into z-rotation conversion routine.
   */
  public void rotateZ(MotionEvent event, boolean fromX) {
    MotionEvent1 motionEvent1 = MotionEvent.event1(event);
    if (motionEvent1 != null)
      _rotateZ(motionEvent1, _wheel(event) ? this.wheelSensitivity() : this.rotationSensitivity());
  }

  /**
   * User gesture into z-rotation conversion routine.
   */
  public void rotateZ(MotionEvent1 event) {
    _rotateZ(event, _wheel(event) ? this.wheelSensitivity() : this.translationSensitivity());
  }

  /**
   * User gesture into z-rotation conversion routine.
   */
  protected void _rotateZ(MotionEvent1 event, float sensitivity) {
    _spin(screenToQuaternion(0, 0, sensitivity * (isEye() ? -_computeAngle(event) : _computeAngle(event))), event.speed(), event.delay());
  }

  /**
   * User gesture into z-rotation conversion routine.
   */
  public void rotateZPos() {
    _rotateZ(true);
  }

  /**
   * User gesture into z-rotation conversion routine.
   */
  public void rotateZNeg() {
    _rotateZ(false);
  }

  /**
   * User gesture into z-rotation conversion routine.
   */
  protected void _rotateZ(boolean up) {
    rotate(screenToQuaternion(0, 0, _computeAngle() * (up ? keySensitivity() : -keySensitivity())));
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void rotateXYZ(Event event) {
    if (event instanceof MotionEvent)
      rotateXYZ((MotionEvent) event);
    else
      System.out.println("rotateY(Event) requires a motion event");
  }

  /**
   * User gesture into xyz-rotation conversion routine.
   */
  public void rotateXYZ(MotionEvent event) {
    MotionEvent3 motionEvent3 = MotionEvent.event3(event, false);
    if (motionEvent3 != null)
      rotateXYZ(motionEvent3);
    else
      System.out.println("rotateXYZ(Event) requires a motion event of at least 3 DOFs");
  }

  /**
   * User gesture into xyz-rotation conversion routine.
   */
  public void rotateXYZ(MotionEvent3 event) {
    if (event.fired())
      _graph._cadRotationIsReversed = _graph.eye().transformOf(_upVector).y() < 0.0f;
    rotate(screenToQuaternion(
        Vector.multiply(new Vector(_computeAngle(event.dx()), _computeAngle(-event.dy()), _computeAngle(-event.dz())),
            rotationSensitivity())));
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void rotate(Event event) {
    if (event instanceof MotionEvent)
      rotate((MotionEvent) event);
    else
      System.out.println("rotate(Event) requires a motion event");
  }

  /**
   * User gesture into arcball-rotation conversion routine.
   */
  public void rotate(MotionEvent event) {
    MotionEvent2 motionEvent2 = MotionEvent.event2(event);
    if (motionEvent2 != null)
      rotate(motionEvent2);
    else
      System.out.println("rotate(Event) requires a motion event of at least 2 DOFs");
  }

  /**
   * User gesture into arcball-rotation conversion routine.
   */
  public void rotate(MotionEvent2 event) {
    if (event.isAbsolute()) {
      System.out.println("rotate(Event) requires a relative motion-event");
      return;
    }
    if (event.fired())
      stopSpinning();
    if (event.fired())
      _graph._cadRotationIsReversed = _graph.eye().transformOf(_upVector).y() < 0.0f;
    if (event.flushed() && damping() == 0) {
      _startSpinning();
      return;
    }
    if (!event.flushed()) {
      Quaternion rt;
      Vector trns;
      if (isEye())
        rt = _deformedBallQuaternion(event, graph().projectedCoordinatesOf(graph().anchor()));
      else {
        trns = _graph.projectedCoordinatesOf(position());
        rt = _deformedBallQuaternion(event, trns);
        trns = rt.axis();
        trns = _graph.eye().orientation().rotate(trns);
        trns = transformOf(trns);
        rt = new Quaternion(trns, -rt.angle());
      }
      _spin(rt, event.speed(), event.delay());
    }
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void scale(Event event) {
    if (event instanceof MotionEvent)
      scale((MotionEvent) event);
    else
      System.out.println("scale(Event) requires a motion event");
  }

  /**
   * User gesture into scaling conversion routine.
   */
  public void scale(MotionEvent event) {
    MotionEvent1 motionEvent1 = MotionEvent.event1(event);
    if (motionEvent1 != null)
      _scale(motionEvent1, _wheel(event) ? wheelSensitivity() : scalingSensitivity());
  }

  /**
   * User gesture into scaling conversion routine.
   */
  public void scale(MotionEvent1 event) {
    _scale(event, _wheel(event) ? wheelSensitivity() : scalingSensitivity());
  }

  /**
   * User gesture into scaling conversion routine.
   */
  protected void _scale(MotionEvent1 event, float sensitivity) {
    if (isEye()) {
      float delta = event.dx() * sensitivity;
      float s = 1 + Math.abs(delta) / (float) -_graph.height();
      scale(delta >= 0 ? s : 1 / s);
    } else {
      float delta = event.dx() * sensitivity;
      float s = 1 + Math.abs(delta) / (float) _graph.height();
      scale(delta >= 0 ? s : 1 / s);
    }
  }

  /**
   * User gesture into scaling conversion routine.
   */
  public void scalePos() {
    _scale(true);
  }

  /**
   * User gesture into scaling conversion routine.
   */
  public void scaleNeg() {
    _scale(false);
  }

  /**
   * User gesture into scaling conversion routine.
   */
  protected void _scale(boolean up) {
    float s = 1 + Math.abs(keySensitivity()) / (isEye() ? (float) -_graph.height() : (float) _graph.height());
    scale(up ? s : 1 / s);
  }

  /**
   * Use for first person (move forward/backward, lookAround) and cad motion actions.
   */
  protected void _updateUpVector() {
    _upVector = orientation().rotate(new Vector(0.0f, 1.0f, 0.0f));
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void lookAround(Event event) {
    if (event instanceof MotionEvent)
      lookAround((MotionEvent) event);
    else
      System.out.println("lookAround(Event) requires a motion event");
  }

  public void lookAround(MotionEvent event) {
    rotate(_rollPitchQuaternion(event));
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void moveBackward(Event event) {
    if (event instanceof MotionEvent)
      moveBackward((MotionEvent) event);
    else
      System.out.println("moveBackward(Event) requires a motion event");
  }

  /**
   * User gesture into move-backward conversion routine.
   */
  public void moveBackward(MotionEvent event) {
    _moveForward(event, false);
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void moveForward(Event event) {
    if (event instanceof MotionEvent)
      moveForward((MotionEvent) event);
    else
      System.out.println("moveForward(Event) requires a motion event");
  }

  /**
   * User gesture into move-forward conversion routine.
   */
  public void moveForward(MotionEvent event) {
    _moveForward(event, true);
  }

  /**
   * User gesture into move-forward conversion routine.
   */
  protected void _moveForward(MotionEvent event, boolean forward) {
    MotionEvent2 motionEvent2 = MotionEvent.event2(event);
    if (motionEvent2 != null)
      _moveForward(motionEvent2, forward);
    else
      System.out.println("moveForward(Event) requires a motion event of at least 2 DOFs");
  }

  /**
   * User gesture into move-forward conversion routine.
   */
  protected void _moveForward(MotionEvent2 event, boolean forward) {
    if (event.fired())
      _updateUpVector();
    else if (event.flushed()) {
      stopFlying();
      return;
    }
    Vector trns;
    float fSpeed = forward ? -flySpeed() : flySpeed();
    rotate(_rollPitchQuaternion(event));
    _fly.set(0.0f, 0.0f, fSpeed);
    trns = rotation().rotate(_fly);
    _startFlying(event, trns);
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void drive(Event event) {
    if (event instanceof MotionEvent)
      drive((MotionEvent) event);
    else
      System.out.println("drive(Event) requires a motion event");
  }

  /**
   * User gesture into drive conversion routine.
   */
  public void drive(MotionEvent event) {
    MotionEvent2 motionEvent2 = MotionEvent.event2(event);
    if (motionEvent2 != null)
      drive(motionEvent2);
    else
      System.out.println("drive(Event) requires a motion event of at least 2 DOFs");
  }

  /**
   * User gesture into drive conversion routine.
   */
  public void drive(MotionEvent2 event) {
    if (event.fired()) {
      _initEvent = event.get();
      _updateUpVector();
      _flySpeedCache = flySpeed();
    } else if (event.flushed()) {
      setFlySpeed(_flySpeedCache);
      stopFlying();
      return;
    }
    setFlySpeed(0.01f * _graph.radius() * 0.01f * (event.y() - _initEvent.y()));
    Vector trns;
    rotate(_turnQuaternion(event.event1()));
    _fly.set(0.0f, 0.0f, flySpeed());
    trns = rotation().rotate(_fly);
    _startFlying(event, trns);
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void rotateCAD(Event event) {
    if (event instanceof MotionEvent)
      rotateCAD((MotionEvent) event);
    else
      System.out.println("rotateCAD(Event) requires a motion event");
  }

  /**
   * User gesture into CAD-rotation conversion routine.
   */
  public void rotateCAD(MotionEvent event) {
    MotionEvent2 motionEvent2 = MotionEvent.event2(event);
    if (motionEvent2 != null)
      rotateCAD(motionEvent2);
    else
      System.out.println("rotateCAD(Event) requires a motion event of at least 2 DOFs");
  }

  /**
   * User gesture into CAD-rotation conversion routine.
   */
  public void rotateCAD(MotionEvent2 event) {
    if (event.isAbsolute()) {
      System.out.println("rotateCAD(Event) requires a relative motion-event");
      return;
    }
    if (event.fired())
      stopSpinning();
    if (event.fired())
      _graph._cadRotationIsReversed = _graph.eye().transformOf(_upVector).y() < 0.0f;
    if (event.flushed() && damping() == 0) {
      _startSpinning();
      return;
    } else {
      // Multiply by 2.0 to get on average about the same _speed as with the
      // deformed ball
      float dx = -2.0f * rotationSensitivity() * event.dx() / _graph.width();
      float dy = 2.0f * rotationSensitivity() * event.dy() / _graph.height();
      if (_graph._cadRotationIsReversed)
        dx = -dx;
      if (_graph.isRightHanded())
        dy = -dy;
      Vector verticalAxis = transformOf(_upVector);
      _spin(Quaternion.multiply(new Quaternion(verticalAxis, dx), new Quaternion(new Vector(1.0f, 0.0f, 0.0f), dy)), event.speed(),
          event.delay());
    }
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void hinge(Event event) {
    if (event instanceof MotionEvent)
      hinge((MotionEvent) event);
    else
      System.out.println("hinge(Event) requires a motion event");
  }

  /**
   * User gesture into hinge conversion routine.
   */
  public void hinge(MotionEvent event) {
    MotionEvent6 motionEvent6 = MotionEvent.event6(event);
    if (motionEvent6 != null)
      hinge(motionEvent6);
    else
      System.out.println("hinge(Event) requires a motion event of at least 6 DOFs");
  }

  /**
   * User gesture into hinge conversion routine.
   */
  public void hinge(MotionEvent6 event) {
    if (!isEye()) {
      System.out.println("hinge(Event) only makes sense for the eye");
      return;
    }
    // aka google earth navigation
    // 1. Relate the eye reference frame:
    Vector trns = new Vector();
    Vector pos = position();
    Quaternion o = orientation();
    Frame oldRef = reference();
    Frame rFrame = new Frame(_graph);
    rFrame.setPosition(graph().anchor());
    rFrame.setZAxis(Vector.subtract(pos, graph().anchor()));
    rFrame.setXAxis(xAxis());
    setReference(rFrame);
    setPosition(pos);
    setOrientation(o);
    // 2. Translate the refFrame along its Z-axis:
    float deltaZ = event.dz();
    trns = new Vector(0, deltaZ, 0);
    screenToEye(trns);
    float pmag = trns.magnitude();
    translate(0, 0, (deltaZ > 0) ? -pmag : pmag);
    // 3. Rotate the refFrame around its X-axis -> translate forward-backward
    // the frame on the sphere surface
    float deltaY = _computeAngle(event.dy());
    rFrame.rotate(new Quaternion(new Vector(1, 0, 0), _graph.isRightHanded() ? deltaY : -deltaY));
    // 4. Rotate the refFrame around its Y-axis -> translate left-right the
    // frame on the sphere surface
    float deltaX = _computeAngle(event.dx());
    rFrame.rotate(new Quaternion(new Vector(0, 1, 0), deltaX));
    // 5. Rotate the refFrame around its Z-axis -> look around
    float rZ = _computeAngle(event.drz());
    rFrame.rotate(new Quaternion(new Vector(0, 0, 1), _graph.isRightHanded() ? -rZ : rZ));
    // 6. Rotate the frame around x-axis -> move head up and down :P
    float rX = _computeAngle(event.drx());
    Quaternion q = new Quaternion(new Vector(1, 0, 0), _graph.isRightHanded() ? rX : -rX);
    rotate(q);
    // 7. Unrelate the frame and restore state:
    pos = position();
    o = orientation();
    setReference(oldRef);
    graph().pruneBranch(rFrame);
    setPosition(pos);
    setOrientation(o);
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void screenTranslate(Event event) {
    if (event instanceof MotionEvent)
      screenTranslate((MotionEvent) event);
    else
      System.out.println("screenTranslate(Event) requires a motion event");
  }

  /**
   * User gesture screen-translate conversion routine.
   */
  public void screenTranslate(MotionEvent event) {
    MotionEvent2 motionEvent2 = MotionEvent.event2(event);
    if (motionEvent2 != null)
      screenTranslate(motionEvent2);
    else
      System.out.println("screenTranslate(Event) requires a motion event of at least 2 DOFs");
  }

  /**
   * User gesture screen-translate conversion routine.
   */
  public void screenTranslate(MotionEvent2 event) {
    if (event.fired())
      _directionIsFixed = false;
    int dir = _originalDirection(event);
    if (dir == 1)
      translateX(event, true);
    else if (dir == -1)
      translateY(event, false);
  }

  /**
   * Java ugliness and madness requires this one. Should NOT be implemented in JS (due to its dynamism).
   */
  public void screenRotate(Event event) {
    if (event instanceof MotionEvent)
      screenRotate((MotionEvent) event);
    else
      System.out.println("screenRotate(Event) requires a motion event");
  }

  /**
   * User gesture screen-rotation conversion routine.
   */
  public void screenRotate(MotionEvent event) {
    MotionEvent2 motionEvent2 = MotionEvent.event2(event);
    if (motionEvent2 != null)
      screenRotate(motionEvent2);
    else
      System.out.println("screenRotate(Event) requires a motion event of at least 2 DOFs");
  }

  /**
   * User gesture screen-rotation conversion routine.
   */
  public void screenRotate(MotionEvent2 event) {
    if (event.isAbsolute()) {
      System.out.println("screenRotate(Event) requires a relative motion-event");
      return;
    }
    if (event.fired()) {
      stopSpinning();
      //TODO handle me
      //graph.setRotateVisualHint(true); // display visual hint
      _graph._cadRotationIsReversed = _graph.eye().transformOf(_upVector).y() < 0.0f;
    }
    if (event.flushed()) {
      //TODO handle me
      //graph.setRotateVisualHint(false);
      if (damping() == 0) {
        _startSpinning();
        return;
      }
    }
    if (!event.flushed()) {
      Quaternion rt;
      Vector trns;
      float angle;
      if (isEye()) {
        trns = graph().projectedCoordinatesOf(graph().anchor());
        angle = (float) Math.atan2(event.y() - trns._vector[1], event.x() - trns._vector[0]) - (float) Math
            .atan2(event.previousY() - trns._vector[1], event.previousX() - trns._vector[0]);
        if (_graph.isLeftHanded())
          angle = -angle;
        rt = new Quaternion(new Vector(0.0f, 0.0f, 1.0f), angle);
      } else {
        trns = _graph.projectedCoordinatesOf(position());
        float prev_angle = (float) Math.atan2(event.previousY() - trns._vector[1], event.previousX() - trns._vector[0]);
        angle = (float) Math.atan2(event.y() - trns._vector[1], event.x() - trns._vector[0]);
        Vector axis = transformOf(_graph.eye().orientation().rotate(new Vector(0.0f, 0.0f, -1.0f)));
        if (_graph.isRightHanded())
          rt = new Quaternion(axis, angle - prev_angle);
        else
          rt = new Quaternion(axis, prev_angle - angle);
      }
      _spin(rt, event.speed(), event.delay());
    }
  }

  /**
   * User gesture into anchor from pixel conversion routine.
   */
  //TODO missed
  /*
  public void anchorFromPixel(TapEvent _event) {
    if (isEye())
      graph().setAnchorFromPixel(new Point(_event.x(), _event.y()));
    else
      Graph.showOnlyEyeWarning("anchorFromPixel");
  }
  */

  // Quite nice

  /**
   * Same as {@code return screenToVector(new Vector(x, y, z))}.
   *
   * @see #screenToVector(Vector)
   */
  public Vector screenToVector(float x, float y, float z) {
    return screenToVector(new Vector(x, y, z));
  }

  /**
   * Same as {@code return eyeToReferenceFrame(screenToEye(vector))}. Transforms the vector
   * from screen (device) coordinates to {@link #reference()} coordinates.
   *
   * @see #screenToEye(Vector)
   * @see #eyeToReferenceFrame(Vector)
   */
  public Vector screenToVector(Vector vector) {
    return eyeToReferenceFrame(screenToEye(vector));
  }

  /**
   * Same as {@code return eyeToReferenceFrame(new Vector(x, y, z))}.
   *
   * @see #eyeToReferenceFrame(Vector)
   */
  public Vector eyeToReferenceFrame(float x, float y, float z) {
    return eyeToReferenceFrame(new Vector(x, y, z));
  }

  /**
   * Converts the vector from eye coordinates to {@link #reference()} coordinates.
   * <p>
   * It's worth noting that all gesture to node motion converting methods, are
   * implemented from just {@link #screenToEye(Vector)}, {@link #eyeToReferenceFrame(Vector)}
   * and {@link #screenToQuaternion(float, float, float)}.
   *
   * @see #screenToEye(Vector)
   * @see #screenToQuaternion(float, float, float)
   */
  public Vector eyeToReferenceFrame(Vector vector) {
    Frame gFrame = isEye() ? this : /* respectToEye() ? */_graph.eye() /* : this */;
    Vector t = gFrame.inverseTransformOf(vector);
    if (reference() != null)
      t = reference().transformOf(t);
    return t;
  }

  /**
   * Same as {@code return screenToEye(new Vector(x, y, z))}.
   *
   * @see #screenToEye(Vector)
   */
  public Vector screenToEye(float x, float y, float z) {
    return screenToEye(new Vector(x, y, z));
  }

  /**
   * Converts the vector from screen (device) coordinates into eye coordinates.
   * <p>
   * It's worth noting that all gesture to node motion converting methods, are
   * implemented from just {@link #screenToEye(Vector)}, {@link #eyeToReferenceFrame(Vector)}
   * and {@link #screenToQuaternion(float, float, float)}.
   *
   * @see #eyeToReferenceFrame(Vector)
   * @see #screenToQuaternion(float, float, float)
   */
  public Vector screenToEye(Vector vector) {
    Vector eyeVector = vector.get();
    // Scale to fit the screen relative _event displacement
    // Quite excited to see how simple it's in 2d:
    //if (_graph.is2D())
    //return eyeVector;
    // ... and amazed as to how dirty it's in 3d:
    switch (_graph.type()) {
      case PERSPECTIVE:
        float k = (float) Math.tan(_graph.fieldOfView() / 2.0f) * Math.abs(
            _graph.eye().coordinatesOf(isEye() ? graph().anchor() : position())._vector[2] * _graph.eye().magnitude());
        // * Math.abs(graph.eye().frame().coordinatesOf(isEye() ?
        // graph.eye().anchor() : position()).vec[2]);
        //TODO check me weird to find height instead of width working (may it has to do with fov?)
        eyeVector._vector[0] *= 2.0 * k / _graph.height();
        eyeVector._vector[1] *= 2.0 * k / _graph.height();
        break;
      case TWO_D:
      case ORTHOGRAPHIC:
        float[] wh = _graph.boundaryWidthHeight();
        // float[] wh = graph.eye().getOrthoWidthHeight();
        eyeVector._vector[0] *= 2.0 * wh[0] / _graph.width();
        eyeVector._vector[1] *= 2.0 * wh[1] / _graph.height();
        break;
    }
    float coef;
    if (isEye()) {
      // float coef = 8E-4f;
      coef = Math.max(Math.abs((coordinatesOf(graph().anchor()))._vector[2] * magnitude()), 0.2f * graph().radius());
      eyeVector._vector[2] *= coef / graph().height();
      // eye _wheel seems different
      // trns.vec[2] *= coef * 8E-4f;
      eyeVector.divide(graph().eye().magnitude());
    } else {
      coef = Vector.subtract(_graph.eye().position(), position()).magnitude();
      eyeVector._vector[2] *= coef / _graph.height();
      eyeVector.divide(_graph.eye().magnitude());
    }
    // if( isEye() )
    return eyeVector;
  }

  /**
   * Same as {@code return screenToQuaternion(angles.vec[0], angles.vec[1], angles.vec[2])}.
   *
   * @see #screenToQuaternion(float, float, float)
   */
  public Quaternion screenToQuaternion(Vector angles) {
    return screenToQuaternion(angles._vector[0], angles._vector[1], angles._vector[2]);
  }

  /**
   * Reduces the screen (device)
   * <a href="http://en.wikipedia.org/wiki/Euler_angles#Extrinsic_rotations"> Extrinsic
   * rotation</a> into a {@link Quaternion}.
   * <p>
   * It's worth noting that all gesture to node motion converting methods, are
   * implemented from just {@link #screenToEye(Vector)}, {@link #eyeToReferenceFrame(Vector)}
   * and {@link #screenToQuaternion(float, float, float)}.
   *
   * @param roll  Rotation angle in radians around the screen x-Axis
   * @param pitch Rotation angle in radians around the screen y-Axis
   * @param yaw   Rotation angle in radians around the screen z-Axis
   * @see Quaternion#fromEulerAngles(float, float, float)
   */
  public Quaternion screenToQuaternion(float roll, float pitch, float yaw) {
    // don't really need to differentiate among the two cases, but eyeFrame can
    // be speeded up
    if (isEye() /* || (!isEye() && !this.respectToEye()) */) {
      return new Quaternion(_graph.isLeftHanded() ? -roll : roll, pitch, _graph.isLeftHanded() ? -yaw : yaw);
    } else {
      Vector trns = new Vector();
      Quaternion q = new Quaternion(_graph.isLeftHanded() ? roll : -roll, -pitch, _graph.isLeftHanded() ? yaw : -yaw);
      trns.set(-q.x(), -q.y(), -q.z());
      trns = _graph.eye().orientation().rotate(trns);
      trns = transformOf(trns);
      q.setX(trns.x());
      q.setY(trns.y());
      q.setZ(trns.z());
      return q;
    }
  }

  /**
   * Returns {@code true} when the node is tossing.
   * <p>
   * During tossing, {@link #damping()} translates the node by its {@link #flyDirection()}
   * at a frequency defined when the node {@link #_startFlying(MotionEvent, Vector)}.
   * <p>
   * Use {@link #_startFlying(MotionEvent, Vector)} and {@link #stopFlying()} to change this
   * state. Default value is {@code false}.
   * <p>
   * {@link #isSpinning()}
   */
  public boolean isFlying() {
    return _flyTask.isActive();
  }

  /**
   * Stops the tossing motion started using {@link #_startFlying(MotionEvent, Vector)}.
   * {@link #isFlying()} will return {@code false} after this call.
   * <p>
   * <b>Attention: </b>This method may be called by {@link #damping()}, since tossing may
   * be decelerated according to {@link #damping()} till it stops completely.
   *
   * @see #damping()
   * @see #_spin()
   */
  public void stopFlying() {
    _flyTask.stop();
  }

  /**
   * Returns the incremental translation that is applied by {@link #damping()} to the
   * node position when it {@link #isFlying()}.
   * <p>
   * Default value is no translation. Use {@link #setFlyDirection(Vector)} to change this
   * value.
   * <p>
   * <b>Attention: </b>Tossing may be decelerated according to {@link #damping()} till it
   * stops completely.
   *
   * @see #spinningQuaternion()
   */
  public Vector flyDirection() {
    return _flyDirection;
  }

  /**
   * Defines the {@link #flyDirection()} in the reference frame coordinate system.
   *
   * @see #setSpinningQuaternion(Quaternion)
   */
  public void setFlyDirection(Vector dir) {
    _flyDirection = dir;
  }

  /**
   * Internal use. Same as {@code startFlying(direction, event.speed())}.
   *
   * @see #startFlying(Vector, float)
   * @see #_startSpinning(MotionEvent, Quaternion)
   */
  protected void _startFlying(MotionEvent event, Vector direction) {
    startFlying(direction, event.speed());
  }

  /**
   * Starts the tossing of the node.
   * <p>
   * This method starts a timer that will call {@link #damping()} every 20
   * milliseconds. The node {@link #isFlying()} until you call
   * {@link #stopFlying()}.
   * <p>
   * <b>Attention: </b>Tossing may be decelerated according to {@link #damping()} till it
   * stops completely.
   *
   * @see #damping()
   * @see #_spin()
   * @see #_startFlying(MotionEvent, Vector)
   * @see #startSpinning(Quaternion, float, long)
   */
  public void startFlying(Vector direction, float speed) {
    _eventSpeed = speed;
    setFlyDirection(direction);
    _flyTask.run(_flyUpdatePeriod);
  }

  /**
   * Translates the node by its {@link #flyDirection()}. Invoked by
   * {@link #_moveForward(MotionEvent, boolean)} and {@link #drive(MotionEvent)}.
   * <p>
   * <b>Attention: </b>Tossing may be decelerated according to {@link #damping()} till it
   * stops completely.
   *
   * @see #_spin()
   */
  protected void _fly() {
    translate(flyDirection());
  }

  /**
   * Returns the fly speed, expressed in graph units.
   * <p>
   * It corresponds to the incremental displacement that is periodically applied to the
   * node by {@link #_moveForward(MotionEvent, boolean)}.
   * <p>
   * <b>Attention:</b> When the node is set as the {@link Graph#eye()}, this value is set according
   * to the {@link Graph#radius()} by {@link Graph#setRadius(float)}.
   */
  public float flySpeed() {
    return _flySpeed;
  }

  /**
   * Sets the {@link #flySpeed()}, defined in  graph units.
   * <p>
   * Default value is 0, but it is modified according to the {@link Graph#radius()} when the node
   * is set as the {@link Graph#eye()}.
   */
  public void setFlySpeed(float speed) {
    _flySpeed = speed;
  }

  protected Quaternion _rollPitchQuaternion(MotionEvent event) {
    MotionEvent2 motionEvent2 = MotionEvent.event2(event);
    if (motionEvent2 != null)
      return _rollPitchQuaternion(motionEvent2);
    else {
      System.out.println("rollPitchQuaternion(Event) requires a motion event of at least 2 DOFs");
      return null;
    }
  }

  /**
   * Returns a Quaternion that is the composition of two rotations, inferred from the
   * 2-DOF gesture (e.g., mouse) roll (X axis) and pitch.
   */
  protected Quaternion _rollPitchQuaternion(MotionEvent2 event) {
    float deltaX = event.dx();
    float deltaY = event.dy();

    if (_graph.isRightHanded())
      deltaY = -deltaY;

    Quaternion rotX = new Quaternion(new Vector(1.0f, 0.0f, 0.0f), rotationSensitivity() * deltaY / graph().height());
    Quaternion rotY = new Quaternion(transformOf(_upVector), rotationSensitivity() * (-deltaX) / graph().width());
    return Quaternion.multiply(rotY, rotX);
  }

  // drive:

  /**
   * Returns a Quaternion that is a rotation around Y-axis, proportional to the horizontal
   * event X-displacement.
   */
  protected Quaternion _turnQuaternion(MotionEvent1 event) {
    float deltaX = event.dx();
    return new Quaternion(new Vector(0.0f, 1.0f, 0.0f), rotationSensitivity() * (-deltaX) / graph().width());
  }

  // end decide

  /**
   * Returns the picking precision threshold in pixels used by the node to {@link #track(Event)}.
   *
   * @see #setPrecisionThreshold(float)
   */
  public float precisionThreshold() {
    if (precision() == Precision.ADAPTIVE)
      return _threshold * scaling() * _graph.pixelToGraphRatio(position());
    return _threshold;
  }

  /**
   * Returns the node picking precision. See {@link #setPrecision(Precision)} for details.
   *
   * @see #setPrecision(Precision)
   * @see #setPrecisionThreshold(float)
   */
  public Precision precision() {
    return _Precision;
  }

  /**
   * Sets the node picking precision.
   * <p>
   * When {@link #precision()} is {@link Precision#FIXED} or
   * {@link Precision#ADAPTIVE} Picking is done by checking if the pointer lies
   * within a squared area around the node {@link #center()} screen projection which size
   * is defined by {@link #setPrecisionThreshold(float)}.
   * <p>
   * When {@link #precision()} is {@link Precision#EXACT}, picking is done
   * in a precise manner according to the projected pixels of the visual representation
   * related to the node. It is meant to be implemented by derived classes (providing the
   * means attach a visual representation to the node) and requires the graph to implement
   * a back buffer.
   * <p>
   * Default implementation of this policy will behave like {@link Precision#FIXED}.
   *
   * @see #precision()
   * @see #setPrecisionThreshold(float)
   */
  public void setPrecision(Precision precision) {
    if (precision == Precision.EXACT)
      System.out.println("Warning: EXACT picking precision will behave like FIXED. EXACT precision is meant to be implemented for derived nodes and scenes that support a backBuffer.");
    _Precision = precision;
  }

  /**
   * Sets the length of the squared area around the node {@link #center()} screen
   * projection that defined the {@link #track(Event)} condition used for
   * node picking.
   * <p>
   * If {@link #precision()} is {@link Precision#FIXED}, the {@code threshold} is expressed
   * in pixels and directly defines the fixed length of a 'shooter target', centered
   * at the projection of the node origin onto the screen.
   * <p>
   * If {@link #precision()} is {@link Precision#ADAPTIVE}, the {@code threshold} is expressed
   * in object space (world units) and defines the edge length of a squared bounding box that
   * leads to an adaptive length of a 'shooter target', centered at the projection of the node
   * origin onto the screen. Use this version only if you have a good idea of the bounding box
   * size of the object you are attaching to the node shape.
   * <p>
   * The value is meaningless when the {@link #precision()} is* {@link Precision#EXACT}. See
   * {@link #setPrecision(Precision)} for details.
   * <p>
   * Default behavior is to set the {@link #precisionThreshold()} (in a non-adaptive
   * manner) to 20.
   * <p>
   * Negative {@code threshold} values are silently ignored.
   *
   * @see #precision()
   * @see #precisionThreshold()
   * @see #track(Event)
   */
  public void setPrecisionThreshold(float threshold) {
    if (threshold >= 0)
      _threshold = threshold;
  }

  /**
   * Check if this node is the {@link Agent#inputGrabber()}. Returns
   * {@code true} if this object grabs the agent and {@code false} otherwise.
   */
  public boolean grabsInput(Agent agent) {
    return agent.inputGrabber() == this;
  }

  /**
   * Checks if the node grabs input from any agent registered at the graph input-handler.
   */
  public boolean grabsInput() {
    for (Agent agent : _graph.inputHandler().agents()) {
      if (agent.inputGrabber() == this)
        return true;
    }
    return false;
  }
}
