package basics;

import common.InteractiveFrame;
import frames.core.Graph;
import frames.input.Shortcut;
import frames.input.event.MotionEvent;
import frames.processing.Scene;
import frames.processing.Shape;
import processing.core.PApplet;
import processing.core.PGraphics;

public class ProsceneNodes extends PApplet {
  Scene scene;
  IFrame node;

  public void settings() {
    size(800, 800, P3D);
  }

  public void setup() {
    scene = new Scene(this);
    InteractiveFrame eye = new InteractiveFrame(scene);
    scene.setEye(eye);
    //interactivity defaults to the eye
    scene.setDefaultNode(eye);
    scene.setRadius(200);
    scene.fitBallInterpolation();

    node = new IFrame();
    //frame.setPrecision(Frame.Precision.FIXED);
  }

  public void draw() {
    background(0);
    scene.drawAxes();
    scene.traverse();
  }

  public void keyPressed() {
    if (key == 'e')
      if (scene.type() == Graph.Type.PERSPECTIVE)
        scene.setType(Graph.Type.ORTHOGRAPHIC);
      else
        scene.setType(Graph.Type.PERSPECTIVE);

  }

  public class IFrame extends Shape {
    Shortcut left = new Shortcut(PApplet.LEFT);
    Shortcut right = new Shortcut(PApplet.RIGHT);
    Shortcut wheel = new Shortcut(processing.event.MouseEvent.WHEEL);

    //button dimensions
    public IFrame() {
      super(scene);
    }

    @Override
    public void interact(MotionEvent event) {
      if (event.shortcut().matches(left))
        translate(event);
      else if (event.shortcut().matches(right))
        rotate(event);
      else if (event.shortcut().matches(wheel))
        if (isEye() && graph().is3D())
          translateZ(event);
        else
          scale(event);
    }

    @Override
    protected void set(PGraphics pGraphics) {
      pGraphics.fill(255, 0, 0);
      pGraphics.sphere(50);
    }
  }

  public static void main(String args[]) {
    PApplet.main(new String[]{"basics.ProsceneNodes"});
  }
}
