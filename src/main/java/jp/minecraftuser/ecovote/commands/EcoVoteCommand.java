
package jp.minecraftuser.ecovote.commands;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.Utl;
import jp.minecraftuser.ecovote.timer.AsyncVoteTimer;
import jp.minecraftuser.ecovote.timer.VotePayload;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * EcoDataBridgeコマンドクラス
 * @author ecolight
 */
public class EcoVoteCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EcoVoteCommand(PluginFrame plg_, String name_) {
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
        return "ecovote";
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
            // vote 総合投票数 top10 / 自分の順位 / stock item数
            AsyncVoteTimer t = (AsyncVoteTimer) plg.getPluginTimer("vote");
            VotePayload p = new VotePayload(plg, pl.getUniqueId(), VotePayload.Type.REQ_VOTE);
            t.sendData(p);
            Utl.sendPluginMessage(plg, sender, "voteデータを要求中です");
        }
        else {
            Utl.sendPluginMessage(plg, sender, "パラメータが不正です");
        }
        return true;
    }
    
}
