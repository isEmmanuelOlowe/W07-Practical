import java.sql.SQLException;
/**
* Allows users to perform quries on Restaurant dataset.
*/
public class W07Practical {

  /**
  * Allows user to perform quries on Restaurant dataset.
  *
  * @param args arguments by user which determine actions of program
  */
  public static void main(String[] args) {
    String usage = "Usage: java -cp sqlite-jdbc.jar:. W07Practical <db_file> "
    + "<action> [input_file | minimum_rating]";

    try {

      String dbFile = args[0];
      String operation = args[1];
      Database database = new Database(dbFile);
      //find the selected operation user wants to perform
      switch (operation) {

        case "create": database.create(args[2]); break;
        case "query1": database.query1(); break;
        case "query2": database.query2(args[2]); break;
        case "query3": database.query3(args[2]); break;
        case "query4": database.query4(); break;
        case "query5": database.query5(); break;
        case "query6": database.query6(); break;
        case "query7": database.query7(); break;
        default: System.out.println(usage);
      }

      //closes connection to the database
      database.close();
    }

    //in event incorrect command line arguments are given
    catch (ArrayIndexOutOfBoundsException e) {

      System.out.println(usage);
    }

    //in event of SQL Error
    catch (SQLException e) {

      System.out.println(e.getMessage());
    }
  }
}
