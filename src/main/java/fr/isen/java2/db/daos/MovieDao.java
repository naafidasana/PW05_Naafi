package fr.isen.java2.db.daos;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import fr.isen.java2.db.entities.Genre;
import fr.isen.java2.db.entities.Movie;

import javax.swing.plaf.nimbus.State;
import javax.xml.crypto.Data;

public class MovieDao {

	String dbUrl = "jdbc:sqlite:sqlite.db";

	GenreDao genreDao = new GenreDao();

	/**
	 * Retrieves a list of all movies from the database.
	 *
	 * @return a list of Movie objects representing the movies in the database
	 */
	public List<Movie> listMovies() {
		List<Movie> listOfMovies = new ArrayList<>();

		try (Connection connection = DriverManager.getConnection(dbUrl)) {
			try (Statement statement = connection.createStatement()) {
				String sqlQuery = "SELECT * FROM movie JOIN genre ON movie.genre_id = genre.idgenre";
				try (ResultSet results = statement.executeQuery(sqlQuery)) {
					while (results.next()) {
						// Get genre_id
						Integer genreId = results.getInt("genre_id");
						Genre genre = genreDao.getGenreById(genreId);

						// Create Movie Object
						Movie movie = new Movie(
								results.getInt("idmovie"),
								results.getString("title"),
								results.getDate("release_date").toLocalDate(),
								genre,
								results.getInt("duration"),
								results.getString("director"),
								results.getString("summary")
						);

						// Add movie object to list
						listOfMovies.add(movie);
					}

				}
			}
		} catch (SQLException e) {
			return null;
		}
		return listOfMovies;
	}

	/**
	 * Retrieves a list of movies from the database that belong to the specified genre.
	 *
	 * @param genreName the name of the genre to filter movies by
	 * @return a list of Movie objects representing the movies belonging to the specified genre
	 */
	public List<Movie> listMoviesByGenre(String genreName) {
		List<Movie> listOfMovies = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(dbUrl)) {
			String sqlQuery = "SELECT * FROM movie JOIN genre ON movie.genre_id = genre.idgenre WHERE genre.name=?";
			try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
				statement.setString(1, genreName);
				try (ResultSet results = statement.executeQuery()) {
					while (results.next()) {
						Integer genreId = results.getInt("genre_id");
						Genre genre = genreDao.getGenreById(genreId);

						Movie movie = new Movie(
								results.getInt("idmovie"),
								results.getString("title"),
								results.getDate("release_date").toLocalDate(),
								genre,
								results.getInt("duration"),
								results.getString("director"),
								results.getString("summary")
						);
						listOfMovies.add(movie);
					}
				}
			}
		} catch (SQLException e) {
			return null;
		}
		return listOfMovies;
	}

	/**
	 * Adds a new movie to the database.
	 *
	 * @param movie the Movie object representing the movie to add
	 * @return the Movie object representing the added movie, or null if the operation failed
	 */
	public Movie addMovie(Movie movie) {
		Movie newMovie = new Movie();
		try (Connection connection = DriverManager.getConnection(dbUrl)) {
			String sqlQuery = "INSERT INTO movie(title,release_date,genre_id,duration,director,summary) VALUES(?,?,?,?,?,?)";
			try (PreparedStatement statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
				// Fill in Values with relevant data from 'movie' (object) parameter.
				statement.setString(1, movie.getTitle());
				statement.setObject(2, movie.getReleaseDate());
				statement.setInt(3, movie.getGenre().getId());
				statement.setInt(4, movie.getDuration());
				statement.setString(5, movie.getDirector());
				statement.setString(6, movie.getSummary());

				statement.executeUpdate();

				try (ResultSet ids = statement.getGeneratedKeys()) {
					if (ids.next()) {
						int generatedId = ids.getInt(1);
						movie.setId(generatedId);	// Sets the generated key as the id of the movie object from given in the function parameters.
						newMovie.setId(generatedId);
						newMovie.setTitle(movie.getTitle());
						newMovie.setReleaseDate(movie.getReleaseDate());
						newMovie.setGenre(movie.getGenre());
						newMovie.setDirector(movie.getDirector());
						newMovie.setDuration(movie.getDuration());
						newMovie.setSummary(movie.getSummary());
					} else {
						throw new SQLException("Failed to Create Movie.");
					}
				}
			}
		} catch (SQLException e) {
			newMovie = null;
		}
		return newMovie;
	}
}
