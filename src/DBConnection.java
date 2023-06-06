import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;

public class DBConnection {
    
    public static Connection getConnection(){
        
          try{
    
            String dbRoot = "jdbc:mysql://";
            String hostName = "localhost:3306/";
            String dbName = "tabulation_database";
            String dbUrl = dbRoot + hostName + dbName;

            String hostUsername = "root";
            String hostPassword = "";

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection myConn = DriverManager.getConnection(dbUrl, hostUsername, hostPassword);
            
//            JOptionPane.showMessageDialog(null, "Connected to Database");
            return myConn;

          } catch(Exception e){
              JOptionPane.showMessageDialog(null, e);
              JOptionPane.showMessageDialog(null, "Connection Error");
              return null;
          }         
    }
    
    static Connection conns(){
        throw new UnsupportedOperationException("Not supported yet: ");
    }
}