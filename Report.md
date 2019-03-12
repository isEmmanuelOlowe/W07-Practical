

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

Is used with aggregate functions and groups the result-set by one or more columns. This was implemented in the initial practical as it was the most effective way to implement solution giving the results already grouped meaning less code needed to be written in java or 2 queries in SQL to find solution.

### VIEWS

Is a virtual table which is based on result set created from a SQL query. This could allow for the implementation as queries we want the database as databases and allow for simple

```sql
SELECT * FROM view
```

queries to get the desired result where `view` is the table which represents the desired result set.

### STDEV

This is a statistical aggregate function which find the deviation in a column. Here additional functionally will be added to the program to 

### Extensions Implemented

* Use of GROUP BY
* Use of VIEWS
* Additional Statistical functionality (e.g. standard deviation of ratings in a city)

## Design

![](UML\Database.png)

The interactions of the program with the SQLite database has been abstracted into a class. Then the actions which the program is required to fulfilled were made into a methods of this class. It was found that each of the queries that were required to be ran by the program could be written almost entirely in SQL and then formatted in java, so this supported the implementation of them as methods as the amount of code would be minimal as they would be . This allows for the main methods to be clean and show the general functionality that is being carried out given the input of the program. 

It was decided to make the connection a private variable of the Database class. This design decision was chosen to allow for the use of the same connection to the database used across any of the `query*` methods without having to create a connection to the database in every single method. So instead the connection is opened in the constructor of the Database class and closed with the `close` method. 

## Testing

### `stacscheck`

Example test for the functionality of the program.

## Evaluation

The program ass that 

## Conclusion



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



