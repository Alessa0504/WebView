package listener

/**
 * @Description:
 * @author zouji
 * @date 2023/1/14
 */
interface IRemoteListener {
    fun deal(cmd: String, param: String)
}