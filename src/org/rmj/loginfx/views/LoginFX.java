package org.rmj.loginfx.views;

import java.sql.ResultSet;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.rmj.appdriver.GProperty;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.ShowMessageFX;


public class LoginFX extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("Login2023.fxml"));
        
        LoginController loCon = new LoginController();
        loCon.setGRider(poGRider);
        loCon.setProdctID(psProdctID);
        fxmlLoader.setController(loCon);
        Parent parent = fxmlLoader.load();
        
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("GhostRider Application Driver FX v1.0");
        stage.getIcons().add(new Image("org/rmj/loginfx/images/64.png"));
        stage.show();
        
        Stage secondary = new Stage();
        secondary.initOwner(stage);
        secondary.setTitle("");
        secondary.initStyle(StageStyle.TRANSPARENT);
        secondary.initModality(Modality.APPLICATION_MODAL);
        
        Scene scene = new Scene(parent);
        scene.setFill(null);
        secondary.setScene(scene);    
        secondary.showAndWait();
        
        String lsUserIDxx = loCon.getUserIDxx();
        String lsProdctID = loCon.getProductID();
        
        if (!lsProdctID.equals("") && !lsUserIDxx.equals("")){
            GRider loGRider = new GRider(lsProdctID);
            GProperty loProp = new GProperty("GhostRiderXP");
            if (!loGRider.loadEnv(lsProdctID)) {
                if (loGRider.getErrMsg().equals("")){
                    ShowMessageFX.Error(loGRider.getMessage(), "Login", "Please inform MIS Department.");
                    System.exit(1);
                } else{
                    ShowMessageFX.Warning(loGRider.getErrMsg(), "Login", "Please inform MIS Department.");
                    System.exit(0);
                }
            }
            if (!loGRider.logUser(lsProdctID, lsUserIDxx)) {
                if (loGRider.getErrMsg().equals("")){
                    ShowMessageFX.Error(loGRider.getMessage(), "Login", "Please inform MIS Department.");
                    System.exit(1);
                } else{
                    ShowMessageFX.Warning(loGRider.getErrMsg(), "Login", "Please inform MIS Department.");
                    System.exit(0);
                }
            }
            
            String lsAppPath = "";
            String lsAppName = "";
            
            String lsSQL = "SELECT" +
                                "  a.sApplPath" +
                                ", d.sApplName" +
                            " FROM xxxSysApplicationSub a" +
                                    ", xxxSysApplication b" +
                                    ", xxxSysObject c" +
                                    ", xxxSysObjectSub d" +
                            " WHERE a.sProdctID = b.sProdctID" +
                                    " AND a.sClientID = b.sClientID" +
                                    " AND a.sProdctID = c.sProdctID" +
                                    " AND c.sProdctID = d.sProdctID" +
                                    " AND a.sProjctID = d.sProjctID" +
                                    " AND d.nEntryNox = 2" + //2 = java programs
                                    " AND a.sClientID = " + SQLUtil.toSQL(loGRider.getClientID()) +
                                    " AND a.sProdctID = " + SQLUtil.toSQL(lsProdctID);
            
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            if (MiscUtil.RecordCount(loRS) <= 0){
                ShowMessageFX.Warning("Application is not registered for this client.", "Login", "Please inform MIS Department.");
                System.exit(0);
            } else {
                loRS.first();
                lsAppPath = loRS.getString("sApplPath");
                lsAppName = loRS.getString("sApplName");
            }
            
            String lsApplication = lsAppPath + lsAppName + " " + loGRider.getProductID() + " " + loGRider.getUserID();
                
            if (lsApplication.equals("")){
                ShowMessageFX.Warning("Application was not set...", "Login", "Please inform MIS Department.");
                System.exit(0);
            } else{
                Runtime re = Runtime.getRuntime();    
                re.exec(lsApplication); 
            }   
        }
        stage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public boolean isLoggedIn(){return pbLoggedIn;}
    public void setGRider(GRider foGRider){this.poGRider = foGRider;}
    public void setGProperty(GProperty foProperty){this.poProperty = foProperty;}
    public void setProdctID(String fsProdctID){this.psProdctID = fsProdctID;}
    
    private boolean pbLoggedIn = false;
    public static GRider poGRider;
    public static GProperty poProperty;
    public static String psProdctID = "";
}
