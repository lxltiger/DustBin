package com.example.x6.serial;

import java.util.List;

/**
 * 控制锁控板的柜子服务
 * 1.打开柜子
 * 2.查询柜子
 */
public interface LockerService<T extends Grid> {


//    void startOpen(T grid);

//    void stopOpen(T grid);


//    void startClose(T grid);

//    void stopClose(T grid);

    void check(String command);


    void handleOpenDoor1();
    void handleOpenDoor2();

    //停止当前的批量耗时操作，比如queryAll，用户可能离开了页面，不需要继续操作
    void stopCurrentOperation();


    //停止串口服务，可能是由于更换串口地址
    void stopLockerService();

    //退出应用 释放资源
    void exit();

}
