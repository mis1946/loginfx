package org.rmj.loginfx.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;
import org.rmj.appdriver.constants.UserState;
import org.rmj.appdriver.constants.UserType;
import org.rmj.appdriver.GProperty;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.ShowMessageFX;
import org.rmj.appdriver.agentfx.CommonUtils;

public class LoginController implements Initializable {

    @FXML
    private Button btnExit;
    @FXML
    private FontAwesomeIconView glyphExit;
    @FXML
    private Label lblProduct;
    @FXML
    private Button cmdOK;
    @FXML
    private Button cmdCancel;
    @FXML
    private Label lblAddress;
    @FXML
    private Label lblTelephone;
    @FXML
    private AnchorPane acMain;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtUserName;
    @FXML
    private ComboBox cboProduct;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initForm();
        getApps();
        txtUserName.setOnKeyPressed(this::txtField_KeyPressed);
        txtPassword.setOnKeyPressed(this::txtField_KeyPressed);
        txtUserName.requestFocus();
    }    

    @FXML
    private void btnExit(ActionEvent event) {
        unloadScene(event);
    }

    @FXML
    private void cmdOK(ActionEvent event) {
        if (pnRetry < 3){
            if (isUserOkay()) 
                unloadScene(event);
            else pnRetry += 1;
        } else unloadScene(event);
        
        if (pnRetry >= 3) unloadScene(event);
    }

    @FXML
    private void cmdCancel(ActionEvent event) {
        unloadScene(event);
    }
    
    private void unloadScene(ActionEvent event){
        Node source = (Node)  event.getSource(); 
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
    
    private void unloadScene(MouseEvent event){
        Node source = (Node)  event.getSource(); 
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
    
    private Stage getStage(){
        Stage stage = (Stage) txtUserName.getScene().getWindow();
        return stage;
    }
    
    private void txtField_KeyPressed(KeyEvent event){
        TextField txtField = (TextField)event.getSource();
        
        if (event.getCode() == ENTER){CommonUtils.SetNextFocus(txtField);}
    }
    
    private void initForm(){
        lblProduct.setText("");
        lblAddress.setText("");
        lblTelephone.setText("");
        
        lblProduct.setText(poGRider.getBranchName());
        lblAddress.setText(poGRider.getAddress() + ", " + poGRider.getTownName() + " " + poGRider.getProvince());
        if (!poGRider.getTelNo().equals("")) lblTelephone.setText("Tel. No. " + poGRider.getTelNo() + " ");
        if (!poGRider.getFaxNo().equals("")) lblTelephone.setText(lblTelephone.getText() + "Fax No. " + poGRider.getFaxNo());
    }   
    
    private void getApps(){
        String lsSQL = "SELECT" +
                            "  b.sProdctID" +
                            ", b.sProdctNm" +
                        " FROM xxxSysObject a" +
                            ", xxxSysObjectSub b" +
                            ", xxxSysApplication c" +
                        " WHERE a.sProdctID = b.sProdctID" +
                            " AND b.sProdctID = c.sProdctID" +
                            " AND b.nEntryNox = '2'" + //2 = Java programs
                            " AND c.sClientID = " + SQLUtil.toSQL(poGRider.getClientID()) +
                        " ORDER BY b.sProdctNm";
                            //" AND a.sProdctID <> " + SQLUtil.toSQL(xeDriverID);
        
        //if product id is specified, load it as the main app
        if (!psProdctID.equals("")) lsSQL = MiscUtil.addCondition(lsSQL, "a.sProdctID = " + SQLUtil.toSQL(psProdctID));
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
                
        ObservableList<String> laIDx = FXCollections.observableArrayList();
        ObservableList<String> laApp = FXCollections.observableArrayList();
        
        try {
            while(loRS.next()){
                laIDx.add(loRS.getString("sProdctID"));
                laApp.add(loRS.getString("sProdctNm"));
            }
            
            if (laIDx.isEmpty()){
                if (psProdctID.equals(""))
                    ShowMessageFX.Warning("Login failed. No application was registered for the client.", "Login", "Please inform MIS Department");
                else ShowMessageFX.Warning("Login failed. Application is not registered for the client.", "Login", "Please inform MIS Department");
                System.exit(0);
            }
            
            paAppCode = new String[laIDx.size()];
            paAppName = new String[laApp.size()];
            
            for(int ln=0; ln<laIDx.size();ln++){
                paAppName[ln] = (String) laApp.get(ln);
                paAppCode[ln] = (String) laIDx.get(ln);
            }
            
            cboProduct.setItems(laApp);
            cboProduct.getSelectionModel().selectFirst();
            TextFields.bindAutoCompletion(cboProduct.getEditor(), cboProduct.getItems());
        } catch (SQLException e) {
            ShowMessageFX.Error(e.getMessage(), pxeModuleName, "Please inform MIS Department");
        }
    }
       
    private boolean isUserOkay(){
        if (txtUserName.getText().trim().isEmpty()){
            ShowMessageFX.Warning(getStage(), null, "Login", "Please fill up the username.");
            txtUserName.requestFocus();
            return false;
        }
        
        if (txtPassword.getText().trim().isEmpty()){
            ShowMessageFX.Warning(getStage(), null, "Login", "Please fill up the password.");
            txtPassword.requestFocus();
            return false;
        }
        
        String lsUserName = poGRider.Encrypt(txtUserName.getText());
        String lsPassWord = poGRider.Encrypt(txtPassword.getText());
        psProdctID = paAppCode[cboProduct.getSelectionModel().getSelectedIndex()];
        
        String lsSQL = "SELECT *" +
                        " FROM xxxSysUser" +
                        " WHERE sLogNamex = " + SQLUtil.toSQL(lsUserName) +
                            " AND sPassword = " + SQLUtil.toSQL(lsPassWord) + 
                        " ORDER BY cUserType DESC, nUserLevl DESC";
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            loRS.beforeFirst();
            if (MiscUtil.RecordCount(loRS) != 0) {
                boolean lbLogged = false;
                
                loRS.first();
                if (MiscUtil.RecordCount(loRS) == 1){
                    loRS.first();
                    if (loRS.getString("sProdctID").equals(psProdctID)){
                        lbLogged = true;
                    }
                }else{
                    loRS.first();
                    while(loRS.next()){   
                        System.out.println("««««« USER ACCOUNT »»»»»");
                        System.out.println("USER ID: " + loRS.getString("sUserIDxx"));
                        System.out.println("PRODUCT ID: " + loRS.getString("sProdctID"));
                        System.out.println("------------------------");
                        if (loRS.getString("sProdctID").equals(psProdctID)){
                            lbLogged = true; break;
                        }
                    }
                    loRS.beforeFirst();
                    loRS.first();
                }
                
                psUserIDxx = loRS.getString("sUserIDxx");
                
                if (loRS.getString("cUserStat").equals(UserState.SUSPENDED)){
                    ShowMessageFX.Warning("User is currently suspended.\n" + 
                                            "Application use is not allowed!!!", "Login", "Unable to Login");
                    System.exit(0);
                }
                
                if (!lbLogged) {
                    if (loRS.getString("cUserType").equals(UserType.LOCAL)){
                        ShowMessageFX.Warning("User is not a member of this application.\n" + 
                                                "Application use is not allowed!!!", "Login", "Unable to Login");
                        System.exit(0);
                    }   
                } 
                
                lsSQL = "UPDATE xxxSysApplication SET" +
                            "  dSysDatex = " + SQLUtil.toSQL(poGRider.getServerDate()) + 
                        " WHERE sClientID = " + SQLUtil.toSQL(poGRider.getClientID()) +
                            " AND sProdctID = " + SQLUtil.toSQL(psProdctID);
                
                if (poGRider.executeUpdate(lsSQL) <= 0){
                    ShowMessageFX.Warning("Unable to update system date.", "Login", "Please inform MIS Department");
                    System.exit(0);
                }
            }
        } catch (SQLException e) {
            ShowMessageFX.Error(e.getMessage(), pxeModuleName, "Please inform MIS Department");
            System.exit(0);
        }
        
        if (psUserIDxx.equals("")){
            ShowMessageFX.Warning(null, "Login", "Invalid user detected.");
            pbOkay = false;
            return false;
        } else {
            pbOkay = true;
            return true;
        }
    }
    
    
    public boolean isOkay(){return pbOkay;}
    public String getProductID(){return psProdctID;}
    public String getUserIDxx(){return psUserIDxx;}
    
    public void setGRider(GRider foGRider){this.poGRider = foGRider;}
    public void setGProperty(GProperty foProperty){this.poProperty = foProperty;}
    public void setProdctID(String fsProdctID){this.psProdctID = fsProdctID;}
    
    private static final String xeDriverID = "GRider";
    private final String pxeModuleName = "LoginController";
    
    private static GRider poGRider;
    private static GProperty poProperty;
    private static String psProdctID = "";
    private String psUserIDxx = "";
    private boolean pbOkay = false;
    
    private int pnRetry = 0;
    private String[] paAppName = null;
    private String[] paAppCode = null;
}