import java.sql.*;
import java.io.BufferedReader;
import java.io.FileReader;

/**
* Allows the user to interact with the database
*/
public class Database {

  private String databaseURL;
  private Connection connection = null;

  /**
  * Adds the name of the database to
  *
  * @param dbFile the location of database file
  */
  public Database(String dbFile) {
    try {
      this.databaseURL = "jdbc:sqlite:" + dbFile;
      this.connection = DriverManager.getConnection(this.databaseURL);
    }
    catch (SQLEception e) {
      System.out.println(e.getMessage);
    }
  }
  /**
  * Creates new tables for the sql file using the provided csv file
  *
  * @param fileName the name of the file being used to created database.
  */
  public void create(String fileName) {
    String sql = "DROP TABLE IF EXISTS restaurant;"
    + "CREATE TABLE restaurant (name varchar(255), city varchar(255),"
    + " cuisine varchar(255), ranking INT, rating REAL,"
    + " priceRange varchar(255), reviewNo varchar(255), reviews varchar(255));";
    Statement statement = this.connection.createStatement();
    statement.executeUpdate("DROP TABLE IF EXISTS restaurant;");
    // Add code to append on restaurants and cuisine styles
    BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
    String line;
    while ((line = bufferedReader.readLine()) != null){
      this.addRow(line);
    }
    bufferedReader.close();
    //closes statement
    statement.close();
  }

  /**
  * Adds a row of data to the database
  *
  * @param line the unformatted line of data
  */
  private void addRow(String sLine){
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
    statement.setString(3, line[styleIndex].replaceALL("[/W]", ""));
    statement.setInt(4, (int)Double.parseDouble(line[rankingIndex]));
    statement.setDouble(5, Double.parseDouble(line[ratingIndex]));
    statement.setString(6, line[priceRangeIndex]);
    statement.setString(7, line[reviewNoIndex]);
    statement.setString(8, line[reviewsIndex]);
    statement.executeUpdate();
    statement.close();

  }

  /**
  * Lists the city, name, rating and cuisine style for the best rated resutaurants
  in Amsterdam and Edinbrugh (with rating 5) which serve European Cuisine.
  */
  public void query1() {
    String sql = "SELECT city, name, cuisine MAX(rating) "
    + "FROM restaurant WHERE name ='Edinbrugh' or name = 'Amsterdam' GROUP BY city";
    PreparedStatement statement = this.connection.prepareStatement(sql);
    statment.executeUpdate();
    statement.close();
  }

  /**
  * prints out the total number of restaurants with a rating greater or equal to
  the provided minimum rating.
  *
  * @param minimumRating the search boundary for which restaurants which meet shall be selected
  */
  public void query2(String minimumRating) {
    PreparedStatement statement = connection.prepareStatement("SELECT COUNT(name) FROM restaurant WHERE rating >= ?;");
    //Now insert for gettings rating
    statement.setDouble(1, Double.parseDouble(minimumRating));
    statement.executeUpdate();
    statement.close();
  }

  /**
  * prints out a table containing for each city: the city and number of restaurants
  with a rating greater or equal to the provided minimum rating.
  *
  * @param minimumRating the search boundary for which resturatns which meet shall be selected
  */
  public void query3(String minimumRating) {
    PreparedStatement statement = connection.prepareStatement("SELECT city, COUNT(rating) FROM restaurant WHERE rating >= ? GROUP BY city");
    statment.setDouble(1, Double.parseDouble(minimumRating));
    statement.executeUpdate();
    statement.close();
  }

  /**
  * prints out a table, containing for each city, the city and average rating of
  restaurants in that city
  */
  public void query4() {
    PreparedStatement statement = connection.prepareStatement("SELECT city, AVERAGE(rating) FROM restaurant GROUP BY city");
    statement.executeUpdate();
    statement.close();
  }

  /**
  * For each city prints out the resaturant with the lowest rating: the city name,
  the restaurant name and the rating.
  */
  public void query5() {
    PreparedStatement statement = connection.prepareStatement("SELECT city, name MIN(rating) FROM restaurant GROUP BY city");
    statement.executeUpdate();
    statement.close();
  }

  /**
  * closes the connection to the database
  */
  public void close() {

    if (connection != null) {
      connection.close();
    }
  }
}
