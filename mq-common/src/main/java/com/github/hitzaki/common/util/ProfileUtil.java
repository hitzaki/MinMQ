package com.github.hitzaki.common.util;

import java.util.*;

public class ProfileUtil {
    // 使用对象存储而不是类: 防止其他类误操作,因为未来还有刷新参数的需求
    // 配置文件参数, 选项和参数
    private final Map<String, List<String>> options = new HashMap<>();
    private final List<String> arguments = new ArrayList<>();
    // 只提供get方法, 除了刷新配置文件, 不提供对外修改的接口.

    // 配置文件刷新
    // 构造方法会从配置文件中拿参数, 提供有参(文件名)和无参两种
    public static void main(String[] args) {
        try {
            // 读取配置文件配置
            ResourceBundle mq = ResourceBundle.getBundle("mq");
            String user = mq.getString("mq.user");
            System.out.println(user);
            // 读取系统环境变量
            String env = System.getenv("mq.ps");
            System.out.println(env);
            // 读取命令行参数
            String port = System.getProperty("mq.port");
            System.out.println(port);
            for(String str: args){
                System.out.println(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
