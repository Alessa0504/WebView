package listener

/**
 * @Description: 接口回调方法
 * @author zouji
 * @date 2023/1/14
 */
interface IRemoteListener {
    fun deal(cmd: String, param: String)
}