package org.hibernate.search.test.facet;

import org.hibernate.search.annotations.Facet;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Indexed
public class Book {

    @Id
    @GeneratedValue
    private int id;

    @Field
    public String title;

    @Field
    @Facet
    public String author;

	@Facet
    public String[] categories;
}
