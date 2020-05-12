
package jp.minecraftuser.ecovote.commands;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import java.util.ArrayList;
import java.util.Calendar;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.Utl;
import static jp.minecraftuser.ecoframework.Utl.sendPluginMessage;
import jp.minecraftuser.ecovote.listener.VoteListener;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * リロードコマンドクラス
 * @author ecolight
 */
public class EcoVoteTestCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EcoVoteTestCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
        setAuthBlock(true);
        setAuthConsole(true);
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecovote.test";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        // パラメータチェック:0のみ
        //if (!checkRange(sender, args, 0, 0)) return true;
        Player pl = (Player) sender;
        sendPluginMessage(plg, pl, "args.length = {0}", Integer.toString(args.length));
        int count = 0;
        for (String s : args) {
            sendPluginMessage(plg, pl, "args[{0}] = {1}", Integer.toString(count), s);
            count++;
        }

        if ((args.length == 2) && (args[0].equalsIgnoreCase("vote"))) {
            // テストコード
            ItemStack item;
            int v = Integer.parseInt(args[1]);
            switch (v) {
                case 1:
                    item = new ItemStack(Material.PLAYER_HEAD, 1);
                    SkullMeta meta = (SkullMeta) item.getItemMeta();
                    meta.setOwningPlayer(plg.getServer().getOfflinePlayer(pl.getUniqueId()));
                    item.setItemMeta(meta);
                    sendPluginMessage(plg, pl, "{0}への投票で次のアイテムが配布されました", "xx-service");
                    break;
                case 2:
                    item = new ItemStack(Material.DIAMOND_BLOCK, 1);
                    sendPluginMessage(plg, pl, "{0}への投票で次のアイテムが配布されました", "xx-service");
                    break;
                case 3:
                    item = new ItemStack(Material.COOKIE, 1);
                    sendPluginMessage(plg, pl, "{0}への投票で次のアイテムが配布されました", "xx-service");
                    break;
            }
        }
        if ((args.length == 3) && (args[0].equalsIgnoreCase("server"))) {
            // テストコード /evote test server service user
            sendPluginMessage(plg, pl, "execute service[" + args[1] + "] notify simulation");
            VotifierEvent e = new VotifierEvent(new Vote(args[1], args[2], "127.0.0.1", Calendar.getInstance().getTime().toString()));
            VoteListener l = (VoteListener) plg.getPluginListener("vote");
            l.onVotifierEvent(e);
        }
        return true;
    }
    
}
