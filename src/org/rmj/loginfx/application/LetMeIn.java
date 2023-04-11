package org.rmj.loginfx.application;

import org.rmj.loginfx.views.LoginFX;
import javafx.application.Application;
import org.rmj.appdriver.GProperty;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.agent.GRiderX;

public class LetMeIn {
    public static void main(String [] args){
        GRiderX poGRider = new GRiderX("gRider");
        GProperty loProp = new GProperty("GhostRiderXP");
        
        LoginFX loLogin = new LoginFX();
        loLogin.setGRider((GRider) poGRider);
        loLogin.setGProperty(loProp);
        
        Application.launch(loLogin.getClass(), args);
    }
}
