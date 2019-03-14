import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
/**
* Allows the user to interact with the database.
*/
public class Database {

  //stores the connection
  private Connection connection = null;
  //for formating of numbers
  private DecimalFormat decimalFormat;

  /**
  * Adds the name of the database to.
  *
  * @param dbFile the location of database file.
  * @throws SQLException inevent of SQL Error.
  */
  public Database(String dbFile) throws SQLException {

    //the pattern the number formating will follow
    String pattern = "##0.#";
    this.decimalFormat = new DecimalFormat(pattern);
    String databaseURL = "jdbc:sqlite:" + dbFile;
    this.connection = DriverManager.getConnection(databaseURL);
  }

  /**
  * Creates new tables for the sql file using the provided csv file.
  *
  * @param fileName the name of the file being used to created database entries.
  * @throws SQLException in event of SQL Error.
  */
  public void create(String fileName) throws SQLException {

    try {

      //deletes the table if it exists and create a new empty table
      String sql = "DROP TABLE IF EXISTS restaurant";
      String sql2 = "CREATE TABLE restaurant (number int, name varchar(255), city varchar(255),"
      + " cuisine varchar(255), rating REAL)";
      executeUpdate(sql);
      executeUpdate(sql2);

      // Add code to append on restaurants and cuisine styles
      BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
      //for ingnoring first line which is not a data entry
      String line = bufferedReader.readLine();
      this.connection.setAutoCommit(false);
      while ((line = bufferedReader.readLine()) != null) {

        this.addRow(line);
      }

      bufferedReader.close();
      createViews();
      this.connection.commit();
      //closes statement
      System.out.println("OK");
    }
    catch (FileNotFoundException e) {
      System.out.println(e.getMessage());
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }


  /**
  * Creates a statement which runs SQL for an Update to database.
  *
  * @param sql the sql being run on.
  * @throws SQLException in the event of an SQL Error.
  */
  private void executeUpdate(String sql) throws SQLException {

      Statement statement = this.connection.createStatement();
      statement.executeUpdate(sql);
      statement.close();
  }

  /**
  * Create a new SQL view.
  *
  * @param name the name of the view
  * @param query the query which will be set as the view
  * @throws SQLException in even of an SQL Error
  */
  private void createView(String name, String query) throws SQLException {
    String sql = "CREATE VIEW IF NOT EXISTS " + name + " AS " + query;
    executeUpdate(sql);
  }

  /**
  * Adds a row of data to the database.
  *
  * @param line the unformatted line of data
  * @throws SQLException in event of SQL Error
  */
  private void addRow(String sLine) throws SQLException {

    //The indexes of the required data
    final int numberIndex = 0;
    final int nameIndex = 1;
    final int cityIndex = 2;
    final int styleIndex = 3;
    final int ratingIndex = 5;

    //database indexes being used
    final int dNumberIndex= 1;
    final int dNameIndex = 2;
    final int dCityIndex = 3;
    final int dStyleIndex = 4;
    final int dRatingIndex = 5;

    String[] line = sLine.split(",");

    String sql = "INSERT INTO restaurant VALUES (?, ?, ?, ?, ?)";

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
  */
  private void createViews() throws SQLException {
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
  * @throws SQLException in the event of an SQL Error.
  */
  private void getView(String name) throws SQLException {
    String query = "SELECT * FROM " + name;
    executeQuery(query);
  }

  /**
  * Lists the city, name, rating and cuisine style for the best rated resutaurants
  in Amsterdam and Edinbrugh (with rating 5) which serve European Cuisine.
  *
  * @throws SQLException in the event of an SQL Error.
  */
  public void query1() throws SQLException {

    System.out.println("city, name, rating, cuisine_style");
    getView("[query1]");
  }

  /**
  * prints out the total number of restaurants with a rating greater or equal to
  the provided minimum rating.
  *
  * @param minimumRating the search boundary for which restaurants which meet shall be selected
  * @throws SQLException in the event of an SQL Error
  */
  public void query2(String minimumRating) throws SQLException {

    PreparedStatement statement = connection.prepareStatement("SELECT COUNT(name) AS counter FROM restaurant WHERE rating >= ?");
    //Now insert for gettings rating
    statement.setDouble(1, Double.parseDouble(minimumRating));
    ResultSet resultSet = statement.executeQuery();
    System.out.println("Total number of restaurants with rating above (or equal to) "
    + minimumRating);
    System.out.println(resultSet.getInt("counter"));
    statement.close();
  }

  /**
  * prints out a table containing for each city: the city and number of restaurants
  with a rating greater or equal to the provided minimum rating.
  *
  * @param minimumRating the search boundary for which resturatns which meet shall be selected
  * @throws SQLException in the event of an SQL Error
  */
  public void query3(String minimumRating) throws SQLException {

    PreparedStatement statement = connection.prepareStatement("SELECT city, COUNT(rating) AS counter FROM restaurant WHERE rating >= ? GROUP BY city");
    statement.setDouble(1, Double.parseDouble(minimumRating));
    ResultSet resultSet = statement.executeQuery();
    System.out.println("city, number of restaurants with rating above (or equal to) "
    + minimumRating);

    print(resultSet);
    statement.close();
  }

  /**
  * prints out a table, containing for each city, the city and average rating of
  restaurants in that city.
  *
  * @throws SQLException in the event of an SQL Error.
  */
  public void query4() throws SQLException {

    System.out.println("city, average rating");
    getView("[query4]");
  }

  /**
  * For each city prints out the restaurant with the lowest rating: the city name,
  the restaurant name and the rating.
  *
  * @throws SQLException in the event of an SQL Error.
  */
  public void query5() throws SQLException {

    System.out.println("city, name, rating");
    String sql = "SELECT city, min(rating) as minimum FROM restaurant GROUP BY city";
    Statement statement = this.connection.createStatement();
    ResultSet resultSet = statement.executeQuery(sql);
    while (resultSet.next()) {
      Statement statement2 = this.connection.createStatement();
      String sql2 = "SELECT city, name, rating FROM restaurant WHERE rating = "
      + resultSet.getDouble("minimum") + " and city = '" + resultSet.getString("city") + "'";
      ResultSet resultSet2 = statement2.executeQuery(sql2);
      print(resultSet2);
      statement2.close();
    }
    statement.close();
  }

  /**
  * Prints out the standard deviation of all the ratings in the database.
  *
  * @throws SQLException in the event of an SQL Error.
  */
  public void query6() throws SQLException {
    System.out.println("Standard Deviation of Rating");
    getView("[query6]");
  }


  /**
  * Prints out the standard deviation of all rating for each city.
  *
  * @throws SQLException in the event of an SQL Error.
  */
  public void query7() throws SQLException {
    System.out.println("City, Standard Deviation of Rating");
    getView("[query7]");
  }

/**
* Executes a query and prints its result set.
*
* @param sql the query being executed
* @throws SQLException in the event of an SQL error
*/
  private void executeQuery(String sql) throws SQLException {

    Statement statement = this.connection.createStatement();
    ResultSet resultSet = statement.executeQuery(sql);
    print(resultSet);
    statement.close();
  }

  /**
  * prints a result set with columns seperate by commas.
  *
  * @param resultSet the result set being printed
  * @throws SQLException in event of SQL Error
  */
  private void print(ResultSet resultSet) throws SQLException {

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
  * closes the connection to the database.
  *
  * @throws SQLException in the event of an SQL Error
  */
  public void close() throws SQLException {

    if (connection != null) {

      connection.close();
    }
  }
}
