/**************************************************************************************
 * dandelion_tree
 * Copyright (c) 2014-2017 National University of Colombia, https://github.com/remixlab
 * @author Jean Pierre Charalambos, http://otrolado.info/
 *
 * All rights reserved. Library that eases the creation of interactive
 * scenes, released under the terms of the GNU Public License v3.0
 * which is available at http://www.gnu.org/licenses/gpl.html
 **************************************************************************************/

package frames.core.constraint;

import frames.core.Node;
import frames.primitives.Quaternion;
import frames.primitives.Vector;

/**
 * An interface class for Frame constraints. This interface API aims to conform that of the
 * great <a href=
 * "http://libqglviewer.com/refManual/classqglviewer_1_1Constraint.html">libQGLViewer
 * Constraint</a>.
 * <p>
 * This class defines the interface for the constraint that can be applied to a Frame to
 * limit its motion. Use {@link Node#setConstraint(Constraint)}
 * to associate a Constraint to a Frame (default is a {@code null}
 * {@link Node#constraint()}.
 */
public abstract class Constraint {
  /**
   * Filters the translation applied to the Frame. This default implementation is empty
   * (no filtering).
   * <p>
   * Overload this method in your own Constraint class to define a new translation
   * constraint. {@code frame} is the Frame to which is applied the translation. You
   * should refrain from directly changing its value in the constraint. Use its
   * {@link Node#position()} and update the translation
   * accordingly instead.
   * <p>
   * {@code translation} is expressed in the local Frame coordinate system. Use
   * {@link Node#inverseTransformOf(Vector)} to express it in the
   * world coordinate system if needed.
   */
  public Vector constrainTranslation(Vector translation, Node frame) {
    return translation.get();
  }

  /**
   * Filters the rotation applied to the {@code frame}. This default implementation is
   * empty (no filtering).
   * <p>
   * Overload this method in your own Constraint class to define a new rotation
   * constraint. See {@link #constrainTranslation(Vector, Node)} for details.
   * <p>
   * Use {@link Node#inverseTransformOf(Vector)} on the
   * {@code rotation} {@link Quaternion#axis()} to express
   * {@code rotation} in the world coordinate system if needed.
   */
  public Quaternion constrainRotation(Quaternion rotation, Node frame) {
    return rotation.get();
  }
}
