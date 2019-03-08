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

  private String databaseURL;
  private Connection connection = null;
  private DecimalFormat decimalFormat;
  /**
  * Adds the name of the database to
  *
  * @param dbFile the location of database file
  */
  public Database(String dbFile) {
    String pattern = "##0.#";
    this.decimalFormat = new DecimalFormat(pattern);
    try {
      this.databaseURL = "jdbc:sqlite:" + dbFile;
      this.connection = DriverManager.getConnection(this.databaseURL);
    }
    catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }
  /**
  * Creates new tables for the sql file using the provided csv file
  *
  * @param fileName the name of the file being used to created database.
  */
  public void create(String fileName) {
    try {
      String sql = "DROP TABLE IF EXISTS restaurant";
      String sql2 = "CREATE TABLE restaurant (name varchar(255), city varchar(255),"
      + " cuisine varchar(255), ranking INT, rating REAL,"
      + " priceRange varchar(255), reviewNo varchar(255), reviews varchar(255));";
      Statement statement = this.connection.createStatement();
      statement.executeUpdate(sql);
      statement.close();
      Statement statement2 = this.connection.createStatement();
      statement2.executeUpdate(sql2);
      statement2.close();
      // Add code to append on restaurants and cuisine styles
      BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
      //for ingnoring first line which is not a data entry
      String line = bufferedReader.readLine();
      this.connection.setAutoCommit(false);
      while ((line = bufferedReader.readLine()) != null){
        this.addRow(line);
      }
      bufferedReader.close();
      this.connection.commit();
      //closes statement
      System.out.println("OK");
    }
    catch (SQLException e){
      System.out.println(e.getMessage());
    }
    catch (FileNotFoundException e){
      System.out.println(e.getMessage());
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
  * Adds a row of data to the database
  *
  * @param line the unformatted line of data
  */
  private void addRow(String sLine){
    try {

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
    catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
  * Lists the city, name, rating and cuisine style for the best rated resutaurants
  in Amsterdam and Edinbrugh (with rating 5) which serve European Cuisine.
  */
  public void query1() {
    try {

      String sql = "SELECT city, name, cuisine, rating "
      + "FROM restaurant WHERE (city = 'Edinburgh' or city = 'Amsterdam') and cuisine "
      + "LIKE '%European%'and rating = 5";
      PreparedStatement statement = this.connection.prepareStatement(sql);
      ResultSet resultSet = statement.executeQuery();
      System.out.println("city, name, rating, cuisine_style");
      while (resultSet.next()) {
        String city = resultSet.getString("city");
        String name = resultSet.getString("name");
        Double rating = resultSet.getDouble("rating");
        String cuisine = resultSet.getString("cuisine");
        System.out.println(city + ", " + name + ", " + this.decimalFormat.format(rating)
        + ", " + cuisine);
      }
      statement.close();
    }
    catch (SQLException e) {
      System.out.println("Oh");
      System.out.println(e.getMessage());
    }
  }

  /**
  * prints out the total number of restaurants with a rating greater or equal to
  the provided minimum rating.
  *
  * @param minimumRating the search boundary for which restaurants which meet shall be selected
  */
  public void query2(String minimumRating) {
    try {

      PreparedStatement statement = connection.prepareStatement("SELECT COUNT(name) AS counter FROM restaurant WHERE rating >= ?");
      //Now insert for gettings rating
      statement.setDouble(1, Double.parseDouble(minimumRating));
      ResultSet resultSet = statement.executeQuery();
      System.out.println("Total number of restaurants with rating above (or equal to) " +
      minimumRating);
      System.out.println(resultSet.getInt("counter"));
      statement.close();
    }
    catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
  * prints out a table containing for each city: the city and number of restaurants
  with a rating greater or equal to the provided minimum rating.
  *
  * @param minimumRating the search boundary for which resturatns which meet shall be selected
  */
  public void query3(String minimumRating) {
    try {

      PreparedStatement statement = connection.prepareStatement("SELECT city, COUNT(rating) AS counter FROM restaurant WHERE rating >= ? GROUP BY city");
      statement.setDouble(1, Double.parseDouble(minimumRating));
      ResultSet resultSet = statement.executeQuery();
      System.out.println("city, number of restaurants with rating above (or equal to) "
      + minimumRating);

      while (resultSet.next()) {
        String city = resultSet.getString("city");
        int count = resultSet.getInt("counter");
        System.out.println(city + ", " + count);
      }
      statement.close();
    }
    catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
  * prints out a table, containing for each city, the city and average rating of
  restaurants in that city
  */
  public void query4() {
    try {

      PreparedStatement statement = connection.prepareStatement("SELECT city, AVG(rating) AS average FROM restaurant GROUP BY city");
      ResultSet resultSet = statement.executeQuery();
      System.out.println("city, average rating");
      while (resultSet.next()) {

        String city = resultSet.getString("city");
        Double average = resultSet.getDouble("average");

        System.out.println(city + ", " + this.decimalFormat.format(average));
      }
      statement.close();
    }
    catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
  * For each city prints out the resaturant with the lowest rating: the city name,
  the restaurant name and the rating.
  */
  public void query5() {
    try {
;
      //TOO SLOW

      // String sql = "SELECT city as currentCity, name,"
      // + "rating FROM restaurant WHERE rating  = (SELECT min(rating) FROM restaurant" +
      // " WHERE city=currentCity)";
      String sql = "SELECT city, min(rating) as minimum FROM restaurant GROUP BY city";
      Statement statement = this.connection.createStatement();
      ResultSet resultSet = statement.executeQuery(sql);
      System.out.println("city, name, rating");
      while (resultSet.next()){
        Statement statement2 = this.connection.createStatement();
        String sql2 = "SELECT city, name, rating FROM restaurant WHERE rating = "
        + resultSet.getDouble("minimum") + " and city = '" + resultSet.getString("city") + "'";
        ResultSet resultSet2 = statement2.executeQuery(sql2);
        while (resultSet2.next()) {
          String city = resultSet2.getString("city");
          String name = resultSet2.getString("name");
          Double rating = resultSet2.getDouble("rating");
          System.out.println(city + ", " + name + ", " + this.decimalFormat.format(rating));
        }
        statement2.close();
      }
      statement.close();
    }
    catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
  * closes the connection to the database
  */
  public void close() {
    try {
      if (connection != null) {
        connection.close();
      }
    }
    catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }
}
