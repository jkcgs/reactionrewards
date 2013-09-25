package ro.raizen.reactionrewards;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiConnection {
	private String path;
	private Connection conn;
	private Statement query;

	public SQLiConnection(String path) {
		this.path = path;
	}

	/**
	 * Realiza la conexión a la base de datos
	 * @return Un valor booleano dependiendo si se ha realizado o no la conexión
	 */
	public boolean Connect() {
		try {
			Close();
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + path);
			query = conn.createStatement();
			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Realiza una actualiación en la DB
	 * @param sql La consulta SQL
	 * @return Un booleano dependiendo si se pudo realizar consulta
	 */
	public boolean Update(String sql) {
		if (Connect()) {
			try {
				query.executeUpdate(sql);
				Close();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Realiza una consulta en la DB, devolviendo el resultado
	 * @param sql La consulta SQL
	 * @return Un ResultSet con los resultados de la consulta, null si no se pudo realizar
	 */
	public ResultSet Query(String sql) {
		if (Connect()) {
			try {
				ResultSet r = query.executeQuery(sql);
				return r;
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Revisa si una tabla existe
	 * @param table La tabla a comprobar
	 * @return Un booleano dependiendo de la existencia de la tabla
	 */
	public boolean TableExists(String table) {
		try {
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getTables(null, null, table, null);
			return rs.next();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Cierra la conexión a la DB
	 * @return Un valor booleano dependiendo de si se pudo cerrar o no la conexión
	 */
	public boolean Close() {
		try {
			query.close();
			query = null;
			conn.close();
			conn = null;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
