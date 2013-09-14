package ro.raizen.reactionrewards;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
    public ReactionRewards plugin;
    private Connection con;
    private PreparedStatement sql;

    public Database(ReactionRewards plugin) {
        this.plugin = plugin;
        this.Connect();
        this.CheckTables();
    }

    public void Connect() {
    	try {
    		Class.forName("org.sqlite.JDBC");
			con = DriverManager.getConnection("jdbc:sqlite:"+ plugin.getDataFolder().getAbsolutePath() + "/leaderboard.db");
		} catch (Exception e) {
            plugin.getLogger().info(e.getMessage());
            plugin.getPluginLoader().disablePlugin(plugin);
		}
    }

    private void CheckTables() {
        try {
			if(!Query("SELECT name FROM sqlite_master WHERE type='table' AND name='leaderboard';").next()) {
				Query("CREATE TABLE leaderboard (id INTEGER PRIMARY KEY AUTOINCREMENT, playername VARCHAR(50), wins INT DEFAULT '0');");
				plugin.getLogger().info("Table 'leaderboard' has been created.");
			}
        } catch (Exception e) {}
    }

    public int getWins(String player) {
        try {
            return Query("SELECT wins FROM leaderboard WHERE playername = '" + player + "';").getInt("wins");
        } catch (Exception e) {
            return 0;
        }
    }

    public ResultSet getTop(int limit) {
        try {
            return Query("SELECT * FROM leaderboard ORDER BY wins DESC LIMIT " + limit + ";");
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isEmpty() {
        try {
            return (Query("SELECT COUNT(*) AS cnt FROM leaderboard;").getInt("cnt") > 0) ? false: true;
        } catch (Exception e) {
            return true;
        }
    }

    public boolean updatePlayer(String player) {
        try {
            if (isSet(player)) {
                int wins = Query("SELECT wins FROM leaderboard WHERE playername = '" + player + "';").getInt("wins") + 1;
    			Query("UPDATE leaderboard SET wins = '" + wins + "' WHERE playername = '" + player + "';");
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isSet(String player) {
        try {
            return (Query("SELECT COUNT(*) as CNT FROM leaderboard WHERE playername = '" + player + "';").getInt("cnt") > 0);
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean insertPlayer(String player) {
        try {
			Query("INSERT INTO leaderboard(playername, wins) VALUES ('" + player + "', '1');");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public ResultSet Query(String sql) {
    	PreparedStatement stmt;
		try {
			stmt = con.prepareStatement(sql);
			return stmt.executeQuery();
		} catch (SQLException e) {
			plugin.getLogger().info(e.getMessage());
			return null;
		}
    }

    public void Close() {
        try {
			sql.close();
		} catch (Exception e) {}
    }
}