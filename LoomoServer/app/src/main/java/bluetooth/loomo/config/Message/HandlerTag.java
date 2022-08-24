package bluetooth.loomo.config.Message;

/**
 * @Description:
 * Constants to mark the source of Message
 * Only used by handler of three class: MovementUnit, BluetoothUnit and PositionUnit
 *
 * @author : Zhouyao
 * Date: 2021/12/26
 */
public class HandlerTag {

    /**
     *  Mark message from Position unit
     */
    public static final int MSG_FROM_POSITION = 11;

    /**
     * Message from host computer via bluetooth unit
     */
    public static final int MSG_FROM_BLUETOOTH = 12;

    /**
     * Message from host computer via UDP
     */
    public static final int MSG_FROM_UDP = 13;
}
