package com.elifguler.fordcopilot;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Controller {
    private static RestInterfaceController controller;

    public static RestInterfaceController getController() {
        if (controller != null) {
            return controller;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ford-copilot.herokuapp.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        controller = retrofit.create(RestInterfaceController.class);
        return controller;
    }

    private Controller() {}
}
