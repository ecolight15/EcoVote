
package jp.minecraftuser.ecovote.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.DatabaseFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.db.CTYPE;
import jp.minecraftuser.ecovote.gist.BukkitSerialization;
import org.bukkit.inventory.ItemStack;


/**
 * プレイヤー固有ファイル保存
 * @author ecolight
 */
public class VoteStore extends DatabaseFrame {

    public VoteStore(PluginFrame plg_, String dbfilepath_, String name_) throws ClassNotFoundException, SQLException {
        super(plg_, dbfilepath_, name_);
    }

    public VoteStore(PluginFrame plg_, String server_, String user_, String pass_, String dbname_, String name_) throws ClassNotFoundException, SQLException {
        super(plg_, server_, user_, pass_, dbname_, name_);
    }

    /**
     * データベース移行処理
     * 基底クラスからDBをオープンするインスタンスの生成時に呼ばれる
     * 
     * @throws SQLException
     */
    @Override
    protected void migrationData(Connection con) throws SQLException  {
        // 全体的にテーブル操作になるため、暗黙的コミットが走り失敗してもロールバックが効かない
        // 十分なテストの後にリリースするか、何らかの形で異常検知し、DBバージョンに従い元に戻せるようテーブル操作順を考慮する必要がある
        // 本処理においては取り敢えずロールバックは諦める
        
        // version 1 の場合、新規作成もしくは旧バージョンのデータベース引き継ぎの場合を検討する
        if (dbversion == 1) {
            if (justCreated) {
                // 新規作成の場合、初版のテーブルのみ作成して終わり
                MessageFormat mf = new MessageFormat("CREATE TABLE IF NOT EXISTS totalvote(most {0} NOT NULL, least {1} NOT NULL, vote {2} NOT NULL, lastdate {3} NOT NULL, PRIMARY KEY(most, least))");
                try {
                    executeStatement(con, mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[totalvote].");
                    Logger.getLogger(VoteStore.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "Create table[totalvote].");

                mf = new MessageFormat("CREATE TABLE IF NOT EXISTS ranktotalvote(most {0} NOT NULL, least {1} NOT NULL, rank {2} NOT NULL, vote {3} NOT NULL, lastdate {4} NOT NULL, PRIMARY KEY(most, least))");
                try {
                    executeStatement(con, mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[ranktotalvote].");
                    Logger.getLogger(VoteStore.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "Create table[ranktotalvote].");

//                mf = new MessageFormat("CREATE TABLE IF NOT EXISTS weeklyvote(most {0} NOT NULL, least {1} NOT NULL, date {2} NOT NULL)");
//                executeStatement(mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
//                log.log(Level.INFO, "Create table[weeklyvote].", name);

                mf = new MessageFormat("CREATE TABLE IF NOT EXISTS itemqueue(most {0} NOT NULL, least {1} NOT NULL, date {2} NOT NULL, items {3} NOT NULL)");
                try {
                    executeStatement(con, mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[itemqueue].");
                    Logger.getLogger(VoteStore.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "Create table[itemqueue].");

                log.log(Level.INFO, "{0}DataBase checked.", name);
                try {
                    updateSettingsVersion(con);
                } catch (Exception e) {
                    log.log(Level.INFO, "Error updateSettingsVersion.");
                    Logger.getLogger(VoteStore.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "create {0} version {1}", new Object[]{name, dbversion});
            } else {
                // 既存DB引き継ぎの場合はdbversionだけ上げてv2->3の処理へ
                log.log(Level.INFO, "convert {0} version 1 -> 2 start", name);
                try {
                    updateSettingsVersion(con);
                } catch (Exception e) {
                    log.log(Level.INFO, "Error updateSettingsVersion 1 -> 2.");
                    Logger.getLogger(VoteStore.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "convert {0} version 1 -> 2 complete", name);
            }
        }
        // Version 2 -> 3
//        if (dbversion == 2) {
//            log.log(Level.INFO, "convert {0} version {1} -> {2} start", new Object[]{name, dbversion, dbversion + 1});
//            // ユーザー状態テーブル追加
//            MessageFormat mf = new MessageFormat("CREATE TABLE IF NOT EXISTS playerstats(most {0} NOT NULL, least {1} NOT NULL, logout {2} NOT NULL, PRIMARY KEY(most, least))");
//            executeStatement(mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
//            // ユーザー統計データデーブル追加
//            mf = new MessageFormat("CREATE TABLE IF NOT EXISTS statstable(most {0} NOT NULL, least {1} NOT NULL, name {2} NOT NULL, size {3} NOT NULL, data {4} NOT NULL, PRIMARY KEY(most, least))");
//            executeStatement(mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.BLOB.get(jdbc)}));
//            // ユーザー実績データテーブル追加
//            mf = new MessageFormat("CREATE TABLE IF NOT EXISTS advtable(most {0} NOT NULL, least {1} NOT NULL, name {2} NOT NULL, size {3} NOT NULL, data {4} NOT NULL, PRIMARY KEY(most, least))");
//            executeStatement(mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.BLOB.get(jdbc)}));
//            // 既存テーブルからlogoutを分離(playerstatsへ)、
//            renameTable("datatable", "datatable_");
//            mf = new MessageFormat("CREATE TABLE IF NOT EXISTS datatable(most {0} NOT NULL, least {1} NOT NULL, name {2} NOT NULL, size {3} NOT NULL, data {4} NOT NULL, PRIMARY KEY(most, least))");
//            executeStatement(mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.BLOB.get(jdbc)}));
//
//            PreparedStatement prep = con.prepareStatement("SELECT * FROM datatable_");
//            PreparedStatement prep2 = con.prepareStatement("REPLACE INTO datatable VALUES (?, ?, ?, ?, ?)");
//            PreparedStatement prep3 = con.prepareStatement("REPLACE INTO playerstats VALUES (?, ?, ?)");
//            try {
//                // 実行
//                ResultSet rs = prep.executeQuery();
//                try {
//                    // 結果取得
//                    while (rs.next()) {
//                        prep2.setLong(1, rs.getLong("most"));
//                        prep2.setLong(2, rs.getLong("least"));
//                        prep2.setString(3, rs.getString("name"));
//                        prep2.setLong(4, rs.getLong("size"));
//                        prep2.setBlob(5, rs.getBlob("data"));
//                        prep3.setLong(1, rs.getLong("most"));
//                        prep3.setLong(2, rs.getLong("least"));
//                        prep3.setLong(3, rs.getLong("logout"));
//                        prep2.executeUpdate();
//                        prep3.executeUpdate();
//                    }
//                } catch (SQLException ex) {
//                    // PreparedStatementがcloseできればカーソルリークしないはずだが、
//                    // 念のため確実にResultSetをcloseするようにしておく
//                    rs.close();
//                    // 投げなおして上位で異常検知させる
//                    throw ex;
//                }
//                // 後処理
//                rs.close();
//            } catch (SQLException ex) {
//                prep.close();
//                prep2.close();
//                prep3.close();
//                throw ex;
//                // ロールバックは上位のスーパークラスでやる
//            }
//            prep.close();
//            dropTable("datatable_");
//
//            updateSettingsVersion();
//            log.log(Level.INFO, "convert {0} version {1} -> {2} complete", new Object[]{name, dbversion - 1, dbversion});
//        }
    }

    /**
     * 総合Vote数保存
     * @param con
     * @param uuid プレイヤーUUID
     * @throws SQLException
     * @throws java.io.FileNotFoundException
     */
    public void updateTotalVote(Connection con, UUID uuid) throws SQLException, FileNotFoundException, IOException {
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM totalvote WHERE most = ? AND least = ?");
        PreparedStatement prep2 = con.prepareStatement("UPDATE totalvote SET vote = ?, lastdate = ? WHERE most = ? AND least = ?");
        PreparedStatement prep3 = con.prepareStatement("INSERT INTO totalvote VALUES(?, ?, ?, ?)");
        try {
            prep1.setLong(1, uuid.getMostSignificantBits());
            prep1.setLong(2, uuid.getLeastSignificantBits());
            ResultSet rs = prep1.executeQuery();
            long count = 0;
            boolean hit = false;
            try {
                // 結果取得
                if (rs.next()) {
                    count = rs.getLong("vote");
                    hit = true;
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();

            if (hit) {
                prep2.setLong(1, count + 1);
                prep2.setLong(2, Calendar.getInstance().getTime().getTime());
                prep2.setLong(3, uuid.getMostSignificantBits());
                prep2.setLong(4, uuid.getLeastSignificantBits());
                // 実行
                prep2.executeUpdate();
            } else { 
                prep3.setLong(1, uuid.getMostSignificantBits());
                prep3.setLong(2, uuid.getLeastSignificantBits());
                prep3.setLong(3, 1);
                prep3.setLong(4, Calendar.getInstance().getTime().getTime());
                // 実行
                prep3.executeUpdate();
            }

        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            prep2.close();
            prep3.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        prep2.close();
        prep3.close();
    }

    /**
     * 総合Vote数ランキング更新
     * @param con
     * @throws SQLException
     * @throws java.io.FileNotFoundException
     */
    public void updateRankTotalVote(Connection con) throws SQLException, FileNotFoundException, IOException {
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("DELETE FROM ranktotalvote");
        PreparedStatement prep2 = con.prepareStatement("SELECT * FROM totalvote ORDER BY vote DESC, lastdate ASC");
        PreparedStatement prep3 = con.prepareStatement("INSERT INTO ranktotalvote VALUES(?, ?, ?, ?, ?)");
        try {
            // delete RankTotalVote
            prep1.executeUpdate();

            // select TotalVote
            long rank = 1;
            ResultSet rs = prep2.executeQuery();
            try {
                // 結果取得
                while (rs.next()) {
                    prep3.setLong(1, rs.getLong("most"));
                    prep3.setLong(2, rs.getLong("least"));
                    prep3.setLong(3, rank);
                    prep3.setLong(4, rs.getLong("vote"));
                    prep3.setLong(5, rs.getLong("lastdate"));
                    // 実行
                    prep3.executeUpdate();
                    rank++;
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();

        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            prep2.close();
            prep3.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        prep2.close();
        prep3.close();
    }
    
    /**
     * ユーザ情報
     */
    public class UserStat {
        public UUID uuid;
        public long rank;
        public long vote;
        public long lastdate;
        public UserStat(UUID uuid_, long rank_, long vote_, long lastdate_) {
            uuid = uuid_;
            rank = rank_;
            vote = vote_;
            lastdate = lastdate_;
        }
    }

    /**
     * 総合Vote数TOP[n]取得 / 検索回数削減のため
     * @param con
     * @param limit
     * @return 
     * @throws SQLException
     * @throws java.io.FileNotFoundException
     */
    public ArrayList<UserStat> getRankTotalVote(Connection con, int limit) throws SQLException, FileNotFoundException, IOException {
        ArrayList<UserStat> list = new ArrayList<>();
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM ranktotalvote ORDER BY rank ASC");
        try {
            ResultSet rs = prep1.executeQuery();
            long count = 0;
            try {
                // 結果取得
                while (rs.next()) {
                    list.add(new UserStat(new UUID(rs.getLong("most"), rs.getLong("least")), rs.getLong("rank"), rs.getLong("vote"), rs.getLong("lastdate")));
                    count++;
                    if (count >= limit) break;
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        return list;
    }

    /**
     * 総合Vote数TOP[n]取得 / 検索回数削減のため
     * @param con
     * @param uuid
     * @return 
     * @throws SQLException
     * @throws java.io.FileNotFoundException
     */
    public UserStat getRankTotalVote(Connection con, UUID uuid) throws SQLException, FileNotFoundException, IOException {
        UserStat user = null;
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM ranktotalvote WHERE most = ? AND least = ?");
        try {
            prep1.setLong(1, uuid.getMostSignificantBits());
            prep1.setLong(2, uuid.getLeastSignificantBits());
            ResultSet rs = prep1.executeQuery();
            try {
                // 結果取得
                if (rs.next()) {
                    user = new UserStat(new UUID(rs.getLong("most"), rs.getLong("least")), rs.getLong("rank"), rs.getLong("vote"), rs.getLong("lastdate"));
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        return user;
    }

    /**
     * VoteItemStack保存
     * @param con
     * @param uuid プレイヤーUUID
     * @param item
     * @throws SQLException
     * @throws java.io.FileNotFoundException
     */
    public void insertVoteItem(Connection con, UUID uuid, ItemStack[] item) throws SQLException, FileNotFoundException, IOException {
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("INSERT INTO itemqueue VALUES(?, ?, ?, ?)");
        try {
            Calendar calendar = Calendar.getInstance();
            prep1.setLong(1, uuid.getMostSignificantBits());
            prep1.setLong(2, uuid.getLeastSignificantBits());
            prep1.setLong(3, calendar.getTime().getTime());
            prep1.setString(4, BukkitSerialization.itemStackArrayToBase64(item));
            // 実行
            prep1.executeUpdate();
        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
    }

    /**
     * Voteアイテム数取得
     * @param con
     * @param uuid
     * @return 
     * @throws SQLException
     * @throws java.io.FileNotFoundException
     */
    public int getVoteItemCount(Connection con, UUID uuid) throws SQLException, FileNotFoundException, IOException {
        int count = 0;
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT COUNT(*) FROM itemqueue WHERE most = ? AND least = ?");
        try {
            prep1.setLong(1, uuid.getMostSignificantBits());
            prep1.setLong(2, uuid.getLeastSignificantBits());
            ResultSet rs = prep1.executeQuery();
            try {
                // 結果取得
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        return count;
    }

    /**
     * Voteアイテム取得
     * @param con
     * @param uuid プレイヤーUUID
     * @param delete
     * @return 
     * @throws SQLException
     * @throws java.io.FileNotFoundException
     */
    public ArrayList<ItemStack[]> getVoteItem(Connection con, UUID uuid, boolean deleteFlag) throws SQLException, IOException {
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM itemqueue WHERE most = ? AND least = ?");
        PreparedStatement prep2 = con.prepareStatement("DELETE FROM itemqueue WHERE most = ? AND least = ?");
        ArrayList<ItemStack[]> items = new ArrayList<>();
        try {
            prep1.setLong(1, uuid.getMostSignificantBits());
            prep1.setLong(2, uuid.getLeastSignificantBits());
            ResultSet rs = prep1.executeQuery();
            boolean hit = false;
            try {
                // 結果取得
                while (rs.next()) {
                    items.add(BukkitSerialization.itemStackArrayFromBase64(rs.getString("items")));
                    hit = true;
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();

            if (hit) {
                if (deleteFlag) {
                    prep2.setLong(1, uuid.getMostSignificantBits());
                    prep2.setLong(2, uuid.getLeastSignificantBits());
                    // 実行
                    prep2.executeUpdate();
                }
            }

        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
                if (deleteFlag) {
                    prep2.close();
                }
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        if (deleteFlag) {
            prep2.close();
        }    
        return items;
    }


}
