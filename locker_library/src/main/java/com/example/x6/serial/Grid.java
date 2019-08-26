package com.example.x6.serial;

//柜子的格口
public interface Grid {

    //格口的唯一标识Id
    String id();


    String startOpenCommand();
    String stopOpenCommand();

    String startCloseCommand();
    String stopCloseCommand();



}
