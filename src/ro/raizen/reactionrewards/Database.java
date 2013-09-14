package ro.raizen.reactionrewards;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    public ReactionRewards plugin;
    private Connection con;
    private Statement sql;

    public Database(ReactionRewards plugin) {
        this.plugin = plugin;
        this.Connect();
        this.CheckTables();
    }

    public void Connect() {
    	try {
    		Class.forName("org.sqlite.JDBC");
			con = DriverManager.getConnection("jdbc:sqlite:leaderboard.db");
			sql = con.createStatement();
		} catch (Exception e) {
			// TODO: Traducible
            plugin.log.info(String.format("[%s] %s", plugin.getDescription().getName(), e.getMessage()));
            plugin.getPluginLoader().disablePlugin(plugin);
		}
    }

    public int getWins(String player) {
        try {
            sql.executeUpdate("SELECT wins FROM leaderboard WHERE playername = '" + player + "';");
            ResultSet result = sql.getResultSet();
            
            return result.getInt("wins");
        } catch (SQLException e) {
            plugin.log.info(String.format("[%s] %s", plugin.getDescription().getName(), e.getMessage()));

            return 0;
        }
    }

    public ResultSet getTop(int limit) {
        try {
            sql.executeUpdate("SELECT * FROM leaderboard ORDER BY wins DESC LIMIT " + limit + ";");
            ResultSet result = sql.getResultSet();
            
            return result;
        } catch (SQLException e) {
            plugin.log.info(String.format("[%s] %s", plugin.getDescription().getName(), e.getMessage()));

            return null;
        }
    }

    public boolean isEmpty() {
        try {
            sql.executeUpdate("SELECT COUNT(*) AS cnt FROM leaderboard;");
            ResultSet result = sql.getResultSet();
            return (result.getInt("cnt") > 0) ? false: true;
            
        } catch (SQLException e) {
            plugin.log.info(String.format("[%s] %s", plugin.getDescription().getName(), e.getMessage()));

            return true;
        }
    }

    public boolean updatePlayer(String player) {
        try {
            if (isSet(player)) {
                sql.executeUpdate("SELECT wins FROM leaderboard WHERE playername = '" + player + "';");
                ResultSet result = sql.getResultSet();
                int wins = result.getInt("wins") + 1;

                sql.executeUpdate("UPDATE leaderboard SET wins = '" + wins + "' WHERE playername = '" + player + "';");

                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            plugin.log.info(String.format("[%s] %s", plugin.getDescription().getName(), e.getMessage()));

            return false;
        }
    }

    public boolean isSet(String player) {
        try {
            sql.executeUpdate("SELECT COUNT(*) as CNT FROM leaderboard WHERE playername = '" + player + "';");
            ResultSet result = sql.getResultSet();
            
            if (result.getInt("cnt") > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            plugin.log.info(String.format("[%s] %s", plugin.getDescription().getName(), e.getMessage()));

            return false;
        }
    }

    public boolean insertPlayer(String player) {
        try {
            sql.executeUpdate("INSERT INTO leaderboard(playername, wins) VALUES ('" + player + "', '1');");

            return true;
        } catch (SQLException e) {
            plugin.log.info(String.format("[%s] %s", plugin.getDescription().getName(), e.getMessage()));

            return false;
        }
    }

    private void CheckTables() {
        try {
            sql.executeUpdate("CREATE TABLE IF NOT EXISTS leaderboard (id INTEGER PRIMARY KEY AUTOINCREMENT, playername VARCHAR(50), wins INT DEFAULT '0');");
            plugin.log.info(String.format("[%s] Table 'leaderboard' has been created.",
                                          plugin.getDescription().getName()));
        } catch (SQLException e) {
            plugin.log.info(String.format("[%s] %s", plugin.getDescription().getName(), e.getMessage()));
        }
    }

    public void Close() {
        try {
			sql.close();
		} catch (SQLException e) {}
    }
}