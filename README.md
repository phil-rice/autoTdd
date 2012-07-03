AutoTDD
=======

Write the tests, and the code writes itself.


This is effectively TDD to produce a function.

I see multiple DSLs:
* Pure function (1 or more inputs, 1 output)
* Matching (each time the condition is true, get an output)

Features we need:
* Outputs that we merge with folding functions
* Outputs with side effects
* Outputs that return a result

1: Need an Eclipse nature to build and run the tests (this would also create any intermediate source code files)
2: Need a maven plugin to execute the tests in the same way that maven runs junit tests
3: Need multiple eclipse views:
** The tree
** A trace of "this" constraint going through the tree
** A trace of "this" situation going through the tree
** Run all "these things" through, until "this" condition is true

Currently proposing three plans:
* Java Internal DSL in which we use closures, and use JDT or a decompiler to display the conditions
* Hybrid DSL with Java/Groovy

Run time:
* Either interpreted (i.e. like ExampleG)
* or compiled: i.e. turn into Java (or some other language )source codes which then get compiled and executed

Increase of scope.
* One the DSLs handle conflict resolution, and the above features, then we need to consider how to compose them
** Composition by aggregating constraints
** Composition by one function calling another
** Composition using tools like spring integration

It would be very cool to have integration tests that state the input to the composition, and place assertions along the way. The assertions could be strongly bound to constraints, or just tests

Alternative development environments:
Note that after eclipse, we should move to IntelliJ, Emacs and Visual Studio. Maybe Textmate also, but I don't know enough about it. 