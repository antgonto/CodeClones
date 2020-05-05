/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 *
 * @author Fede
 */
public class Neo4jSessionFactory {

    private final static Configuration configuration = new Configuration.Builder()
            .uri("bolt://localhost")
            .credentials("neo4j", "clones")
            .build();
    private final static SessionFactory sessionFactory = new SessionFactory(configuration, "domain");
    private static Neo4jSessionFactory factory = new Neo4jSessionFactory();

    public static Neo4jSessionFactory getInstance() {
        return factory;
    }

    // prevent external instantiation
    private Neo4jSessionFactory() {
    }

    public Session getNeo4jSession() {
        return sessionFactory.openSession();
    }
}
