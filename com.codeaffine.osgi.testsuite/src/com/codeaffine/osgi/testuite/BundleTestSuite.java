/*******************************************************************************
 * Copyright (c) 2012, 2013 Rüdiger Herrmann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rüdiger Herrmann - initial API and implementation
 ******************************************************************************/
package com.codeaffine.osgi.testuite;

import static java.lang.String.format;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Comparator;

import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

/**
 * The BundleTestSuite test runner can be used to run all tests within a given number of OSGi
 * bundles.
 * <p>
 * To use it, annotate a class with <code>@RunWith(BundleTestSuite.class)</code> and
 * <code>@TestBundles({"bundle.1", ...})</code>.  When you run this class, it will run all the
 * tests in all the bundles.
 * </p>
 * <p>
 * Example:
 * <pre>
 *   @RunWith( BundleTestSuite.class )
 *   @TestBundles( { "org.example.bundle1", "org.example.bundle2" } )
 *   public class MasterTestSuite {
 *   }
 * </pre>
 * </p>
 * <p>
 * An <code>InitializationError</code> is thrown if the <code>@TestBundles</code> annotation is
 * missing or if it list bundles that cannot be found.
 * </p>
 *
 * @see RunWith
 * @see TestBundles
 * @since 1.0
 */
public class BundleTestSuite extends Suite {

  /**
   * The <code>TestBundles</code> annotation specifies the bundles to be scanned for test classes
   * when a class annotated with <code>@RunWith(BundleTestSuite.class)</code> is run.
   *
   * <p>A test class is identified by its name. If no <code>TestNamePattern</code> is defined all public classes whose names end with 'Test' are
   * considered test classes.</p>
   */
  @Retention( RetentionPolicy.RUNTIME )
  @Target( ElementType.TYPE )
  public @interface TestBundles {

    /**
     * @return the symbolic names of the bundles that should be scanned for tests
     */
    String[] value();
  }

  /**
   * The <code>TestNamePattern</code> annotation specifies the name pattern that identifies the test classes.
   *
   * <p>Example @TestNamePattern( { "*Test*.class", "_*.class" } )
   */
  @Retention( RetentionPolicy.RUNTIME )
  @Target( ElementType.TYPE )
  public @interface TestNamePattern {

    /**
     * @return the name pattern that identifies the test classes
     */
    String[] value();
  }

  public BundleTestSuite( Class<?> type ) throws InitializationError {
    super( type, getTestClasses( type ) );
  }

  @Override
  protected void runChild( Runner runner, RunNotifier notifier ) {
    super.runChild( runner, notifier );
  }

  private static Class<?>[] getTestClasses( Class<?> type ) throws InitializationError {
    TestBundles bundlesAnnotation = type.getAnnotation( TestBundles.class );
    checkTestBundlesAnnotationExists( type, bundlesAnnotation );
    TestNamePattern patternAnnotation = type.getAnnotation( TestNamePattern.class );
    final TestCollector collector;
    if( patternAnnotation != null ) {
      collector = new TestCollector( patternAnnotation.value(), bundlesAnnotation.value() );
    } else {
      collector = new TestCollector( bundlesAnnotation.value() );
    }
    Class<?>[] collect = collector.collect();
    Arrays.sort( collect , new Comparator<Class<?>>() {

      public int compare( Class<?> o1, Class<?> o2 ) {
        return o1.getName().compareTo( o2.getName() );
      }
    });
    return collect;
  }

  private static void checkTestBundlesAnnotationExists( Class<?> type, TestBundles testBundles )
    throws InitializationError
  {
    if( testBundles == null ) {
      String msg = format( "Class '%s' must have a TestBundles annotation", type.getName() );
      throw new InitializationError( msg );
    }
  }
}