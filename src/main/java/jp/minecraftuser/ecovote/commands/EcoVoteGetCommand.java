
package jp.minecraftuser.ecovote.commands;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.Utl;
import jp.minecraftuser.ecovote.timer.AsyncVoteTimer;
import jp.minecraftuser.ecovote.timer.VotePayload;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * リロードコマンドクラス
 * @author ecolight
 */
public class EcoVoteGetCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EcoVoteGetCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
        setAuthBlock(false);
        setAuthConsole(false);
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecovote.get";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Player pl = (Player) sender;
            // アイテムの取得申請
            AsyncVoteTimer t = (AsyncVoteTimer) plg.getPluginTimer("vote");
            VotePayload p = new VotePayload(plg, pl.getUniqueId(), VotePayload.Type.REQ_GET);
            t.sendData(p);
            Utl.sendPluginMessage(plg, sender, "vote配布アイテムを要求中です");
        }
        else {
            Utl.sendPluginMessage(plg, sender, "パラメータが不正です");
        }
        return true;
    }
    
}
