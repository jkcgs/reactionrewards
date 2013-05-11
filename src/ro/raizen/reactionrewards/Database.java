package ro.raizen.reactionrewards;

import lib.PatPeter.SQLibrary.SQLite;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
    public ReactionRewards plugin;
    private SQLite         sql;

    public Database(ReactionRewards plugin) {
        this.plugin = plugin;
        this.Connect();
        this.CheckTables();
    }

    public int getWins(String player) {
        try {
            ResultSet result = sql.query("SELECT wins FROM leaderboard WHERE playername = '" + player + "';");

            return result.getInt("wins");
        } catch (SQLException e) {
            plugin.log.info(String.format("[%s] %s", plugin.getDescription().getName(), e.getMessage()));

            return 0;
        }
    }

    public ResultSet getTop(int limit) {
        try {
            ResultSet result = sql.query("SELECT * FROM leaderboard ORDER BY wins DESC LIMIT " + limit + ";");

            return result;
        } catch (SQLException e) {
            plugin.log.info(String.format("[%s] %s", plugin.getDescription().getName(), e.getMessage()));

            return null;
        }
    }

    public boolean isEmpty() {
        try {
            ResultSet result = sql.query("SELECT COUNT(*) AS cnt FROM leaderboard;");

            if (result.getInt("cnt") > 0) {
                return false;
            }

            return true;
        } catch (SQLException e) {
            plugin.log.info(String.format("[%s] %s", plugin.getDescription().getName(), e.getMessage()));

            return true;
        }
    }

    public boolean updatePlayer(String player) {
        try {
            if (isSet(player)) {
                ResultSet result = sql.query("SELECT wins FROM leaderboard WHERE playername = '" + player + "';");
                int       wins   = result.getInt("wins") + 1;

                sql.query("UPDATE leaderboard SET wins = '" + wins + "' WHERE playername = '" + player + "';");

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
            ResultSet result = sql.query("SELECT COUNT(*) as CNT FROM leaderboard WHERE playername = '" + player
                                         + "';");

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
            sql.query("INSERT INTO leaderboard(playername, wins) VALUES ('" + player + "', '1');");

            return true;
        } catch (SQLException e) {
            plugin.log.info(String.format("[%s] %s", plugin.getDescription().getName(), e.getMessage()));

            return false;
        }
    }

    private void CheckTables() {
        if (sql.isTable("leaderboard")) {
            return;
        } else {
            try {
                sql.query("CREATE TABLE leaderboard (id INTEGER PRIMARY KEY AUTOINCREMENT, playername VARCHAR(50), wins INT DEFAULT '0');");
                plugin.log.info(String.format("[%s] Table 'leaderboard' has been created.",
                                              plugin.getDescription().getName()));
            } catch (SQLException e) {
                plugin.log.info(String.format("[%s] %s", plugin.getDescription().getName(), e.getMessage()));
            }
        }
    }

    public void Connect() {
        sql = new SQLite(plugin.log, "ReactionRewards", plugin.getDataFolder().getAbsolutePath(), "leaderboard");

        try {
            sql.open();
        } catch (Exception e) {
            plugin.log.info(String.format("[%s] %s", plugin.getDescription().getName(), e.getMessage()));
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    public void Close() {
        sql.close();
    }
}