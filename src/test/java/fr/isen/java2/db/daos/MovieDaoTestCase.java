package fr.isen.java2.db.daos;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import fr.isen.java2.db.entities.Genre;
import fr.isen.java2.db.entities.Movie;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;

public class MovieDaoTestCase {

	String dbUrl = "jdbc:sqlite:sqlite.db";
	private MovieDao movieDao = new MovieDao();

	@Before
	public void initDb() throws Exception {
		Connection connection = DriverManager.getConnection(dbUrl);
		Statement stmt = connection.createStatement();
		stmt.executeUpdate(
				"CREATE TABLE IF NOT EXISTS genre (idgenre INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , name VARCHAR(50) NOT NULL);");
		stmt.executeUpdate(
				"CREATE TABLE IF NOT EXISTS movie (\r\n"
				+ "  idmovie INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\r\n" + "  title VARCHAR(100) NOT NULL,\r\n"
				+ "  release_date DATETIME NULL,\r\n" + "  genre_id INT NOT NULL,\r\n" + "  duration INT NULL,\r\n"
				+ "  director VARCHAR(100) NOT NULL,\r\n" + "  summary MEDIUMTEXT NULL,\r\n"
				+ "  CONSTRAINT genre_fk FOREIGN KEY (genre_id) REFERENCES genre (idgenre));");
		stmt.executeUpdate("DELETE FROM movie");
		stmt.executeUpdate("DELETE FROM genre");
		stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (1,'Drama')");
		stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (2,'Comedy')");
		stmt.executeUpdate("INSERT INTO movie(idmovie,title, release_date, genre_id, duration, director, summary) "
				+ "VALUES (1, 'Title 1', '2015-11-26 12:00:00.000', 1, 120, 'director 1', 'summary of the first movie')");
		stmt.executeUpdate("INSERT INTO movie(idmovie,title, release_date, genre_id, duration, director, summary) "
				+ "VALUES (2, 'My Title 2', '2015-11-14 12:00:00.000', 2, 114, 'director 2', 'summary of the second movie')");
		stmt.executeUpdate("INSERT INTO movie(idmovie,title, release_date, genre_id, duration, director, summary) "
				+ "VALUES (3, 'Third title', '2015-12-12 12:00:00.000', 2, 176, 'director 3', 'summary of the third movie')");
		stmt.close();
		connection.close();
	}
	
	 @Test
	 public void shouldListMovies() {
		 // When
		List<Movie> movies = movieDao.listMovies();
		assertThat(movies).hasSize(3);
		assertThat(movies).extracting("id", "title").containsOnly(tuple(1, "Title 1"), tuple(2, "My Title 2"), tuple(3, "Third title"));
	 }
	
	 @Test
	 public void shouldListMoviesByGenre() {
		 // When
		 List<Movie> movies = movieDao.listMoviesByGenre("Comedy");
		 assertThat(movies).hasSize(2);
		 List<String> expectedTitles = new ArrayList<>();
		 expectedTitles.add("My Title 2");
		 expectedTitles.add("Third title");
		 List<String> actualTitles = new ArrayList<>();
		 actualTitles.add(movies.get(0).getTitle());
		 actualTitles.add(movies.get(1).getTitle());
		 assertEquals(expectedTitles, actualTitles);
	 }
	
	 @Test
	 public void shouldAddMovie() throws Exception {
		// When
		Movie movie = new Movie();
		movie.setTitle("Gone With The Wind");
		movie.setReleaseDate(LocalDate.parse("1939-12-15 12:00:00.000", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
		movie.setDuration(221);
		movie.setGenre(new Genre(1, "Drama"));
		movie.setDirector("Victor Fleming");
		movie.setSummary("A 1939 American romantic adapted from the 1936 novel by Margaret Mitchel.");
		Movie addedMovie = movieDao.addMovie(movie);

		Connection connection = DriverManager.getConnection(dbUrl);
		String sqlQuery = "SELECT * FROM movie WHERE idmovie=?";
		PreparedStatement statement = connection.prepareStatement(sqlQuery);
		statement.setInt(1, addedMovie.getId());
		ResultSet result = statement.executeQuery();
		assertThat(result.next()).isTrue();
		assertThat(result.getInt("idmovie")).isNotNull();
		assertThat(result.getInt("idmovie")).isEqualTo(movie.getId());
		assertThat(result.getString("title")).isEqualTo("Gone With The Wind");
		assertThat(result.next()).isFalse();
		result.close();
		statement.close();
		connection.close();
	 }
}
