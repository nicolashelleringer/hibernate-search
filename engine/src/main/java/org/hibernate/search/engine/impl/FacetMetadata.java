/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2012, Red Hat, Inc. and/or its affiliates or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat, Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.search.engine.impl;

import org.apache.lucene.analysis.Analyzer;

import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.bridge.StringBridge;
import org.hibernate.search.bridge.TwoWayStringBridge;
import org.hibernate.search.bridge.impl.BridgeFactory;
import org.hibernate.search.engine.spi.AbstractDocumentBuilder;
import org.hibernate.search.impl.ConfigContext;
import org.hibernate.search.util.impl.ReflectionHelper;
import org.hibernate.search.util.logging.impl.Log;
import org.hibernate.search.util.logging.impl.LoggerFactory;

/**
 * Encapsulating the metadata for a single {@code @Facet} annotation.
 *
 * @author Nicolas Helleringer
 */
public class FacetMetadata {
	private static final Log log = LoggerFactory.make();

	private final XProperty facetGetter;
	private final String facetPath;
	private final String nullToken;
	private final StringBridge facetBridge;

	public FacetMetadata(String prefix,
						XProperty member,
						org.hibernate.search.annotations.Facet facetAnn,
						NumericField numericFieldAnn,
						ConfigContext context,
						ReflectionManager reflectionManager) {
		ReflectionHelper.setAccessible( member );
		facetGetter = member;
		String indexNullAs;
		Analyzer tmpAnalyzer;

		facetPath = prefix + ReflectionHelper.getAttributeName( member, facetAnn.path() );
		// null token
		indexNullAs = facetAnn.indexNullAs();
		if ( indexNullAs.equals( org.hibernate.search.annotations.Field.DO_NOT_INDEX_NULL ) ) {
			indexNullAs = null;
		}
		else if ( indexNullAs.equals( org.hibernate.search.annotations.Field.DEFAULT_NULL_TOKEN ) ) {
			indexNullAs = context.getDefaultNullToken();
		}
		nullToken = indexNullAs;

		StringBridge bridge = BridgeFactory.guessFacetType( facetAnn, numericFieldAnn, member, reflectionManager );
		if ( indexNullAs != null && bridge instanceof TwoWayStringBridge ) {
			//bridge = new NullEncodingTwoWayStringBridge( (TwoWayFieldBridge) bridge, indexNullAs );
		}
		facetBridge = bridge;
	}

	public String getFacetPath() {
		return facetPath;
	}

	public void appendToPropertiesMetadata(AbstractDocumentBuilder.PropertiesMetadata propertiesMetadata) {
		sanityCheckFieldConfiguration( propertiesMetadata );

		propertiesMetadata.facetGetters.add( facetGetter );
		propertiesMetadata.facetGetterNames.add( facetGetter.getName() );
		propertiesMetadata.facetPaths.add( facetPath );
		propertiesMetadata.facetPathToPositionMap.put( facetPath, propertiesMetadata.facetPaths.size() );
		propertiesMetadata.facetNullTokens.add( nullToken );
		propertiesMetadata.facetBridges.add( facetBridge );
	}

	private void sanityCheckFieldConfiguration(AbstractDocumentBuilder.PropertiesMetadata propertiesMetadata) {
		/*int indexOfFieldWithSameName = propertiesMetadata.facetPaths.lastIndexOf( facetPath );
		if ( indexOfFieldWithSameName != -1 ) {
			if ( !propertiesMetadata.fieldIndex.get( indexOfFieldWithSameName ).equals( index ) ) {
				log.inconsistentFieldConfiguration( facetPath );
			}
		}*/
	}
}


