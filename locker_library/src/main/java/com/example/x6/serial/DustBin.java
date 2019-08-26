package com.example.x6.serial;

public class DustBin implements Grid {
    private String id;
    private String startOpenCommand;
    private String stopOpenCommand;

    private String startCloseCommand;
    private String stopCloseCommand;

    public DustBin(String id, String startOpenCommand, String stopOpenCommand, String startCloseCommand, String stopCloseCommand) {
        this.id = id;
        this.startOpenCommand = startOpenCommand;
        this.stopOpenCommand = stopOpenCommand;
        this.startCloseCommand = startCloseCommand;
        this.stopCloseCommand = stopCloseCommand;
    }

    @Override
    public String id() {
        return id;
    }



    @Override
    public String startOpenCommand() {
        return startOpenCommand;
    }

    @Override
    public String stopOpenCommand() {
        return stopOpenCommand;
    }

    @Override
    public String startCloseCommand() {
        return startCloseCommand;
    }

    @Override
    public String stopCloseCommand() {
        return stopCloseCommand;
    }


}
