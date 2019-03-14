import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.text.DecimalFormat;

/**
* Features the Database class requires to fulfil functionality.
*/
public class DataOperator {

  //stores the connection
  protected Connection connection = null;
  //for formating of numbers
  private DecimalFormat decimalFormat;

  /**
  * Adds the name of the database to.
  *
  * @param dbFile the location of database file.
  * @throws SQLException inevent of SQL Error.
  */
  public DataOperator(String dbFile) throws SQLException {

    //checks the file is a database file
    if (!validFile(dbFile, "db")) {
      System.out.println("Warning Extension is not .db");
    }
      //the pattern the number formating will follow
      String pattern = "##0.#";
      this.decimalFormat = new DecimalFormat(pattern);
      String databaseURL = "jdbc:sqlite:" + dbFile;
      this.connection = DriverManager.getConnection(databaseURL);
  }

  /**
  * Determines if a file is of a valid format.
  *
  * @param filename the name of the file being verified
  * @param extension the expected extension of that file
  * @return true if the extension is the expected type
  */
  public static boolean validFile(String filename, String extension) {

    String[] sFileName = filename.split("\\.");
    boolean valid = false;
    if (sFileName[sFileName.length - 1].equals(extension)) {
      valid = true;
    }
    return valid;
  }

  /**
  * check the haeding of the inputted csv file matche the expect of the CSV dataset.
  *
  * @param heading the actual heading of the current file
  * @return true if the dataset heading is the same as the expected.
  */
  public static boolean validDataset(String heading) {

    String expectedHeading = "Number,Name,City,Cuisine Style,Ranking,Rating,"
    + "Price Range,Number of Reviews,Reviews,URL_TA,ID_TA";
    boolean valid = false;
    if (heading != null && heading.equals(expectedHeading)) {
      valid = true;
    }
      return valid;
  }

  /**
  * Verifies a number is correct minimum rating format.
  *
  * @param number the number being checked if it is in the correct format
  * @return true if it is in an valid format
  */
  public static boolean verifyNumber(String number) {

    final int minRating = 0;
    final int maxRating = 5;
    String error = "minimum is in invalid format";
    boolean valid = false;
    try {
      Double num = Double.parseDouble(number);
      //checks number does not fall out the boundary of possible ratings
      if (num < minRating || num > maxRating) {
        System.out.println(error);
      }
      else {
        valid = true;
      }
    }
    catch (NumberFormatException e) {
      System.out.println(error);
    }
    return valid;
  }

  /**
  * prints a result set with columns seperate by commas.
  *
  * @param resultSet the result set being printed
  * @throws SQLException in event of SQL Error
  */
  protected void print(ResultSet resultSet) throws SQLException {

    //finds the number of columns in a result set
    int columnNumber = resultSet.getMetaData().getColumnCount();

    while (resultSet.next()) {

      String output = "";
      for (int i = 1; i <= columnNumber; i++) {
        try {
          output += this.decimalFormat.format(Double.parseDouble(resultSet.getString(i))) + ", ";
        }
        catch (NumberFormatException e) {
          output += resultSet.getString(i) + ", ";

        }

      }

      //to remove extra comma and space
      output = output.substring(0, output.length() - 2);
      System.out.println(output);
    }
  }

  /**
  * Creates a statement which runs SQL for an Update to database.
  *
  * @param sql the sql being run on.
  * @throws SQLException in the event of a SQL Error.
  */
  protected void executeUpdate(String sql) throws SQLException {

      Statement statement = this.connection.createStatement();
      statement.executeUpdate(sql);
      statement.close();
  }

  /**
  * Create a new SQL view.
  *
  * @param name the name of the view
  * @param query the query which will be set as the view
  * @throws SQLException in even of a SQL Error
  */
  protected void createView(String name, String query) throws SQLException {
    String sql = "CREATE VIEW IF NOT EXISTS " + name + " AS " + query;
    executeUpdate(sql);
  }

  /**
  * Adds a row of data to the database.
  *
  * @param line the unformatted line of data
  * @throws SQLException in event of SQL Error
  */
  protected void addRow(String sLine) throws SQLException {

    //The indexes of the required data
    final int numberIndex = 0;
    final int nameIndex = 1;
    final int cityIndex = 2;
    final int styleIndex = 3;
    final int ratingIndex = 5;

    //database indexes being used
    final int dNumberIndex = 1;
    final int dNameIndex = 2;
    final int dCityIndex = 3;
    final int dStyleIndex = 4;
    final int dRatingIndex = 5;

    String[] line = sLine.split(",");

    String sql = "INSERT INTO restaurant VALUES (?, ?, ?, ?, ?)";
    //adds the values to the corresponding question mark
    PreparedStatement statement = this.connection.prepareStatement(sql);
    statement.setString(dNumberIndex, line[numberIndex]);
    statement.setString(dNameIndex, line[nameIndex]);
    statement.setString(dCityIndex, line[cityIndex]);

    if (line[styleIndex].isEmpty()) {

      statement.setNull(dStyleIndex, 0);
    }
    else {

      statement.setString(dStyleIndex, line[styleIndex]);
    }

    if (line[ratingIndex].isEmpty() || Double.parseDouble(line[ratingIndex]) < 0) {
      statement.setNull(dRatingIndex, 0);
    }
    else {
      statement.setString(dRatingIndex, line[ratingIndex]);
    }

    statement.executeUpdate();
    statement.close();
  }

  /**
  * Creates all the views the database will contain.
  *
  * @throws SQLException in the event of a SQL Error
  */
  protected void createViews() throws SQLException {
    //A view for query1
    String query1 = "SELECT city, name, rating, cuisine "
    + "FROM restaurant WHERE (city = 'Edinburgh' or city = 'Amsterdam') and cuisine "
    + "LIKE '%European%'and rating = 5";
    createView("[query1]", query1);

    String query4 = "SELECT city, AVG(rating) AS average FROM restaurant GROUP BY city";
    createView("[query4]", query4);

    String query6 = "SELECT STDEV(rating) FROM restaurant";
    createView("[query6]", query6);

    String query7 = "SELECT city, STDEV(rating) FROM restaurant GROUP BY city";
    createView("[query7]", query7);

  }

  /**
  * Gets a view from database and prints it to terminal.
  *
  * @param name the view being accessed.
  * @throws SQLException in the event of a SQL Error.
  */
  protected void getView(String name) throws SQLException {
    String query = "SELECT * FROM " + name;
    executeQuery(query);
  }

  /**
  * Executes a query and prints its result set.
  *
  * @param sql the query being executed
  * @throws SQLException in the event of a SQL Error
  */
    protected void executeQuery(String sql) throws SQLException {

      Statement statement = this.connection.createStatement();
      ResultSet resultSet = statement.executeQuery(sql);
      print(resultSet);
      statement.close();
    }

}
