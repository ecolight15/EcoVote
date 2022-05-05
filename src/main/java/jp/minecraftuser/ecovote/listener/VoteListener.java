
package jp.minecraftuser.ecovote.listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import java.util.UUID;
import java.util.logging.Level;
import jp.minecraftuser.ecoframework.ListenerFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import jp.minecraftuser.ecomqttserverlog.EcoMQTTServerLog;
import jp.minecraftuser.ecovote.timer.AsyncVoteTimer;
import jp.minecraftuser.ecovote.timer.VotePayload;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * ログイン・ログアウトListenerクラス
 * @author ecolight
 */
public class VoteListener extends ListenerFrame {
    
    /**
     * コンストラクタ
     * @param plg_ プラグインフレームインスタンス
     * @param name_ 名前
     */
    public VoteListener(PluginFrame plg_, String name_) {
        super(plg_, name_);
    }

    /**
     * Vote処理
     * @param event イベント
     */
    @EventHandler(priority=EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        log.log(Level.INFO, "Address:" + vote.getAddress());
        log.log(Level.INFO, "ServiceName:" + vote.getServiceName());
        log.log(Level.INFO, "TimeStamp:" + vote.getTimeStamp() + "(local:"+vote.getLocalTimestamp()+")");
        log.log(Level.INFO, "Username:" + vote.getUsername());

        // 受信設定済みのサーバーからの受信のみ処理する
        boolean hit = false;
        for (String s : conf.getArrayList("servers")) {
            if (s.equals(vote.getServiceName())) {
                log.log(Level.INFO, "service[" + vote.getServiceName() + "] check[" + s + "]");
                hit = true;
                break;
            }
        }
        if (!hit) return;
        
        // Todo:ロード出来ない場合の処理は保留
        EcoMQTTServerLog emsl = (EcoMQTTServerLog) plg.getPluginFrame("EcoUserManager");
        UUID uuid = emsl.latestUUID(vote.getUsername());

        if (uuid != null) {
            VotePayload v = new VotePayload(plg, vote.getServiceName(), uuid);
            AsyncVoteTimer timer = (AsyncVoteTimer) plg.getPluginTimer("vote");
            timer.sendData(v);
        }

        // 投票ブロードキャストメッセージ
        if (conf.getBoolean("broadcast.use")) {
            if (vote.getServiceName().contains("minecraft.jp")) {
                Utl.sendPluginMessage(plg, null, conf.getString("broadcast.message").replaceAll("\\{player\\}", vote.getUsername()).replaceAll("\\{service\\}", "https://minecraft.jp/servers/52d049314ddda10f0d0041a7"));
            } else if (vote.getServiceName().contains("monocraft.net")) {
                Utl.sendPluginMessage(plg, null, conf.getString("broadcast.message").replaceAll("\\{player\\}", vote.getUsername()).replaceAll("\\{service\\}", "https://monocraft.net/servers/W1XCgEv8JWHiwAtOGtGo/vote"));
            } else {
                Utl.sendPluginMessage(plg, null, conf.getString("broadcast.message").replaceAll("\\{player\\}", vote.getUsername()).replaceAll("\\{service\\}", vote.getServiceName()));
            }
        }
    }
}

