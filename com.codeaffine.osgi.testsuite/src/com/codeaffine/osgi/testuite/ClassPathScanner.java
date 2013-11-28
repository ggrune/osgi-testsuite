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

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;

class ClassPathScanner {

  private static final String DOT_CLASS = ".class";
  private final Bundle bundle;
  private final Properties devProperties;
  private final String[] pattern;
  private final Set<Class<?>> classes;

  ClassPathScanner( Bundle bundle, Properties devProperties, String... pattern ) {
    this.bundle = bundle;
    this.devProperties = devProperties;
    this.pattern = pattern;
    this.classes = new HashSet<Class<?>>();
  }

  Class<?>[] scan() throws InitializationError {
    Collection<String> resources = listResources();
    loadClasses( resources );
    return classes.toArray( new Class[ classes.size() ] );
  }

  private Collection<String> listResources() {
    Collection<String> result = new ArrayList<String>();
    String[] classPathRoots = getClassPathRoots();
    for( String classPathRoot : classPathRoots ) {
      Collection<String> resources = listResources( classPathRoot );
      result.addAll( resources );
    }
    return result;
  }

  private Collection<String> listResources( String classPathRoot ) {
    List<String> resources = new ArrayList<String>();
    for( String p : pattern ) {
      Enumeration<URL> root = bundle.findEntries( classPathRoot, p, true );
      while( root != null && root.hasMoreElements() ) {
        URL url = root.nextElement();
        resources.add( url.getFile().replace( classPathRoot + "/", "" ) );
      }
    }
    return resources;
  }

  private void loadClasses( Collection<String> resources ) throws InitializationError {
    for( String resource : resources ) {
      String className = toClassName( stripClassPathRoot( resource ) );
      Class<?> loadedClass = loadClass( className );
      TestClass testClass = new TestClass( loadedClass );
      List<FrameworkMethod> annotatedMethods = testClass.getAnnotatedMethods( Test.class );

      if( !annotatedMethods.isEmpty() && !Modifier.isAbstract( loadedClass.getModifiers() ) ) {
        classes.add( loadedClass );
      }
    }
  }

  private String[] getClassPathRoots() {
    Collection<String> classPathRoots = new LinkedList<String>();
    if( !devProperties.isEmpty() ) {
      appendClassPathRoots( classPathRoots, devProperties.getProperty( bundle.getSymbolicName() ) );
      appendClassPathRoots( classPathRoots, devProperties.getProperty( "*" ) );
    }
    if( classPathRoots.isEmpty() ) {
      classPathRoots.add( "/" );
    }
    return classPathRoots.toArray( new String[ classPathRoots.size() ] );
  }

  private Class<?> loadClass( String className ) throws InitializationError {
    BundleDescription desc = ( BundleDescription )bundle.adapt( BundleRevision.class );
    try {
      if( desc.getTypes() == BundleRevision.TYPE_FRAGMENT ) {
        HostSpecification host = desc.getHost();
        BundleDescription[] hosts = host.getHosts();
        if( hosts.length == 0 ) {
          throw new InitializationError( "Missing host bundle for fragment "
                                         + bundle.getSymbolicName() );
        }
        return hosts[ 0 ].getBundle().loadClass( className );
      }
      return bundle.loadClass( className );
    } catch( ClassNotFoundException exception ) {
      throw new InitializationError( exception );
    }
  }

  private String stripClassPathRoot( String resource ) {
    String result = resource;
    String[] classPathRoots = getClassPathRoots();
    boolean found = false;
    for( int i = 0; !found && i < classPathRoots.length; i++ ) {
      String classPathRoot = classPathRoots[ i ];
      if( result.startsWith( classPathRoot ) ) {
        result = result.substring( classPathRoot.length() );
        found = true;
      }
    }
    if( result.startsWith( "/" ) ) {
      result = result.substring( 1 );
    }
    return result;
  }

  private static String toClassName( String string ) {
    String result = string.replace( '/', '.' );
    if( result.endsWith( DOT_CLASS ) ) {
      result = result.substring( 0, result.length() - DOT_CLASS.length() );
    }
    return result;
  }

  private static void appendClassPathRoots( Collection<String> collection, String classPathRoots ) {
    if( classPathRoots != null ) {
      collection.addAll( Arrays.asList( classPathRoots.split( "," ) ) );
    }
  }
}
