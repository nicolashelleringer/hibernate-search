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
package org.hibernate.search.test.query.facet;

import java.util.Iterator;
import java.util.List;

import org.apache.lucene.facet.search.params.FacetRequest;
import org.apache.lucene.facet.search.params.FacetSearchParams;
import org.apache.lucene.facet.search.results.FacetResult;
import org.apache.lucene.facet.search.results.FacetResultNode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.poi.hssf.record.IterationRecord;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.SearchException;
import org.hibernate.search.query.engine.spi.FacetManager;
import org.hibernate.search.query.facet.Facet;
import org.hibernate.search.query.facet.FacetSortOrder;
import org.hibernate.search.query.facet.FacetingRequest;

/**
 * @author Nicolas Helleringer
 */
public class SimpleNativeFacetingTest extends AbstractFacetTest {
	private final String indexFieldName = "cubicCapacity";
	private final String facetName = "ccs";

	public void testSimpleFaceting() throws Exception {
		FacetRequest facetRequest = queryBuilder( NativeCar.class ).facet()
				.name( facetName )
				.onField( indexFieldName )
				.discrete()
				.createNativeFacetingRequest();
		FullTextQuery query = queryHondaWithFacet( facetRequest );

		List<FacetResult> facetResults = query.getFacetManager().getFacetResults();

		assertEquals( "Wrong number of facets", 3, facetResults.get(0).getFacetResultNode().getNumSubResults() );
	}

	public void testDefaultSortOrderIsCount() throws Exception {
		FacetRequest request = queryBuilder( NativeCar.class ).facet()
				.name( facetName )
				.onField( indexFieldName )
				.discrete()
				.createNativeFacetingRequest();
		FullTextQuery query = queryHondaWithFacet( request );

		List<FacetResult> facetResults = query.getFacetManager().getFacetResults();

		assertNativeFacetCounts( facetResults.get( 0 ), new int[] {5, 4, 4} );
	}

	public void testCountSortOrderAsc() throws Exception {
		FacetRequest request = queryBuilder( NativeCar.class ).facet()
				.name( facetName )
				.onField( indexFieldName )
				.discrete()
				.orderedBy( FacetSortOrder.COUNT_ASC )
				.createNativeFacetingRequest();
		FullTextQuery query = queryHondaWithFacet( request );

		List<FacetResult> facetResults = query.getFacetManager().getFacetResults();

		assertNativeFacetCounts( facetResults.get( 0 ), new int[] {4, 4, 5} );
	}

	public void testCountSortOrderDesc() throws Exception {
		FacetRequest request = queryBuilder( NativeCar.class ).facet()
				.name( facetName )
				.onField( indexFieldName )
				.discrete()
				.orderedBy( FacetSortOrder.COUNT_DESC )
				.createNativeFacetingRequest();
		FullTextQuery query = queryHondaWithFacet( request );

		List<FacetResult> facetResults = query.getFacetManager().getFacetResults();

		assertNativeFacetCounts( facetResults.get( 0 ), new int[] {5, 4, 4} );
	}

	public void testAlphabeticalSortOrder() throws Exception {
		/* not supported by native lucene facets
		FacetRequest request = queryBuilder( NativeCar.class ).facet()
				.name( facetName )
				.onField( indexFieldName )
				.discrete()
				.orderedBy( FacetSortOrder.FIELD_VALUE )
				.createNativeFacetingRequest();
		FullTextQuery query = queryHondaWithFacet( request );

		List<FacetResult> facetResults = query.getFacetManager().getFacetResults();

		Iterator<? extends FacetResultNode> resultNodes = facetResults.get( 0 ).getFacetResultNode().getSubResults().iterator();

		String previousFacetValue = resultNodes.next().getLabel().lastComponent();
		String currentFacetValue;
		*/
		/*
		while ( resultNodes.hasNext() ) {
			currentFacetValue = resultNodes.next().getLabel().lastComponent();
			assertTrue( "Wrong alphabetical sort order", previousFacetValue.compareTo( currentFacetValue ) < 0 );
			previousFacetValue = currentFacetValue;
		}
		*/
		/*
		Not supported by native facets
		*/
	}

	public void testZeroCountsExcluded() throws Exception {
		FacetRequest request = queryBuilder( NativeCar.class ).facet()
				.name( facetName )
				.onField( indexFieldName )
				.discrete()
				.orderedBy( FacetSortOrder.COUNT_DESC )
				.includeZeroCounts( false )
				.createNativeFacetingRequest();
		FullTextQuery query = queryHondaWithFacet( request );

		/*
		Not supported by native facets
		*/
	}

	// see also HSEARCH-776
	public void testMaxFacetCounts() throws Exception {
		FacetRequest request = queryBuilder( NativeCar.class ).facet()
				.name( facetName )
				.onField( indexFieldName )
				.discrete()
				.orderedBy( FacetSortOrder.COUNT_DESC )
				.maxFacetCount( 1 )
				.createNativeFacetingRequest();
		FullTextQuery query = queryHondaWithFacet( request );

		List<FacetResult> facetResults = query.getFacetManager().getFacetResults();

		assertEquals( "The number of facets should be restricted", 1, facetResults.get( 0 ).getFacetResultNode().getNumSubResults() );
		assertNativeFacetCounts( facetResults.get( 0 ), new int[] { 5 } );
	}

	public void testNullFieldNameThrowsException() {
		try {
			queryBuilder( NativeCar.class ).facet()
					.name( facetName )
					.onField( null )
					.discrete()
					.createNativeFacetingRequest();
			fail( "null should not be a valid field name" );
		}
		catch ( IllegalArgumentException e ) {
			// success
		}
	}

	public void testNullRequestNameThrowsException() {
		try {
			queryBuilder( NativeCar.class ).facet()
					.name( null )
					.onField( indexFieldName )
					.discrete()
					.createNativeFacetingRequest();
			fail( "null should not be a valid request name" );
		}
		catch ( IllegalArgumentException e ) {
			// success
		}
	}

	public void testUnknownFieldNameReturnsEmptyResults() {
		FacetRequest request = queryBuilder( NativeCar.class ).facet()
				.name( "foo" )
				.onField( "foobar" )
				.discrete()
				.createNativeFacetingRequest();
		FullTextQuery query = queryHondaWithFacet( request );

		List<FacetResult> facetResults = query.getFacetManager().getFacetResults();

		assertTrue( facetResults.size() == 0 );
	}

	public void testRangeDefinitionSortOrderThrowsExceptionForDiscreteFaceting() {
		try {
			queryBuilder( NativeCar.class ).facet()
					.name( facetName )
					.onField( indexFieldName )
					.discrete()
					.orderedBy( FacetSortOrder.RANGE_DEFINITION_ODER )
					.createNativeFacetingRequest();
			fail( "RANGE_DEFINITION_ODER not allowed on discrete faceting" );
		}
		catch ( SearchException e ) {
			// success
		}
	}

	public void testEnableDisableFacets() {
		FacetRequest request = queryBuilder( NativeCar.class ).facet()
				.name( facetName )
				.onField( indexFieldName )
				.discrete()
				.createNativeFacetingRequest();
		FullTextQuery query = queryHondaWithFacet( request );

		List<FacetResult> facetResults = query.getFacetManager().getFacetResults();
		assertTrue( "We should have facet results", query.getFacetManager().getFacetResults().size() > 0 );

		query.getFacetManager().disableFaceting( facetName );
		query.list();

		assertTrue( "We should have no facets", query.getFacetManager().getFacetResults().size() == 0 );
	}

	public void testMultipleFacets() {
		final String descendingOrderedFacet = "desc";
		FacetRequest requestDesc = queryBuilder( NativeCar.class ).facet()
				.name( descendingOrderedFacet )
				.onField( indexFieldName )
				.discrete()
				.createNativeFacetingRequest();

		final String ascendingOrderedFacet = "asc";
		FacetRequest requestAsc = queryBuilder( NativeCar.class ).facet()
				.name( ascendingOrderedFacet )
				.onField( indexFieldName )
				.discrete()
				.orderedBy( FacetSortOrder.COUNT_ASC )
				.createNativeFacetingRequest();
		TermQuery term = new TermQuery( new Term( "make", "honda" ) );
		FullTextQuery query = fullTextSession.createFullTextQuery( term, NativeCar.class );
		FacetManager facetManager = query.getFacetManager();

		FacetSearchParams facetSearchParams = new FacetSearchParams();
		facetSearchParams.addFacetRequest( requestDesc );
		facetSearchParams.addFacetRequest( requestAsc );
		facetManager.enableNativeFaceting( facetSearchParams );

		/*
		assertNativeFacetCounts( facetManager.getFacetResults().get( 0 ), new int[] { 5, 4, 4 } );
		assertNativeFacetCounts( facetManager.getFacetResults().get( 0 ), new int[] { 4, 4, 5 } );
		*/

		/*
			Requesting same facets multiple times is not suppported by Lucene natice facets
		*/

		/*
		facetManager.disableFaceting( descendingOrderedFacet );
		assertTrue(
				"descendingOrderedFacet should be disabled", query.getFacetManager().getFacets(
				descendingOrderedFacet
		).isEmpty()
		);
		assertFacetCounts( facetManager.getFacets( ascendingOrderedFacet ), new int[] { 0, 4, 4, 5 } );

		facetManager.disableFaceting( ascendingOrderedFacet );
		assertTrue(
				"descendingOrderedFacet should be disabled",
				facetManager.getFacets( descendingOrderedFacet ).isEmpty()
		);
		assertTrue(
				"ascendingOrderedFacet should be disabled",
				facetManager.getFacets( ascendingOrderedFacet ).isEmpty()
		);
		*/
	}

	private FullTextQuery queryHondaWithFacet(FacetRequest facetRequest) {
		Query luceneQuery = queryBuilder( NativeCar.class ).keyword().onField( "make" ).matching( "Honda" ).createQuery();
		FullTextQuery query = fullTextSession.createFullTextQuery( luceneQuery, NativeCar.class );
		FacetSearchParams facetSearchParams = new FacetSearchParams();
		facetSearchParams.addFacetRequest( facetRequest );
		query.getFacetManager().enableNativeFaceting( facetSearchParams );
		assertEquals( "Wrong number of query matches", 13, query.getResultSize() );
		return query;
	}

	public void loadTestData(Session session) {
		Transaction tx = session.beginTransaction();
		for ( String make : makes ) {
			for ( String color : colors ) {
				for ( int cc : ccs ) {
					NativeCar car = new NativeCar( make, color, cc );
					session.save( car );
				}
			}
		}
		NativeCar car = new NativeCar( "Honda", "yellow", 2407 );
		session.save( car );

		car = new NativeCar( "Ford", "yellow", 2500 );
		session.save( car );
		tx.commit();
		session.clear();
	}

	protected Class<?>[] getAnnotatedClasses() {
		return new Class[] {
				NativeCar.class
		};
	}
}
