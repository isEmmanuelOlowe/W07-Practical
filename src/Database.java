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
* Allows the user to interact with the database
*/
public class Database {

  //private String databaseURL;
  private Connection connection = null;
  private DecimalFormat decimalFormat;
  /**
  * Adds the name of the database to
  *
  * @param dbFile the location of database file
  */
  public Database(String dbFile) throws SQLException {
    String pattern = "##0.#";
    this.decimalFormat = new DecimalFormat(pattern);
    String databaseURL = "jdbc:sqlite:" + dbFile;
    this.connection = DriverManager.getConnection(databaseURL);
  }
  /**
  * Creates new tables for the sql file using the provided csv file
  *
  * @param fileName the name of the file being used to created database.
  */
  public void create(String fileName) throws SQLException {
    try {

      String sql = "DROP TABLE IF EXISTS restaurant";
      String sql2 = "CREATE TABLE restaurant (name varchar(255), city varchar(255),"
      + " cuisine varchar(255), ranking INT, rating REAL,"
      + " priceRange varchar(255), reviewNo varchar(255), reviews varchar(255));";
      executeUpdate(sql);
      executeUpdate(sql2);
      // Add code to append on restaurants and cuisine styles
      BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
      //for ingnoring first line which is not a data entry
      String line = bufferedReader.readLine();
      this.connection.setAutoCommit(false);
      while ((line = bufferedReader.readLine()) != null){
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

  private void executeUpdate(String sql) throws SQLException {
      Statement statement = this.connection.createStatement();
      statement.executeUpdate(sql);
      statement.close();
  }

  private void createView(String name, String query) throws SQLException {
    String sql = "CREATE VIEW IF NOT EXISTS "+ name +" AS " + query;
    executeUpdate(sql);
  }

  /**
  * Adds a row of data to the database
  *
  * @param line the unformatted line of data
  */
  private void addRow(String sLine) throws SQLException {

    //The various index locations of the labels in the data
    //final int numberIndex = 0;
    final int nameIndex = 1;
    final int cityIndex = 2;
    final int styleIndex = 3;
    final int rankingIndex = 4;
    final int ratingIndex = 5;
    final int priceRangeIndex = 6;
    final int reviewNoIndex = 7;
    final int reviewsIndex = 8;
    //final int urlTaIndex = 9;
    //final int idTaIndex = 10;

    String[] line = sLine.split(",");

    String sql = "INSERT INTO restaurant VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

    PreparedStatement statement = this.connection.prepareStatement(sql);
    statement.setString(1, line[nameIndex]);
    statement.setString(2, line[cityIndex]);
    statement.setString(3, line[styleIndex]);
    if (line[rankingIndex].isEmpty() || Double.parseDouble(line[rankingIndex]) < 0){
      statement.setNull(4, 0);
    }
    else{
      statement.setString(4, line[rankingIndex]);
    }

    if (line[ratingIndex].isEmpty() || Double.parseDouble(line[ratingIndex]) < 0){
      statement.setNull(5, 0);
    }
    else {
      statement.setString(5, line[ratingIndex]);
    }
    statement.setString(6, line[priceRangeIndex]);
    statement.setString(7, line[reviewNoIndex]);
    statement.setString(8, line[reviewsIndex]);
    statement.executeUpdate();
    statement.close();
  }

  private void createViews() throws SQLException {
    //A view for query1
    String query1 = "SELECT city, name, rating, cuisine "
    + "FROM restaurant WHERE (city = 'Edinburgh' or city = 'Amsterdam') and cuisine "
    + "LIKE '%European%'and rating = 5";
    createView("[query1]", query1);

    String query4 = "SELECT city, AVG(rating) AS average FROM restaurant GROUP BY city" ;
    createView("[query4]", query4);

    String query6 = "SELECT STDEV(rating) FROM restaurant";
    createView("[query6]", query6);

    String query7 = "SELECT city, STDEV(rating) FROM restaurant GROUP BY city";
    createView("[query7]", query7);

  }

  private void getView(String name) throws SQLException {
    String query = "SELECT * FROM " + name;
    executeQuery(query);
  }

  /**
  * Lists the city, name, rating and cuisine style for the best rated resutaurants
  in Amsterdam and Edinbrugh (with rating 5) which serve European Cuisine.
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
  */
  public void query2(String minimumRating) throws SQLException {

    PreparedStatement statement = connection.prepareStatement("SELECT COUNT(name) AS counter FROM restaurant WHERE rating >= ?");
    //Now insert for gettings rating
    statement.setDouble(1, Double.parseDouble(minimumRating));
    ResultSet resultSet = statement.executeQuery();
    System.out.println("Total number of restaurants with rating above (or equal to) " +
    minimumRating);
    System.out.println(resultSet.getInt("counter"));
    statement.close();
  }

  /**
  * prints out a table containing for each city: the city and number of restaurants
  with a rating greater or equal to the provided minimum rating.
  *
  * @param minimumRating the search boundary for which resturatns which meet shall be selected
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
  restaurants in that city
  */
  public void query4() throws SQLException {

    System.out.println("city, average rating");
    getView("[query4]");
  }

  /**
  * For each city prints out the restaurant with the lowest rating: the city name,
  the restaurant name and the rating.
  */
  public void query5() throws SQLException {

    System.out.println("city, name, rating")
    String sql = "SELECT city, min(rating) as minimum FROM restaurant GROUP BY city";
    Statement statement = this.connection.createStatement();
    ResultSet resultSet = statement.executeQuery(sql);
    while (resultSet.next()){
      Statement statement2 = this.connection.createStatement();
      String sql2 = "SELECT city, name, rating FROM restaurant WHERE rating = "
      + resultSet.getDouble("minimum") + " and city = '" + resultSet.getString("city") + "'";
      ResultSet resultSet2 = statement2.executeQuery(sql2);
      print(resultSet2);
      statement2.close();
    }
    statement.close();
  }

  public void query6() throws SQLException {
    System.out.println("Standard Deviation of Rating");
    getView("[query6]");
  }

  public void query7() throws SQLException {
    System.out.println("City, Standard Deviation of Rating");
    getView("[query7]");
  }

/**
* Executes a query and prints its result set
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
  * prints a result set with columns seperate by commas
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
  * closes the connection to the database
  *
  * @throws SQLException in the event of an SQL Error
  */
  public void close() throws SQLException {

    if (connection != null) {

      connection.close();
    }
  }
}
