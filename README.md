# Automated OSGi Test Suite

The OSGi Test Suite is a JUnit test runner that lets you specify a list of bundles and runs all tests contained in these bundles. The test runner is provided as a bundle itself so that it can be easily consumed from OSGi projects.

## Usage
Annotate a class with `@RunWith(BundleTestSuite.class)` and `@TestBundles({"bundle.1", ...})`. 
When you run this class, it will run all the tests in all the bundles.
A test class is identified by its name. All public classes whose names end with 'Test' are considered test classes.
To change the name pattern annotate the class with `@TestNamePattern( { "Test*.class", ... }.

For example:
````Java
@RunWith( BundleTestSuite.class )
@TestBundles( { "org.example.bundle1", "org.example.bundle2" } )
@TestNamePattern( { "*Test*.class", ...} )
public class MasterTestSuite {
}
````

## Download & Integration
The p2 repository

> `http://rherrmann.github.io/osgi-testsuite/repository`

contains a feature with a single bundle. Add this to your target platform.

In order to use the BundleTestSuite, Require-Bundle or Package-Import the bundle/package com.codeaffine.osgi.testsuite.
In addition an implementation of the  OSGi specification [Release 4, Version 4.3](http://www.osgi.org/Release4/Download) (tested with Eclipse Equinox 3.7) and JUnit 4.8 or later is required.

## Lincense
The code is published under the terms of the [Eclipse Public License, version 1.0](http://www.eclipse.org/legal/epl-v10.html).
