# Next release: 4.0.0

## Design principles

Simple, expressive, language-agnostic, and extensible [(2D/3D) scene graph](https://en.wikipedia.org/wiki/Scene_graph) featuring interaction, inverse kinematics, visualization and animation frameworks and supporting advanced (onscreen/offscreen) rendering techniques, such as [view frustum culling](http://cgvr.informatik.uni-bremen.de/teaching/cg_literatur/lighthouse3d_view_frustum_culling/index.html). Develop under Java ([processing branch](https://github.com/VisualComputing/proscene.js/tree/processing)) and port it from there to JS ([master branch](https://github.com/VisualComputing/proscene.js/tree/master)). No magic: Proscene does what it is told to do — no more, no less. The new architecture comprises the following packages:

1. [Timing](https://github.com/VisualComputing/proscene.js/tree/processing/src/remixlab/timing). Status: API is completed and tested. Expect some (occasional) API docs updates.
2. [Input](https://github.com/VisualComputing/proscene.js/tree/processing/src/remixlab/input). Status: API is completed and tested. Expect some API (occasional) docs updates.
3. [Primitives](https://github.com/VisualComputing/proscene.js/tree/processing/src/remixlab/primitives). Status: API is completed and tested. Expect some API docs updates.
4. [Core](https://github.com/VisualComputing/proscene.js/tree/processing/src/remixlab/core). Status: API is completed and tested. Expect lots of API docs updates.
5. [Proscene](https://github.com/VisualComputing/proscene.js/tree/processing/src/remixlab/proscene). Status: API is complete (see TODO [here](https://github.com/VisualComputing/proscene.js/blob/processing/TODO.md)).

Observe:

1. *Timing* and *Input* packages should be ported at their own repos (_master_ branches): [here](https://github.com/VisualComputing/fpstiming.js) and [here](https://github.com/VisualComputing/bias.js). Note that the _processing_ branches contain the Java code with the examples to be tested.
2. The _bias_ js port is key as a design proof-of-concept. Testing should be done at least against [p5.js](https://p5js.org/).
3. *Primitives*, *Core* and *Proscene* packages should be ported at the [proscene.js](https://github.com/VisualComputing/proscene.js) repo master branch. Note that the _processing_ branch is synced with the repos in 1 and that it contains the examples [here](https://github.com/VisualComputing/proscene.js/tree/processing/testing).

## Goals

1. ~Remove reflect stuff, i.e., Profiles and iFrame.setShape(method).~
2. ~Make Graph (previously AbstractScene) instantiable. Refactor GenericFrames as Nodes.~
3. ~Make eye nodes indistinguible from other nodes, i.e., they can interchangeably be used.~
4. ~Rethink third person camera by removing Trackable in favor of 3.~
5. ~Better 2D and 3D merging by removing Eye hierarchy and move its functionality into the Graph.~
6. Rethink constraints to cope with inverse kinematics framework.
7. Port the framework to JS (from all the previous points).
8. Implement proscene package to simplify all the examples which should be ported and update the API docs.

## JS port

### Code conventions

* [ECMAScript 2015](http://es6-features.org) (a.k.a., ECMAScript6 or [ES6](https://en.wikipedia.org/wiki/ECMAScript#6th_Edition_-_ECMAScript_2015)) compatibility with [class support](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Classes).
* vars (class attributes) and 'private' methods are prefixed with _underscore_ (_). Convention adopted from [here](https://developer.mozilla.org/en-US/docs/Archive/Add-ons/Add-on_SDK/Guides/Contributor_s_Guide/Private_Properties). Note that Java protected methods aren't prefixed (the same goes for their JS counterparts).
* Method _params_ should be named as explicit as possible (in order to cope with JS dynamic and weak typed features).
* [Emulate interfaces](http://jscriptpatterns.blogspot.com.co/2013/01/javascript-interfaces.html) with comments, attribute checking and duck typing.

### Stages

Port one by one the java packages in the following stages:

#### Stage 1

* Goal: Implement [Desktop version](https://github.com/VisualComputing/proscene.js/tree/processing) and [examples](https://github.com/VisualComputing/proscene.js/tree/processing/testing) (@nakednous). Status: mostly completed, refer to this [TODO](https://github.com/VisualComputing/proscene.js/blob/processing/TODO.md).

#### Stage 2

* Goal: Port _timing_ (@sechaparroc), _input_ (@sechaparroc), _primitives_ (@jiapulidoar) and constraint (@sechaparroc) packages.
* Software testing:
    1. Port the [timing examples](https://github.com/VisualComputing/fpstiming.js/tree/processing/examples) at the [fpstiming.js gh-pages branch](https://github.com/VisualComputing/fpstiming.js/tree/gh-pages) (@sechaparroc and @jiapulidoar); the site will be rendered [here](https://visualcomputing.github.io/fpstiming.js/). Note that the gh-pages branch use [jekyll](https://jekyllrb.com/) to generate the content.
    2. Port the [input examples](https://github.com/VisualComputing/bias.js/tree/processing/examples) at the [bias.js gh-pages branch](https://github.com/VisualComputing/bias.js/tree/gh-pages) (@sechaparroc and @jiapulidoar); the site will be rendered [here](https://visualcomputing.github.io/bias.js/). Note that the gh-pages branch use [jekyll](https://jekyllrb.com/) to generate the content.
    3. Create _primitives_ examples to test the functionality. Study the relation between proscene primitives and p5.js primitives (e.g., [Vector](https://p5js.org/reference/#/p5.Vector)). Port primitives in the following suggested order:
        1. Point
        2. Rectangle
        3. Vector
        4. Matrix
        5. Quaternion. Suggested reading: [3D Math Primer for Graphics and Game Development de F.Dunn and I.Parberry](https://tfetimes.com/wp-content/uploads/2015/04/F.Dunn-I.Parberry-3D-Math-Primer-for-Graphics-and-Game-Development.pdf), Section 10.4. Quaternions.
        6. Frame which comprises a vector (position), a quaternion (orientation) and a magnitude (scalar).

#### Stage 3

* Goal: Port _core_ and _proscene_ packages.
* Software testing: Port the _proscene_ examples at the [proscene.js gh-pages branch](https://github.com/VisualComputing/proscene.js/tree/gh-pages) (@sechaparroc, @jiapulidoar and @nakednous); the site will be rendered [here](https://visualcomputing.github.io/proscene.js/). Note that the gh-pages branch use [jekyll](https://jekyllrb.com/) to generate the content.
* By-product: Software paper with new design, supported platforms and features.

#### Stage 4

* Goal: Port inverse kinematics and implement [leap motion](https://en.wikipedia.org/wiki/Leap_Motion) and [NLP](https://en.wikipedia.org/wiki/Natural_language_processing) agents.
* By-product: Paper: inverse kinematics based advanced interactions in js.

## Working plan

### Suggestion
 
We should focus on stage 2 completion while syncing with each other nicely. Next milestone is running online the _input_ examples (@sechaparroc). Ideally, by 'that time' _primitives_ `Point`, `Rectangle`, `Vector` and `Matrix` should already been ported and tested (@jiapulidoar), and the API docs completed (@nakednous). I guess 'that time' (where we should celebrate a new planning meeting) should be at some point around March, 2018. What do you guys think?

> Milestone: Run _input_ examples

### @nakednous tasks

#### Stage 2

1. API docs. Aproximate due date: March, 2018.
2. Proscene (see TODO [here](https://github.com/VisualComputing/proscene.js/blob/processing/TODO.md)). Aproximate due date: March, 2018.

### @sechaparroc tasks

### @jiapulidoar tasks

#### Stage 2

1. Timing examples, fpsTiming.js gh-pages branch. Aproximate due date: December, 2017.
2. Input Examples, bias.js gh-pages branch. Aproximate due date: December, 2017.
3. Port Primitives: Aproximate due date: -- 2018.
   - Point  -- 
   - Rectangle  --
   - Vector  --
   - Matrix  --
