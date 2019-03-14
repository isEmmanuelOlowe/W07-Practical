# W07 Practical Report

## Overview

The practical requires that the program takes in 2-3 inputs and have the usage information:

```bash
Usage: java -cp sqlite-jdbc.jar:. W07Practical <db_file> <action> [input_file | minimum_rating]
```

The program shall allow 6 possible actions:

1. `create`  will create the SQLite database if it does not exist with the table restaurant. It will taken in the `input_file` which will be the database that shall be converted to the database.
2. `query1` will run a query and that prints out the "city, name, rating and cuisine_style" where the rating is equal to 5, the city the restaurant is in is either "Amsterdam" or "Edinburgh" and if the restaurant has the cuisine_style of "European" and then it will print out the output to the user.
3. `query2` takes in the `minimum_rating` argument and runs a query which prints out the number of restaurants with a rating greater than or equal to the minimum rating given by argument. Then returns output to user
4. `query3` takes in the `minimum_rating` argument and runs a query which prints out the city and number of restaurants with a rating greater than or equal to the minimum rating given by argument in that city. Then returns output to user.
5. `query4` runs a query which returns the average rating for restaurants for each city. Then outputs this information to the user.
6. `query5` for each city, prints out the "city, name, rating" for restaurants which have the lowest rating in that given city. It assumed that there can be multiple restaurants with the same minimum rating.

### Problem Decomposition

* verify input is correct
* create SQLite database
* add data from `.csv` to to rows in that database.
* convert the different `query*` actions to SQL queries.
* Output the result of the action.

### GROUP BY

Is used with aggregate functions and groups the result-set by one or more columns. This was implemented in the initial practical as it was the most effective way to implement solution giving the results already grouped meaning less code needed to be written in java (e.g. using java to do grouping of minimums) or 2 queries (or sub queries) in SQL to find solution.

### VIEWS

Is a virtual table which is based on result set created from a SQL query. This could allow for the implementation as queries we want the database as databases and allow for simple

```sql
SELECT * FROM view
```

queries to get the desired result where `view` is the table which represents the desired result set. This simplifies the query command and allows others who are using the program to still get the results for certain queries without having to run them in the program. Also it means when a user enters for a specific query to be obtained that only a command which gets the query name from the database needs to be ran. e.g.

```sql
SELECT * FROM query1
```

### STDEV

This is a statistical aggregate function which find the deviation from mean in a column. Here additional functionally will be added to the program to find the standard deviation of rating for the total dataset find the standard deviation of rating for each city.  This was added to the initial program as the `stacscheck` require verification of whether for queries which did not exist were attempted to be ran. So it was assumed additional queries implemented would be acceptable.

### Extensions Implemented

* Use of GROUP BY
* Use of VIEWS
* Additional Statistical functionality (e.g. standard deviation of ratings in a city)

## Design

![](UML\Database.png)



​                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                The interactions of the program with the SQLite database has been abstracted into a class. Then the actions which the program is required to fulfilled were made into a methods of this class. It was found that each of the queries that were required to be ran by the program could be written almost entirely in SQL and then formatted in java, so this supported the implementation of them as methods as the amount of code would be minimal as they would be . This allows for the main methods to be clean and show the general functionality that is being carried out given the input of the program. 

It was decided to make the connection a private variable of the Database class. This design decision was chosen to allow for the use of the same connection to the database used across any of the `query*` methods without having to create a connection to the database in every single method. So instead the connection is opened in the constructor of the Database class and closed with the `close` method. 

It was decided to implement: `query1`, `query4`, `query6`, `query7` as views in the SQL database. this allowed for the generalisation of behaviours in to functions e.g. `getViews()`. These queries were the optimal to implement as views since they did not require input from the user so it would be easy to make a view which represents the data they contain. A view was not made for `query5` as it was implemented as two as the query was too slow (discussed further in evaluation), instead it was implemented in two queries with additional java code.

Through the program when a string or concatenation  gets too long involving the concatenation of string, concatenation is taken onto a new line to maintain the readability.  e.g.

```java
    String query1 = "SELECT city, name, rating, cuisine "
    + "FROM restaurant WHERE (city = 'Edinburgh' or city = 'Amsterdam') and cuisine "
    + "LIKE '%European%'and rating = 5";
```

It was chosen every method and the constructor of the database class would throw `SQLException`. This was decided to minimise the try catches throughout the program as every method in the database class then would have to have a try and catch for `SQLException`. So to prevent this the exception be caught in the main method. 

Since all the queries have to print results to the terminal it was decided to generalise this behaviour into a method(`print`). This method prints all the results in a result set and separates the columns by `,`. This means that printing commands do not need to be separately created for each query.

## Testing

### `stacscheck`

Example test for the functionality of the program.

## Evaluation

The program ass that 

## Conclusion

In this practical a program which was capable of interacting with a SQL database was produced. It was capable of creating tables and inserting values into rows of the table and running queries and operation upon data in the table.

Original the SQL query for `query5`

```sql
SELECT city as currentCity, name, rating FROM restaurant WHERE rating = (SELECT MIN(rating) FROM restaurant WHERE city=currentCity)
```

was used but it was found when running this query on the large dataset the query was very slow. This query would also timeout on the `stacscheck`. So increase the speed at which the program executes the `query5` action it was separated into two queries and ran in java.

```sql
SELECT city, MIN(rating) as minimum FROM restaurant GROUP BY city
```

Then this result set was looped over for all the cities in the result set

```sql
SELECT city, name, rating FROM restaurant WHERE rating = currentMinimum and city = currentCity
```
