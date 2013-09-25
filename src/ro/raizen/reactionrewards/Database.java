package ro.raizen.reactionrewards;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
    private ReactionRewards plugin;
    private SQLiConnection db;

    public Database(ReactionRewards plugin) {
        this.plugin = plugin;
        this.Connect();
        this.CheckTables();
    }

    private void Connect() {
    	try {
            db = new SQLiConnection(plugin.getDataFolder().getAbsolutePath().replace("\\", "/") + "/leaderboard.db");
            db.Connect();
		} catch (Exception e) {
            plugin.getLogger().info(e.getMessage());
            plugin.getPluginLoader().disablePlugin(plugin);
		}
    }

    private void CheckTables() {
        try {
        	if(!db.TableExists("leaderboard")) {
				db.Update("CREATE TABLE leaderboard (id INTEGER PRIMARY KEY AUTOINCREMENT, playername VARCHAR(50), wins INT DEFAULT '0');");
				plugin.getLogger().info("Table 'leaderboard' has been created.");
			}
        } catch (Exception e) {}
    }

    public int getWins(String player) {
        try {
            return db.Query("SELECT wins FROM leaderboard WHERE playername = '" + player + "';").getInt("wins");
        } catch (Exception e) {
            return 0;
        }
    }

    public ResultSet getTop(int limit) {
        try {
            return db.Query("SELECT * FROM leaderboard ORDER BY wins DESC LIMIT " + limit + ";");
        } catch (Exception e) {
            return null;
        }
    }

    public boolean updatePlayer(String player) {
        try {
            if (isSet(player)) {
                int wins = db.Query("SELECT wins FROM leaderboard WHERE playername = '" + player + "';").getInt("wins") + 1;
    			db.Update("UPDATE leaderboard SET wins = '" + wins + "' WHERE playername = '" + player + "';");
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
            return db.Query("SELECT * FROM leaderboard WHERE playername = '" + player + "';").next();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean insertPlayer(String player) {
        try {
			db.Update("INSERT INTO leaderboard(playername, wins) VALUES ('" + player + "', '1');");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void Close() {
    	db.Close();
    }
}