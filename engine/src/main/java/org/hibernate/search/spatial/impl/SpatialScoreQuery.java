/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.hibernate.search.spatial.impl;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.CustomScoreProvider;
import org.apache.lucene.search.function.CustomScoreQuery;

import org.hibernate.search.spatial.Coordinates;

public class SpatialScoreQuery extends CustomScoreQuery {

	private final Point center;
	private final Double radius;
	private final String fieldName;

	public SpatialScoreQuery(Query query, Coordinates center, Double radius, String fieldName) {
		super( query );
		this.center = Point.fromCoordinates( center );
		this.radius = radius;
		this.fieldName = fieldName;
	}

	@Override
	protected CustomScoreProvider getCustomScoreProvider(IndexReader reader) throws IOException {
		return new SpatialScoreProvider( reader, center, radius, fieldName);
	}
}
