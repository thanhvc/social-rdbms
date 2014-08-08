package org.exoplatform.social.core.mysql.storage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.mysql.MysqlDBConnect;
import org.picocontainer.Startable;

public class MysqlJNDIConnection implements Startable, MysqlDBConnect {
  private static final Log LOG = ExoLogger.getLogger(MysqlJNDIConnection.class);
  private static final String SCRIPT_FILE_PATH = "conf/mysqlDB_script.sql";
  
  private Statement statement;
  @Override
  public Connection getDBConnection() {
    Connection result = null;
    try {
      Context initContext = new InitialContext();
      Context envContext = (Context) initContext.lookup("java:/comp/env");
      DataSource datasource = (DataSource) envContext
          .lookup(DATASOURCE_CONTEXT);
      if (datasource != null) {
        result = datasource.getConnection();
      } else {
        LOG.error("Failed to lookup datasource.");
      }
    } catch (NamingException ex) {
      LOG.error("Cannot get connection: " + ex);
    } catch (SQLException ex) {
      LOG.error("Cannot get connection: " + ex);
    }
    return result;
  }

  public void start() {
    initDB();
  }

  public void stop() {
    cleanDB();
  }

  private void initDB() {
    //
    cleanDB();
    
    //
    String s = new String();
    StringBuffer sb = new StringBuffer();

    try {
      InputStream in = getClass().getResourceAsStream(SCRIPT_FILE_PATH);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));

      while ((s = br.readLine()) != null) {
        sb.append(s);
      }
      br.close();

      String[] inst = sb.toString().split(";");

      for (int i = 0; i < inst.length; i++) {
        if (!inst[i].trim().equals("")) {
          getStatement().executeUpdate(inst[i]);
        }
      }

    } catch (Exception e) {
      LOG.error("Failed in executing mysql script." + e);
    }
  }

  private void cleanDB() {
    String sql = "DROP DATABASE social";
    String query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'social'";
    
    try {
      ResultSet rs = getStatement().executeQuery(query);                  
      rs.next();
      if (rs.getInt("COUNT(*)") > 0) {
        getStatement().executeUpdate(sql);
      }
    } catch (Exception e) {
      LOG.error("Failed to drop social database.");
    }
  }
  
  
  private Statement getStatement() {
    try {
      if (statement == null) {
        Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
        this.statement = con.createStatement();
      }
      return statement;
    } catch (Exception e) {
      LOG.error("Failed in getting connection." + e);
    }
    return null;
  }
}
