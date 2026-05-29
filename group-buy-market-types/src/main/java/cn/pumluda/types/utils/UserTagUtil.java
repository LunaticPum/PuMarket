package cn.pumluda.types.utils;

/**
 * Project: group-buy-market-better <p>
 * File: UserTagUtil <p>
 * Created by: 16374 <p>
 * Date: 2026/5/29 <p>
 * Time: 09:36 <p>
 * Description: 用户标签工具类：标签位图运算
 */
public class UserTagUtil {

    /**
     * 添加标签
     *
     * @param userTag 用户已有标签
     * @param tag     预定义好的用户标签
     * @return 组合后的标签
     */
    public static int addTag(int userTag, int tag) {
        return userTag | tag;
    }

    /**
     * 移除标签
     *
     * @param userTag 用户已有标签
     * @param tag     预定义好的用户标签
     * @return 删除指定标签后的标签
     */
    public static int removeTag(int userTag, int tag) {
        return userTag & ~tag;
    }

    /**
     * 判断用户标签是否命中指定标签
     *
     * @param userTag 用户已有标签
     * @param tag     指定标签
     * @return 标签命中识别结果
     */
    public static boolean hitAnyTag(int userTag, int tag) {
        return (userTag & tag) != 0;
    }

}
