package com.example.x6.serial;

public interface GridListener<T extends Grid> {
    void updateStatus(T grid);

    void handleResponse(String response);

}
