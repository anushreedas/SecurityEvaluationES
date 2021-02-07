/**
 * This program evalautes the security of an android device based on
 * its API level, Security Patch level, Device Lock set status,
 * Root Access Status and Number of applications with dangerous permissions
 *
 * @author Anushree Sitaram Das
 */

package com.example.securityevaluationes;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import eu.deustotech.clips.CLIPSError;
import eu.deustotech.clips.Environment;
import eu.deustotech.clips.FactAddressValue;
import eu.deustotech.clips.MultifieldValue;

public class MainActivity extends AppCompatActivity {
    String rulesFileName = "expertrules2.clp";  // file with expert system clips rules
    float AndroidSecurityRisk;                  // Android Security Risk Rate
    float ApplicationSecurityRisk;              // Android's Application Security Risk Rate
    float NotLatestAPI;                         // Degree of membership of API level to NotLatestAPI
    float NotLatestDate;                        // Degree of membership of Security Patch level to NotLatestDate
    String LockSetStatus;                       // Android's Lock Set Status
    String RootAccessStatus;                    // Android's Root Access Status

    // method called after application starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // tells which activity's layout to access
        setContentView(R.layout.activity_main);
        // button from selected activity layout
        Button button = findViewById(R.id.button);
        // button action listener
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // evaluate android device after button click
                calculateEvaluationLevel();
            }
        });
    }

    /**
     * this function collects required data from android device for evaluating it security
     * then copies the rules file to running application's local directory,
     * sets up clips environment and executes the evaluation process
     */
    private void calculateEvaluationLevel() {
        // gets android's api level
        int version = getAndroidVersion();
        // gets last security patch date
        String securityPatch= getSecurityPatchDate();
        // finds out if device lock is set
        boolean lockStatus = getLockStatus();
        // finds out if user has root access
        boolean rootAccess = getRootAccessStatus();
        // gets number of applications with dangerous permissions
        int noOfAppsWithDangerousPermissions = getAppsWithDangerousPermissions();
        // copies rule file to app fike directory
        copyRuleFileInAppFileDir();
        // creates new environment for clips
        Environment clips = new Environment();
        // executes clips expert system
        clipsExpertSystem(clips,version,securityPatch,lockStatus,rootAccess,noOfAppsWithDangerousPermissions);
        // destroys clips environment
        clips.destroy();
        // display evaluation
        showEvaluation();

    }

    private void showEvaluation() {
        // final Android Security Evaluation Rating
        // 1 - AndroidSecurityRisk
        int rating = (int)(100 - AndroidSecurityRisk);
        // set rating on progress bar
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(rating);
        // show progress bar
        progressBar.setVisibility(View.VISIBLE);
        // show rating in text
        TextView progressValue = findViewById(R.id.progressValue);
        progressValue.setText("Score: "+(float)rating/10+"/"+10);
        // Set recommendations string
        String recommendation = getString(R.string.recommendation);
        // if API level is not latest
        if (NotLatestAPI > 0.0){
            int version = getAndroidVersion();
            recommendation+="\n\u2022 Your API Level is still "+ version+". Please update your Android version";
        }
        // if patch date is not latest
        if (NotLatestDate > 0.0)
            recommendation+="\n\u2022Your last security patch date is not recent. Please update Security Patch level";
        // if device lock is not set
        if (LockSetStatus.equals("false"))
            recommendation+="\n\u2022 Your device lock is not set. Please set your Device Lock for better security";
        // if user doesn't have root access
        if (RootAccessStatus.equals("false"))
            recommendation+="\n\u2022 You do not have root access. Please get Root Access";
        // if there are applications apart from installed apps which have dangerous permissions
        if (ApplicationSecurityRisk > 0.0)
            recommendation+="\n\u2022 You have couple of applications which are granted dangerous permissions. Please disable dangerous permissions for installed apps";
        // if the device is completely secure
        if (NotLatestAPI <= 0.0 && NotLatestDate <= 0.0 && LockSetStatus.equals("true")
                && RootAccessStatus.equals("true") && ApplicationSecurityRisk <= 0.0)
            recommendation+="\n\u2022 No actions required!";
        // show recommendations
        TextView textView = findViewById(R.id.textView);
        textView.setText(recommendation);
    }

    /**
     * returns android's api level
     * @return  api level
     */
    private int getAndroidVersion(){
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * returns last security patch date
     * @return  security patch date
     */
    private String getSecurityPatchDate(){
        return android.os.Build.VERSION.SECURITY_PATCH;
    }

    /**
     *  finds out if device lock is set
     * @return  true if device lock is set or false otherwise
     */
    private boolean getLockStatus(){
        KeyguardManager manager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        return manager.isKeyguardSecure();
    }

    /**
     *  finds out if user has root access
     * @return  true if user has root access or false otherwise
     */
    private boolean getRootAccessStatus() {
        // create new process
        Process process = null;
        // initialze new flag to false
        boolean rootAccess = false;
        try {
            // run "su" command
            process = Runtime.getRuntime().exec("su");
            // if the command runs successfully
            // then set flag as true
            rootAccess=true;
        } catch (Exception e) {
            // else set flag as false
            rootAccess=false;
        } finally {
            // destroy process
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e) { }
            }
        }
        // return flag
        return rootAccess;
    }

    /**
     * gets number of applications with dangerous permissions
     * @return number of applications with dangerous permissions
     */
    private int getAppsWithDangerousPermissions(){
        // number of apps with dangerous permissions
        int count = 0;
        // list of dangerous permissions
        String[] permissions = {Manifest.permission.READ_CALL_LOG,Manifest.permission.READ_CONTACTS,
                Manifest.permission.CAMERA,Manifest.permission.BODY_SENSORS,Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS};
        List<PackageInfo> listOfApps;
        // create new packetmanager object
        PackageManager packageManager = (PackageManager) getPackageManager();
        // get list of applications which have the permissions mentioned in the list
        listOfApps = packageManager.getPackagesHoldingPermissions(permissions,0);
        // loop through all applications
        for (int n=0; n < listOfApps.size(); n++)
        {
            // check if app is a system app
            if ((listOfApps.get(n).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1)
            {
                continue;
            }
            else
            {
                // increase count
                count++;
            }
        }
        // return the size of list of applications
        return count;
    }


    /**
     * evaluate the android device's security based on given parameters
     *
     * @param env               clips environment
     * @param version           android version
     * @param securityPatch     android's last security patch date
     * @param lockStatus        android's device lock set status
     * @param rootAccess        android's root access status
     * @param noOfAppsWithDangerousPermissions  number of applications with dangerous permissions
     */
    private void clipsExpertSystem(Environment env,int version,String securityPatch,
                                   boolean lockStatus,boolean rootAccess,
                                   int noOfAppsWithDangerousPermissions) {
        // extract year from security patch date
        String x = securityPatch.substring(0,securityPatch.indexOf('-'));
        int year = Integer.parseInt(x);
        // extract month from security patch date
        x = securityPatch.substring(5,securityPatch.lastIndexOf('-'));
        int month = Integer.parseInt(x);
        // set the path where the rules file is located
        String factsFilePath = getFilesDir().getAbsolutePath() + "/" + rulesFileName;
        try {
            // clear the facts, rules, etc from clips environment
            env.clear();
            // assert new facts with android's paramenters
            env.assertString("(API " + version + ")");
            env.assertString("(PatchDate " + year + " " + month + ")");
            env.assertString("(LockSetStatus " + lockStatus + ")");
            env.assertString("(RootAccessStatus " + rootAccess + ")");
            env.assertString("(NoOfApps " + noOfAppsWithDangerousPermissions + ")");
            // load the rules file
            env.load(factsFilePath);
            // run the program
            // new facts with result of the android security evaluation will be generated
            env.run();
            // find facts under AndroidSecurity template
            final String evalStr = "(find-all-facts (( ?f AndroidSecurity )) TRUE)";
            final MultifieldValue evaluated = (MultifieldValue) env.eval( evalStr );
            // get first multivalued fact of AndroidSecurity template
            FactAddressValue factAddressValue = (FactAddressValue) evaluated.get(0);

            AndroidSecurityRisk = Float.parseFloat(factAddressValue.getFactSlot("AndroidSecurityRisk").toString());
            ApplicationSecurityRisk = Float.parseFloat(factAddressValue.getFactSlot("ApplicationSecurityRisk").toString());
            NotLatestAPI = Float.parseFloat(factAddressValue.getFactSlot("NotLatestAPI").toString());
            NotLatestDate = Float.parseFloat(factAddressValue.getFactSlot("NotLatestDate").toString());
            LockSetStatus = factAddressValue.getFactSlot("LockSetStatus").toString();
            RootAccessStatus = factAddressValue.getFactSlot("RootAccessStatus").toString();
        }catch (CLIPSError e)
        {
            Log.d("CLIPS error", e.getMessage());
        }
    }

    /**
     * copy rules file from assets folder to application directory
     */
    private void copyRuleFileInAppFileDir() {
        // create new FileOutputStream object for writing to new file
        FileOutputStream destinationFileStream = null;
        // create new InputStream object
        InputStream assetsOriginFileStream = null;
        try {
            // open file to write
            destinationFileStream = openFileOutput(rulesFileName, Context.MODE_PRIVATE);
            // open file from assets folder
            assetsOriginFileStream = getAssets().open(rulesFileName);

            int aByte;
            // copy content from file in assets folder to new file
            while((aByte = assetsOriginFileStream.read())!=-1){
                destinationFileStream.write(aByte);
            }
        } catch (IOException e) {
            Log.d("CLIPS Expert Rules", e.getMessage());
        } finally {
            try {
                // close all strea s
                assetsOriginFileStream.close();
                destinationFileStream.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}