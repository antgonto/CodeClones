
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import domain.ClassInfo;
import java.util.Map;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.session.Session;
import utils.Globals;
import utils.Neo4jSessionFactory;

/**
 *
 * @author Fede
 */
public class DBUtility {

    public static void insert() throws ConnectionException {
        insert(Globals.getProcessResults());
    }
    
    public static void insert(Map<String, ClassInfo> results) throws ConnectionException {
        Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
         
        session.purgeDatabase();
        for (Map.Entry<String, ClassInfo> entry : results.entrySet()) {
            ClassInfo cls = entry.getValue();
            session.save(cls);
        }
        Globals.resetProcessResults();
    }
}
