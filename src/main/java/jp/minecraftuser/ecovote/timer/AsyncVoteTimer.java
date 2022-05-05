
package jp.minecraftuser.ecovote.timer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.async.*;
import jp.minecraftuser.ecoframework.PluginFrame;
import static jp.minecraftuser.ecoframework.Utl.sendPluginMessage;
import jp.minecraftuser.ecomqttserverlog.EcoMQTTServerLog;
import jp.minecraftuser.ecovote.db.VoteStore;
import jp.minecraftuser.ecovote.listener.VoteListener;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * 非同期プレイヤーデータ保存クラス
 * @author ecolight
 */
public class AsyncVoteTimer extends AsyncProcessFrame {
    // 呼び出し元のリスナを覚えておく(executeReceiveのみR/W可とする)
    private final VoteListener listener;

    /**
     * 親スレッド用コンストラクタ
     * @param plg_ プラグインフレームインスタンス
     * @param listener_ 呼び出し元リスナーフレームインスタンス
     * @param name_ 名前
     */
    public AsyncVoteTimer(PluginFrame plg_, VoteListener listener_, String name_) {
        super(plg_, name_);
        listener = listener_;
    }

    /**
     * 子スレッド用コンストラクタ
     * @param plg_ プラグインフレームインスタンス
     * @param listener_ 呼び出し元リスナーフレームインスタンス
     * @param name_ 名前
     * @param frame_ 子スレッド用フレーム
     */
    public AsyncVoteTimer(PluginFrame plg_, VoteListener listener_, String name_, AsyncFrame frame_) {
        super(plg_, name_, frame_);
        this.listener = listener_;
    }

    /**
     * Data加工子スレッド側処理
     * @param data_ ペイロードインスタンス
     */
    @Override
    protected void executeProcess(PayloadFrame data_) {
        VotePayload data = (VotePayload) data_;
        VoteStore db = (VoteStore) plg.getDB("vote");
        Connection con;
        try {
            con = db.connect();
        } catch (SQLException ex) {
            Logger.getLogger(AsyncVoteTimer.class.getName()).log(Level.SEVERE, null, ex);
            // 処理結果を返送
            data.result = false;
            receiveData(data);
            return;
        }
        
        try {
            // Typeごとの処理
            switch (data.type) {
                case NONE:
                    // 総合データの加算
                    log.log(Level.INFO, "update Total vote table");
                    db.updateTotalVote(con, data.uuid);
                    
                    // ランキングデータの更新
                    log.log(Level.INFO, "update Total vote ranking table");
                    db.updateRankTotalVote(con);

                    // 最大保管量を超えていたらアイテム追加はしない
                    log.log(Level.INFO, "update vote item table");
                    ArrayList<ItemStack> items = new ArrayList<>();
                    if (db.getVoteItemCount(con, data.uuid) < conf.getInt("max-stock-gift")) {
                        // アイテムインサート
                        Random rnd = new Random();
                        int v = rnd.nextInt(100);
                        ItemStack item;
                        if (v < 5) {
                            log.log(Level.INFO, "gen Player head");
                            // プレイヤーヘッド
                            item = new ItemStack(Material.PLAYER_HEAD, 1);
                            SkullMeta meta = (SkullMeta) item.getItemMeta();
                            meta.setOwningPlayer(plg.getServer().getOfflinePlayer(data.uuid));
                            item.setItemMeta(meta);
                        } else if (v < 30) {
                            log.log(Level.INFO, "gen Diamond block");
                            item = new ItemStack(Material.DIAMOND_BLOCK, 1);
                        } else {
                            log.log(Level.INFO, "gen cookie");
                            item = new ItemStack(Material.COOKIE, 1);
                        }
                        items.add(item);
                        db.insertVoteItem(con, data.uuid, items.toArray(new ItemStack[items.size()]));
                    }
                    data.items = items.toArray(new ItemStack[items.size()]);
                    data.result = true;
                    break;
                // voteコマンド実行部 : vote 総合投票数 top10 / 自分の順位 / stock item数
                case REQ_VOTE:
                    // 総合データTOP10の取得
                    data.userList = db.getRankTotalVote(con, 10);
                    
                    // 自分の順位取得
                    data.user = db.getRankTotalVote(con, data.uuid);
                    
                    // ストックアイテム数
                    data.voteItemCount = db.getVoteItemCount(con, data.uuid);

                    data.result = true;
                    break;
                // voteコマンド実行部 : vote 総合投票数 top10 / 自分の順位 / stock item数
                case REQ_GET:
                    // 総合データTOP10の取得
                    ArrayList<ItemStack> l = new ArrayList<>();
                    for (ItemStack[] arr : db.getVoteItem(con, data.uuid, true)) {
                        for (ItemStack i : arr) {
                            l.add(i);
                        }
                    }
                    data.items = l.toArray(new ItemStack[l.size()]);
                    data.result = true;
                    break;
                case REQ_SET:
                    // 総合データTOP10の取得
                    db.insertVoteItem(con, data.uuid, data.items);
                    data.result = true;
                    break;
            }
        } catch (Exception ex) {
            Logger.getLogger(AsyncVoteTimer.class.getName()).log(Level.SEVERE, null, ex);
            data.result = false;
        }

        try {
            try {
                con.commit();
            } catch (SQLException ex1) {
                Logger.getLogger(AsyncVoteTimer.class.getName()).log(Level.SEVERE, null, ex1);
            }
            con.close();
        } catch (SQLException ex1) {
            Logger.getLogger(AsyncVoteTimer.class.getName()).log(Level.SEVERE, null, ex1);
        }
        
        // 処理結果を返送
        receiveData(data);
    }

    /**
     * Data加工後親スレッド側処理
     * @param data_ ペイロードインスタンス
     */
    @Override
    protected void executeReceive(PayloadFrame data_) {
        VotePayload p = (VotePayload) data_;
        OfflinePlayer pl;

        // Typeごとの処理
        switch (p.type) {
            // 処理結果を受け取ったので完了する
            // アイテムスタックの格納があれば通知をする
            case NONE:
                pl = plg.getServer().getOfflinePlayer(p.uuid);
                if (pl.isOnline()) {
                    Player pll = (Player) pl;
                    if (p.result == false) {
                        sendPluginMessage(plg, pll, "投票を検知しましたが、投票結果の記録に失敗しました");
                    } else {
                        if (p.items.length == 0) {
                            sendPluginMessage(plg, pll, "投票を検知しましたが、投票特典の配布に失敗しました");
                        } else {
                            sendPluginMessage(plg, pll, "{0}への投票で次のアイテムが配布されました", p.service);
                            for (ItemStack i : p.items) {
                                sendPluginMessage(plg, pll, " - {0} x {1}", i.getType().name(), Integer.toString(i.getAmount()));
                            }
                            sendPluginMessage(plg, pll, "配布アイテムは /evote get コマンドで取得できます", p.service);
                        }
                    }
                }
                break;
            // vote 総合投票数 top10 / 自分の順位 / stock item数
            case REQ_VOTE:
                pl = plg.getServer().getOfflinePlayer(p.uuid);
                if (pl.isOnline()) {
                    Player pll = (Player) pl;
                    if (p.result == false) {
                        sendPluginMessage(plg, pll, "投票情報の取得に失敗しました");
                    } else {
                        EcoMQTTServerLog logp = (EcoMQTTServerLog) plg.getServer().getPluginManager().getPlugin("EcoMQTTServerLog");

                        sendPluginMessage(plg, pll, "--- 通算投票TOP10 ---");
                        String name = null;
                        for (VoteStore.UserStat us : p.userList) {
                            if (logp != null) {
                                name = logp.latestName(us.uuid);
                            } else {
                                name = "name not found";
                            }
                            sendPluginMessage(plg, pll, us.rank + ":" + name + "(" + us.vote + ")");
                        }
                        sendPluginMessage(plg, pll, "--- プレイヤーの順位 ---");
                        if (p.user == null) {
                            sendPluginMessage(plg, pll, "データなし");
                        } else {
                            if (logp != null) {
                                name = logp.latestName(p.uuid);
                            } else {
                                name = "name not found";
                            }
                            sendPluginMessage(plg, pll, p.user.rank + ":" + name + "(" + p.user.vote + ")");
                        }
                        sendPluginMessage(plg, pll, "--- 未受領配布アイテム数 : " + p.voteItemCount + "(max " + conf.getInt("max-stock-gift") + ") ---");
                    }
                }
                break;
            case REQ_GET:
                pl = plg.getServer().getOfflinePlayer(p.uuid);
                if (pl.isOnline()) {
                    Player pll = (Player) pl;
                    if (p.result == false) {
                        sendPluginMessage(plg, pll, "配布アイテム情報の取得に失敗しました");
                    } else {
                        ArrayList<ItemStack> l = new ArrayList<>();
                        for (ItemStack i : p.items) {
                            HashMap<Integer, ItemStack> tmp = pll.getInventory().addItem(i);
                            for (ItemStack ii: tmp.values()) {
                                l.add(ii);
                            }
                        }
                        if (l.size() != 0) {
                            sendPluginMessage(plg, pll, "一部のアイテムが受け取れませんでした");
                            // DBに書き戻す
                            p.type = VotePayload.Type.REQ_SET;
                            p.items = l.toArray(new ItemStack[l.size()]);
                            p.result = false;
                            this.sendData(data_);
                        } else {
                            sendPluginMessage(plg, pll, "全ての投票特典を受領しました");
                        }
                    }
                }
                break;
            case REQ_SET:
                pl = plg.getServer().getOfflinePlayer(p.uuid);
                if (pl.isOnline()) {
                    Player pll = (Player) pl;
                    if (p.result == false) {
                        sendPluginMessage(plg, pll, "配布アイテムの再保管に失敗しました");
                    } else {
                        if (p.items.length == 0) {
                            sendPluginMessage(plg, pll, "配布アイテムの再保管に際しアイテム情報が不正でした");
                        } else {
                            sendPluginMessage(plg, pll, "次のアイテムを再保管しました", p.service);
                            for (ItemStack i : p.items) {
                                sendPluginMessage(plg, pll, " - {0} x {1}", i.getType().name(), Integer.toString(i.getAmount()));
                            }
                            sendPluginMessage(plg, pll, "配布アイテムは /evote get コマンドで取得できます", p.service);
                        }
                    }
                }
                break;
        }
    }

    /**
     * 継承クラスの子スレッド用インスタンス生成
     * 親子間で共有リソースがある場合、マルチスレッドセーフな作りにすること
     * synchronizedにする、スレッドセーフ対応クラスを使用するなど
     * @return AsyncFrame継承クラスのインスタンス
     */
    @Override
    protected AsyncFrame clone() {
        return new AsyncVoteTimer(plg, listener, name, this);
    }
  
}
