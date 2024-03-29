package com.sdi.persistence.util;


import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sdi.persistence.PersistenceException;

public class Jdbc {
	private static final String QUERIES_PROPERTIES_FILE = "sql_queries.properties";
	private static final String PERSISTANCE_PROPERTIES_FILE = "persistance.properties";
	
	
	private static Properties sqlQueries;
	private static Properties persistanceConfig;
	private static DataSource dataSource;
	
	static {
		sqlQueries = loadProperties( QUERIES_PROPERTIES_FILE );
		persistanceConfig = loadProperties(PERSISTANCE_PROPERTIES_FILE);
		
	
		dataSource = loadDataSource();
	}

	private static ThreadLocal<Connection> threadLocal = new ThreadLocal<>();

	public static Connection createConnection() {
		try {
			
			Connection con = dataSource.getConnection();
			threadLocal.set(con);
			return con;
			
		} catch (SQLTimeoutException e) {
			throw new PersistenceException("Timeout opennig JDBC conection", e);
		} catch (SQLException e) {
			throw new PersistenceException("An unexpected JDBC error has ocurred", e);
		}
	}

	public static Connection getCurrentConnection() {
		Connection con = threadLocal.get();
		if (con == null) {
			con = createConnection();
		}
		return con;
	}

	public static String getSqlQuery(String queryKey) {
		return sqlQueries.getProperty(queryKey).trim();
	}

	public static void close(ResultSet rs, PreparedStatement ps, Connection con) {
		close(rs);
		close(ps);
		close(con);
	}

	public static void close(PreparedStatement ps, Connection con) {
		close( ps );
		close( con );
	}

	static void close(ResultSet rs) {
		if (rs != null) { try{ rs.close(); } catch (Exception ex){}};
	}

	public static void close(PreparedStatement ps) {
		if (ps != null) { try{ ps.close(); } catch (Exception ex){}};
	}

	/**
	 * If not using a Transaction object, a call to this method physically closes 
	 * the connection (each call to a Dao method is in its own transaction).
	 * 
	 * If there is a Transaction open, then this method does nothing as the 
	 * Transaction itself will close the connection by calling commit or rollback
	 *    
	 * @param con
	 */
	public static void close(Connection con) {
		if ( ! isInAutoCommitMode(con) ) return; // Transaction is in charge
		
		threadLocal.set(null);
		if (con != null) { try{ con.close(); } catch (Exception ex){}};
	}

	private static boolean isInAutoCommitMode(Connection con) {
		try {
			return con.getAutoCommit();
		} catch (SQLException e) {
			throw new PersistenceException("Unexpected exception", e);
		}
	}

	private static Properties loadProperties(String fileName) {
		Properties prop = new Properties();
		InputStream stream = Jdbc.class.getClassLoader().getResourceAsStream(fileName);
		try {
			prop.load( stream );
		} catch (IOException e) {
			throw new PersistenceException("Wrong configutation file " + fileName );
		}
		return prop;
	}
	
	private static DataSource loadDataSource(){
		try {
			String jndiKey = persistanceConfig.getProperty("JNDI_DATASOURCE");

			Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup(jndiKey);
			return ds;
			
		} catch (NamingException e) {
			throw new RuntimeException("Can't open JDBC conection from JNDI", e);
		}
	}

}
