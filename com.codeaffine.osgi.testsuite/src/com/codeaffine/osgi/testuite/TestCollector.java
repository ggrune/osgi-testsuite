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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.junit.runners.model.InitializationError;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.wiring.BundleRevision;

class TestCollector {

  private final String[] bundleSymbolicNames;
  private final String[] pattern;
  private final BundleContext bundleContext;
  private final Set<Class<?>> classes;
  private final Properties devProperties;

  TestCollector( String... bundleSymbolicNames ) {
    this( new String[]{
      "*Test.class"
    }, bundleSymbolicNames );
  }

  TestCollector( String[] pattern, String... bundleSymbolicNames ) {
    this( getBundleContext(), pattern, bundleSymbolicNames );
  }

  TestCollector( BundleContext bundleContext, String... bundleSymbolicNames ) {
    this( bundleContext, new String[]{
      "*Test.class"
    }, bundleSymbolicNames );
  }

  TestCollector( BundleContext bundleContext, String[] pattern, String... bundleSymbolicNames ) {
    this.bundleContext = bundleContext;
    this.pattern = pattern;
    this.bundleSymbolicNames = bundleSymbolicNames;
    this.classes = new HashSet<Class<?>>();
    this.devProperties = new DevPropertiesLoader( bundleContext ).load();
  }

  Class<?>[] collect() throws InitializationError {
    for( String bundleSymbolicName : bundleSymbolicNames ) {
      collect( bundleSymbolicName );
    }
    return classes.toArray( new Class[ classes.size() ] );
  }

  private void collect( String bundleSymbolicName ) throws InitializationError {
    Bundle bundle = getBundle( bundleSymbolicName );
    if( matchesPlatform( bundle ) ) {
      Class<?>[] scan = new ClassPathScanner( bundle, devProperties, pattern ).scan();
      classes.addAll( Arrays.asList( scan ) );
    }
  }

  private static boolean matchesPlatform( Bundle bundle ) {
    BundleDescription desc = ( BundleDescription )bundle.adapt( BundleRevision.class );
    String platformFilter = desc.getPlatformFilter();
    if(platformFilter==null) {
      return true;
    }

    try {
      Filter filter = FrameworkUtil.createFilter( platformFilter );
      Map<String, String> prop = new HashMap<String, String>();
      prop.put( "osgi.os",  System.getProperty( "osgi.os"));
      prop.put( "osgi.arch",  System.getProperty( "osgi.arch"));
      prop.put( "osgi.ws",  System.getProperty( "osgi.ws"));
      return filter.matches( prop );
    } catch( InvalidSyntaxException e ) {
      throw new IllegalStateException( e );
    }
  }

  private Bundle getBundle( String bundleSymbolicName ) throws InitializationError {
    Bundle result = null;
    Bundle[] bundles = bundleContext.getBundles();
    for( int i = 0; result == null && i < bundles.length; i++ ) {
      if( bundles[ i ].getSymbolicName().equals( bundleSymbolicName ) ) {
        result = bundles[ i ];
      }
    }
    if( result == null ) {
      throw new InitializationError( "Bundle not found: " + bundleSymbolicName );
    }
    return result;
  }

  private static BundleContext getBundleContext() {
    Bundle bundle = FrameworkUtil.getBundle( TestCollector.class );
    return bundle == null
                         ? null
                         : bundle.getBundleContext();
  }
}