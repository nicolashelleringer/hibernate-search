/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
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

package org.hibernate.search.test.facet;

import junit.framework.Assert;
import org.apache.lucene.facet.search.params.CountFacetRequest;
import org.apache.lucene.facet.search.params.FacetSearchParams;
import org.apache.lucene.facet.search.results.FacetResult;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.test.SearchTestCase;

import java.util.List;

/**
 * Native Lucene facets usage tests
 *
 * @author Nicolas Helleringer <nicolas.helleringer@novacodex.net>
 */
public class NativeFacetTest extends SearchTestCase {

    public void testNativeFacetIndexing() throws Exception {
        //Write
        Book book = new Book();

        book.title = "Nine princes in Amber";
        book.author = "Roger Zelazny";
        book.categories = new String[] {"Sci-Fi","Fantasy"};

        FullTextSession fullTextSession = Search.getFullTextSession(openSession());

        Transaction tx = fullTextSession.beginTransaction();
        fullTextSession.save( book );
        tx.commit();

        //Read
        tx = fullTextSession.beginTransaction();

        final QueryBuilder builder = fullTextSession.getSearchFactory()
                .buildQueryBuilder().forEntity( Book.class ).get();

        org.apache.lucene.search.Query luceneQuery = builder.all().createQuery();

        FullTextQuery hibQuery = fullTextSession.createFullTextQuery( luceneQuery, Book.class );

        FacetSearchParams facetSearchParams = new FacetSearchParams();
        facetSearchParams.addFacetRequest(new CountFacetRequest(
                new CategoryPath("author"), 10));
        hibQuery.getFacetManager().enableNativeFaceting( facetSearchParams );

        List results = hibQuery.list();
        Assert.assertEquals(1, results.size());

		List<FacetResult> facetResults = hibQuery.getFacetManager().getFacetResults();

		System.out.println( facetResults );

        List<?> books = fullTextSession.createQuery( "from " + Book.class.getName() ).list();
        for (Object entity : books) {
            fullTextSession.delete( entity );
        }
        tx.commit();
        fullTextSession.close();
    }


    @Override
    protected Class<?>[] getAnnotatedClasses() {
        return new Class<?>[] {
                Book.class
        };
    }
}
