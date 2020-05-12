
package jp.minecraftuser.ecovote.listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import java.util.UUID;
import java.util.logging.Level;
import jp.minecraftuser.ecoegg.EcoEgg;
import jp.minecraftuser.ecoframework.ListenerFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecousermanager.EcoUserManager;
import jp.minecraftuser.ecousermanager.db.EcoUserUUIDStore;
import jp.minecraftuser.ecovote.timer.AsyncVoteTimer;
import jp.minecraftuser.ecovote.timer.VotePayload;
import org.bukkit.Bukkit;
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
        EcoUserManager eum = (EcoUserManager) plg.getPluginFrame("EcoUserManager");
        EcoUserUUIDStore store = eum.getStore();
        UUID uuid = store.latestUUID(vote.getUsername());

        if (uuid != null) {
            VotePayload v = new VotePayload(plg, vote.getServiceName(), uuid);
            AsyncVoteTimer timer = (AsyncVoteTimer) plg.getPluginTimer("vote");
            timer.sendData(v);
        }

        log.log(Level.INFO, "Address:" + vote.getAddress());
        log.log(Level.INFO, "ServiceName:" + vote.getServiceName());
        log.log(Level.INFO, "TimeStamp:" + vote.getTimeStamp() + "(local:"+vote.getLocalTimestamp()+")");
        log.log(Level.INFO, "Username:" + vote.getUsername());
    }
}

