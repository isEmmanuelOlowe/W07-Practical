/**
* Allows users to perform quries on Restaurant dataset
*/
public class W07Practical {

  /**
  * Allows user to perform quries on Restaurant dataset
  */
  public static void main(String[] args) {
    System.out.println(System.getProperty("java.class.path"));
    if (args.length >= 2){
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
      }
      database.close();
    }
  }
}
