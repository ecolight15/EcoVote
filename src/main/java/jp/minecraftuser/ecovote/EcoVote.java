
package jp.minecraftuser.ecovote;

import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecovote.commands.EcoVoteCommand;
import jp.minecraftuser.ecovote.commands.EcoVoteGetCommand;
import jp.minecraftuser.ecovote.commands.EcoVoteReloadCommand;
import jp.minecraftuser.ecovote.commands.EcoVoteTestCommand;
import jp.minecraftuser.ecovote.config.EcoVoteConfig;
import jp.minecraftuser.ecovote.db.VoteStore;
import jp.minecraftuser.ecovote.listener.VoteListener;
import jp.minecraftuser.ecovote.timer.AsyncVoteTimer;

/**
 * EcoVote(EventListener)Plugin メインクラス
 * @author ecolight
 */
public class EcoVote extends PluginFrame {

    /**
     * 起動時処理
     */
    @Override
    public void onEnable() {
        initialize();
    }

    /**
     * 終了時処理
     */
    @Override
    public void onDisable() {
        disable();
    }

    /**
     * 設定初期化
     */
    @Override
    public void initializeConfig() {
        EcoVoteConfig conf = new EcoVoteConfig(this);
        conf.registerArrayString("servers");
        conf.registerInt("max-stock-gift");
        conf.registerBoolean("votedb.use");
        conf.registerString("votedb.db");
        conf.registerString("votedb.name");
        conf.registerString("votedb.server");
        conf.registerString("votedb.user");
        conf.registerString("votedb.pass");
        registerPluginConfig(conf);
    }
    
    @Override
    public void initializeDB() {
        EcoVoteConfig conf = (EcoVoteConfig) getDefaultConfig();
        try {
            if (conf.getBoolean("votedb.use")) {
                if (conf.getString("votedb.db").equalsIgnoreCase("sqlite")) {
                    registerPluginDB(new VoteStore(this, conf.getString("votedb.name"), "vote"));
                } else if (conf.getString("votedb.db").equalsIgnoreCase("mysql")) {
                    registerPluginDB(new VoteStore(this,
                            conf.getString("votedb.server"),
                            conf.getString("votedb.user"),
                            conf.getString("votedb.pass"),
                            conf.getString("votedb.name"),
                            "vote"));
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(EcoVote.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * コマンド初期化
     */
    @Override
    public void initializeCommand() {
        CommandFrame cmd = new EcoVoteCommand(this, "evote");
        cmd.addCommand(new EcoVoteGetCommand(this, "get"));
        cmd.addCommand(new EcoVoteReloadCommand(this, "reload"));
        cmd.addCommand(new EcoVoteTestCommand(this, "test"));
        registerPluginCommand(cmd);
    }

    /**
     * イベントリスナー初期化
     */
    @Override
    public void initializeListener() {
        registerPluginListener(new VoteListener(this, "vote"));
    }
    
    @Override
    public void initializeTimer() {
        AsyncVoteTimer timer = new AsyncVoteTimer(this, (VoteListener) getPluginListener("vote"), "vote");
        registerPluginTimer(timer);
        timer.runTaskTimer(this, 0, 20);
    }
}
