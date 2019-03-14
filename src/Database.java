import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
/**
* Allows the user to interact with the database.
*/
public class Database extends DataOperator {

  /**
  * Adds the name of the database to.
  *
  * @param dbFile the location of database file.
  * @throws SQLException inevent of SQL Error.
  */
  public Database(String dbFile) throws SQLException {
    super(dbFile);
  }

  /**
  * Creates new tables for the sql file using the provided csv file.
  *
  * @param fileName the name of the file being used to created database entries.
  * @throws SQLException in event of SQL Error.
  */
  public void create(String fileName) throws SQLException {

    //checks the file is a csv file
    if (validFile(fileName, "csv")) {

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

        //to increase speed at which data is added to database
        this.connection.setAutoCommit(false);
        //checks if the file is in format of expected dataset
        if (validDataset(line)) {

        while ((line = bufferedReader.readLine()) != null) {
          //inputs the row into the database
          this.addRow(line);
        }
        System.out.println("OK");
        createViews();
        }
        else {
          System.out.println("This is not the expected dataset as heading is:");
          System.out.println(line);
        }
        bufferedReader.close();
        this.connection.commit();
        //closes statement
      }
      catch (FileNotFoundException e) {
        System.out.println("Selected File does not exist:");
        System.out.println(fileName);
      }
      catch (IOException e) {
        System.out.println(e.getMessage());
      }
    }
    else {
      System.out.println("Not a CSV file:" + fileName);
    }
  }

  /**
  * Lists the city, name, rating and cuisine style for the best rated resutaurants
  in Amsterdam and Edinbrugh (with rating 5) which serve European Cuisine.
  *
  * @throws SQLException in the event of a SQL Error.
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
  * @throws SQLException in the event of a SQL Error
  */
  public void query2(String minimumRating) throws SQLException {

    //checks the minimum rating is valid
    if (verifyNumber(minimumRating)) {

      PreparedStatement statement = connection.prepareStatement("SELECT COUNT(name) AS counter FROM restaurant WHERE rating >= ?");
      //Now insert for gettings rating
      statement.setDouble(1, Double.parseDouble(minimumRating));
      ResultSet resultSet = statement.executeQuery();
      System.out.println("Total number of restaurants with rating above (or equal to) "
      + minimumRating);
      System.out.println(resultSet.getInt("counter"));
      statement.close();
    }

  }

  /**
  * prints out a table containing for each city: the city and number of restaurants
  with a rating greater or equal to the provided minimum rating.
  *
  * @param minimumRating the search boundary for which resturatns which meet shall be selected
  * @throws SQLException in the event of a SQL Error
  */
  public void query3(String minimumRating) throws SQLException {

    //checks the minimum rating is valid
    if (verifyNumber(minimumRating)) {

      PreparedStatement statement = connection.prepareStatement("SELECT city, COUNT(rating) AS counter FROM restaurant WHERE rating >= ? GROUP BY city");
      statement.setDouble(1, Double.parseDouble(minimumRating));
      ResultSet resultSet = statement.executeQuery();
      System.out.println("city, number of restaurants with rating above (or equal to) "
      + minimumRating);

      print(resultSet);
      statement.close();
    }
  }

  /**
  * prints out a table, containing for each city, the city and average rating of
  restaurants in that city.
  *
  * @throws SQLException in the event of a SQL Error.
  */
  public void query4() throws SQLException {

    System.out.println("city, average rating");
    getView("[query4]");
  }

  /**
  * For each city prints out the restaurant with the lowest rating: the city name,
  the restaurant name and the rating.
  *
  * @throws SQLException in the event of a SQL Error.
  */
  public void query5() throws SQLException {

    System.out.println("city, name, rating");
    //gets the minimum rating for each city
    String sql = "SELECT city, min(rating) as minimum FROM restaurant GROUP BY city";
    Statement statement = this.connection.createStatement();
    ResultSet resultSet = statement.executeQuery(sql);
    while (resultSet.next()) {
      //finds all the restaurants with the lowest rating in their city
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
  * @throws SQLException in the event of a SQL Error.
  */
  public void query6() throws SQLException {
    System.out.println("Standard Deviation of Rating");
    getView("[query6]");
  }

  /**
  * Prints out the standard deviation of all rating for each city.
  *
  * @throws SQLException in the event of a SQL Error.
  */
  public void query7() throws SQLException {
    System.out.println("City, Standard Deviation of Rating");
    getView("[query7]");
  }

  /**
  * closes the connection to the database.
  *
  * @throws SQLException in the event of a SQL Error
  */
  public void close() throws SQLException {

    if (connection != null) {

      connection.close();
    }
  }
}
