package com.example.itayrin.assignment_5;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import static java.lang.Thread.sleep;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadFileService extends IntentService {

    public static final String DOWNLOAD_FILE_ACTION = "DOWNLOAD_FILE_ACTION";
    public static final String CURRENT = "CURRENT";

    public DownloadFileService() {
        super("DownloadFileService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            int index = 0;
            for (int i = 0; i < 10; i++) {
                try{
                    Thread.sleep(1000);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                Intent br = new Intent(DOWNLOAD_FILE_ACTION);
                br.putExtra(CURRENT, index);
                sendBroadcast(br);
                index++;
            }
        }
    }

}
