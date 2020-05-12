
package jp.minecraftuser.ecovote.timer;

import java.util.ArrayList;
import java.util.Calendar;
import jp.minecraftuser.ecoframework.async.*;
import java.util.UUID;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecovote.db.VoteStore;
import org.bukkit.inventory.ItemStack;

/**
 * メインスレッドと非同期スレッド間のデータ送受用クラス(メッセージ送受用)
 * @author ecolight
 */
public class VotePayload extends PayloadFrame {
    public String service;
    public UUID uuid;
    public long datetime;
    public ItemStack[] items;
    public boolean result = false;
    public Type type;
    public ArrayList<VoteStore.UserStat> userList;
    public VoteStore.UserStat user;
    public int voteItemCount = 0;

    public enum Type {
        NONE,
        REQ_VOTE,
        REQ_GET,
        REQ_SET,
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス(ただし通信に用いられる可能性を念頭に一定以上の情報は保持しない)
     * @param service_
     * @param uuid_
     */
    public VotePayload(PluginFrame plg_, String service_, UUID uuid_) {
        super(plg_);
        type = Type.NONE;
        service = service_;
        uuid = uuid_;
        datetime = Calendar.getInstance().getTime().getTime();
    }

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス(ただし通信に用いられる可能性を念頭に一定以上の情報は保持しない)
     * @param uuid_
     * @param type_
     */
    public VotePayload(PluginFrame plg_, UUID uuid_, Type type_) {
        super(plg_);
        this.type = type_;
        service = "";
        uuid = uuid_;
        datetime = Calendar.getInstance().getTime().getTime();
    }
}
